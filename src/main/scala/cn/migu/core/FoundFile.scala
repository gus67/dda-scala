package cn.migu.core

import java.io.File

import cn.migu.utils.SqliteDataSourceProvider
import cn.migu.utils.SqliteDataSourceProvider.createDataSource
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
      && !file.getName.toUpperCase().endsWith(".ICONV")) {

      //发现了新的文件先对文件进行正则过滤
      val regSet = InitFileSystem.reg_sinks_map.keySet

      val loop = new Breaks

      loop.breakable {

        for (x <- regSet) {

          if (new Regex(x) findFirstIn file.getName nonEmpty) {

            InitFileSystem.reg_quene_map(x).put(new DDAFile(file.getName, file.getPath, InitFileSystem.reg_sinks_map(x)))

            SqliteDataSourceProvider.createDataSource().getConnection.createStatement().executeUpdate(
              s"insert into files values ('','$x','${file.getPath}',0,0,datetime('now','localtime'))")

            log.info(s"\u001b[35;1m${file.getPath} write to quene pass $x \u001b[0m\n".replace("),", ""))

            loop.break
          }
        }
      }
    }
  }
}
