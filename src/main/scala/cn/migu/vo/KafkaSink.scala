package cn.migu.vo

class KafkaSink(val bootServr: String, val topic: String) {


  override def toString = s"\nKafkaSink==>$bootServr, $topic\n"


  def canEqual(other: Any): Boolean = other.isInstanceOf[KafkaSink]

  override def equals(other: Any): Boolean = other match {
    case that: KafkaSink =>
      (that canEqual this) &&
        bootServr == that.bootServr &&
        topic == that.topic
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(bootServr, topic)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}
