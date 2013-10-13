package dbsync;
import java.io.*;
import java.sql.*;
import oracle.sql.*;


public class LoadBlob {
	public LoadBlob(){}
	
	public static void main(String[] args){
		try {
							Connection conn;
							String sip = "210.42.24.118";
							String ssid = "dbseven";
							String dbDriver = "oracle.jdbc.driver.OracleDriver";
							String strUrl = "jdbc:oracle:thin:@"+sip+":1521:"+ssid;
							Class.forName(dbDriver);
							conn = DriverManager.getConnection(strUrl, "kami", "kami");
							Statement stmt1 = conn.createStatement(); 
							ResultSet rst1 = stmt1.executeQuery("SELECT IMG,NAME,OPTIME FROM KAMI.TDBLOB ORDER BY ID DESC "); 
							
								rst1.next();
								OutputStream fout;
								InputStream ins;
								oracle.sql.BLOB BLOB =(oracle.sql.BLOB)rst1.getBlob("IMG");
								//Blob blob = rst1.getBlob("IMG");  
								//System.out.println(blob.length());
								String targetFile = rst1.getString("NAME");
								System.out.println(rst1.getString("OPTIME"));
								//ins = blob.getBinaryStream();
								ins = BLOB.getBinaryStream();								
								//System.out.println(BLOB.length());
								
								File file = new File("d:\\"+targetFile); 
								System.out.println("Load file:"+targetFile);
								fout = new FileOutputStream(file); 
								byte[] b = new byte[2048]; 
								int len = 0; 
	  					      int sum = 0;
	  					      while ( (len = ins.read(b)) != -1) { 
	  					        sum = sum + len;
	  					        fout.write(b, 0, len); 
	  					        //fout.write(b); 
	  					      }
	  					 System.out.println(sum);
	  					 conn.commit();
	  					 conn.close();
	  					 //fout.close();
	  					 //ins.close();
			}catch(Exception e){System.out.println(e);}
	}
}