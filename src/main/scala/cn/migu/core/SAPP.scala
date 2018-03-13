package cn.migu.core


import java.sql.DriverManager

object SAPP {

  def main(args: Array[String]): Unit = {

    //    Class.forName("org.sqlite.JDBC")
    //    val c = DriverManager.getConnection("jdbc:sqlite:/Users/guxichang/Downloads/sqlite-tools-osx-x86-3220000/dda.db")
    //    val st = c.createStatement
    //    st.setQueryTimeout(3)
    //    val rs = st.executeQuery("SELECT * FROM emp;")
    //
    //    while ( rs.next() ) {
    //      println(rs.getInt("id"))
    //    }

    import org.sqlite.javax.SQLiteConnectionPoolDataSource
    val ds = new SQLiteConnectionPoolDataSource
    ds.setUrl(s"jdbc:sqlite:${InitFileSystem.USER_DIR}/dda.db")
    val st = ds.getPooledConnection.getConnection.createStatement()

    val rs = st.executeQuery("SELECT * FROM t1;")

    while (rs.next()) {

      println(s"${rs.getInt("id")}")
    }

  }
}
