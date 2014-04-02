package com.rajput.ies;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;

import com.rajput.ies.algo.RSA;
import com.rajput.ies.algo.IdeaCipher;

public class Sender {
	public static final int SERVER_PORT=12345;
	static BigInteger myPublicRSA, myModulusRSA;
	final byte[] END_1 = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00};
	final byte[] END_2 ={0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00};
	static {
		RSA.keys(128, 128, 20, new java.security.SecureRandom());
		myModulusRSA = RSA.getModulus();
		myPublicRSA = RSA.getPublicExponent();
		System.err.println("my keys: modulus " + RSA.getModulus());
		System.err.println("my keys: publicExponent "
				+ RSA.getPublicExponent());
	}

	public static void main(String[] args) {
		try {
			ServerSocket ss = new ServerSocket(SERVER_PORT);
			while (true) {
				new ProcessConnection(ss, ss.accept());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

class ProcessConnection extends Thread {
	ServerSocket serverSocket;
	Socket socket;

	public ProcessConnection(ServerSocket ss, Socket socket) {
		this.serverSocket = ss;
		this.socket = socket;
		start();
	}

	@Override
	public void run() {
		try {
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			BigInteger objectReceived = (BigInteger) in.readObject();
			RSA.setModulus(objectReceived);
			//System.err.println("READ DATA1: "+objectReceived);
			objectReceived = (BigInteger) in.readObject();
			RSA.setPublicExponent(objectReceived);
			//System.err.println("READ DATA2: "+objectReceived);
			
			File file = new File("D:/Bhoopen/Bhoopen@Git/IES/test_resources/video.mp4");
			//File file = new File("D:/Software/HP_LoadRunner1.exe");
			
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			BigInteger objectToBeSent = RSA.encrypt(new BigInteger(file.getName().getBytes()));
			//System.err.println("TO BE SENT DATA3: "+objectToBeSent);
			out.writeObject(objectToBeSent);
			
			FileInputStream fis = new FileInputStream(file);
			byte buffer[] = new byte[1024*100];//, plain[], cipher[]; 
			int size = 0;
			final int BUFFER_SIZE=8;//128;
			final int MIN_BUFFER_SIZE=8;
			IdeaCipher ideaReciever = new IdeaCipher(new String(toMD5(file.getName())));
			
			StringBuilder sb = new StringBuilder();
			byte[] newBuffer=null;
			byte[]cipher = new byte[BUFFER_SIZE];
			long newSize=0, lastNewSize=0, total=0;
			int bufferSizeToBeUsed;
			long dataProceed=0;
			while ((size=fis.read(buffer)) > -1) {
			  bufferSizeToBeUsed = size > BUFFER_SIZE ? BUFFER_SIZE : MIN_BUFFER_SIZE;  
			  newBuffer=new byte[(size+bufferSizeToBeUsed-size%bufferSizeToBeUsed)];
			  for (int i =0; i < size; i+=bufferSizeToBeUsed) {
			    //plain = new byte[BUFFER_SIZE];
			    cipher = new byte[bufferSizeToBeUsed];
			    ideaReciever.encrypt(buffer, i, cipher, 0);
			    System.arraycopy(cipher, 0, newBuffer, i, bufferSizeToBeUsed);
			    dataProceed += bufferSizeToBeUsed;
			    System.err.println("Data Procceed: "+dataProceed);
			  }
			  out.writeObject(newBuffer);out.flush();
			  sb.append(newBuffer);
			}
			out.writeObject(new Integer(-1));
			fis.close();
			
		} catch (Exception e) {
			e.printStackTrace();
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
