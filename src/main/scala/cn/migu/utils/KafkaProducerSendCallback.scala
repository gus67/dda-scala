package cn.migu.utils

import org.apache.kafka.clients.producer.{Callback, RecordMetadata}

class KafkaProducerSendCallback(val n: Int, val path: String) extends Callback {

  override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit = {

    if (exception == null) {
      println(n)
    }
  }
}