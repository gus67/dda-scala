package cn.migu.core

import java.util.concurrent.ArrayBlockingQueue

import cn.migu.vo._
import org.slf4j.LoggerFactory

import scala.collection.mutable

import scala.io.Source


object InitFileSystem {

  private val log = LoggerFactory.getLogger(this.getClass)

  val USER_DIR = System.getProperty("user.dir")

  //正则与输出源对应表
  var reg_sinks_map: Map[String, CS] = Map()

  //正则与队列对应关系,队列里存放CS
  var reg_quene_map: Map[String, ArrayBlockingQueue[DDAFile]] = Map()

  //队列边界最大长度，否则阻塞
  val MAX_QUENE_LENGTH = 65535

  //根目录路径
  var root_path: String = _

  def initDDA(configPath: String): Unit = {

    println(configPath)

    //读取配置
    val cfg_it = Source.fromFile(configPath).getLines()

    var tmp_clazz: String = null
    var tmp_reg: String = null

    cfg_it.foreach { x =>
      if (x.startsWith("RP")) {
        root_path = x.split(" ")(1)
      } else if (x.startsWith("REG")) {
        tmp_clazz = x.split(" ")(2)
        tmp_reg = x.split(" ")(1)
        reg_quene_map += (tmp_reg -> new ArrayBlockingQueue(MAX_QUENE_LENGTH))
      } else if (x.startsWith("K")) {
        if (reg_sinks_map.contains(tmp_reg)) {
          reg_sinks_map(tmp_reg).sinks += new KafkaSink(x.split(" ")(1), x.split(" ")(2))
        } else {
          reg_sinks_map += tmp_reg -> new CS(tmp_clazz, mutable.Buffer(new KafkaSink(x.split(" ")(1), x.split(" ")(2))))
        }
      } else if (x.startsWith("H")) {
        if (reg_sinks_map.contains(tmp_reg)) {
          reg_sinks_map(tmp_reg).sinks += new HdfsSink(x.split(" ")(1))
        } else {
          reg_sinks_map += tmp_reg -> new CS(tmp_clazz, mutable.Buffer(new HdfsSink(x.split(" ")(1))))
        }
      } else if (x.startsWith("F")) {
        if (reg_sinks_map.contains(tmp_reg)) {
          reg_sinks_map(tmp_reg).sinks += new FtpSink(x.split(" ")(1), x.split(" ")(2).toInt, x.split(" ")(3), x.split(" ")(4), x.split(" ")(5))
        } else {
          reg_sinks_map += tmp_reg -> new CS(tmp_clazz, mutable.Buffer(new FtpSink(x.split(" ")(1), x.split(" ")(2).toInt,
            x.split(" ")(3), x.split(" ")(4), x.split(" ")(5))))
        }
      }
    }

    log.info(s"配置文件解析\n\u001b[33;1m ${reg_sinks_map.toString().replace("(", "\n").replace(")", "").replace("\n,", "")} \u001b[0m\n")

    //创建文件数据库


  }
}
