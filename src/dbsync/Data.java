package dbsync;
import java.util.*;

public class Data{
		String rowId;
		Vector<String> data;
		
		public Data(){
			data = new Vector<String>(); //
			rowId = null;
		}
		
		public void addToVector(String info){
			//System.out.println(info);
			if (info == null){
				//System.out.println("!!!");
				info = null;	
			}
			data.add(info);
		}
		
		public int getSize(){
			return data.size();
		}
		
		public String getDataInfo(int _i){
			return data.get(_i);
		}
}