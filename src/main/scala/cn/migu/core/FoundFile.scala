package cn.migu.core

import java.io.File

import cn.migu.vo.DDAFile
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor
import org.slf4j.LoggerFactory

import scala.util.matching.Regex
import scala.util.control._


class FoundFile extends FileAlterationListenerAdaptor() {

  private val log = LoggerFactory.getLogger(this.getClass)

  override def onFileCreate(file: File): Unit = {

    //println(file.getName,file.getPath)

    if (!file.getName.toUpperCase().endsWith(".TMP")
      && !file.getName.toUpperCase().endsWith(".DDA")
      && !file.getName.toUpperCase().endsWith(".SWP")
      && !file.getName.toUpperCase().endsWith(".FILEPART")
      && !file.getName.toUpperCase().endsWith(".READY")
      && !file.getName.toUpperCase().endsWith(".KAFKA")
      && !file.getName.toUpperCase().endsWith(".HDFS")
      && !file.getName.toUpperCase().endsWith(".FTP")
      && !file.getName.toUpperCase().endsWith(".COMPLETED")
      && !file.getName.toUpperCase().endsWith(".TRANSITION")
      && !file.getName.toUpperCase().endsWith(".ICONV")
      && !file.getName.toUpperCase().endsWith(".LINE_NUM")) {

      //发现了新的文件先对文件进行正则过滤
      val regSet = InitFileSystem.reg_sinks_map.keySet

      var notFound = true

      val loop = new Breaks

      loop.breakable {

        //任意文件只会匹配到一个正则
        for (x <- regSet) {

          if (new Regex(x) findFirstIn file.getName nonEmpty) {

            InitFileSystem.reg_quene_map(x).put(new DDAFile(file.getName, file.getPath, InitFileSystem.reg_sinks_map(x)))

            log.info(s"\n\u001b[34;1m${file.getPath} 匹配到一个正则 $x \u001b[0m\n".replace("),", ""))

            notFound = false

            loop.break
          }
        }
        if (notFound) log.info(s"\n\u001b[33;1m${file.getPath} 不能在 $regSet 匹配到任何一个正则  \u001b[0m\n")
      }
    }
  }
}
