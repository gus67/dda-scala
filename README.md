# 分布式数据传输组件 DDA

version | update | items 
:--: | :--: | :--:
0.10 | 初始项目| 2018.02.28
0.11 | 项目整体框架完成|2018.03.09
0.12 | 代码性能优化| 2018.03.13
0.13 | 代码review| 2018.03.21

🔗 [Scala 中文官方文档](http://docs.scala-lang.org/zh-cn/overviews/)

# 环境说明

> 1、测试服务器kafka 对应的地址
> /up/kafka_2.11-1.0.0/bin/kafka-console-consumer.sh --bootstrap-server 172.18.111.4:9093,172.18.111.5:9093,172.18.111.6:9093 --new-consumer --topic t6
>
> 2、测试服务器Hdfs 对应的地址
> hdfs://hadoop （hdfs://192.168.129.186:8020/）


# 分布式传输系统处理流程概要图

![Alt text](https://github.com/gus67/dda-scala/blob/master/src/main/resources/2.png)


# 分布式传输系统开发流程

1、初始化配置文件，获得关键属性

```scala
//正则与输出源对应表
var reg_sinks_map: Map[String, CS] = Map()
```

2、初始化配置文件完成以后，单独一个线程，递归扫描根目录下（含）所有子文件夹下所有非COMPLETED后缀的文件

``` scala 
val s = Seq("bash", "-c", s"find ${InitFileSystem.root_path} -type f  ! -name '*.COMPLETED' $timing ") !!
```

