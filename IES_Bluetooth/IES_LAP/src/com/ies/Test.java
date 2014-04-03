package com.ies;

import java.math.BigInteger;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
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
}
