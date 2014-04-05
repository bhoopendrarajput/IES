package com.ies;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;

import javax.microedition.io.StreamConnection;

import com.ies.algos.IDEA.IdeaCipher;
import com.ies.algos.IDEA.RSA;

public class ProcessConnectionThread implements Runnable{

	private StreamConnection mConnection;
	
	// Constant that indicate command from devices
	private static final int EXIT_CMD = -1;
	private static final int KEY_RIGHT = 1;
	private static final int KEY_LEFT = 2;
	private long startTime;
	public ProcessConnectionThread(StreamConnection connection) {
		mConnection = connection;
	}
	static BigInteger myPublicRSA, myModulusRSA;
	static {
		RSA.keys(128, 128, 20, new java.security.SecureRandom());
		myModulusRSA = RSA.getModulus();
		myPublicRSA = RSA.getPublicExponent();
		System.err.println("my keys: modulus " + RSA.getModulus());
		System.err.println("my keys: publicExponent " + RSA.getPublicExponent());
	}
	@Override
	public void run() {
		FileOutputStream fos = null;
		try {
			ObjectOutputStream out = new ObjectOutputStream(mConnection.openOutputStream());
			out.writeObject(RSA.getModulus());
			out.writeObject(RSA.getPublicExponent());
			
			ObjectInputStream ins = new ObjectInputStream(mConnection.openInputStream());
			BigInteger objectReceived = (BigInteger) ins.readObject();
			
			String fileName=new String(RSA.decrypt(objectReceived).toByteArray());
			IdeaCipher ideaReciever = new IdeaCipher(new String(toMD5(fileName)));
			File file = new File("c:/Received_"+fileName);
			fos = new FileOutputStream(file);
			final int BUFFER_SIZE=8;//128;
			final int MIN_BUFFER_SIZE=8;
      //System.err.println("READ DATA2: "+fileName);
      byte newBuffer[], deCipher[] = new byte[BUFFER_SIZE];; 
      StringBuilder sb = new StringBuilder();
      Object obj;byte[] data;
      int bufferSizeToBeUsed;
      long dataProceed=0;
			while(true){
				try{
				  obj = ins.readObject();
				  if(obj instanceof Integer && ((Integer)obj).intValue() == -1 ){
				    break;
				  }
				  data = (byte[]) obj;
				  //System.out.println("data recieved["+data.length+"]");
				  bufferSizeToBeUsed = data.length > BUFFER_SIZE ? BUFFER_SIZE : MIN_BUFFER_SIZE;  
				  //newBuffer=new byte[data.length];
				  for (int index = 0; index < data.length; index += bufferSizeToBeUsed) {
				    /*for (int j=0; j<BUFFER_SIZE; j++) {  
				      deCipher[j]=0;  
	          }*/
				    deCipher = new byte[bufferSizeToBeUsed];
				    ideaReciever.decrypt(data, index, deCipher, 0);
	          fos.write(deCipher);fos.flush();
	          dataProceed += bufferSizeToBeUsed;
	          System.err.println("Data Procceed: "+formatBytes(dataProceed));
	        }
				  data= null;data = null;newBuffer=null;
				  obj=null;System.gc();
				}catch(Exception e){
					e.printStackTrace();break;
				}
			}
			System.err.println("data rcvd: "+sb.length()+"\n"+sb);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try {
			  if(mConnection!=null)
				  mConnection.close();
			  if(fos!=null)
          fos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public String toMD5(String md5) {
		try {
			java.security.MessageDigest md = java.security.MessageDigest
					.getInstance("MD5");
			md.reset();
			byte[] array = md.digest(md5.getBytes("UTF-8"));
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < array.length; ++i) {
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100)
						.substring(1, 3));
			}
			return sb.toString();
		} catch (java.security.NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (java.io.UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		return null;
	}

	public static String byteArrayToHexString(byte[] b, int noOfBytes) {
		StringBuffer sb = new StringBuffer(noOfBytes * 2);
		for (int i = 0; i < noOfBytes; i++) {
			int v = b[i] & 0xff;
			if (v < 16) {
				sb.append('0');
			}
			sb.append(Integer.toHexString(v));
		}
		return sb.toString().toUpperCase();
	}

	public static String byteArrayToHexString(byte[] b) {
		StringBuffer sb = new StringBuffer(b.length * 2);
		for (int i = 0; i < b.length; i++) {
			int v = b[i] & 0xff;
			if (v < 16) {
				sb.append('0');
			}
			sb.append(Integer.toHexString(v));
		}
		return sb.toString().toUpperCase();
	}
	public static void main(String a[]){
		System.out.println(formatBytes(10));
	}
	public static String formatBytes(long bytes) {
		// TODO: add flag to which part is needed (e.g. GB, MB, KB or bytes)
		String retStr = "";
		// One binary gigabyte equals 1,073,741,824 bytes.
		if (bytes > 1073741824) {// Add GB
			long gbs = bytes / 1073741824;
			retStr += (new Long(gbs)).toString() + "GB ";
			bytes = bytes - (gbs * 1073741824);
		}
		// One MB - 1048576 bytes
		if (bytes > 1048576) {// Add GB
			long mbs = bytes / 1048576;
			retStr += (new Long(mbs)).toString() + "MB ";
			bytes = bytes - (mbs * 1048576);
		}
		if (bytes > 1024) {
			long kbs = bytes / 1024;
			retStr += (new Long(kbs)).toString() + "." + (bytes % 1024) + "KB";
			bytes = bytes - (kbs * 1024);
		} else
			retStr += (new Long(bytes)).toString() + " bytes";
		return retStr;
	}
}