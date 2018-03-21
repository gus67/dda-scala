package cn.migu.utils

import java.io.FileWriter

import cn.migu.core.InitFileSystem
import org.slf4j.{Logger, LoggerFactory}

object FaildFileWriter {

  private val log: Logger = LoggerFactory.getLogger(this.getClass)

  def failedFileWrite(filePath: String): Unit = {

    this.synchronized {

      try {
        val writer = new FileWriter(s"${InitFileSystem.USER_DIR}/.failed", true)

        writer.write(s"$filePath\n")

        writer.close()

      } catch {
         case ex: Exception => log.error(s"失败文件记录器失败！ ---> ${LogUtils.getTrace(ex)}")

      }

    }
  }
}
