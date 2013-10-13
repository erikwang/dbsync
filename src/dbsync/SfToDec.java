package dbsync;
//import java.math.*;

class SfToDec{
//------------------------------------------------------------------------------------		
	public static long getDec(String st){
		byte[] bt= st.getBytes();
		int leng = st.length();
		long dout = 0;
		for(int i = 0; i<leng ; i++){
			if ((bt[i]>=65)&&(bt[i]<=90)){
				dout = dout + (long)(bt[i]-65)*(long)Math.pow(64,leng-i-1);
			}else if((bt[i]>=97)&&(bt[i]<=122)){
				dout = dout + (long)(bt[i]-71)*(long)Math.pow(64,leng-i-1);
			}else if((bt[i]>=48)&&(bt[i]<=57)){
				dout = dout + (long)(bt[i]+4)*(long)Math.pow(64,leng-i-1);
			}else if((bt[i]==43)){
				dout = dout + (long)(bt[i]+19)*(long)Math.pow(64,leng-i-1);
			}else if((bt[i]==47)){
				dout = dout + (long)(bt[i]+16)*(long)Math.pow(64,leng-i-1);
			}
		}
		return dout;
	}

//------------------------------------------------------------------------------------			
	public static int getOffset(String st1,String st2){
		int offset;
		offset = Math.abs((int)getDec(st1) - (int)getDec(st2));
		return offset;
	}

//------------------------------------------------------------------------------------		
		public static int getRowidSn(String st1){
			return (int)getDec(st1.substring(15,18));		
		}
	
}