package dbsync;
import java.util.*;
import java.math.*;
import java.sql.*;
import oracle.jdbc.driver.*;
import oracle.sql.*;


public class CompareData{
				DbConn sdb,ddb,sysdb,sysdb1;
				String confFile;
				xmlReader xmlReader;
				int insertTime = 0;
				int matchTime = 0;
				int updateTime = 0;
				int deleteTime = 0;
				int savedSql = 0;
				int executedSql = 0;
				int loggedSql = 0;
				boolean tbCreateFlag = false;
				LogMod log;
				String _emptyChar; //���dvl or StrArr��Ϊ�յ�λ�ã���ʲô�ַ�����
                                FunctionalDependency fd;
                                Vector<String> scolumnnames;

    public void setScolumnnames(Vector<String> scolumnnames) {
        this.scolumnnames = scolumnnames;
    }
                                
public void setFd(FunctionalDependency _fd){
    this.fd = _fd;
}


//------------------------------------------------------------------------------------

public boolean checkFD(Data _data, FunctionalDependency fd){
    String pivot = fd.getAttribute();
    int pindex = getIndexOfColumn(pivot,scolumnnames);
    pindex++;
    String operator = fd.getOperator();
    
    System.out.println("[ENG][DEBUG] size:"+_data.getSize());
    for(int t = 0 ;t < _data.getSize(); t++){
        System.out.println("[ENG][DEBUG]"+_data.getDataInfo(t));
    }
    
    if (operator.equals("great")){
        System.out.print("[Eng][FD] Catch a >= ");
        System.out.println("[Eng][Debug]"+new Float(_data.getDataInfo(pindex)).floatValue()+"  "+fd.getValue());
        if(new Float(_data.getDataInfo(pindex)).floatValue() >= fd.getValue()){
            return true;
        }
    }
    System.out.print("[Eng][FD] Noting...");
    return false;
}

			
//------------------------------------------------------------------------------------		
		public CompareData(String _logFileName,String _confFile){
			xmlReader = new xmlReader();
			log = new LogMod(_logFileName);
			String suser = xmlReader.getFromConf("SUSER",_confFile);
			String duser = xmlReader.getFromConf("DUSER",_confFile);
			String spswd = xmlReader.getFromConf("SPSWD",_confFile);
			String dpswd = xmlReader.getFromConf("DPSWD",_confFile);
			//System.out.println("^^^"+suser+" "+duser);
			try{
				sdb = new DbConn("sou",_confFile,suser,spswd);
				ddb = new DbConn("des",_confFile,duser,dpswd);
			}catch (Exception e){
				System.out.println ("Exception from cd init[SOU DES, _logfile, _confFile]:"+e);		
			}
			try{

				//sysdb = new DbConn("sys","./dbsync/conf/config.xml","","");
				//sysdb1 = new DbConn("sys","./dbsync/conf/config.xml","","");
                                sysdb = new DbConn("sys","conf/config.xml","","");
				sysdb1 = new DbConn("sys","conf/config.xml","","");
			}catch (Exception e){
				System.out.println ("Exception from cd init[SYS,SYS, _logfile, _confFile]:"+e);
				log.saveLog(1,"[!!!EXCEPTION!!!]Compare Data Init:"+e+"\n");
			}
			
			confFile = _confFile;
			_emptyChar = xmlReader.getFromConf("EMPTYCHAR",confFile);
		}
		
		public CompareData(String _logFileName){
			log = new LogMod(_logFileName);
			try{
				//sysdb = new DbConn("sys","./dbsync/conf/config.xml","","");
				//sysdb1 = new DbConn("sys","./dbsync/conf/config.xml","","");
                                  sysdb = new DbConn("sys","conf/config.xml","","");
				  sysdb1 = new DbConn("sys","conf/config.xml","","");
			}catch (Exception e){
				System.out.println ("Exception from cd init:"+e);
				log.saveLog(1,"[!!!EXCEPTION!!!]Compare Data Init:"+e+"\n");
			}
			/*xmlReader = new xmlReader();
			_emptyChar = xmlReader.getFromConf("EMPTYCHAR",confFile);*/
		}



//------------------------------------------------------------------------------------		
		public boolean initSystem(){  //���sis.sqlcommand
				boolean flag = false;
				try{
						String sql1 = "delete from sqlcommand"; 
						Statement stmt1 = sysdb.createStatement();
						stmt1.executeQuery(sql1);
						stmt1.close();
						flag = true;
				}catch (Exception e){flag = false;}
				return flag;
		}

//------------------------------------------------------------------------------------		
		public void compareDataVectorList(DataVectorList sdvl,DataVectorList ddvl){
				//xmlReader = new xmlReader();
				boolean flag;
				flag = false;
				int sdvlLength,ddvlLength;
				sdvlLength = sdvl.getDataVectorListSize();
				ddvlLength = ddvl.getDataVectorListSize();
				log.saveLog(3,"[CD] DataVector Size | [Sou DVL Size]:"+sdvlLength+" [Des DVL Size]:"+ddvlLength+"\n");
				//System.out.println(sdvlLength+"!!"+ddvlLength);
				
				System.out.println("[CD S to D] >Begin a Source to Destination compare"); //Դ��Ŀ��ıȽϣ����ж����׷�ӵ������ͬʱƽ�бȽ��ɴ˷���
				log.saveLog(2,"[CD S to D]>Begin a Source to Destination compare"+"\n");
				for(int i=0;i<sdvlLength;i++){
						for(int j=0;j<ddvlLength;j++){
								if (! flag){
										if (sdvl.get(i).getIndex().equals(ddvl.get(j).getIndex())){
												//System.out.println("[CD S to D] Find a equal Index "+sdvl.get(i).getIndex()+", Entering parallel compare MOD...");
												System.out.print(">");
												log.saveLog(3,"[CD S to D] Find a equal Index "+sdvl.get(i).getIndex()+", Entering parallel compare MOD..."+"\n");
												paraCompareDv(sdvl.get(i),ddvl.get(j));  // Enter para compare
												flag = true;
												log.saveLog(3,"[CD S to D] Index "+sdvl.get(i).getIndex()+" Parallel compare done!"+"\n");
										}
								}
						}
					if(! flag){
						//System.out.println("[CD S to D] Index "+sdvl.get(i).getIndex()+" Not found in DDVL, mass insert started.");//Some Insert actions should been done.
						System.out.print("+");
						log.saveLog(3,"[CD S to D] Index "+sdvl.get(i).getIndex()+" Not found in DDVL, Begin A Block Dump process..."+"\n");
						//dumpDvData("des","sisdest.td_sisdest",sdvl.get(i)); //Found a new index , begin some INSERT actions to insert all the records with the new index to destination
						dumpDvData("des",xmlReader.getFromConf("DTABLE",confFile),xmlReader.getFromConf("STABLE",confFile),sdvl.get(i)); 
						log.saveLog(3,"[CD S to D] Index "+sdvl.get(i).getIndex()+" Block Dump DONE!."+"\n");
						flag = false;
					}
					flag = false;
				}
				System.out.println("\n");
				System.out.println("[CD S to D] >Finished a Source to Destination compare");
				log.saveLog(2,"[CD S to D]>A Source to Destination compare finished."+"\n");
				System.out.println("[CD D to S] >Begin a Destination to Source compare");	//Ŀ�굽Դ�ıȽϣ��ж����ɾ������
				log.saveLog(2,"[CD D to S]>Begin a Destination to Source compare"+"\n");
				for(int i=0;i<ddvlLength;i++){
						for(int j=0;j<sdvlLength;j++){
								if (! flag){
										if (ddvl.get(i).getIndex().equals(sdvl.get(j).getIndex())){
												log.saveLog(3,"[CD D to S] Find a equal Index :"+ddvl.get(i).getIndex()+" but do nothing.\n");
												System.out.print("<");
												//System.out.println("[CD] (!)Find a equal Index :"+ddvl.get(i).getIndex());
												//System.out.println("[CD] (>)Entering Block Data Compare MOD...");
												flag = true;
										}
								}
						}
					if(! flag){
						//System.out.println("[CD D to S](!)Index "+ddvl.get(i).getIndex()+" Not found in SDVL, Some Delete,import actions should been done.");
						System.out.print("-");
						log.saveLog(3,"[CD D to S] Index "+ddvl.get(i).getIndex()+" Not found in SDVL, Begin A Block Remove (in D) process..."+"\n");
						removeDvData("des",xmlReader.getFromConf("DTABLE",confFile),xmlReader.getFromConf("STABLE",confFile),ddvl.get(i)); //Found a index not in destination , begin some delete actions to remove the records with the index in destination
						log.saveLog(3,"[CD D to S] Index "+ddvl.get(i).getIndex()+" Block Remove DONE!."+"\n");
						flag = false;
					}
					flag = false;
				}
				System.out.println("\n[CD] >Finished a Destination to Source compare------------------");
				log.saveLog(2,"[CD D to S]>A Destination to Source compare finished."+"\n");
		}
		//------------------------------------------------------------------------------------			
		public void dumpDatabaseTable(String sdbsource,String ddbsource,String stablename,String dtablename){
			try{
				/*DbConn sdb,ddb;
				sdb = new DbConn(sdbsource);
				ddb = new DbConn(ddbsource);*/
				String[] ss = new String[20];
				ss = dtablename.split("[.]");
				String sql0 = "select owner,table_name from all_tables where table_name = '"+ss[1]+"'";
				ResultSet rst0 = null;
				Statement stmt0 = ddb.createStatement();
				rst0 = stmt0.executeQuery(sql0);
				String tsche = null;
				if (rst0.next()){
						if (rst0.getString(1) != null){
											tsche = rst0.getString(1)+"."+rst0.getString(2);
						}
				}
				stmt0.close();
				rst0.close();
				
				if (dtablename.equals(tsche)){
					System.out.println("[DB] ----------------------------");
					System.out.println("[DB] | Table already exisisted  |");
					System.out.println("[DB] ----------------------------");
					log.saveLog(1,"\n[DB] Table already exisisted\n");
					return;
				}
				
				String sql1 = "select * from "+stablename+" where rownum < 1"; 
				ResultSet rst1 = null;
				Statement stmt1 = sdb.createStatement();
				rst1 = stmt1.executeQuery(sql1);
				ResultSetMetaData rsmd = rst1.getMetaData();
				int numberOfColumns = rsmd.getColumnCount();
				//System.out.println(numberOfColumns);
				String sql2;
				String dtcolumnname,dtcolumntype;
				int dtcolumnleng;
				
				//׷��sis_ori_rowid,opdate�����±�
				sql2 = "CREATE TABLE "+dtablename+" (sis_ori_rowid varchar2(32) primary key";
				//sql2 = "CREATE TABLE "+dtablename+" (sis_ori_rowid rowid primary key";
				sql2 = sql2 + " , sis_des_optime Date default SYSDATE , sis_des_rowidsn number(32)";
				
				for(int y = 1;y < (numberOfColumns+1);y++){
						dtcolumnname = rsmd.getColumnName(y);
						dtcolumntype = rsmd.getColumnTypeName(y);
						dtcolumnleng = rsmd.getColumnDisplaySize(y);
						if ( ! (dtcolumntype.equals("ROWID"))){
								//if (! ((dtcolumntype.equals("DATE")) || (dtcolumntype.equals("LONG")) || (dtcolumntype.equals("NUMBER")) || (dtcolumntype.equals("BLOB")) ||  (dtcolumntype.equals("CLOB")))){ ���ϣ�����Զ��ж�data���͵ȣ��������������룬���Ӽ�¼data���͵�index���飬�Ա������insert����ж�Ӧλ��ʹ��to_date()
								if (! ((dtcolumntype.equals("DATE")) || (dtcolumntype.equals("LONG")) || (dtcolumntype.equals("NUMBER")) || (dtcolumntype.equals("BLOB")) ||  (dtcolumntype.equals("CLOB")))){
										sql2 = sql2 +" , "+dtcolumnname+" "+dtcolumntype+"("+dtcolumnleng+")";
								}else{
										if (dtcolumntype.equals("DATE")){
											sql2 = sql2 +" , "+dtcolumnname+" varchar2(64) ";
									}else{
											sql2 = sql2 +" , "+dtcolumnname+" "+dtcolumntype;
									}
								}
						}
				}
				sql2 = sql2 + ")";
				rst1.close();
				System.out.println("[DB] Preparing to run create table "+dtablename);
				//System.out.println(sql2);
				ResultSet rst2 = null;
				Statement stmt2 = ddb.createStatement();
				rst2 = stmt2.executeQuery(sql2);
				stmt2.close();
				rst2.close();
				System.out.println("[DB] -------------------------");
				System.out.println("[DB] |New table created!      |");
				System.out.println("[DB] -------------------------");
				log.saveLog(1,"[DB] New table "+dtablename+" created!"+"\n");
				tbCreateFlag = true;
			}catch(Exception e){
				System.out.println("[DB] ------------------------------");
				System.out.println("[DB] | Exception during dump table!|");
				System.out.println("[DB] ------------------------------");
				System.out.println(e);
				log.saveLog(1,"\n[DB] Exception:"+e+"\n");
			}
	}
	
	//------------------------------------------------------------------------------------			
		public Vector<String> getDvlIndex(DataVectorList dvl){
				Vector<String> dvlindex;
				dvlindex = new Vector<String>();
				String currentindex;
				String lastindex;
				currentindex = "";
				lastindex = "";
				int dvlLength = dvl.getDataVectorListSize();
				for(int i=0;i<dvlLength;i++){
							currentindex = dvl.get(i).getIndex();
							if (! currentindex.equals(lastindex)){
									//System.out.println(i+"~~get a new index:"+currentindex);
									dvlindex.add(currentindex);
									lastindex = currentindex;
							}
				}
				return dvlindex;
		}

	//------------------------------------------------------------------------------------			
		 public void showDataVector(DataVector dv,String index){
		 		System.out.println("[DV] Entering show Datavector process...");
	  		Data tempdata;
				String output;
				output = "";
				tempdata  = new Data();
				int vsize = dv.getDataVectorSize();
				for (int t = 0; t < vsize; t++){
					//System.out.println("vsize"+vsize);
					output = index;
					//output = "*Row Vector:" + t+"\r\n"; 
					tempdata = dv.getFromDataVector(t);
					int tsize = tempdata.getSize();
					//System.out.println("tsize"+tsize);
					for (int j = 0;j<tsize; j++){
						output = output + "["+t+"-"+j+"] "+(String)tempdata.getDataInfo(j);
					}
					System.out.println(output);
					//System.out.println("[DV] MD5:"+getMd5(tempdata,1));
				}
	  }
	  
	 //------------------------------------------------------------------------------------			
		 public void showDatavectorList(DataVectorList dvl){
		 		System.out.println("[DV] Entering show DatavectorList process...");
		 		Vector<String> vtemp;
		 		DataVector dv;
		 		int vtempsize;
		 		vtemp = new Vector<String>();
		 		vtemp = getDvlIndex(dvl);
		 		vtempsize = vtemp.size();
		 		for(int i=0;i<vtempsize;i++){
		 			
		 			dv =  dvl.getDataVector(vtemp.get(i));
		 			showDataVector(dv,vtemp.get(i));	
		 		}
	  	}
	
	//------------------------------------------------------------------------------------			
		public String getMd5(Data tempdata , int offset){ //offset��ʾ�ӵڼ��п�ʼȡ��ݣ���֤׷�ӵ��ֶ��ڱ����ǰn�� ����MD5ֵ
				int tsize = tempdata.getSize();
				String output;
				output = "";
				for (int j = offset - 1;j<tsize; j++){
					output = output + (String)tempdata.getDataInfo(j);
				}
				Md5 md5;
				md5 = new Md5();
				return md5.getMd5(output);
		}

//------------------------------------------------------------------------------------			
		public String getMd5OriStr(Data tempdata , int offset){ //offset��ʾ�ӵڼ��п�ʼȡ��ݣ���֤׷�ӵ��ֶ��ڱ����ǰn�� ����String
				int tsize = tempdata.getSize();
				String output;
				output = "";
				for (int j = offset - 1;j<tsize; j++){
					output = output + (String)tempdata.getDataInfo(j);
				}
				
				return output;
		}

//------------------------------------------------------------------------------------			
		public long getMd5OriLong(Data tempdata , int offset){ //offset��ʾ�ӵڼ��п�ʼȡ��ݣ���֤׷�ӵ��ֶ��ڱ����ǰn�� ����String
				int tsize = tempdata.getSize();
				long  output;
				output = 0;
				for (int j = offset - 1;j<tsize; j++){
					if ( ! (tempdata.getDataInfo(j)==null)){
						output = output + tempdata.getDataInfo(j).hashCode();
					}else{
						output = output + "".hashCode();
					}
				}
				
				return output;
		}
	
		
	//------------------------------------------------------------------------------------			
		public String[] getSelectColumns(String dbsource,String tablename,String rowidname){ //get a string like ",col1,col2,col3"
				String SCOLUMNS[];
				SCOLUMNS = new String[2];
				SCOLUMNS[0] = "";
				SCOLUMNS[1] = "";
				//SCOLUMNS = "";
				String sql1;
				sql1 = "SELECT * FROM "+tablename+" WHERE rownum < 2";
				try{
						/*DbConn db;
						db = new DbConn(dbsource);*/
						ResultSet rst1 = null;
						Statement stmt1;
						if (dbsource.equals("des")){
								stmt1 = ddb.createStatement();
								rst1 = stmt1.executeQuery(sql1);
						}else{
								stmt1 = sdb.createStatement();
								rst1 = stmt1.executeQuery(sql1);
						}
						
						ResultSetMetaData rsmd = rst1.getMetaData();
						int numberOfColumns = rsmd.getColumnCount();
						//for (;rst1.next();){
							for(int y = 1;y < (numberOfColumns+1);y++){
								//tcs.add(rst1.getString(rsmd.getColumnName(y)));
								if ( ! rsmd.getColumnName(y).equals(rowidname)){
										SCOLUMNS[0] = SCOLUMNS[0] + "," + rsmd.getColumnName(y);
										SCOLUMNS[1] = SCOLUMNS[1] + "," + "trim("+rsmd.getColumnName(y)+")";
								}
							}
						//}
						rst1.close();
						stmt1.close();
				}catch (Exception e){System.out.println(e);}
				return SCOLUMNS;
		}
	
	//------------------------------------------------------------------------------------			
		public void dumpDvData(String datasource,String dtablename,String stablename,DataVector dv){ //First time dump data to destination table
				
                                int executeflag = 0;
                                SfToDec sftd;
				sftd = new SfToDec();
				String sql1;
				Data tempdata;
				tempdata  = new Data();
				int vsize = dv.getDataVectorSize();
				for (int t = 0; t < vsize; t++){
					tempdata = dv.getFromDataVector(t);
					int tsize = tempdata.getSize();
					sql1 = "INSERT INTO "+dtablename+" VALUES('";
					//sql1 = sql1 + (String)tempdata.getDataInfo(0)+"',SYSDATE,seq_des_all.nextval";
					sql1 = sql1 + (String)tempdata.getDataInfo(0)+"',SYSDATE,"+ Long.toString(sftd.getDec((String)tempdata.getDataInfo(0)));
					for (int j = 1;j<tsize; j++){
						if (! ((tempdata.getDataInfo(j)) == null)){
							sql1 = sql1 + ",'"+(String)tempdata.getDataInfo(j) + "'";
						}else{
							sql1 = sql1 + ","+(String)tempdata.getDataInfo(j) + " ";
						}
						//System.out.println(sql1);
						
					}
					sql1 = sql1 + ")";
					saveSqlCommand(sql1,stablename,executeflag); //save the sqltext to sis.sqlcommand;
					//if(boolean fdflag = true){
                                            
                                        //}
                                        insertTime ++;
				}
				executeSqlCommand(); // execute all sqltext in sis.sqlcommand and turn stat = 1;
				
		}
		
	//------------------------------------------------------------------------------------			
		public void removeDvData(String datasource,String dtablename,String stablename,DataVector dv){
				int executeflag = 0;
                                String sql1;
				sql1 ="";
				Data tempdata;
				tempdata = new Data();
				int vsize = dv.getDataVectorSize();
				for (int t = 0;t < vsize ; t++){
					tempdata = dv.getFromDataVector(t);
					String srowid = (String)tempdata.getDataInfo(0);
					sql1 = "DELETE FROM "+dtablename+" WHERE SIS_ORI_rowid = '"+srowid+"'";
					saveSqlCommand(sql1,stablename,executeflag);
					deleteTime ++;
				}	
				executeSqlCommand(); // execute all sqltext in sis.sqlcommand and turn stat = 1;
		}
		
	//------------------------------------------------------------------------------------			
		public void saveSqlCommand(String sql1,String sname, int executeflag){ //save a sql text to sis.sqlcommand
			//database user:sis | table name:sqlcommand
				try{
					String sql0;
					Statement stmt1 = sysdb.createStatement();
					sql1 = sql1.replace("'","''");
					
					sql0 = "INSERT INTO sqlcommand(sqltext,sqlsn,sourcename,executeflag)values('"+sql1+"',seq_sis_sqlcommand.nextval,'"+sname+"',"+executeflag+")";
					stmt1.executeQuery(sql0);
					savedSql ++;
					
					//System.out.println("~SQL COMMAND Saved");
					
					log.saveLog(3,"[SQL SAVED] SQL COMMAND:"+sql1+" saved"+"\n");
					stmt1.close();
				}catch (Exception e){
					System.out.println("[DB] Exception from saveSqlCommand:" +e);
					log.saveLog(1,"[!!!EXCEPTION!!!] Exception from saveSqlCommand:" +e+"\n");
				}
		}
	
	
//------------------------------------------------------------------------------------			
		public void executeSqlCommand(){ // ִ��sis.sqlcommand�������е�sql���
			//sql0 ������sqlִ����ɺ��״̬
			//sql1 ѡȡsqlcommand����������δִ�е�sql���
			//sql2 ִ��sql��������
				
					String sql1,sql0,sql3;
					// whether there is a excuteflag
                                        sql1 = "SELECT sqltext,sourcename FROM sqlcommand where executeflag = 0 order by sqlsn desc";
					String sql2="";
					String source ="";
					ResultSet rst1 = null;
					Statement stmt1;
					stmt1 =null;
					try{
							stmt1 = sysdb1.createStatement();
							rst1 = stmt1.executeQuery(sql1);
							log.saveLog(3,"[SQL EXECUTE] A executing sql command list set ready"+"\n");
					}catch (Exception e0){
								System.out.println("[DB]~ Exception from executeSql:"+e0);
								log.saveLog(1,"\n[!!!EXCEPTION!!!]~ Exception from create sql list:"+e0+"\n");
					} 
					try{
						PreparedStatement  psmt = sysdb.prepareStatement("INSERT INTO sqlcommandbak values(seq_sis_sqlcommand.nextval,?,sysdate,?)");
						for(;rst1.next();){
								try{
										Statement stmt2 = ddb.createStatement();
										sql2 = rst1.getString("sqltext");
										sql2 = sql2.replace("''","'");
										source = rst1.getString("sourcename");
										try{
											stmt2.executeQuery(sql2);
											stmt2.close();
										}catch (Exception e0){
											System.out.println("[DBe0] Exception from executeSql:"+e0+" "+sql2);
										}
										executedSql ++;
										//System.out.println("[DB] Sql command excuted total:"+executedSql);
										//System.out.println("(!)A sql command executed.");
										log.saveLog(2,"[SQL EXECUTE] SQL Command executed:"+sql2+"\n");
										sql2 = sql2.replace("'","''");
										//sql0 = "INSERT INTO sqlcommandbak values(seq_sis_sqlcommand.nextval,'"+sql2+"',sysdate,'"+source+"')";
										psmt.setString(1,sql2);
										psmt.setString(2,source);
										psmt.execute();
										loggedSql ++;
										log.saveLog(2,"[SQL EXECUTE] Last SQL command logged"+"\n");
										//stmt0.executeQuery(sql0);
										//System.out.println("(!)Sql Command Logged.");
										//stmt0.close();
								}catch (Exception e1){
										System.out.println("[DBe1] Exception from executeSql:"+e1+" "+sql2);
										log.saveLog(1,"\n[!!!EXCEPTION!!!] Exception from LOG SQL:"+e1+"\n");
										//return;
								}
						}
						psmt.close();
						rst1.close();
						stmt1.close();
					}catch (Exception e2){
									System.out.println("[DBe2] Exception from executeSql:"+e2);
									log.saveLog(1,"\n[!!!EXCEPTION!!!eee] Exception from execute SQL:"+e2+"\n");
					}
                                                                                // remove those executed SQL - if executeflag = 0
										sql3 = "delete from sqlcommand where executeflag =0";
										try{
											Statement stmt3 = sysdb.createStatement();
											stmt3.executeQuery(sql3);
											stmt3.close();
											log.saveLog(3,"[SQL EXECUTE] Last SQL Command list removed from sql pool"+"\n");		
										}catch (Exception e3){
											System.out.println("[DB e3] Exception from delete Sql:"+e3);
											log.saveLog(1,"\n[!!!EXCEPTION!!!] Exception from delete sql list:"+e3+"\n");
										}
		}
//------------------------------------------------------------------------------------
	  public int getOffset(DataVector sdv,DataVector ddv){
	  	SfToDec sftd;
			sftd = new SfToDec();
	  	String firstrowid,lastrowid;
	  	firstrowid = "";
	  	lastrowid = "";
	  	
	  	if (sftd.getDec(sdv.getFirstRowid()) > sftd.getDec(ddv.getFirstRowid())){
					firstrowid = sdv.getFirstRowid();
			}else{
					firstrowid = ddv.getFirstRowid();
			}
		
			if (sftd.getDec(sdv.getLastRowid()) > sftd.getDec(ddv.getLastRowid())){
					lastrowid = sdv.getLastRowid();
			}else{
					lastrowid = ddv.getLastRowid();
			}
			
			int rowidoffset = sftd.getOffset(firstrowid,lastrowid);	
			return rowidoffset;
	  }


//------------------------------------------------------------------------------------			
		public void paraCompareDv(DataVector sdv,DataVector ddv){ //ƽ�бȽ�
				boolean sqlflag;
				String[][] sstrarr;
				String[][] dstrarr;
						sstrarr = getStringArray(sdv,ddv,2);  ////??????????????????
						//showStrArr(sstrarr);
						dstrarr = getStringArray(ddv,sdv,4);
						//showStrArr(dstrarr);
						//paraCompareStrArr(sstrarr,dstrarr,sdv);
						sqlflag = paraCompareStrArr(sstrarr,dstrarr,sdv);
						if(sqlflag){
								executeSqlCommand();
						}
		}
	//------------------------------------------------------------------------------------
	public boolean paraCompareStrArr(String[][] sstrarr,String[][] dstrarr,DataVector sdv){ //ƽ�бȽ�String Array
		try{
                                int executeflag = 0;
                                String dTable = xmlReader.getFromConf("DTABLE",confFile); 		//Ϊ��ȷ�����е������Դ˱�Ϊ�еı�׼
				String sTable = xmlReader.getFromConf("STABLE",confFile);
				boolean sqlflag;
				sqlflag = false;
				Data tData = new Data();						//���ڼ�¼һ����¼����Ϣ
				SfToDec sftd;
				sftd = new SfToDec();
				String sql1;
				sql1 = "";
				//int i=0;
				//System.out.println("strarr length:"+sstrarr.length);
				/*showStrArr(sstrarr);
				System.out.println("========================================================");
				showStrArr(dstrarr);*/
				boolean lastDel;
				lastDel = false;
				for(int t = 0 ;t<(sstrarr.length);t++){		//ƽ�бȽϴ���
					if(!(sstrarr[t][1].equals(dstrarr[t][1]))){	//���������������ֵ��ͬ�����
							//System.out.println(sstrarr[t][1]+" |||| "+dstrarr[t][1]);
							sqlflag = false;
							//if((sstrarr[t][0].equals("*")) && ( ! dstrarr[t][0].equals("*"))){ //Դ�����ڼ�¼��Ŀ����ڣ���ΪԴ���������ɾ��
							if((sstrarr[t][0].equals(_emptyChar)) && ( ! dstrarr[t][0].equals(_emptyChar))){ //Դ�����ڼ�¼��Ŀ����ڣ���ΪԴ���������ɾ��
									sql1 = "DELETE FROM "+dTable+" WHERE sis_ori_rowid = '"+dstrarr[t][0]+"'";
									sqlflag = true;
									//lastDel = true;
									log.saveLog(2,"[ParaCompare delete] "+sql1+"\n"); 
									deleteTime ++;
							}
							
							if(! (sstrarr[t][0].equals(_emptyChar)) && ( (dstrarr[t][0].equals(_emptyChar)))){ //Դ���ڼ�¼��Ŀ�겻���ڣ���ΪԴ�������������
									//if (lastDel == true){i++;lastDel = false;}
									//System.out.println("current i is "+i+" sdv.lenth=" + sdv.getDataVectorSize());
									//tData = sdv.getFromDataVector(i);
									tData = sdv.getFromDataVectorBySn(sstrarr[t][0]);
									sql1 = "INSERT INTO "+dTable+" VALUES('";
									sql1 = sql1 + (String)tData.getDataInfo(0)+"',SYSDATE,"+ Long.toString(sftd.getDec((String)tData.getDataInfo(0)));
									for (int j = 1;j<tData.getSize(); j++){
											
											if  (!((tData.getDataInfo(j))== null )){
												sql1 = sql1 + ",'"+(String)tData.getDataInfo(j) + "'";
											}else{
												sql1 = sql1 + ","+(String)tData.getDataInfo(j) + " ";
											}
									}
									sql1 = sql1 + ")";
									sqlflag = true;
									log.saveLog(2,"[ParaCompare insert] "+sql1+"\n"); 
									//i++;
									insertTime ++;
							}
							
							if(! (sstrarr[t][0].equals(_emptyChar)) && ( !(dstrarr[t][0].equals(_emptyChar)))){ //ԴĿ�����ڼ�¼����Ϊ������Դ��ݱ��
									//if (lastDel == true){i++;lastDel = false;}
									//System.out.println(sdv.getDataVectorSize()+" "+i);
									
                                                            
                                                        
                                                                        tData = sdv.getFromDataVectorBySn(sstrarr[t][0]);
                                                                        
                                                                   
                                                                        if(checkFD(tData,fd)){
                                                                            executeflag = 1;
                                                                            System.out.println("[Eng][FD] Found a violated~~~~~~~~~~~~~");
                                                                        }else{
                                                                            executeflag = 0;
                                                                        }
                                                                        
                                                                        
									Vector<String> vColumnName;
									vColumnName = new Vector<String>();			
									vColumnName = getColumnName(dTable,"d");
									sql1 = "UPDATE "+dTable+" SET SIS_DES_OPTIME = SYSDATE";
									for(int k = 1;k<vColumnName.size();k++){
										if (tData.getDataInfo(k) == null){
											sql1 = sql1 + ", "+vColumnName.get(k)+" = null";
										}else{
											sql1 = sql1 + ", "+vColumnName.get(k)+" = '"+tData.getDataInfo(k)+"'";
										}
									}
									sql1 =sql1 + " WHERE SIS_ORI_ROWID = '"+sstrarr[t][0]+"'";
									sqlflag = true;
									log.saveLog(2,"[ParaCompare update] "+sql1+"\n");
									//i++;
									updateTime ++;
							}
							System.out.println("[PCD]>>Find a different at line"+t);
							System.out.println("---------->>S "+sstrarr[t][0]+"----"+sstrarr[t][1]);
							System.out.println("---------->>D "+dstrarr[t][0]+"----"+dstrarr[t][1]);
							System.out.println("[PCDSQL] "+ sql1);
							log.saveLog(2,"---------->>S "+sstrarr[t][0]+"----"+sstrarr[t][1]+"\n");
							log.saveLog(2,"---------->>D "+dstrarr[t][0]+"----"+dstrarr[t][1]+"\n");
							log.saveLog(2,"[PCDSQL] "+sql1+"\n");
							if (sqlflag){
								saveSqlCommand(sql1,sTable,executeflag); // Prepare for an update
							}
					}else{
						//if (lastDel == true){i++;lastDel = false;}
						//System.out.println("Matched! T : I "+t+" : "+i);
						if (!(sstrarr[t][1].equals(_emptyChar))){/*i++;*/matchTime ++;}
					}
				}
				return sqlflag;
			}catch(Exception e){System.out.println("!!!!"+e);}
				return true;
		}
	

//------------------------------------------------------------------------------------
	  public String[][] getStringArray(DataVector dv1,DataVector dv2,int offset){ //������dv����String Array�Ĵ�С�����ҽ�dv1�������д��String Array��
	  	//offset ��ʾ��dv�е���������d_table׷����3����,��ôΪ�˺�s_table���е�����һ��,�ڴ˱�����ƫ����
	  	SfToDec sftd;
			sftd = new SfToDec();
	  	int firstrowid,lastrowid;
	  	log.saveLog(4,"[String Array] ^^^"+dv1.getMinRowidSn()+"^^^"+dv2.getMinRowidSn()+"\n"); 
	  	log.saveLog(4,"[String Array] ___"+dv1.getMaxRowidSn()+"___"+dv2.getMaxRowidSn()+"\n");
	  	
	  	//System.out.println("^^^"+dv1.getFirstRowidSn()+"^^^"+dv2.getFirstRowidSn());
	  	//System.out.println("___"+dv1.getLastRowidSn()+"___"+dv2.getLastRowidSn());
	   	
	   	/*if (dv1.getFirstRowidSn() < dv2.getFirstRowidSn()){
					firstrowid = dv1.getFirstRowidSn();
			}else{
					firstrowid = dv2.getFirstRowidSn();
			}
		
			if (dv1.getLastRowidSn() > dv2.getLastRowidSn()){ //use sn to compare to advoid String compare
					lastrowid = dv1.getLastRowidSn();
			}else{
					lastrowid = dv2.getLastRowidSn();
			}*/
			if (dv1.getMinRowidSn() < dv2.getMinRowidSn()){
					firstrowid = dv1.getMinRowidSn();
			}else{
					firstrowid = dv2.getMinRowidSn();
			}
		
			if (dv1.getMaxRowidSn() > dv2.getMaxRowidSn()){ //use sn to compare to advoid String compare
					lastrowid = dv1.getMaxRowidSn();
			}else{
					lastrowid = dv2.getMaxRowidSn();
			}
			
			int rowidoffset = lastrowid - firstrowid + 1;
			//System.out.println("|| offset:"+rowidoffset+" min:"+firstrowid+" max:"+lastrowid);
			log.saveLog(3,"[String Array] || Offset:"+rowidoffset+" Min:"+firstrowid+" Max:"+lastrowid+"\n");
	  	String strarr[][] = new String[rowidoffset][2];
	  	int t1 = firstrowid;
			Data tData = new Data();
		
			String tDataStr;
			for(int t = 0;t<rowidoffset;t++){
							strarr[t][0] = _emptyChar;		
							strarr[t][1] = _emptyChar;		
			}
						
						for (int i = 0;i < dv1.getDataVectorSize();i++){
								tData = dv1.getFromDataVector(i);
								int tt =  (sftd.getRowidSn(tData.getDataInfo(0))- t1);
									strarr[tt][0] = tData.getDataInfo(0);
									strarr[tt][1] = Long.toString(getMd5OriLong(tData,offset));
						}
						
				//System.out.println("~~:Out of FILL String Array �� Total insert(not *) = "+ i);
			return strarr;
	}
	
	//------------------------------------------------------------------------------------
	public void showStrArr(String[][] strarr){
		int arrLength;
		arrLength = strarr.length;
		System.out.println("String Array Length is :"+arrLength);
		for (int t=0;t<arrLength;t++){
			System.out.println("["+t+"][0]:"+strarr[t][0]);
			System.out.println("["+t+"][1]:"+strarr[t][1]);
		}
	}


//------------------------------------------------------------------------------------
	public Vector<String> getColumnName(String tablename,String side){
				int iter = 0;
                                if(side.equals("s")){
                                iter = 1;} else {
                                iter = 3;
                                }
                                Vector<String> vColumnName;
				vColumnName = new Vector<String>();
				/*DbConn ddb;*/
				try{
						/*ddb = new DbConn("des");*/
						String sql1 = "select * from "+tablename+" where rownum < 2"; 
						ResultSet rst1 = null;
						Statement stmt1 = ddb.createStatement();
						rst1 = stmt1.executeQuery(sql1);
						ResultSetMetaData rsmd = rst1.getMetaData();
						int numberOfColumns = rsmd.getColumnCount();
						String sql2;
						String dtcolumnname,dtcolumntype;
						int dtcolumnleng;
						//׷��sis_ori_rowid,opdate�����±�
						for (;rst1.next();){
							for(;iter < (numberOfColumns+1);iter++){
								dtcolumnname = rsmd.getColumnName(iter);
								vColumnName.add(dtcolumnname);		
							}
						}
						rst1.close();
						stmt1.close();
				}catch(Exception e){System.out.println (e);}
				return vColumnName;
	
	}


        //------------------------------------------------------------------------------------
	public int getIndexOfColumn(String columnname,Vector<String>columnlist){ // if I have a column name, it returns the column sequence id of the name
            for(int t = 0 ; t < columnlist.size();t++){
                if(columnname.equals(columnlist.get(t))){
                    return t;
                }
            }
            return -1;
        }        
        
        
//------------------------------------------------------------------------------------
	public int getLoopTime(int dvllength,int offset){
				return (int)Math.floor((dvllength / offset)) + 1;
	}	
	//------------------------------------------------------------------------------------
	public int getDvlLength(String stablename,int offset){
				int dvllength = 0;
				try{
						/*ddb = new DbConn("des");*/
						String sql1 = "select count(*) from "+stablename; 
						ResultSet rst1 = null;
						Statement stmt1 = sdb.createStatement();
						rst1 = stmt1.executeQuery(sql1);
						for (;rst1.next();){
							dvllength = rst1.getInt(1);
						}
						rst1.close();
						stmt1.close();
				}catch(Exception e){System.out.println (e);}
				if (offset == 0){
						offset = 1;
				}
				return dvllength;
	}	
	//------------------------------------------------------------------------------------
	public Vector<String> getGroupBoundary(String stablename,int offset,String rowidname){
				Vector<String> vgb;
				vgb = null;
				try{
						
						vgb = new Vector<String>();
						//String sql1 = "select rd from(select rownum rm , rid rd from "+stablename+" xx order by rowidtochar(rid)) where  mod(rm,"+offset+") = 1 ";
						String sql1 = "select * from (select a.rd brd, rownum nn from  (select  "+rowidname+" rd  ,rownum arm from "+stablename+" order by rowidtochar("+rowidname+")) a ) b  where  mod(b.nn ,"+offset+")  = 1";
						//System.out.println(sql1);
						ResultSet rst1 = null;
						Statement stmt1 = sdb.createStatement();
						rst1 = stmt1.executeQuery(sql1);
						for (;rst1.next();){
								vgb.add(rst1.getString(1));		
						}
						rst1.close();
						stmt1.close();
						vgb.add("end");
				}catch(Exception e){System.out.println ("[GetBG]"+e);}
				return vgb;
	}	
		
}