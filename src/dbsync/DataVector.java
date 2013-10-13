package dbsync;
import java.util.*;

public class DataVector{
		Vector<Data> datavector;
		String index;

//------------------------------------------------------------------------------------
	public DataVector(){
			datavector = new Vector<Data>();
	}
		
		
//------------------------------------------------------------------------------------
		public String getIndex(){
			return index;	
		}
		

//------------------------------------------------------------------------------------		
		public String changeRowidToIndex(String inrowId){
			return inrowId.substring(9,15);	
		}

//------------------------------------------------------------------------------------		
		public static int getRowidSn(String st1){
			SfToDec sftd;
			sftd = new SfToDec();
			return (int)sftd.getDec(st1.substring(15,18));		
		}


//------------------------------------------------------------------------------------
		public int getLastRowidSn(){
	  	Data vtlast;
	  	vtlast = datavector.lastElement();
	  	return getRowidSn((String)vtlast.getDataInfo(0));
	  }
	  
//------------------------------------------------------------------------------------
	  public int getFirstRowidSn(){
	  	Data vtfirst;
	  	vtfirst = datavector.firstElement();
	  	return getRowidSn((String)vtfirst.getDataInfo(0));
	  }

//------------------------------------------------------------------------------------
		public String getLastRowid(){
	  	Data vtlast;
	  	vtlast = datavector.lastElement();
	  	return changeRowidToIndex((String)vtlast.getDataInfo(0));
	  }
	  
//------------------------------------------------------------------------------------
	  public String getFirstRowid(){
	  	Data vtfirst;
	  	vtfirst = datavector.firstElement();
	  	return changeRowidToIndex((String)vtfirst.getDataInfo(0));
	  }
	  

	  
//------------------------------------------------------------------------------------
		public DataVector(String _index){
			index = _index;
			datavector = new Vector<Data>(); //ππ‘Ï
		}
		
//------------------------------------------------------------------------------------
		public void addData(Data data){
			datavector.add(data);	
		}
		
//------------------------------------------------------------------------------------
		public Data getFromDataVector(int i){
			return datavector.get(i);
		}
	
//------------------------------------------------------------------------------------
		public Data getFromDataVectorBySn(String sn){
			for (int i =0;i<datavector.size();i++){
				if (getRowidSn(sn) == getRowidSn((datavector.get(i)).getDataInfo(0))){
					return datavector.get(i);	
				}
			}
			return null;
		}
	
		
//------------------------------------------------------------------------------------
		public int getDataVectorSize(){
	  	return 	datavector.size();
	  }
	  
//------------------------------------------------------------------------------------
		public int getMaxRowidSn(){
	  	//Data tdata;
	  	int tmax = 0;
	  	int tmaxlast = 0;
	  	for (int t=0;t < datavector.size();t++){
	  		tmax = getRowidSn((datavector.get(t)).getDataInfo(0));
	  		if (tmax > tmaxlast){
	  				tmaxlast = tmax;
	  		}
	  	}
	  	return tmaxlast;
	  }

//------------------------------------------------------------------------------------
		public int getMinRowidSn(){
	  	//Data tdata;
	  	int tmin = 0;
	  	int tminlast = 0;
	  	for (int t=0;t < datavector.size();t++){
	  		tmin = getRowidSn((datavector.get(t)).getDataInfo(0));
	  		if (t == 0){
	  			tminlast = tmin;	
	  		}
	  		if (tmin < tminlast){
	  				tminlast = tmin;
	  		}
	  	}
	  	return tminlast;
	  }	  
}


