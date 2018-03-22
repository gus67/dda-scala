# 分布式数据传输组件 DDA

version | update | items 
:--: | :--: | :--:
0.10 | 初始项目| 2018.02.28
0.11 | 项目整体框架完成|2018.03.09
0.12 | 代码性能优化| 2018.03.13
0.13 | 代码review| 2018.03.21

🔗 [Scala 中文官方文档](http://docs.scala-lang.org/zh-cn/overviews/)

# 环境说明

> 1、测试服务器kafka 172.18.111.4:9093,172.18.111.5:9093,172.18.111.6:9093
>
> 2、测试服务器Hdfs  hdfs://hadoop （hdfs://192.168.129.186:8020/）

# 测试文档

1、配置文件说明

```config
RP /Users/guxichang/monitor

REG ^[\s\S]*\.log1$ cn.migu.utils.SimulationPlugin!!foo
K 172.18.111.4:9093,172.18.111.5:9093,172.18.111.6:9093 t6

REG ^[\s\S]*\.log2$ NA
K 172.18.111.4:9093,172.18.111.5:9093,172.18.111.6:9093 t7

REG ^[\s\S]*\.log3$ NA
H hdfs://hadoop/dda/d3/ YMD

REG ^[\s\S]*\.log4$ NA
H hdfs://hadoop/dda/d4 YMDH

```
RP 表示根目录

REG 表示正则 反射插件类!!方法 如果没有用NA表示

K kafkaSink topic（只能1个）

H HdfsSink YMDH（按小时分区） YMD（按天分区）YM（按月分区） Y（按年分区）

2、启动DDA组件方法

3、监控方文件方式

4、观测结果

5、失败如何处理

6、心跳如何保持

7、日志观测


# 分布式传输系统组件架构图

![Alt text](https://github.com/gus67/dda-scala/blob/master/src/main/resources/DDA-zj.png)

# 分布式传输系统开发文档概要

1、初始化配置文件，获得关键属性

reg_sinks_map:

K -> 正则表达字符串

V -> CS包含插件需要的类，以及相对应输出的SINK（KafkaSink，HdfsSink）

reg_quene_map:

K -> 正则表达字符串

V ->  ArrayBlockingQueue[DDAFile]] DDAFile == class DDAFile(val fileName: String, val path: String, val cs: CS)

```scala
//正则与输出源对应表
var reg_sinks_map: Map[String, CS] = Map()

var reg_quene_map: Map[String, ArrayBlockingQueue[DDAFile]] = Map()
```

2、初始化配置文件完成以后，单独一个线程，递归扫描根目录下（含）所有子文件夹下所有非COMPLETED后缀的文件

``` scala 
val s = Seq("bash", "-c", s"find ${InitFileSystem.root_path} -type f  ! -name '*.COMPLETED' $timing ") !!
```

3、验证扫描出来的文件是否符合正则表达，符合正则表达的一律放进相应的队列之中

```scala
loop.breakable {

//任意文件只会匹配到一个正则
  for (x <- regSet) {

    if (new Regex(x) findFirstIn s nonEmpty) {

      InitFileSystem.reg_quene_map(x).put(new DDAFile(InitFileSystem.getFileNameWithSuffix(s), s, InitFileSystem.reg_sinks_map(x)))

      log.info(s"\n\u001b[34;1m$s 匹配到一个正则 $x \u001b[0m\n".replace("),", ""))

      notFound = false

      loop.break
      }
    }
  }
```

4、并行的一个线程，文件发现线程，发现自进程启动以后，实时侦测文件下CREATE文件是件，侦测后的文件处理逻辑同3

```scala
 val interval = TimeUnit.SECONDS.toMillis(5)

 val observer = new FileAlterationObserver(InitFileSystem.root_path)

 observer.addListener(new FoundFile())

 //创建文件变化监听器
 val monitor = new FileAlterationMonitor(interval, observer)

 // 开始监控
 monitor.start()
```

5、并行的多个队列处理线程，每一个队列向目标Sink发送数据

```scala

//毒丸控制
val pp = new Breaks

while (true) {

  pp.breakable {

    try {

      val dda = abq.take

      val path = dda.path

      if (path.endsWith(".POISON_PILL")) {

        log.info(s"\n\u001b[32;1m ######## 发现毒丸文件，侦测文件到\u001b[0m\u001b[35;1m $k \u001b[0m\u001b[32;1m队列运行正常 ########！！！ \u001b[0m\n")

        pp.break()
      }

      val fileName = dda.fileName

      val clazz = dda.cs.clazz

      val sinks = dda.cs.sinks
             .
             .
             .
             略

}

/**
  * 实际处理细节
  * 1、插件化反射     
  * PluginUtils.reflectPlugin(clazz.split("!!")(0), clazz.split("!!")(1), path, lastFileName)
  * 
  * 2、转码       
  * Seq("bash", "-c", s"file --mime-encoding $lastFileName") !!
  * Seq("bash", "-c", s"iconv -f gbk -t utf-8 $lastFileName -o $tmpPath.UTF-8 ") !!
  *                           
  * 3、文件头+行号   
  * Seq("bash", "-c", "awk '$0=\"" + path + "=\"NR\"\037 \"$0' " + tmpFileArr.last + " > " + s"${tmpFileArr.last}.LINE_NUM") !!
  * 
  */

```

6、队列内部处理逻辑

```scala
for (sink <- sinks) {

  sink match {

    case k: KafkaSink =>

      new KafkaUtils().kafkaProducer4DDA(k, tmpFileArr)

    case h: HdfsSink =>

      new HdfsUtils().hdfsPut4DDA(h, tmpFileArr)

    case f: FtpSink =>
                   
                     .
                     .
                     .
                     略
                }
              }
```

7、毒丸线程

```scala
Executors.newSingleThreadExecutor().submit(new Runnable {
      
  override def run(): Unit = {

    while (true) {

      log.info(s"\n\u001b[34;1m ######## 开始生成毒丸 ########\u001b[0m\n")

       Seq("bash", "-c", s"rm -rf ${InitFileSystem.root_path}/*.POISON_PILL") !!

       Thread.sleep(5 * 1000)

       Seq("bash", "-c", s"touch ${InitFileSystem.root_path}/${System.currentTimeMillis()}.POISON_PILL") !!

       Thread.sleep(5 * 1000)
        }
      }
    })
```