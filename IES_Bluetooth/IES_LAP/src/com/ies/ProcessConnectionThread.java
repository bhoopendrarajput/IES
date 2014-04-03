package com.ies;

import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.HashMap;

import javax.microedition.io.StreamConnection;
import javax.swing.JTextField;

import com.ies.algos.IDEA.IdeaCipher;

public class ProcessConnectionThread implements Runnable{

	private StreamConnection mConnection;
	
	// Constant that indicate command from devices
	private static final int EXIT_CMD = -1;
	private static final int KEY_RIGHT = 1;
	private static final int KEY_LEFT = 2;
	private long startTime;
	public ProcessConnectionThread(StreamConnection connection)
	{
		mConnection = connection;
	}
	
	@Override
	public void run() {
		try {
			RemoteBluetoothServer.textField.append("My RSA Public("+Cryptosystem.publicExponent.toByteArray().length+"): "+byteArrayToHexString(Cryptosystem.publicExponent.toByteArray()));
			RemoteBluetoothServer.textField.append("\nMy RSA Modulus("+Cryptosystem.modulus.toByteArray().length+"): "+byteArrayToHexString(Cryptosystem.modulus.toByteArray()));
			// prepare to receive data
			InputStream inputStream = mConnection.openInputStream();
			OutputStream outputStream=mConnection.openOutputStream();
			byte[] toBeSent=new byte["PUBLICRSA:".getBytes().length+RemoteBluetoothServer.myPublicRSA.toByteArray().length];
			System.arraycopy("PUBLICRSA:".getBytes(), 0, toBeSent, 0, "PUBLICRSA:".getBytes().length);
            System.arraycopy(Cryptosystem.publicExponent.toByteArray(), 0, toBeSent, "PUBLICRSA:".getBytes().length,RemoteBluetoothServer.myPublicRSA.toByteArray().length);
            outputStream.write(toBeSent);
            RemoteBluetoothServer.textField.append("\nSENT RSA PUBLIC EXPONENT("+toBeSent.length+"): "+byteArrayToHexString(toBeSent));
			toBeSent=null;
            toBeSent=new byte["MODULUSRSA:".getBytes().length+RemoteBluetoothServer.myModulusRSA.toByteArray().length];
            System.arraycopy("MODULUSRSA:".getBytes(), 0, toBeSent, 0, "MODULUSRSA:".getBytes().length);
            System.arraycopy(Cryptosystem.modulus.toByteArray(), 0, toBeSent, "MODULUSRSA:".getBytes().length,RemoteBluetoothServer.myModulusRSA.toByteArray().length);
            outputStream.write(toBeSent);
            RemoteBluetoothServer.textField.append("\nSENT RSA MODULUS EXPONENT("+toBeSent.length+"): "+byteArrayToHexString(toBeSent));
			System.out.println("waiting for input");
			System.out.println("m/m used: "+(( (double)((double)(Runtime.getRuntime().totalMemory()/1024)/1024))- ((double)((double)(Runtime.getRuntime().freeMemory()/1024)/1024))));
	        String mConnectedDeviceName="Device"+WaitThread.i;
	        RemoteBluetoothServer.rsaCollection.put(mConnectedDeviceName, new HashMap<String,BigInteger>());
	        while (true) {
        		byte receivedBytes1[]=new byte [1024];
        		int size=0;
        		//StringBuilder msg=new StringBuilder();
        		while((size=inputStream.read(receivedBytes1))>-1){
        			if(size==1){
        				int command = (int) receivedBytes1[0];
        	        	if (command == EXIT_CMD)
        	        	{	
        	        		System.out.println("finish process");
        	        		mConnection.close();
        	        		break;
        	        	}
        			}
        			//msg.append(new String(b,0,size));
        			//System.err.println("mm: "+new String(b,0,size));
        			//System.err.println("len: "+new String(b,0,size).length());
        			byte receivedBytes[]=new byte[size];
        	        System.arraycopy(receivedBytes1, 0, receivedBytes, 0, size);
        			RemoteBluetoothServer.textField.append("\n\n\n@Message Received length: "+size);
        			
        			RemoteBluetoothServer.textField.append("\n"+byteArrayToHexString(receivedBytes,size));
        			/*if(size<128){
        				RemoteBluetoothServer.textField.append("Received Message to short to process any more");
        				continue;
        			}*/
        			String recievedMsg=new String(receivedBytes);
        			if(recievedMsg.startsWith("PUBLICRSA")){//PUBLICRSA:
        				int indexOfModul=recievedMsg.indexOf("MODULUSRSA");
        				int byteUpto=0;
        				if(indexOfModul>-1){
        					byteUpto=indexOfModul;
        					byte receivedBytesModulus[]=new byte[size-10];
            				System.arraycopy(receivedBytes, 11+indexOfModul, receivedBytesModulus, 0, size-10);
                        	//RemoteBluetoothServer.rsaCollection.get(mConnectedDeviceName).put("MODULUS", new BigInteger(receivedBytesModulus));
            				Cryptosystem.modulus=new BigInteger(receivedBytesModulus);
                        	byteUpto+=receivedBytesModulus.length;
        				}
        				byte receivedBytesPublic[]=new byte[size-10-byteUpto];
        				System.arraycopy(receivedBytes, 10, receivedBytesPublic, 0, size-10-byteUpto);
        				//RemoteBluetoothServer.rsaCollection.get(mConnectedDeviceName).put("PUBLIC", new BigInteger(receivedBytesPublic));
        				Cryptosystem.publicExponent=new BigInteger(receivedBytesPublic);
        				RemoteBluetoothServer.textField.append("\nRSA PUBLIC EXPONENT: "+byteArrayToHexString(receivedBytesPublic));
        				if(indexOfModul>-1){
        				RemoteBluetoothServer.textField.append("\nRSA MODULUS: "+byteArrayToHexString(Cryptosystem.modulus.toByteArray()));
        				}
        				continue;
                    }else if(recievedMsg.startsWith("MODULUSRSA")){//MODULUSRSA:
                    	byte receivedBytesModulus[]=new byte[size-11];
        				System.arraycopy(receivedBytes, 11, receivedBytesModulus, 0, size-11);
                    	//RemoteBluetoothServer.rsaCollection.get(mConnectedDeviceName).put("MODULUS", new BigInteger(receivedBytesModulus));
        				Cryptosystem.modulus=new BigInteger(receivedBytesModulus);
                    	RemoteBluetoothServer.textField.append("\nRSA MODULUS: "+byteArrayToHexString(receivedBytesModulus));
                    	continue;
                    }
        			try {
        				byte receiveMd5CipherByte[]=new byte[size-8];
        				System.arraycopy(receivedBytes, 8, receiveMd5CipherByte, 0, size-8);
        				RemoteBluetoothServer.textField.append("\nMD5("+receiveMd5CipherByte.length+") "+byteArrayToHexString(receiveMd5CipherByte));
        				byte receiveMd5[] = Cryptosystem.decrypt(new BigInteger(receiveMd5CipherByte)).toByteArray();
        				RemoteBluetoothServer.textField.append("\nreceive MD5 and decipher it by RSA: ");
        				RemoteBluetoothServer.textField.append("\nDecipher MD5: "+byteArrayToHexString(receiveMd5));
        				IdeaCipher ideaReciever = new IdeaCipher(new String(receiveMd5));
        				byte receivedCipherByte[]=new byte[8];
        	        	System.arraycopy(receivedBytes, 0, receivedCipherByte, 0, 8);//receiveMd5CipherByte
        	        	RemoteBluetoothServer.textField.append("\nCipher MSG: "+byteArrayToHexString(receivedCipherByte));
        	        	byte[] plain=new byte[8];//cipherBytes.length
        		        ideaReciever.decrypt(receivedCipherByte, plain);
        		        RemoteBluetoothServer.textField.append("\nApplying IDEA using decipher md5  :\n"+new String(plain));
        		        if(MD5(new String(plain)).equals(new String(receiveMd5CipherByte))){
        		        	RemoteBluetoothServer.textField.append("\nReceived MD5 and generated MD5 of decipher is mactched");
        		        }
        		        else RemoteBluetoothServer.textField.append("\nReceived MD5 and generated MD5 of decipher is not mactched");
        			} catch (Exception e) {
        				e.printStackTrace();
        			}
        			System.gc();
        		}
        		/*if(msg.length()!=1)
        		System.out.println("Message has been recieved: "+msg);
	        	else
	        	{
	        	//int command = inputStream.read();
	        	int command = msg.charAt(0); 
	        	if (command == EXIT_CMD)
	        	{	
	        		System.out.println("finish process");
	        		mConnection.close();
	        		break;
	        	}
	        	
	        	processCommand(command);
	        	}*/
        	}
        } catch (Exception e) {
    		e.printStackTrace();
    	}
	}
	
	/**s
	 * Process the command from client
	 * @param command the command code
	 */
	private void processCommand(int command) {
		try {
			Robot robot = new Robot();
			switch (command) {
	    	case KEY_RIGHT:
	    		robot.keyPress(KeyEvent.VK_RIGHT);
	    		System.out.println("Right");
	    		break;
	    	case KEY_LEFT:
	    		robot.keyPress(KeyEvent.VK_LEFT);
	    		System.out.println("Left");
	    		break;
	    		default:
	    			System.out.print("Message has been recieved: ");
	    			System.err.println(command);
//	    			robot.keyPress(command);
//	    			robot.keyRelease(command);
	    			typeCharacter(robot,""+(char)command);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	 public static void typeCharacter(Robot robot, String letter)  
	    {  
	        try  
	        {  
	            boolean upperCase = Character.isUpperCase( letter.charAt(0) );  
	            String variableName = "VK_" + letter.toUpperCase();  
	  
	            KeyEvent ke = new KeyEvent(new JTextField(), 0, 0, 0, 0, ' ');  
	            Class clazz = ke.getClass();  
	            Field field = clazz.getField( variableName );  
	            int keyCode = field.getInt(ke);  
	  
	            //robot.delay(1000);  
	  
	            if (upperCase) robot.keyPress( KeyEvent.VK_SHIFT );  
	  
	            robot.keyPress( keyCode );  
	            robot.keyRelease( keyCode );  
	  
	            if (upperCase) robot.keyRelease( KeyEvent.VK_SHIFT );  
	        }  
	        catch(Exception e)  
	        {  
	            System.out.println(e);  
	        }  

	    }
	 public String MD5(String md5) {
  	   try {
  	        java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
  			md.reset();
  	        byte[] array = md.digest(md5.getBytes("UTF-8"));
  	        StringBuffer sb = new StringBuffer();
  	        for (int i = 0; i < array.length; ++i) {
  	          sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
  	        }
  	        return sb.toString();
  	   } catch (java.security.NoSuchAlgorithmException e) {e.printStackTrace();}
  		catch (java.io.UnsupportedEncodingException e1) {e1.printStackTrace();}
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
}