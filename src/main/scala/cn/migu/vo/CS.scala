package cn.migu.vo

import scala.collection.mutable

class CS(val clazz: String, val sinks: mutable.Buffer[Any]) {

  override def toString = s"CS($clazz, $sinks)\n\n"

}
