package dbsync;
import java.util.*;
import java.sql.*;
import oracle.jdbc.driver.*;
import java.io.*;

public class Syncdb extends Thread{
	public Syncdb(){}
	public static void main(String[] args){
		int	tmatch = 0 ;
		int tinsert = 0;
		int tdelete = 0;
		int tupdate = 0;
		int	tsaved = 0;
		int	texec = 0;
		int	tlogg = 0;
		xmlReader xmlReader;
		xmlReader = new xmlReader();
		LogMod log;
		log = new LogMod();
		String _logFileName = log.logFileName;
		System.out.println("LOGFILENAME:"+_logFileName);
		//String _confFile = "./dbsync/conf/config.xml";
                String _confFile = "conf/config.xml";
		Vector<String> syncList;
		syncList = new Vector<String>();
		syncList = xmlReader.getListFromConf("DBSYNC",_confFile);
		int synctime  = Integer.parseInt(xmlReader.getFromConf("SYNCTIME",_confFile)); 
		
		System.out.println("//////////////////////////////////////////////////////////////////////////");
		System.out.println("::::::Database Sync Engine V-1.1 get ready @ "+new java.util.Date());
		System.out.println("[SYS] Log saved in file:"+_logFileName);
		
		System.out.println("---------------------------------------------------------------------------");
		for(int xx=0;xx<syncList.size();xx++){
			System.out.println("| [SYS] Using config file list:"+syncList.get(xx));
		}
		System.out.println("---------------------------------------------------------------------------");
		System.out.println("//////////////////////////////////////////////////////////////////////////");
		log.saveLog(1,"::::::Database Sync Engine V-1.0 get ready\n");
		
		CompareData cd1;
		cd1 = new CompareData(_logFileName);
		
		System.out.println("\n[Engine] ==== Phase 1:System initialize.====================");
		log.saveLog(2,"[Engine] >>>> Phase 1:System initialize.\n");
						if (cd1.initSystem()){
								System.out.println("[SYS] System initialize done.");
								log.saveLog(2,"[SYS] System initialize done.\n");
						}else{
								System.out.println("[SYS EXCEPTIONS] System initialize failed. Please check destnation database.");
								log.saveLog(1,"[SYS EXCEPTIONS] System initialize failed. Please check destnation database.\n");
								return;
						}
		System.out.println("[Engine]__________________ End of Phase 1 __________________");
		
		
			try{
				for(int r=0;r<syncList.size();r++){
								String starttime = new java.util.Date().toString();
								String confFile = syncList.get(r);
								System.out.println(confFile);
								String ROWIDNAME = xmlReader.getFromConf("ROWID",confFile);
								String STABLE = xmlReader.getFromConf("STABLE",confFile);
								String DTABLE = xmlReader.getFromConf("DTABLE",confFile);
								String suser = xmlReader.getFromConf("SUSER",confFile);
								String duser = xmlReader.getFromConf("DUSER",confFile);
								String spswd = xmlReader.getFromConf("SPSWD",confFile);
								String dpswd = xmlReader.getFromConf("DPSWD",confFile);
								int offset = Integer.parseInt(xmlReader.getFromConf("OFFSET",confFile));
								CompareData cd;
								cd = new CompareData(_logFileName,confFile);
								String SCOLUMNS[] = new String[2];
								SCOLUMNS = cd.getSelectColumns("sou",STABLE,ROWIDNAME);
								String DCOLUMNS,DCOLUMNS1;
								DCOLUMNS = "sis_ori_rowid,sis_des_optime,sis_des_rowidsn "+SCOLUMNS[0];
								DCOLUMNS1 = "sis_ori_rowid,sis_des_optime,sis_des_rowidsn "+SCOLUMNS[1];
								//SCOLUMNS = ROWIDNAME+" "+SCOLUMNS;		//��souû��Ԥ�������ROWID,��������view���ṩ��rid...
								//SCOLUMNS = "ROWID "+SCOLUMNS;		
								int dvllength;
								SfToDec sftd;
								sftd = new SfToDec();
								long output;
								DataVectorList sdvl,ddvl;
								sdvl = new DataVectorList();
								ddvl = new DataVectorList();
								
								System.out.println("\n[Engine] ==== Phase 2:Datasource dump.======================");
								log.saveLog(2,"[Engine] >>>> Phase 2:Datasource dump.\n");
								System.out.println("[Engine] Using config file:"+confFile);
								log.saveLog(2,"[Engine] Using config file:"+confFile+"\n");
								System.out.println("[DB] Database dump started.\n");
								log.saveLog(2,"[DB] Database dump started.");
								
								cd.dumpDatabaseTable("sou","des",STABLE,DTABLE);
								
								System.out.println("[Engine]__________________ End of Phase 2 __________________");
								log.saveLog(2,"[DB] Database dump finished.\n");
								//����sql����ж�����ֶ�һһ��Ӧ
																
								System.out.println("\n[Engine] ==== Phase 3:Compare.==============================");
								log.saveLog(2,"[Engine] >>>> Phase 3:Compare.\n");
									
								DataVector tdv;
								dvllength = cd.getDvlLength(STABLE,offset);
								int looptime = cd.getLoopTime(dvllength,offset);
								
								System.out.println("[Engine]"+STABLE+" contains "+dvllength+" records , engine will loop "+looptime+" times");
								log.saveLog(2,"[Engine]"+confFile+" will loop "+looptime+" times.\n");
								int requeststart = 1,requestend = 1;
								Vector<String> bgList;
								bgList = new Vector<String>();
								System.out.println("\n[Engine] Now calculate check point......");
								log.saveLog(2,"[Engine] Begin to calculate check point.\n");
								bgList = cd.getGroupBoundary(STABLE,offset,ROWIDNAME);
								System.out.println("[Engine] Check point calculate done, "+bgList.size()+" check point setted.");
								log.saveLog(2,"[Engine] Check point calculate done.\n");
								
								for(int lt = 0;lt < looptime;lt++){
										int dvllength1 = cd.getDvlLength(STABLE,offset);
										System.out.println("--"+bgList.get(lt));
										if (dvllength1 != dvllength){ //if source data has been modified (insert, delete),transcation must be halt
  											System.out.println("[Engine]!!!!!!!!!!!!!Source Data has changed! process halt!!!!!!!!!!!!!!!!");
  											log.saveLog(2,"[Engine]!!!!!!!!!!!!!Source Data has changed! process halt!!!!!!!!!!!!!!!!");
												//break;
											}
										
										if (lt > 0){
												if(lt == looptime - 1){
														requeststart = requestend + 1;
														dvllength = cd.getDvlLength(STABLE,offset);
														requestend = dvllength;
												}else{
														requeststart = requestend + 1 ;
														requestend = requestend + offset;
												}
										}else{
											requeststart = 1;
											requestend = requeststart + offset - 1;
										}
										String sqls="",sqld="";
										if ( ! bgList.get(lt+1).equals("end")){
											sqls = "SELECT rd"+SCOLUMNS[1]+"  from (select rownum rm, "+ROWIDNAME+" rd "+ SCOLUMNS[0] +" from (select "+ROWIDNAME+" "+SCOLUMNS[0]+" from "+STABLE+" order by rowidtochar("+ROWIDNAME+") )) a WHERE  rowidtochar(a.rd) >= '"+bgList.get(lt)+"' and rowidtochar(a.rd) <'"+bgList.get(lt+1)+"'";
											sqld = "SELECT "+DCOLUMNS1+"  from (select rownum rm,sis_ori_rowid rd,"+ DCOLUMNS +" from (select "+DCOLUMNS+" from "+DTABLE+" ORDER BY sis_ori_rowid)) a WHERE  a.rd >='"+bgList.get(lt)+"' and a.rd < '"+bgList.get(lt+1)+"'";
										}else{
											sqls = "SELECT rd"+SCOLUMNS[1]+"  from (select rownum rm, "+ROWIDNAME+" rd "+ SCOLUMNS[0] +" from (select "+ROWIDNAME+" "+SCOLUMNS[0]+" from "+STABLE+" order by rowidtochar("+ROWIDNAME+") )) a WHERE  rowidtochar(a.rd) >= '"+bgList.get(lt)+"'";
											sqld = "SELECT "+DCOLUMNS1+"  from (select rownum rm,sis_ori_rowid rd,"+ DCOLUMNS +" from (select "+DCOLUMNS+" from "+DTABLE+" ORDER BY sis_ori_rowid)) a WHERE  a.rd >= '"+bgList.get(lt)+"'";
										}
										
										System.out.println("[ENGINE] Starting "+(lt+1)+" / "+looptime+" | Total "+Math.floor(((double)(lt+1)/looptime)*100)+"%\n");
										System.out.println("[SDB] Read  "+requeststart+" TO "+requestend+" started @ "+new java.util.Date());
										log.saveLog(2,"[DB] Read s-database started , with sql: "+sqls+"\n");
										sdvl = sdvl.readFromDatabase("sou",sqls,confFile,suser,spswd);
										System.out.println("[DDB] Read  "+requeststart+" TO "+requestend+" started @ "+new java.util.Date());
										log.saveLog(2,"[DB] Read from d-database started , with sql: "+sqld+"\n");
										ddvl = ddvl.readFromDatabase("des",sqld,confFile,duser,dpswd);
										/*cd.showDatavectorList(ddvl); ����DVL�Ƿ���ȷ�Ĵ���
										System.out.println("---------------------------------------------");
										cd.showDatavectorList(sdvl);*/
										System.out.println("[CD] Compare started @ "+new java.util.Date());		
										log.saveLog(2,"[ENGINE] Starting "+(lt+1)+" of "+looptime+"\n");
										cd.compareDataVectorList(sdvl,ddvl); //Every foe start here orz
										System.out.println("[Engine] End of "+(lt+1)+" of "+looptime);
										System.out.println("[Summary] Matched :"+cd.matchTime+" | Insert :"+cd.insertTime+" | Delete:"+cd.deleteTime+" | Update:"+cd.updateTime+" |SQL Produce/Execute/Logged:"+cd.savedSql+"/"+cd.executedSql+"/"+cd.loggedSql+"\n");
										log.saveLog(2,"[Engine] End of "+(lt+1)+"/"+looptime+".\n");
										log.saveLog(2,"[Summary] Matched :"+cd.matchTime+" | Insert :"+cd.insertTime+" | Delete:"+cd.deleteTime+" | Update:"+cd.updateTime+" |SQL Produce/Execute/Logged:"+cd.savedSql+"/"+cd.executedSql+"/"+cd.loggedSql+"\n");
								}
								System.out.println("[Engine]__________________ End of Phase 3 __________________");
								log.saveLog(2,"[Engine] ====Compare done.\n");
								System.out.println("\n[Engine] ==== Phase 4:The summary.==========================");
								System.out.println("[Engine] ==== Job Start @"+starttime);
								System.out.println("[Engine] ==== Job finished @"+new java.util.Date());
								System.out.println("[Engine] See log file @"+_logFileName);
								System.out.println("[Sum] Matched times:"+cd.matchTime+" times.");
								System.out.println("[Sum] Insert action:"+cd.insertTime+" times.");
								System.out.println("[Sum] Delete action:"+cd.deleteTime+" times.");
								System.out.println("[Sum] Update action:"+cd.updateTime+" times.");
								System.out.println("[Sum] Number of producted sql command:"+tsaved);
								System.out.println("[Sum] Number of executed sql command:"+cd.executedSql);
								System.out.println("[Sum] Number of logged sql command:"+cd.loggedSql);
								System.out.println("[Engine]__________________ End of Phase 5 __________________");
								
								log.saveLog(2,"[Summary] Matched :"+cd.matchTime+" | Insert :"+cd.insertTime+" | Delete:"+cd.deleteTime+" | Update:"+cd.updateTime+" |SQL Produce/Execute/Logged:"+cd.savedSql+"/"+cd.executedSql+"/"+cd.loggedSql+"\n");
								
								if(r<(syncList.size()-1)){
										System.out.println("[Engine] ==== Now sleeping for "+(synctime/1000)+" seconds");
										System.out.println("");
										log.saveLog(2,"[Engine] Sleeping for "+synctime+"\n");
										sleep(synctime);
								}else{
									System.out.println("[Engine] All done! Good bye~");
									log.saveLog(2,"[Engine] All done! see ya~\n");
								}
				}
			}catch (Exception e){
				System.out.println("[SYS] A exception raised:"+e);
				log.saveLog(1,"[!!!SYS EXCEPTION!!!] A exception raised:"+e+"\n");
				return;
			} 
 }
}
