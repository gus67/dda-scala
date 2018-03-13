package cn.migu.utils

object PluginUtils {

  /**
    * @param clazzName
    * @param methodName
    * @param filePath
    * @return
    */
  def reflectPlugin(clazzName: String, methodName: String, filePath: String, targetFileName: String): String = {
    val clz = Class.forName(clazzName)
    val o = clz.newInstance()
    val m = clz.getMethod(methodName, classOf[String], classOf[String])
    m.invoke(o, filePath, targetFileName).toString
  }
}
