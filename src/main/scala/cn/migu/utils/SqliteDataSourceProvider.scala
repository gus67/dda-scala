package cn.migu.utils

import java.io.File
import javax.sql.DataSource

import cn.migu.core.InitFileSystem
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import org.sqlite.javax.SQLiteConnectionPoolDataSource
import org.sqlite.SQLiteConfig

object SqliteDataSourceProvider {

  private val log = LoggerFactory.getLogger(this.getClass)

  var ds: SQLiteConnectionPoolDataSource = _

  try {
    if (!new File(s"${InitFileSystem.USER_DIR}/dda.db").isFile) {

      FileUtils.touch(new File(s"${InitFileSystem.USER_DIR}/dda.db"))

      createDataSource().getConnection.createStatement().executeUpdate("create table files (reg text,path text,status int,update text)")
    }
  } catch {
    case ex: Exception => log.error(s"SingleThreadExecutor fatal error ---> ${LogUtils.getTrace(ex)}")
      System.exit(1)
  }

  def createDataSource(): DataSource = {

    this.synchronized {
      if (ds == null) {

        val config = new SQLiteConfig

        config.setEncoding(SQLiteConfig.Encoding.getEncoding("UTF-8"))

        ds = new SQLiteConnectionPoolDataSource

        ds.setUrl(s"jdbc:sqlite:${InitFileSystem.USER_DIR}/dda.db")

        ds.setConfig(config)

        //log.info("Using a new data source .")
      }
    }
    ds
  }


  //  def main(args: Array[String]): Unit = {
  //
  //    //createDataSource().getConnection.createStatement().executeUpdate("create table t1 (id int)")
  //
  //    for (x <- 1 to 10) {
  //
  //      new Thread(() => {
  //
  //        synchronized {
  //
  //          val c = createDataSource().getConnection
  //
  //          val pst = c.prepareStatement("insert into t1 values (?)")
  //          pst.setInt(1, x)
  //
  //          pst.executeUpdate()
  //
  //        }
  //      }).start()
  //    }
  //  }
}
