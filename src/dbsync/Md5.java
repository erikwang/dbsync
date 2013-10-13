package dbsync;
import java.security.*; 
import java.security.spec.*;  
//import java.io.*;

class Md5{
	public final static String getMd5(String s){ 
		int t;
		try{
			/*File inputfile = new File(s);
			FileInputStream fs = new FileInputStream(inputfile);
			BufferedInputStream bi = new BufferedInputStream(fs);
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			byte[] buffstr = new byte[bi.available()];
			while ((t = bi.read(buffstr,0,buffstr.length)) != -1){
				bo.write(buffstr,0,t);
			}
			bo.close();*/
			byte[] buffstr = s.getBytes();
			char hexDigits[] = { 
				'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
			}; 
			MessageDigest mdTemp = MessageDigest.getInstance("MD5"); 
			mdTemp.update(buffstr);
			byte[] md = mdTemp.digest(); 
			int j = md.length; 
			char str[] = new char[j * 2]; 
			int k = 0; 
			for (int i = 0; i < j; i++) { 
				byte byte0 = md[i]; 
				str[k++] = hexDigits[byte0 >>> 4 & 0xf]; 
				str[k++] = hexDigits[byte0 & 0xf]; 
			} 
			return new String(str); 
	
		}	
		catch (Exception e){ 
			return null; 
		} 
	} 

	/*public static void main(String[] args){ 
		System.out.print(Md5.Md5(args[0])); 
	}*/
	
}
