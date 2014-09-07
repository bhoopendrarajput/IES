package com.ies;

import java.math.BigInteger;
import java.util.Arrays;

import com.ies.algos.IDEA.RSA;

/*public class Test {

	*//**
	 * @param args
	 *//*
	public static void main(String[] args) {
		// TODO Auto-generated method stubenew
		
		
		//byte[] b={(byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0};
		//byte[] b={0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
		byte[] b= {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00};
		byte[] b1={0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00};
		System.out.println("length: "+b.length);
		System.out.println("byte: "+byteArrayToHexString(b,8));
		System.out.println("byte: "+new String(b));
		System.out.println("int: "+new BigInteger(b).intValue());
		System.out.println("int1: "+byteArrayToInt(b));
		if (Arrays.equals(b, b1)){
		    System.out.println("Yup, they're the same!");
		}
		if(true)
		return ;
		String msg="Bhoopendra";
		try {
			RSA key = new RSA(1024);
			BigInteger encrypt = key.encrypt(new BigInteger(msg.getBytes()));
			System.err.println("Encrypted: "+byteArrayToHexString(encrypt.toByteArray(), encrypt.toByteArray().length));
			
			
			RSA keyR = new RSA(1024);
			BigInteger encryptR = key.decrypt(new BigInteger(encrypt.toByteArray()));
			System.err.println("Decipher: "+new String(encryptR.toByteArray()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static String byteArrayToHexString(byte[] b, int noOfBytes) {
	    StringBuffer sb = new StringBuffer(noOfBytes);
	    for (int i = 0; i < noOfBytes; i++) {
	      int v = b[i] & 0xff;
//	      if (v < 16) {
//	        sb.append('0');
//	      }
	      sb.append(Integer.toHexString(v));
	    }
	    return sb.toString().toUpperCase();
	  }
	public static int byteArrayToInt(byte[] b) {
		int value = 0;
		for (int i = 0; i < b.length; i++)
		    value = (value << 8) + (b[i] & 0xFF);
	    return value;
	  }
}*/
