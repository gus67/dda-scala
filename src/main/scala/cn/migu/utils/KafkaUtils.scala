package cn.migu.utils

import java.io.File
import java.util.Properties

import cn.migu.vo.KafkaSink
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord

import scala.collection.mutable
import scala.io.Source

class KafkaUtils {

  private val log = LoggerFactory.getLogger(this.getClass)

  def kafkaProducer4DDA(kafkaSink: KafkaSink, tmpFiles: mutable.Buffer[String]): Unit = {

    val props = new Properties()
    props.put("bootstrap.servers", kafkaSink.bootServr)
    props.put("acks", "1")
    // 打开重试机制必须让max.in.flight.requests.per.connection等于1,否则在发生重排序的时候，不允许重试
    //    props.put("max.in.flight.requests.per.connection", "1")
    //    props.put("retries", 1)
    props.put("batch.size", "16384")
    //延迟发送，增加吞吐量，可以减少发送的请求数量，但会在没有负载的情况下为发送的记录添加最多5毫秒的延迟。
    props.put("linger.ms", "1")
    props.put("buffer.memory", "33554432")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    for (topic <- kafkaSink.topic.split(",")) {

      val producer = new KafkaProducer[String, String](props)

      val lines = Source.fromFile(tmpFiles.last).getLines()

      for (line <- lines) {

        producer.send(new ProducerRecord(topic, "", line))

      }

      producer.close()

      log.info(s"\n\u001b[33;1m${tmpFiles.last} 写入 Kafka ---> ${kafkaSink.bootServr}/$topic 成功  \u001b[0m\n")

      FileUtils.moveFile(new File(tmpFiles.head), new File(tmpFiles.head + ".COMPLETED"))

      log.info(s"\n\u001b[33;1m${tmpFiles.head} ---> ${tmpFiles.head}.COMPLETED  \u001b[0m\n")

      for (f <- tmpFiles.takeRight(tmpFiles.size - 1)) {

        FileUtils.forceDelete(new File(f))

        log.info(s"\n\u001b[34;1m$f ---> removed  \u001b[0m\n")
      }
    }
  }
}
