package cn.migu.vo

class HdfsSink(val path: String,val partitionShu:String) {


  override def toString = s"$path, $partitionShu)"


  def canEqual(other: Any): Boolean = other.isInstanceOf[HdfsSink]

  override def equals(other: Any): Boolean = other match {
    case that: HdfsSink =>
      (that canEqual this) &&
        path == that.path &&
        partitionShu == that.partitionShu
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(path, partitionShu)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}
