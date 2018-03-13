package cn.migu.vo

class FtpSink(val ip: String, val port: Int, val name: String, val passwd: String, val path: String) {


  override def toString = s"\nFtpSink==>$ip, $port, $name, $passwd, $path\n"


  def canEqual(other: Any): Boolean = other.isInstanceOf[FtpSink]

  override def equals(other: Any): Boolean = other match {
    case that: FtpSink =>
      (that canEqual this) &&
        ip == that.ip &&
        port == that.port &&
        name == that.name &&
        passwd == that.passwd &&
        path == that.path
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(ip, port, name, passwd, path)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}
