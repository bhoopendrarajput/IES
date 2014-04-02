package com.rajput.ies.algo;


import java.math.*;
import java.util.*;
import java.io.*;

/**
 * Java Cryptosystem (RSA algorithm implementation)
 * 
 * <p>The sender and receiver of secret messages both have copies of this program in
 * a directory, on their machines.
 * First, the recipient runs this program, and generates some keys, with option 'g'.
 * The prime strength determines the strength of the modulus, which is the maximum bitlength
 * of messages to encrypt.</p>
 *
 * <p>The recipient can then test keys with 't' if desired, and save them to disk with 'w'.
 * The recipient then copies the files "PUBLIC" and "MODULUS" to the sender to be placed 
 * in a sender's program directory, with a copy of Cryptosystem.</p>
 *
 * <p>To transfer a secret message, sender and recipient start Cryptosystem and load  
 * keys if necessary with 'l'. Sender uses option 'e' to encrypt a message
 * that is saved as "FILE" and then copied to the recipient's directory.
 * The recipient uses option 'd' to load and decrypt "FILE".</p>
 * 
 * <p>Note that the first secret message, that the sender transfers with a new keypair
 * better not be too sensitive! The reason for this is that third parties can
 * substitute "PUBLIC" and "MODULUS" in transit for their own,
 * then intercept the first "FILE".
 * The recipient will know that the message has been intercepted
 * if it fails to decrypt properly.</p>
 *
 * <p>Based on the algorithm seen at: http://www.rsasecurity.com/rsalabs/faq/3-1-1.html
 * </p>
 * @author Michael John Wensley
 */
public class RSA {
	/* System input stream */
	private static final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

	/* store last key used to accept input */
	private static int enterkey = -1;

	/** Integer zero */
//	private static final BigInteger ZERO = BigInteger.valueOf(0);

	/** Integer one */
	private static final BigInteger ONE = BigInteger.valueOf(1);

	/** Integer two */
//	private static final BigInteger TWO = BigInteger.valueOf(2);

	/** two large primes */
	private static BigInteger prime1, prime2;

	/** The modulus */
	public static BigInteger modulus;

	public static BigInteger getModulus() {
		return modulus;
	}

	public static void setModulus(BigInteger modulus) {
		RSA.modulus = modulus;
	}

	public static BigInteger getPublicExponent() {
		return publicExponent;
	}

	public static void setPublicExponent(BigInteger publicExponent) {
		RSA.publicExponent = publicExponent;
	}
	/** the public exponent */
	public static BigInteger publicExponent;

	/** The private exponent */
	private static BigInteger privateExponent;

	/** We don't let anyone else instantiate this class */
	private RSA() { }

	/**
	 * Invokes the program.<p>
	 * It displays the main menu, and waits for a key and Carriage
	 * Return to be pressed.<p>
	 * That command is then acted upon, and the the main menu is displayed again.
	 * Space and Carriage Return causes this method to return, and hence, the program
	 * to terminate.
	 *
	 * @param args Are not used
	 * @exception IOException if there is an I/O problem
	 */
	public static void main(String args[]) throws IOException {
		readin();
		System.out.println("Author - Michael John Wensley");
		String input;
		boolean exit = false;
		do {
			System.out.println("l - load keys");
			System.out.println("w - write keys");
			System.out.println("v - view keys");
			System.out.println("t - test keys");
			System.out.println("e - encrypt message");
			System.out.println("d - decrypt message");
			System.out.println("s - swap exponents");
			System.out.println("g - generate keys");
			
			input = input();
			if (input.length() > 0)
				switch(input.charAt(0)) {
					case 'g':
						System.out.println("prime1 strength?");
						int prime1s = Integer.decode(input()).intValue();
						System.out.println("prime2 strength?");
						int prime2s = Integer.decode(input()).intValue();
						keys(prime1s, prime2s, 20, new java.security.SecureRandom());
						break;
					case 'v' : view(); break;
					case 'l' : readin(); break;
					case 'e' : encrypter(); break;
					case 'd' : decrypter(); break;
					case 's' : BigInteger swap = publicExponent;
						publicExponent = privateExponent;
						privateExponent = swap;
						System.out.println(publicExponent.compareTo(privateExponent) < 0 ? "Cipher" : "Auth");
						break;
					case 't' : test(BigInteger.valueOf(64)); break;
					case 'w' : writeout(); break;
					case ' ' : exit = true; break;
				}
                } while (! exit );
	}
	/** display the keys */
	private static void view() {
		System.out.println("Values:");
		System.out.println("Key strength: " + modulus.bitLength() + " bits");
	}	
	/**
	 * Test a keypair.
	 * A good practical way to check the keypair
	 * is likely to work is to try some values
	 *
	 * @param maxima Maximum value to be tested
	 */
	protected static boolean test(BigInteger maxima) {
		
		boolean ok = true;
		System.out.println("Verifying key");

		BigInteger exponent = publicExponent.multiply(privateExponent);
		for (BigInteger t = ONE; t.compareTo(maxima) < 0; t = t.add(ONE))
			ok = ok && t.equals(crypt(t, exponent));

		System.out.println(ok ? "Key Appears ok" : "KEY NOT OK");
                return ok;
        }
	/**
	 * Make a keypair
	 * @param prime1s Prime 1 strength.
	 * @param prime2s Prime 2 strength.
	 * @param prob Prime probability requirement.
	 * @param random Random number generator to use.
	 */
	public static void keys(int prime1s, int prime2s, int prob, Random random) {
		
		prime1 = new BigInteger(prime1s, prob, random);
		prime2 = new BigInteger(prime2s, prob, random);
		modulus = prime1.multiply(prime2);
		BigInteger primeminus = prime1.subtract(ONE).multiply(prime2.subtract(ONE));

		publicExponent = ONE;
		do publicExponent = publicExponent.add(ONE);
		while ( (!publicExponent.gcd(primeminus).equals(ONE)) && (publicExponent.compareTo(modulus) < 0));
		
		privateExponent = publicExponent.modInverse(primeminus);
	}
	/**
	 * Encrypt a message
	 * @param message The message
	 * @return Cyphertext
	 */
	public static BigInteger encrypt(BigInteger message) {
		return crypt(message, publicExponent);
	}
	/**
	 * Decrypt a message
	 * @param cyphertext Cyphertext
	 * @return A message
	 */
	public static BigInteger decrypt(BigInteger cyphertext) {
		return crypt(cyphertext, privateExponent);
	}
	/**
	 * Do cryptography
	 * @param message Message to process
	 * @param exponent Exponent to utilise
	 * @return Processed Message
	 */
	public static BigInteger crypt(BigInteger message, BigInteger exponent) {
		return message.modPow(exponent, modulus);
	}

	/**
	 * Convert a secret message into an integer ready for encryption
	 * @param s Secret message
	 * @return Encoded message
	 */
	public static BigInteger encode(String s) {
		return new BigInteger(s.getBytes());
	}
	/**
	 * Recover the text from a decrypted integer
	 * @param b Encoded message
	 * @return Secret message
	 */
	protected static String decode(BigInteger b) {
                return new String(b.toByteArray());
	}
	/**
	 * Write the cryptograhic integers to files
	 * @exception IOException if there is a problem writing the files
	 */
	public static void writeout() throws IOException {
		out(prime1, "PRIME1");
		out(prime2, "PRIME2");
		out(publicExponent, "PUBLIC");
		out(privateExponent, "PRIVATE");
		out(modulus, "MODULUS");
	}
        public static void writeout(String filePrefix) throws IOException {
		out(prime1, filePrefix+"PRIME1");
		out(prime2, filePrefix+"PRIME2");
		out(publicExponent, filePrefix+"PUBLIC");
		out(privateExponent, filePrefix+"PRIVATE");
		out(modulus, filePrefix+"MODULUS");
	}
	/**
	 * Writes an integer to a file
	 * @param nu The integer to store
	 * @param fname The filename to store in.
	 * @exception IOException if there's a problem accessing the file
	 */
	protected static void out(BigInteger nu, String fname) throws IOException {
		OutputStream file = new FileOutputStream(fname);
		file.write(nu.toByteArray());
		file.flush();
		file.close();
	}
	/** Read cryptographic integers from files, ignoring those not present */
	private static void readin() {
		System.out.println("Loading Keys");
		try { prime1          = in("PRIME1"); } catch (IOException ex) { }
		try { prime2          = in("PRIME2"); } catch (IOException ex) { }
		try { publicExponent  = in("PUBLIC"); } catch (IOException ex) { }
		try { privateExponent = in("PRIVATE"); } catch (IOException ex) { } 
		try { modulus         = in("MODULUS"); } catch (IOException ex) { }
		System.out.println("Keys loaded");
	}
	/**
	 * Reads an integer from a file
	 * @param fname The filename integer is stored in.
	 * @return The integer.
	 * @exception IOException if there's a problem accessing the file
	 */
	protected static BigInteger in(String fname) throws IOException {
		RandomAccessFile file = new RandomAccessFile(fname, "r");
		byte[] b = new byte[(int)file.length()];
		file.read(b);
		file.close();
		return new BigInteger(b);
	}
	/**
	 * Encrypt a message
	 * @exception IOException if there's a problem from the console or saving "FILE".
	 */
	public static void encrypter() throws IOException {
		System.out.println("Key a message to encrypt:");
		System.out.println("Max: " + modulus.bitLength() / 8 + " characters");

		String s = input();
		if (s.length() > 0) {
			System.out.println("Encrypting...");
			BigInteger en = encrypt(encode(s));
			out(en, "FILE");
		}
	}
	/**
	 * Decrypt a message
	 * @exception IOException if there's a problem loading "FILE".
	 */
	private static void decrypter() throws IOException {
		System.out.println("Decrypting...");
                System.out.println(decode(decrypt(in("FILE"))));
	}
	/**
	 * Gets a line of text from the console
	 * strips trailing CR or LF
	 * @return Text from console keyboard
	 * @exception IOException if there's a problem from the console
	 */
	private static String input() throws IOException {
		String s = "";
		int ch;
		do {
			do {
				ch = in.read();
			} while(
				((ch == '\r') && (enterkey == '\n')) ||
				((ch == '\n') && (enterkey == '\r'))
				);

			if (! Character.isISOControl((char)ch))
				s = s + (char)ch;
		} while (! Character.isISOControl((char)ch));
		enterkey = ch;

		return s;
	}
}
