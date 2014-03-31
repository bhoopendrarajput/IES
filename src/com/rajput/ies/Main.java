package com.rajput.ies;

import java.io.File;

import com.rajput.ies.algo.IDEA;

public class Main {
	public static void main(String[] args) {
		/*
		 * String text = "balloon"; String key = "criptare"; Playfair p = new
		 * Playfair(key);
		 * 
		 * System.out.println("Text: " + text); System.out.println("Key: " + key
		 * + "/n");
		 * 
		 * System.out.println(p.showMatrix()); System.out.println("Replaced: " +
		 * p.showWithSpaces(p.replacedText(text)));
		 * System.out.println("Encrypted: " + p.encrypt(text));
		 * 
		 * //System.out.println(p.showMatrixPosition());
		 */
		/*
		 * Hill hill = new Hill("GYBNQKURP");
		 * 
		 * System.out.println(hill.showKeyMatrix());
		 * 
		 * System.out.println(hill.encrypt("ACT"));
		 */

		String toEncrypt = "1234567890";// 90123";
		String key = "1234578";// 23456";//78901234567";
		IDEA idea = new IDEA(key);

		String encrypted = idea.encrypt(toEncrypt);
		String decrypted = idea.decrypt(encrypted);

		System.out.println("Key: " + key);
		System.out.println("Text     : " + toEncrypt);
		System.out.println("Ecrypted : " + encrypted);
		System.out.println("Decrypted: " + decrypted);
		
		//String sourceFile = "D:/eclipse_workspace/workspace1/Encryption/src/com/drey/encryption/plain.txt";
		//String sourceFile = "C:/Users/bhoopendra.rajput/Pictures/Java-duke-guitar.png";
		//String sourceFile = "C:/Users/Public/Videos/Sample Videos/Wildlife.wmv";
		String sourceFile = "D:/Software/eclipse_plugins.txt";
		String destFile = new File(sourceFile).getParent()+"/cipher."+getExtension(sourceFile);
		String [] response = idea.encryptFile(sourceFile, destFile);
		System.out.println("Encryption on File : " + sourceFile);
		System.out.println("Status         : " + response[0]);
		System.out.println("Status Message : " + response[1]);
		
		
		sourceFile = destFile;
		destFile = new File(sourceFile).getParent()+"/cipherToPlain."+getExtension(sourceFile);
		
		response = idea.decryptFile(sourceFile, destFile);
		System.out.println("Descryption on File : " + sourceFile);
		System.out.println("Status         : " + response[0]);
		System.out.println("Status Message : " + response[1]);
	}
	static String getExtension(String file) {
		String ext="";
		if (file != null && file.trim().length() > 0){
			int indexDOT = file.lastIndexOf(".");
			if(indexDOT > -1){
				ext=file.substring(indexDOT+1, file.length());
			}
		}
		return ext;
	}
}
