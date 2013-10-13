package dbsync;
import java.util.*;
import java.io.*;
import java.text.*;


public class LogMod{
				DbConn sdb,ddb,sysdb,sysdb1;
				String confFile = "./dbsync/conf/config.xml";
				xmlReader xmlReader;
				String logDest;
				String logFileName;	
				long logSizeSet;

			
//------------------------------------------------------------------------------------		
		public LogMod(String _logFileName){
				xmlReader = new xmlReader();
				logSizeSet = new Long(xmlReader.getFromConf("LOGFILESIZE",confFile));
				logFileName = _logFileName;
		}
		
		public LogMod(){
					xmlReader = new xmlReader();
					logSizeSet = new Long(xmlReader.getFromConf("LOGFILESIZE",confFile));
					logFileName = newLog();
		}
		
		
		public String newLog(){
			logDest = xmlReader.getFromConf("LOGDESTINATION",confFile);
			Date tdate ;
			tdate = new Date();
			String logFileNameMid = new SimpleDateFormat("yyMMDD_HHMMSS").format(tdate);
			logFileName = logDest + xmlReader.getFromConf("LOGFILEPREFIX",confFile)+logFileNameMid+".txt";
			return logFileName;
		}
		
				
		public boolean saveLog(int logLevel,String logMain){
			File tfile = new File(logFileName);
			try{
						int logLevelSet =new Integer(xmlReader.getFromConf("LOGLEVEL",confFile));
						if (logLevel <= logLevelSet){
								FileOutputStream fos = new FileOutputStream(logFileName,true);
								logMain = new Date()+"> "+logMain;
								fos.write(logMain.getBytes());
								fos.close();
								if (tfile.length() > logSizeSet){logFileName = newLog();}
								return true;
						}else{
							return true;
						}
			}catch(Exception e){
					System.out.println("[LOG EXCEPTION] A exception raised when save log:" + e+"@"+logFileName);
					return false;
				}
		}
}
