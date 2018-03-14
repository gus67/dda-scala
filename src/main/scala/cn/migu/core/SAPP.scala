package cn.migu.core


import java.io.File
import java.util.Properties

import cn.migu.utils.KafkaProducerSendCallback
import org.apache.commons.io.FileUtils
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer

import scala.io.Source


object SAPP {

  def main(args: Array[String]): Unit = {


    val props = new Properties()
    props.put("bootstrap.servers", "172.18.111.4:9093,172.18.111.5:9093,172.18.111.6:9093")
    props.put("acks", "all")
    props.put("transactional.id", System.currentTimeMillis().toString)
    props.put("max.in.flight.requests.per.connection", "1")

    // 打开重试机制必须让max.in.flight.requests.per.connection等于1,否则在发生重排序的时候，不允许重试
    //props.put("max.in.flight.requests.per.connection", "1")
    //props.put("retries", 1)
    //props.put("batch.size", "16384")
    //延迟发送，增加吞吐量，可以减少发送的请求数量，但会在没有负载的情况下为发送的记录添加最多5毫秒的延迟。
    //props.put("linger.ms", "1")
    //props.put("buffer.memory", "33554432")


    val producer = new KafkaProducer[String, String](props, new StringSerializer(), new StringSerializer())

    val lines = Source.fromFile("/Users/guxichang/monitor/21.log1").getLines()

      producer.initTransactions()

      var n = 1

      try {

        producer.beginTransaction()

        for (line <- lines) {

          producer.send(new ProducerRecord("t6", "", line), new KafkaProducerSendCallback(n, "/Users/guxichang/monitor/21.log1"))

          n += 1

        }

        producer.commitTransaction()



      } catch {
        //case _: ProducerFencedException => producer.close()
        //case _: OutOfOrderSequenceException => producer.close()
        //case _: AuthorizationException => producer.close()
        //case _: KafkaException => producer.abortTransaction()
        case _: Exception => producer.abortTransaction()

      }
      producer.close()

    }

}

