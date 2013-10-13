package dbsync;
import java.sql.*;
import oracle.jdbc.driver.*;
import oracle.sql.*;

public class DbConn{
		Connection conn;
		String dbDriver = "oracle.jdbc.driver.OracleDriver";
			
		public DbConn(String datasource,String confFile,String user,String pswd) throws Exception{ //从每一个任务文件中读取数据库连接参数
			xmlReader xmlReader;
			xmlReader = new xmlReader();
			Class.forName(dbDriver);
			if (datasource == "sou"){
					String sip = xmlReader.getFromConf("SIP",confFile);
					String ssid = xmlReader.getFromConf("SSID",confFile);
					String strUrl = "jdbc:oracle:thin:@"+sip+":1521:"+ssid;
					conn = DriverManager.getConnection(strUrl,user,pswd);
	
			}else if (datasource == "des"){
					String dip = xmlReader.getFromConf("DIP",confFile);
					String dsid = xmlReader.getFromConf("DSID",confFile);
					String strUrl = "jdbc:oracle:thin:@"+dip+":1521:"+dsid;
					conn = DriverManager.getConnection(strUrl,user,pswd);
			}else if (datasource == "sys"){
					String dip = xmlReader.getFromConf("SYS",confFile);
					String dsid = xmlReader.getFromConf("SYSSID",confFile);
					String strUrl = "jdbc:oracle:thin:@"+dip+":1521:"+dsid;
					System.out.println("!!!SYS connection string is"+strUrl);
					conn = DriverManager.getConnection(strUrl, "pandb", "pandb");  // a dbconnection for SQLCOMMAND and SQLCOMMANDBAK, database credential is pandb/pandb
			}
		}
		
		/*public OracleCallableStatement prepareCall(String sql) throws Exception{
			return (OracleCallableStatement) conn.prepareCall(sql);
		}*/
				
			
		public Statement createStatement() throws Exception{
   			return conn.createStatement();
  		}
  	
  	public PreparedStatement prepareStatement(String _sql) throws Exception{
   			return conn.prepareStatement(_sql);
  		}
  		
  	public void close() throws Exception{
  		conn.close();
  	}
  	  	
  	public String info(){
  			String information;
  			information = "Java connect to oracle9i testing";
  			return information;
  		}	
}