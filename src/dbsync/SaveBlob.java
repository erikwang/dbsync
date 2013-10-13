package dbsync;
import java.util.*;
import java.io.*;
import java.sql.*;
import oracle.jdbc.driver.*;
import oracle.sql.*;
import java.sql.Types;
import java.text.*;

public class SaveBlob{
	

		public SaveBlob(){}
		
		public static void doSave(){
				try{
									Connection conn;
									String sip = "210.42.24.118";
									String ssid = "dbseven";
									String dbDriver = "oracle.jdbc.driver.OracleDriver";
									String strUrl = "jdbc:oracle:thin:@"+sip+":1521:"+ssid;
									Class.forName(dbDriver);
									conn = DriverManager.getConnection(strUrl, "kami", "kami");
									conn.setAutoCommit(false);
									
									String souFile;
									String sql1;
									File ifile;
									java.util.Date tdate ;
									tdate = new java.util.Date();
									String fileNameMid = new SimpleDateFormat("MMDDHHMMSS_").format(tdate);
									ifile = new File("c:\\1.jpg");
									String ifileName = ifile.getName();
									ifileName = fileNameMid+ifileName;
									sql1 = "INSERT INTO KAMI.TDBLOB VALUES (seq_des_all.nextval,'"+ifileName+"',empty_blob(),sysdate)";
									Statement stmt1 = conn.createStatement();
									stmt1.executeQuery(sql1);
									stmt1.close();
									
									
									String sql2;
									sql2 = "SELECT img FROM KAMI.TDBLOB WHERE NAME = '"+ifileName+"' FOR UPDATE";				
									Statement stmt2 = conn.createStatement();
									ResultSet rst2;
									rst2 = stmt2.executeQuery(sql2);
									
									rst2.next();
									oracle.sql.BLOB blob = (oracle.sql.BLOB) rst2.getBlob("IMG");
  								
  								OutputStream os = blob.getBinaryOutputStream();
			  					InputStream is = new FileInputStream(ifile);
			  					byte[] b = new byte[blob.getBufferSize()]; 
						      int len = 0; 
					        int sum = 0;
					        while ( (len = is.read(b)) != -1) { 
					        	os.write(b, 0, len); 
				          	sum = sum + len;
				          	//blob.putBytes(1,b); 
					       	} 
					       	is.close(); 
							   	os.flush(); 
							   	os.close(); 
			  					
  								/*byte[] buff = new byte[2048];  //用做文件写入的缓冲
  								int bytesRead;
  								
  								while(-1 != (bytesRead = input.read(buff, 0, buff.length))) {
  								//while(-1 != (bytesRead = input.read(buff))) {
   										output.write(buff, 0, bytesRead);
   										//output.write(buff);
   										
  								}*/
  								System.out.println(sum);
  								rst2.close();
  								stmt2.close();
  								conn.commit();
  							conn.setAutoCommit(true);  
  			}catch(Exception e){
  				System.out.println(e);
  			}
		}
		
		public static void main(String[] args){
				doSave();

		}
		
		
}