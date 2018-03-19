package cn.migu.utils

import java.io.File
import java.net.URI

import cn.migu.vo.HdfsSink
import org.apache.commons.io.FileUtils
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.permission.{FsAction, FsPermission}
import org.apache.hadoop.fs.{FileSystem, Path}
import org.slf4j.LoggerFactory

import scala.collection.mutable

class HdfsUtils {

  private val log = LoggerFactory.getLogger(this.getClass)

  def hdfsPut4DDA(hdfsSink: HdfsSink, tmpFiles: mutable.Buffer[String]): Unit = {

    val url = hdfsSink.path
    var n = 0
    var host = ""
    var path = ""
    for (x <- url.split("/")) {
      n match {
        case 0 => host += x
        case 1 => host += "//"
        case 2 => host += x
        case _ => path += "/" + x
      }
      n += 1
    }

    try {

      val hdfs = FileSystem.get(new URI(host), new Configuration())

      if (!hdfs.exists(new Path(path))) {
        hdfs.mkdirs(new Path(path), new FsPermission(
          FsAction.ALL,
          FsAction.ALL,
          FsAction.ALL))
      }
      //先不考虑切分问题
      hdfs.copyFromLocalFile(false, new Path(tmpFiles.last), new Path(path))

      hdfs.close()

      log.info(s"\n\u001b[33;1m${tmpFiles.last} 写入 Hdfs ---> ${hdfsSink.path} 成功  \u001b[0m\n")

      FileUtils.moveFile(new File(tmpFiles.head), new File(tmpFiles.head + ".COMPLETED"))

    } catch {

      case ex: Exception => log.error(s"Hdfs 上传文件过程中发生一个严重错误 ---> ${LogUtils.getTrace(ex)}")
    }
  }
}
