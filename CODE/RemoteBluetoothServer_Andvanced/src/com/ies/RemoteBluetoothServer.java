package com.ies;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class RemoteBluetoothServer{
	/*static Map<String,HashMap<String,BigInteger>> rsaCollection=new HashMap<String, HashMap<String, BigInteger>>();
	static BigInteger myPublicRSA, myModulusRSA;
	static {
		Cryptosystem.keys(128, 128, 20, new java.security.SecureRandom());
		myModulusRSA=Cryptosystem.modulus;
		myPublicRSA=Cryptosystem.publicExponent;
		System.err.println("my keys: modulus "+Cryptosystem.modulus);
		System.err.println("my keys: publicExponent "+Cryptosystem.publicExponent);
	}*/
	public static void main(String[] args) {
		ProcessConnectionThread.textField = new JTextArea(10, 25);  
		JScrollPane scrollPane = new JScrollPane(ProcessConnectionThread.textField);
        JFrame frame = new JFrame();  
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );  
        frame.add( scrollPane );
        frame.pack();  
        frame.setLocationRelativeTo( null );  
        frame.setVisible( true );
		Thread waitThread = new Thread(new WaitThread());
		waitThread.start();
	}
}
