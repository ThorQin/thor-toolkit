package com.github.thorqin.toolkit.utility;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;

public class Encryptor {
	public final static String AES = "aes";
	public final static String DES = "des";
	public final static String DESede = "desede"; // 3DES
	public final static String DEScbc = "descbc";
	public final static Map<String, String> cipherMap = new HashMap<String, String>(){
		{
			put("aes", "aes");
			put("des", "des");
			put("desede", "desede");
			put("descbc", "des/cbc/pkcs5padding");
		}
	};
	private final Cipher cipher;
	private final SecretKey secretKey;
	private final byte[] ivBytes;

	private static String getFullName(String name) {
		return cipherMap.get(name);
	}
	public final static SecretKey generateKey(String cipher) throws NoSuchAlgorithmException {
		return generateKey(cipher, null);
	}
	public final static SecretKey generateKey(String cipher, byte[] key) throws NoSuchAlgorithmException {
		final int keySize;
		switch (cipher.toLowerCase()) {
			case AES:
				keySize = 128;
				break;
			case DES:
				keySize = 56;
				break;
			case DESede:
				keySize = 168;
				break;
			default:
				throw new NoSuchAlgorithmException("Unsupport cipher: " + cipher);
		}
		KeyGenerator keygen = KeyGenerator.getInstance(getFullName(cipher));
		if (key != null)
			keygen.init(keySize, new SecureRandom(key));
		else
			keygen.init(keySize);
		return keygen.generateKey();
	}

	public final static byte[] md5(byte[] bytes) {
		try {
			MessageDigest mdTemp = MessageDigest.getInstance("MD5");
			mdTemp.update(bytes);
			return mdTemp.digest();
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}
	private final static char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
			'a', 'b', 'c', 'd', 'e', 'f' };
	public final static String md5String(byte[] bytes) {
		byte[] md = md5(bytes);
		if (md == null)
			return null;
		int j = md.length;
		char str[] = new char[j * 2];
		int k = 0;
		for (int i = 0; i < j; i++) {
			str[k++] = hexDigits[md[i] >>> 4 & 0xf];
			str[k++] = hexDigits[md[i] & 0xf];
		}
		return new String(str);
	}
	private Encryptor(Cipher cipher, SecretKey key, byte[] ivBytes) {
		this.cipher = cipher;
		this.secretKey = key;
		this.ivBytes = ivBytes;
	}
	public static Encryptor createByEncodedKey(String cipher, byte[] key) throws Exception {
		return new Encryptor(Cipher.getInstance(cipher),
				new SecretKeySpec(key, getFullName(cipher)),
				null);
	}
	public static Encryptor createByEncodedKey(String cipher, byte[] key, byte[] ivBytes) throws Exception {
		return new Encryptor(Cipher.getInstance(cipher),
				new SecretKeySpec(key, getFullName(cipher)),
				ivBytes);
	}
	public static Encryptor create(String cipher, byte[] key, byte[] ivBytes) throws Exception {
		SecretKey secretKey;
		switch (cipher.toLowerCase()) {
			case DES:
			case DEScbc:
				secretKey = generateKey(DES, key);
				break;
			case AES:
				secretKey = generateKey(AES, key);
				break;
			case DESede:
				secretKey = generateKey(DESede, key);
				break;
			default:
				throw new NoSuchAlgorithmException("Unsupport cipher: " + cipher);
		}
		return new Encryptor(Cipher.getInstance(getFullName(cipher)), secretKey, ivBytes);
	}
	public static Encryptor create(String cipher, byte[] ivBytes) throws Exception {
		return create(cipher, null, ivBytes);
	}
	public static Encryptor create(String cipher) throws Exception {
		return create(cipher, null, null);
	}

	public byte[] getEncodedKey() {
		return this.secretKey.getEncoded();
	}
	
	public synchronized final byte[] encrypt(byte[] src) throws Exception {
		if (ivBytes != null) {
			IvParameterSpec ipSpec = new IvParameterSpec(ivBytes);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, ipSpec);
		} else
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		return cipher.doFinal(src);
	}
	public synchronized final byte[] decrypt(byte[] src) throws Exception {
		if (ivBytes != null) {
			IvParameterSpec ipSpec = new IvParameterSpec(ivBytes);
			cipher.init(Cipher.DECRYPT_MODE, secretKey, ipSpec);
		} else
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
		return cipher.doFinal(src);
	}


	public static void main(String arg[]) throws Exception {
		String cipher;
		if (arg.length != 1) {
			System.out.println();
			System.out.println("Usage: Encryptor <Cipher: AES|DES|DESede>");
			System.out.println();
			return;
		} else {
			cipher = arg[0].toLowerCase();
			if (!cipher.equals(AES) && !cipher.equals(DES) && !cipher.equals(DESede)&& !cipher.equals("descbc")) {
				System.out.println();
				System.out.println("Usage: Encryptor <Cipher: AES|DES|DESede>");
				System.out.println();
				return;
			}
		}
		SecretKey secKek = Encryptor.generateKey(cipher);
		String key = Base64.encodeBase64String(secKek.getEncoded());
		System.out.println("-----------------------------------");
		System.out.println(cipher.toUpperCase() + " KEY: " + key);
		System.out.println("-----------------------------------");
	}
}
