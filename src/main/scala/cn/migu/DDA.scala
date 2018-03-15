package cn.migu

import java.util.concurrent.{Executors, TimeUnit}

import cn.migu.core.{FoundFile, InitFileSystem}
import cn.migu.utils.{KafkaUtils, LogUtils, PluginUtils}
import cn.migu.vo.{FtpSink, HdfsSink, KafkaSink}
import org.apache.commons.io.monitor.{FileAlterationMonitor, FileAlterationObserver}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable
import scala.sys.process._


object DDA {

  private val log: Logger = LoggerFactory.getLogger(this.getClass)

  def main(args: Array[String]): Unit = {

    val os = System.getProperty("os.name")

    val filename = "/Users/guxichang/code-src/dda-scala/src/main/resources/dda.cfg"

    InitFileSystem.initDDA(filename)

    log.info(s"根目录----> ${InitFileSystem.root_path}\n")

    val interval = TimeUnit.SECONDS.toMillis(1)
    val observer = new FileAlterationObserver(InitFileSystem.root_path)
    observer.addListener(new FoundFile())
    //创建文件变化监听器
    val monitor = new FileAlterationMonitor(interval, observer)
    // 开始监控
    monitor.start()

    for ((_, y) <- InitFileSystem.reg_quene_map) {

      Executors.newSingleThreadExecutor().submit(new Runnable {
        override def run(): Unit = {
          val abq = y
          while (true) {
            try {
              val dda = abq.take
              val path = dda.path
              val fileName = dda.fileName
              val clazz = dda.cs.clazz
              val sinks = dda.cs.sinks

              val tmpFileArr = mutable.Buffer[String](path)

              log.info(s"\n\u001b[36;1m$path 被阻塞队列获得,即将向 $sinks 写入\u001b[0m\n".replace(")", "").replace("ArrayBuffer(",""))

              /**
                * 1、反射
                * 2、转码
                * 3、切分
                * 3、传输
                * 3.1、部分失败策略，文件部分成功，输出源部分成功如何应对？
                * 3.1、对于HDFS，FTP部分文件数据成功可以删除可以覆盖，对于kafka部分成功则可能需要断点续传
                * 4、改名
                */

              var lastFileName = path

              //插件
              if (!"NA".equals(clazz)) {

                lastFileName = s"${InitFileSystem.USER_DIR}/.tmp/" + fileName + "_" + System.currentTimeMillis() + ".TRANSITION"

                PluginUtils.reflectPlugin(clazz.split("!!")(0), clazz.split("!!")(1), path, lastFileName)

                tmpFileArr += lastFileName

                //println("lastFileName----->" + lastFileName)
              }

              //转码
              val res = s"file --mime-encoding $lastFileName" !!

              if (res.indexOf("utf-8") == -1 && res.indexOf("us-ascii") == -1) {

                //println("fileName----->" + fileName)

                if (os.indexOf("linux") != -1) {

                  s"iconv -f gbk -t utf-8 $lastFileName -o $lastFileName.UTF-8 " !!

                  tmpFileArr += s"$lastFileName.UTF-8"

                } else {

                  s"iconv -c -f gbk -t utf-8 $lastFileName" #> new java.io.File(s"$lastFileName.UTF-8") !!

                  tmpFileArr += s"$lastFileName.UTF-8"
                }
              }

              //println("tmpFileArr------>" + tmpFileArr)

              for (sink <- sinks) {

                sink match {
                  case k: KafkaSink =>

                    new KafkaUtils().kafkaProducer4DDA(k, tmpFileArr)

                  case h: HdfsSink =>
                    println(s"${h.path}")

                  case f: FtpSink =>
                    println(s"${f.ip} ${f.name}")

                  case _ => println("error")

                }
              }



              //所有文件都发送成功

            } catch {
              case ex: Exception => log.error(s"线程池发生一个严重错误 ---> ${LogUtils.getTrace(ex)}")
            }
          }
        }
      })
    }


    //间隔一秒刷盘，记录seek，当文件完全成功，会直接删除map中记录，若文件不成功，下次断点的文件将被延续
    Executors.newSingleThreadExecutor().submit(new Runnable {
      override def run(): Unit = {

      }
    })
  }
}
