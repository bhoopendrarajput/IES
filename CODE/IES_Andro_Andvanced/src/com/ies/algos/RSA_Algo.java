package com.ies.algos;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

public class RSA_Algo {

	public BigInteger e = null;
	public BigInteger d = null;
	public int n = 0;
	public BigInteger a = null;
	public BigInteger b = null;
	public BigInteger p = null;
	public BigInteger q = null;

	public RSA_Algo(BigInteger p, BigInteger q) {
		this.p=p;
		this.q=q;
		int p_in_i = p.intValue();
		int q_in_i = q.intValue();
		n = p_in_i * q_in_i;
		int fi_n = ((p_in_i - 1) * (q_in_i - 1));
		BigInteger fi_n_in_bi = new BigInteger("" + fi_n);
		Random r = new Random();
		while (true) {
			e = new BigInteger("" + r.nextInt(99999));
			if (e.intValue() >= fi_n || e.intValue() < 1) {
				continue;
			}
			if (fi_n_in_bi.gcd(e).intValue() == 1) {
				break;
			}
		}
		a = new BigInteger("" + r.nextInt(99999));
		b = new BigInteger("" + (e.intValue() * a.intValue()));
		d = e.modInverse(fi_n_in_bi);
		RSA rsa = new RSA();
		rsa.a = a;
		rsa.b = b;
		rsa.d = d;
		rsa.e = e;
		rsa.n = n;
	}
	/**
	 * created by bhoopendra.rajput
	 */
//	public int[] encrypt(byte data[], int offset, int length) {
//		int encrypteddata[] = new int[data.length];
//		for (int i = offset; i < length; i++) {
//			BigInteger M = new BigInteger("" + data[i]);
//			BigInteger C = (M.pow(b.intValue() / a.intValue()))
//					.mod(new BigInteger("" + n));
//			int t = C.intValue();
//			encrypteddata[i] = t;
//		}
//		return encrypteddata;
//	}
	/**
	 * created by bhoopendra.rajput
	 */
	public int[] encrypt(byte data[]) {
		int encrypteddata[] = new int[data.length];
		for (int i = 0; i < data.length; i++) {
			BigInteger M = new BigInteger("" + data[i]);
			BigInteger C = (M.pow(b.intValue() / a.intValue())).mod(new BigInteger("" + n));
			//BigInteger C = (M.pow(e.intValue())).mod(new BigInteger("" + n));
			int t = C.intValue();
			encrypteddata[i] = t;
		}
		return encrypteddata;
	}
	
//	public int[] sendEncrypt(byte data[], int offset, int length, BigInteger a,
//			BigInteger b, int n) {
//		int encrypteddata[] = new int[data.length];
//		for (int i = offset; i < length; i++) {
//			BigInteger M = new BigInteger("" + data[i]);
//			BigInteger C = (M.pow(b.intValue() / a.intValue()))
//					.mod(new BigInteger("" + n));
//			int t = C.intValue();
//			encrypteddata[i] = t;
//		}
//		return encrypteddata;
//	}
	public int[] sendEncrypt(byte data[], BigInteger a,	BigInteger b, int n) {
		int encrypteddata[] = new int[data.length];
		for (int i = 0; i < data.length; i++) {
			BigInteger M = new BigInteger("" + data[i]);
			BigInteger C = (M.pow(b.intValue() / a.intValue())).mod(new BigInteger("" + n));
			int t = C.intValue();
			encrypteddata[i] = t;
		}
		return encrypteddata;
	}

	public int[] decrypt_sk(byte data[], int offset, int length) {
		int decrypteddata[] = new int[data.length];
		for (int i = offset; i < length; i++) {
			BigInteger M = new BigInteger("" + data[i]);
			BigInteger C = (M.pow(d.intValue())).mod(new BigInteger("" + n));
			int t = C.intValue();
			decrypteddata[i] = t;
		}
		return decrypteddata;
	}
/**
 * created by: bhoopendra.rajput
 */
	public byte[] decrypt(int data[]) {
		byte decrypteddata[] = new byte[data.length];
		for (int i = 0; i < data.length; i++) {
			BigInteger M = new BigInteger("" + data[i]);
			BigInteger C = (M.pow(d.intValue())).mod(new BigInteger("" + n));
			int t = C.intValue();
			decrypteddata[i] = (byte) t;
		}
		return decrypteddata;
	}
	public byte[] decrypt(int data[], int offset, int length) {
		byte decrypteddata[] = new byte[data.length];
		for (int i = offset; i < length; i++) {
			BigInteger M = new BigInteger("" + data[i]);
			BigInteger C = (M.pow(d.intValue())).mod(new BigInteger("" + n));
			int t = C.intValue();
			decrypteddata[i] = (byte) t;
		}
		return decrypteddata;
	}

	public static void main (String aa[]){
		RSA_Algo algoB = new RSA_Algo(new BigInteger("19"), new BigInteger("29"));
		RSA_Algo algo = new RSA_Algo(new BigInteger("3"), new BigInteger("11"));
		algo.n = algoB.n;
		algo.a = algoB.a;
		algo.b = algoB.b;
		System.err.println(algo.n);
		System.err.println(algo.a);
		System.err.println(algo.b);
		byte [] data="12345678".getBytes();
		int [] cipher = algo.encrypt(data);
		System.err.println(Arrays.toString(cipher));
		byte [] deCipher = algoB.decrypt(cipher, 0, cipher.length);
		System.err.println(new String (deCipher));
	}
	public BigInteger getE() {
		return e;
	}
	public BigInteger getD() {
		return d;
	}
}

class RSA implements Serializable {
	private static final long serialVersionUID = 3219966193290855268L;
	int n;
	BigInteger e;
	BigInteger d;
	BigInteger a;
	BigInteger b;
}