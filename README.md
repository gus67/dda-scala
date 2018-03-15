## 分布式数据传输组件 DDA

version | update | items 
:--: | :--: | :--:
0.10 | 初始项目| 2018.02.28
0.11 | 项目整体框架完成|2018.03.09
0.12 | kafka幂等性发送问题| 2018.03.13

🔗 [Scala 中文官方文档](http://docs.scala-lang.org/zh-cn/overviews/)

🔗 [Scala 菜鸟教程](http://www.runoob.com/scala/scala-tutorial.html)

-----

![Alt text](https://github.com/gus67/dda-scala/blob/master/src/main/resources/1.png)


### 最大挑战之

-----

#### 1、发送幂等性问题处理

0.11之后支持幂等性发送和跨topic事务处理

   由于文本发送存在kafka集群宕机，客户端程序宕机，客户端程序异常等问题，发送的文本可能部分成功
导致Exactly once（精确的一次）存在巨大挑战，在同一个KafkaProducer下可能实现幂等性发送，只是针对kafka
内部的重试机制，而对外部的或业务方的重复发送消息，并不能在源头上解决这个问题，即使通过callback
可以知道当前成功的消息，但是不能确保ack一定能够到达，这个的挑战留在后续版本实现

#### 当前版本只能确保非极端情况发生示例Demo

```scala
      val producer = new KafkaProducer[String, String](props)

      val lines = Source.fromFile(tmpFiles.last).getLines()

      producer.initTransactions()

      var n = 0

      try {

        producer.beginTransaction()

        for (line <- lines) {

          producer.send(new ProducerRecord(kafkaSink.topic, "", line))

          n += 1

        }

        producer.commitTransaction()

        log.info(s"\n\u001b[33;1m${tmpFiles.last} $n 行全部 写入 Kafka ---> ${kafkaSink.bootServr}/${kafkaSink.topic} 成功  \u001b[0m\n")

        FileUtils.moveFile(new File(tmpFiles.head), new File(tmpFiles.head + ".COMPLETED"))

        log.info(s"\n\u001b[33;1m${tmpFiles.head} ---> ${tmpFiles.head}.COMPLETED  \u001b[0m\n")

        for (f <- tmpFiles.takeRight(tmpFiles.size - 1)) {

          FileUtils.forceDelete(new File(f))

          log.info(s"\n\u001b[34;1m$f ---> removed  \u001b[0m\n")
        }

      } catch {

        case _: Exception =>
          producer.abortTransaction()

      }
      producer.close()
```

