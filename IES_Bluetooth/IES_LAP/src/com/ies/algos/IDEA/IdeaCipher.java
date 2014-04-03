package com.ies.algos.IDEA;


//Referenced classes of package Acme.Crypto:
//         BlockCipher, Cipher, CryptoUtils

public class IdeaCipher extends BlockCipher
{

 private int encryptKeys[];
 private int decryptKeys[];
 private int tempShorts[];

 public IdeaCipher(String keyStr)
 {
     super(16, 8);
     encryptKeys = new int[52];
     decryptKeys = new int[52];
     tempShorts = new int[4];
     setKey(keyStr);
 }

 public IdeaCipher(byte key[])
 {
     super(16, 8);
     encryptKeys = new int[52];
     decryptKeys = new int[52];
     tempShorts = new int[4];
     setKey(key);
 }

 public void setKey(byte key[])
 {
     int k1;
     for(k1 = 0; k1 < 8; k1++)
     {
         encryptKeys[k1] = (key[2 * k1] & 0xff) << 8 | key[2 * k1 + 1] & 0xff;
     }

     for(; k1 < 52; k1++)
     {
         encryptKeys[k1] = (encryptKeys[k1 - 8] << 9 | encryptKeys[k1 - 7] >>> 7) & 0xffff;
     }

     k1 = 0;
     int k2 = 51;
     int t1 = mulinv(encryptKeys[k1++]);
     int t2 = -encryptKeys[k1++];
     int t3 = -encryptKeys[k1++];
     decryptKeys[k2--] = mulinv(encryptKeys[k1++]);
     decryptKeys[k2--] = t3;
     decryptKeys[k2--] = t2;
     decryptKeys[k2--] = t1;
     for(int j = 1; j < 8; j++)
     {
         t1 = encryptKeys[k1++];
         decryptKeys[k2--] = encryptKeys[k1++];
         decryptKeys[k2--] = t1;
         t1 = mulinv(encryptKeys[k1++]);
         t2 = -encryptKeys[k1++];
         t3 = -encryptKeys[k1++];
         decryptKeys[k2--] = mulinv(encryptKeys[k1++]);
         decryptKeys[k2--] = t2;
         decryptKeys[k2--] = t3;
         decryptKeys[k2--] = t1;
     }

     t1 = encryptKeys[k1++];
     decryptKeys[k2--] = encryptKeys[k1++];
     decryptKeys[k2--] = t1;
     t1 = mulinv(encryptKeys[k1++]);
     t2 = -encryptKeys[k1++];
     t3 = -encryptKeys[k1++];
     decryptKeys[k2--] = mulinv(encryptKeys[k1++]);
     decryptKeys[k2--] = t3;
     decryptKeys[k2--] = t2;
     decryptKeys[k2--] = t1;
 }

 public void encrypt(byte clearText[], int clearOff, byte cipherText[], int cipherOff)
 {
     CryptoUtils.squashBytesToShorts(clearText, clearOff, tempShorts, 0, 4);
     idea(tempShorts, tempShorts, encryptKeys);
     CryptoUtils.spreadShortsToBytes(tempShorts, 0, cipherText, cipherOff, 4);
 }

 public void decrypt(byte cipherText[], int cipherOff, byte clearText[], int clearOff)
 {
     CryptoUtils.squashBytesToShorts(cipherText, cipherOff, tempShorts, 0, 4);
     idea(tempShorts, tempShorts, decryptKeys);
     CryptoUtils.spreadShortsToBytes(tempShorts, 0, clearText, clearOff, 4);
 }

 private void idea(int inShorts[], int outShorts[], int keys[])
 {
     int x1 = inShorts[0];
     int x2 = inShorts[1];
     int x3 = inShorts[2];
     int x4 = inShorts[3];
     int k = 0;
     for(int round = 0; round < 8; round++)
     {
         x1 = mul(x1 & 0xffff, keys[k++]);
         x2 += keys[k++];
         x3 += keys[k++];
         x4 = mul(x4 & 0xffff, keys[k++]);
         int t2 = x1 ^ x3;
         t2 = mul(t2 & 0xffff, keys[k++]);
         int t1 = t2 + (x2 ^ x4);
         t1 = mul(t1 & 0xffff, keys[k++]);
         t2 = t1 + t2;
         x1 ^= t1;
         x4 ^= t2;
         t2 ^= x2;
         x2 = x3 ^ t1;
         x3 = t2;
     }

     outShorts[0] = mul(x1 & 0xffff, keys[k++]) & 0xffff;
     outShorts[1] = x3 + keys[k++] & 0xffff;
     outShorts[2] = x2 + keys[k++] & 0xffff;
     outShorts[3] = mul(x4 & 0xffff, keys[k++]) & 0xffff;
 }

 private static int mul(int a, int b)
 {
     int ab = a * b;
     if(ab != 0)
     {
         int lo = ab & 0xffff;
         int hi = ab >>> 16;
         return (lo - hi) + (lo >= hi ? 0 : 1) & 0xffff;
     }
     if(a != 0)
     {
         return 1 - a & 0xffff;
     } else
     {
         return 1 - b & 0xffff;
     }
 }

 private static int mulinv(int x)
 {
     if(x <= 1)
     {
         return x;
     }
     int t0 = 1;
     int t1 = 0x10001 / x;
     int y = 0x10001 % x & 0xffff;
     do
     {
         if(y == 1)
         {
             return 1 - t1 & 0xffff;
         }
         int q = x / y;
         x %= y;
         t0 = t0 + q * t1 & 0xffff;
         if(x == 1)
         {
             return t0;
         }
         q = y / x;
         y %= x;
         t1 = t1 + q * t0 & 0xffff;
     } while(true);
 }

 public static void main(String args[])
 {
     for(int a = 0; a < 0x10000; a++)
     {
         int b = mulinv(a);
         int c = mul(a, b);
         if(c != 1)
         {
             System.err.println("mul/mulinv flaw: " + a + " * " + b + " = " + c);
         }
     }
     
 }
}
