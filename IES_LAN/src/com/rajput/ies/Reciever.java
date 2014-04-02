package com.rajput.ies;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.Socket;

import com.rajput.ies.algo.RSA;
import com.rajput.ies.algo.IdeaCipher;

public class Reciever {
	static BigInteger myPublicRSA, myModulusRSA;
	final byte[] END_1 = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00 };
	final byte[] END_2 = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00 };
	static {
		RSA.keys(128, 128, 20, new java.security.SecureRandom());
		myModulusRSA = RSA.getModulus();
		myPublicRSA = RSA.getPublicExponent();
		System.err.println("my keys: modulus " + RSA.getModulus());
		System.err.println("my keys: publicExponent "
				+ RSA.getPublicExponent());
	}

	public static void main(String[] args) {
		for(int i=0;i<1;i++){
			run();
		}
	}
	public static void run(){
		Socket socket = null;
		FileOutputStream fos = null;
		try {
			socket = new Socket("localhost", Sender.SERVER_PORT);
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			out.writeObject(RSA.getModulus());
			out.writeObject(RSA.getPublicExponent());
			//System.err.println("SENT KEY");
			
			ObjectInputStream ins = new ObjectInputStream(socket.getInputStream());
			BigInteger objectReceived = (BigInteger) ins.readObject();
			
			String fileName=new String(RSA.decrypt(objectReceived).toByteArray());
			IdeaCipher ideaReciever = new IdeaCipher(new String(toMD5(fileName)));
			File file = new File("D:/Bhoopen/Bhoopen@Git/IES/test_resources/Received_"+fileName);
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
	          //System.arraycopy(deCipher, 0, newBuffer, index, BUFFER_SIZE);
				    //System.out.print(new String(deCipher));
	          fos.write(deCipher);fos.flush();
	          dataProceed += bufferSizeToBeUsed;
	          System.err.println("Data Procceed: "+(dataProceed));
	        }
				  //sb.append(new String(newBuffer));
				  //fos.write(newBuffer);fos.flush();
          //System.err.println("READ DATA: "+new String(newBuffer));
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
			  if(socket!=null && socket.isClosed())
			    socket.close();
			  if(fos!=null)
          fos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

  public static String toMD5(String md5) {
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
  final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

  public static String bytesToHex(byte[] bytes) {
    char[] hexChars = new char[bytes.length * 2];
    for (int j = 0; j < bytes.length; j++) {
      int v = bytes[j] & 0xFF;
      hexChars[j * 2] = hexArray[v >>> 4];
      hexChars[j * 2 + 1] = hexArray[v & 0x0F];
    }
    return new String(hexChars);
  }
}
