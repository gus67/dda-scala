package cn.migu

import java.util.concurrent.{Executors, TimeUnit}

import cn.migu.core.{FoundFile, InitFileSystem}
import cn.migu.utils._
import cn.migu.vo.{DDAFile, FtpSink, HdfsSink, KafkaSink}

import org.apache.commons.io.monitor.{FileAlterationMonitor, FileAlterationObserver}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable
import scala.sys.process._
import scala.util.control.Breaks
import scala.util.matching.Regex


object DDA {

  private val log: Logger = LoggerFactory.getLogger(this.getClass)

  def main(args: Array[String]): Unit = {

    val filename = "/Users/guxichang/code-src/dda-scala/src/main/resources/dda.cfg"

    InitFileSystem.initDDA(filename)

    log.info(s"\n根目录----> ${InitFileSystem.root_path}\n")

    //启动，按时间点抓取文件，写入队列
    Executors.newSingleThreadExecutor().submit(new Runnable {

      override def run(): Unit = {

        var timing = ""

        try {

          if (args.nonEmpty) {

            timing = s"-newermt '${args(0)}'"
          }

          val s = Seq("bash", "-c", s"find ${InitFileSystem.root_path} -type f  ! -name '*.COMPLETED' $timing ") !!

          val set = mutable.Set[String]()

          val files = s.split("\n")

          for (x <- files) {

            set.add(x)
          }

          log.info(s"\n\u001b[32;1m${InitFileSystem.root_path} 扫描出未完成的文件数量为 ${set.size} \u001b[0m\n")

          for (s <- set) {

            if (!s.endsWith(".TMP")

              && !s.endsWith(".SWP")

              && !s.endsWith(".FILEPART")

              && !s.endsWith(".COMPLETED")) {

              //发现了新的文件先对文件进行正则过滤
              val regSet = InitFileSystem.reg_sinks_map.keySet

              var notFound = true

              val loop = new Breaks

              loop.breakable {

                //任意文件只会匹配到一个正则
                for (x <- regSet) {

                  if (new Regex(x) findFirstIn s nonEmpty) {

                    InitFileSystem.reg_quene_map(x).put(new DDAFile(InitFileSystem.getFileNameWithSuffix(s), s, InitFileSystem.reg_sinks_map(x)))

                    log.info(s"\n\u001b[34;1m$s 匹配到一个正则 $x \u001b[0m\n".replace("),", ""))

                    notFound = false

                    loop.break
                  }
                }
              }
            }
          }

        } catch {

          case ex: Exception =>

            ex.printStackTrace()

            System.exit(1)
        }
      }
    })

    val os = System.getProperty("os.name")

    val interval = TimeUnit.SECONDS.toMillis(1)

    val observer = new FileAlterationObserver(InitFileSystem.root_path)

    observer.addListener(new FoundFile())

    //创建文件变化监听器
    val monitor = new FileAlterationMonitor(interval, observer)

    // 开始监控
    monitor.start()

    for ((_, v) <- InitFileSystem.reg_quene_map) {

      Executors.newSingleThreadExecutor().submit(new Runnable {

        override def run(): Unit = {

          val abq = v

          var fileTmp = ""

          while (true) {

            try {

              val dda = abq.take

              val path = dda.path

              val fileName = dda.fileName

              val clazz = dda.cs.clazz

              val sinks = dda.cs.sinks

              fileTmp = path

              val tmpFileArr = mutable.Buffer[String](path)

              log.info(s"\n\u001b[32;1m$path 被阻塞队列获得,即将向 $sinks 写入\u001b[0m\n".replace(")", "").replace("ArrayBuffer(", ""))

              var lastFileName = path

              var isTmp = false

              //插件
              if (!"NA".equals(clazz)) {

                lastFileName = s"${InitFileSystem.USER_DIR}/.tmp/" + fileName + "_" + System.currentTimeMillis() + ".TRANSITION"

                PluginUtils.reflectPlugin(clazz.split("!!")(0), clazz.split("!!")(1), path, lastFileName)

                tmpFileArr += lastFileName

                isTmp = true

                log.info(s"\n\u001b[34;1m$lastFileName ---> 插件化成功\u001b[0m\n")

              }

              var tmpPath = lastFileName

              //转码
              val res = Seq("bash", "-c", s"file --mime-encoding $lastFileName") !!

              //如果没有插件,还是要确保写入临时文件夹
              if (!isTmp) tmpPath = s"${InitFileSystem.USER_DIR}/.tmp/$fileName"

              if (res.indexOf("utf-8") == -1 && res.indexOf("us-ascii") == -1) {

                if (os.indexOf("linux") != -1) {

                  Seq("bash", "-c", s"iconv -f gbk -t utf-8 $lastFileName -o $tmpPath.UTF-8 ") !!

                  tmpFileArr += s"$tmpPath.UTF-8"

                } else {

                  Seq("bash", "-c", s"iconv -c -f gbk -t utf-8 $lastFileName > $tmpPath.UTF-8") !!

                  tmpFileArr += s"$tmpPath.UTF-8"
                }

                log.info(s"\n\u001b[34;1m${tmpFileArr.last} ---> 转码成功\u001b[0m\n")
              }

              //文件名+行号
              Seq("bash", "-c", "awk '$0=\"" + path + "=\"NR\"\037 \"$0' " + tmpFileArr.last + " > " + s"${tmpFileArr.last}.LINE_NUM") !!

              tmpFileArr += s"${tmpFileArr.last}.LINE_NUM"

              log.info(s"\n\u001b[34;1m${tmpFileArr.last} ---> 增加行号成功\u001b[0m\n")

              for (sink <- sinks) {

                sink match {

                  case k: KafkaSink =>

                    new KafkaUtils().kafkaProducer4DDA(k, tmpFileArr)

                  case h: HdfsSink =>

                    new HdfsUtils().hdfsPut4DDA(h, tmpFileArr)

                  case f: FtpSink =>
                    //暂不实现
                    println(s"${f.ip} ${f.name}")

                  case _ => println("error")
                }
              }
            } catch {

              case ex: Exception =>

                FaildFileWriter.failedFileWrite(fileTmp)

                log.error(s"线程池发生一个严重错误 ---> ${LogUtils.getTrace(ex)}")
            }
          }
        }
      })
    }
  }
}
