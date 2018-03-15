# 分布式数据传输组件 DDA

version | update | items 
:--: | :--: | :--:
0.10 | 初始项目| 2018.02.28
0.11 | 项目整体框架完成|2018.03.09
0.12 | kafka幂等性发送问题| 2018.03.13

🔗 [Scala 中文官方文档](http://docs.scala-lang.org/zh-cn/overviews/)

🔗 [Scala 菜鸟教程](http://www.runoob.com/scala/scala-tutorial.html)


# 环境说明

> 1、测试服务器kafka 对应的地址
> /up/kafka_2.11-1.0.0/bin/kafka-console-consumer.sh --bootstrap-server 172.18.111.4:9093,172.18.111.5:9093,172.18.111.6:9093 --new-consumer --topic t6
> 2、测试服务器Hdfs 对应的地址
> hdfs://hadoop （hdfs://192.168.129.186:8020/）


# 分布式传输系统处理流程

![Alt text](https://github.com/gus67/dda-scala/blob/master/src/main/resources/2.png)


# 挑战

## 1、发送幂等性问题处理

>单分区幂等性: 幂等producer无法实现多分区上的幂等性。如前所述，若要实现多分区上的原子性，需要引入事务
>单会话幂等性: 幂等producer无法跨会话实现幂等性。即使同一个producer宕机并重启也无法保证消息的EOS语义

