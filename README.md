## 分布式数据传输组件 DDA

version | update | items 
:--: | :--: | :--:
0.10 | 初始项目| 2018.02.28
0.11 | 项目整体框架完成|2018.03.09
0.12 | kafka幂等性发送问题| 2018.03.13

🔗 [Scala 中文官方文档](http://docs.scala-lang.org/zh-cn/overviews/)

🔗 [Scala 菜鸟教程](http://www.runoob.com/scala/scala-tutorial.html)


#### 最大挑战之

---

### 1、发送性幂等问题

0.11之后支持幂等性发送和夸topic事务处理

由于文本发送存在kafka集群宕机，客户端程序宕机，客户端程序异常等问题，发送的文本可能部分成功
导致Exactly once（精确的一次）存在巨大挑战，在同一个KafkaProducer下可能实现幂等性发送，只是针对kafka
内部的重试机制，而对外部的或业务方的重复发送消息，并不能在源头上解决这个问题，即使通过callback
可以知道当前成功的消息，但是不能确保ack一定能够到达，所以这个巨大的挑战该如何实现？