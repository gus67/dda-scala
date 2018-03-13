package cn.migu.utils

object LogUtils {

  import java.io.PrintWriter
  import java.io.StringWriter

  def getTrace(t: Throwable): String = {
    val stringWriter = new StringWriter
    val writer = new PrintWriter(stringWriter)
    t.printStackTrace(writer)
    val buffer = stringWriter.getBuffer
    s"${buffer.toString}\n系统不会因为该严重错误而停止运行\n"
  }
}
