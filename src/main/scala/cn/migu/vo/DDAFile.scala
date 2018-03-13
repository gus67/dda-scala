package cn.migu.vo

class DDAFile(val fileName: String, val path: String, val cs: CS) {


  override def toString = s"DDAFile($fileName,$path, $cs)\n"
}
