package cn.migu.vo

class HdfsSink(val path: String) {


  override def toString = s"HdfsSink==>$path"


  def canEqual(other: Any): Boolean = other.isInstanceOf[HdfsSink]

  override def equals(other: Any): Boolean = other match {
    case that: HdfsSink =>
      (that canEqual this) &&
        path == that.path
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(path)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}
