package cn.migu.utils

import java.lang.management.ManagementFactory

object JVMUtils {

  def jvmPid(): Int = {
    try {
      val runtime = ManagementFactory.getRuntimeMXBean
      val jvm = runtime.getClass.getDeclaredField("jvm")
      jvm.setAccessible(true)
      val mgmt = jvm.get(runtime)
      val pidMethod = mgmt.getClass.getDeclaredMethod("getProcessId")
      pidMethod.setAccessible(true)
      pidMethod.invoke(mgmt).asInstanceOf[Int]
    } catch {
      case ex: Exception => ex.printStackTrace()
        -1
    }
  }

  def main(args: Array[String]): Unit = {
    println(jvmPid())
  }
}
