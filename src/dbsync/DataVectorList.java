package dbsync;
import java.util.*;
import java.sql.*;
import oracle.jdbc.driver.*;
import oracle.sql.*;


public class DataVectorList{
		Vector<DataVector> datavectorlist;
		int reslength;
                //Vector<String> attributelist;
		
		
//------------------------------------------------------------------------------------		
		public DataVectorList(){
			datavectorlist = new Vector<DataVector>();
			reslength = 0;
		}

		
//------------------------------------------------------------------------------------		
		public int getDataVectorListSize(){
			return datavectorlist.size();	
		}

		
//------------------------------------------------------------------------------------		
		public DataVector get(int __index){
			return datavectorlist.get(__index);	
		}
				              
                
//------------------------------------------------------------------------------------
		public DataVector getDataVector(String index){
				for(int i=0;i<datavectorlist.size();i++){
						if (datavectorlist.get(i).getIndex().equals(index)){
							return datavectorlist.get(i);
						}
				}
				return null;	
		}

		
//------------------------------------------------------------------------------------		
		public String changeRowidToIndex(String inrowId){
			return inrowId.substring(9,15);	
		}
		
		
//------------------------------------------------------------------------------------
		public void insertIntoDataVectorList(Data data){
				String index = changeRowidToIndex(data.rowId);
				DataVector dv = getDataVector(index);
				if (dv==null){
						dv = new DataVector(index);
						//System.out.println("In DVL a new DV has created with index:"+index);
						dv.addData(data);
						datavectorlist.add(dv);
				}else{
						dv.addData(data);
						//System.out.println("Add DV to a exist DVL with index:"+index);
				}
		}
		
//------------------------------------------------------------------------------------		
		public DataVectorList readFromDatabase(String dbsource,String sql1,String conffile,String user,String pswd){
			try{
				//System.out.println("[DB] @"+dbsource+" Entering DataVectorList read process from DATABASE......");
				//System.out.println("Using SQL:"+sql1+" AT "+dbsource);
				Data tdata;
				tdata = new Data();
				DbConn db;
				db = new DbConn(dbsource,conffile,user,pswd);
				DataVectorList dvl;
				dvl = new DataVectorList();
				ResultSet rst1 = null;
				Statement stmt1 = db.createStatement();
				rst1 = stmt1.executeQuery(sql1);
				ResultSetMetaData rsmd = rst1.getMetaData();
				int numberOfColumns = rsmd.getColumnCount();
                               
				for (;rst1.next();){
					for(int y = 1;y < (numberOfColumns+1);y++){
						//System.out.println(rst1.getString(rsmd.getColumnName(y)));
						tdata.addToVector(rst1.getString(rsmd.getColumnName(y)));
					}
						tdata.rowId = rst1.getString(rsmd.getColumnName(1));
						dvl.insertIntoDataVectorList(tdata);
						tdata  = new Data();
						reslength ++;
				}
				rst1.close();
				stmt1.close();
				return dvl;
			}catch(Exception e){
				System.out.println(e);
			}
			return null;
		}


	//------------------------------------------------------------------------------------			
	  public String compareData(){
	  	return null;
	  }
	
			
		//------------------------------------------------------------------------------------			
				
}
