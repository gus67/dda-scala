package cn.migu.utils

import java.net.URI

import cn.migu.vo.HdfsSink
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.permission.{FsAction, FsPermission}
import org.apache.hadoop.fs.{FileSystem, Path}
import org.slf4j.LoggerFactory

class HdfsUtils {

  private val log = LoggerFactory.getLogger(this.getClass)

  def hdfsPut4DDA(filePath: String, hdfsSink: HdfsSink): Unit = {

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

    val hdfs = FileSystem.get(new URI(host), new Configuration())

    if (!hdfs.exists(new Path(path))) {
      hdfs.mkdirs(new Path(path), new FsPermission(
        FsAction.ALL,
        FsAction.ALL,
        FsAction.ALL))
    }
    //先不考虑切分问题
    hdfs.copyFromLocalFile(false, new Path(filePath), new Path(path))
    hdfs.close()

  }
}
