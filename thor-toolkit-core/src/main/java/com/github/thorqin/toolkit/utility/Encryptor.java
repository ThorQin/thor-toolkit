package com.github.thorqin.toolkit.utility;

import java.security.*;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;

public class Encryptor {

	private final Cipher cipher;
	private final SecretKey key;
	private final IvParameterSpec iv;

	private static String getFirstPart(String algorithm) {
		if (algorithm == null)
			return null;
		int splitterPos = algorithm.indexOf('/');
		if (splitterPos <= 0)
			return algorithm;
		else
			return algorithm.substring(0, splitterPos);
	}

	public final static SecretKey generateKey(String cipher) throws NoSuchAlgorithmException {
		return generateKey(cipher, null);
	}

	public final static SecretKey generateKey(String cipher, byte[] pwdBytes) throws NoSuchAlgorithmException {
		final int keySize;
		String firstPart = getFirstPart(cipher);
		switch (firstPart.toLowerCase()) {
			case "aes":
				keySize = 128;
				break;
			case "des":
				keySize = 56;
				break;
			case "desede":
				keySize = 168;
				break;
			default:
				throw new NoSuchAlgorithmException("Unsupport cipher: " + cipher);
		}
		KeyGenerator keygen = KeyGenerator.getInstance(firstPart);
		if (pwdBytes != null)
			keygen.init(keySize, new SecureRandom(pwdBytes));
		else
			keygen.init(keySize);
		return keygen.generateKey();
	}

	public final static byte[] digest(byte[] bytes, String digestAlgorithm) {
		try {
			MessageDigest mdTemp = MessageDigest.getInstance(digestAlgorithm);
			mdTemp.update(bytes);
			return mdTemp.digest();
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}

	public final static byte[] md5(byte[] bytes) {
		return digest(bytes, "MD5");
	}
	public final static byte[] sha1(byte[] bytes) {
		return digest(bytes, "SHA1");
	}
	private final static char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
			'a', 'b', 'c', 'd', 'e', 'f' };
	public final static String hexString(byte[] bytes) {
		if (bytes == null)
			return null;
		char str[] = new char[bytes.length * 2];
		int k = 0;
		for (int i = 0; i < bytes.length; i++) {
			str[k++] = hexDigits[bytes[i] >>> 4 & 0xf];
			str[k++] = hexDigits[bytes[i] & 0xf];
		}
		return new String(str);
	}

	public final static String md5String(byte[] bytes) {
		return hexString(md5(bytes));
	}
	public final static String sha1String(byte[] bytes) {
		return hexString(sha1(bytes));
	}

	private Encryptor(Cipher cipher, SecretKey key, byte[] ivBytes) {
		this.cipher = cipher;
		this.key = key;
		if (ivBytes != null)
			this.iv = new IvParameterSpec(ivBytes);
		else
			this.iv = null;
	}
	public static Encryptor createByEncodedKey(String cipher, byte[] key) throws Exception {
		return new Encryptor(Cipher.getInstance(cipher),
				new SecretKeySpec(key, getFirstPart(cipher)),
				null);
	}
	public static Encryptor createByEncodedKey(String cipher, byte[] key, byte[] ivBytes) throws Exception {
		return new Encryptor(Cipher.getInstance(cipher),
				new SecretKeySpec(key, getFirstPart(cipher)),
				ivBytes);
	}
	public static Encryptor create(String cipher, byte[] key, byte[] ivBytes) throws Exception {
		SecretKey secretKey = generateKey(cipher, key);
		return new Encryptor(Cipher.getInstance(cipher), secretKey, ivBytes);
	}
	public static Encryptor create(String cipher, byte[] ivBytes) throws Exception {
		return create(cipher, null, ivBytes);
	}
	public static Encryptor create(String cipher) throws Exception {
		return create(cipher, null, null);
	}

	public byte[] getEncodedKey() {
		return this.key.getEncoded();
	}
	
	public synchronized final byte[] encrypt(byte[] src) throws Exception {
		if (iv != null) {
			cipher.init(Cipher.ENCRYPT_MODE, key, iv);
		} else
			cipher.init(Cipher.ENCRYPT_MODE, key);
		return cipher.doFinal(src);
	}
	public synchronized final byte[] decrypt(byte[] src) throws Exception {
		if (iv != null) {
			cipher.init(Cipher.DECRYPT_MODE, key, iv);
		} else
			cipher.init(Cipher.DECRYPT_MODE, key);
		return cipher.doFinal(src);
	}


	public static void main(String arg[]) throws Exception {
		String cipher;
		if (arg.length != 1) {
			System.out.println();
			System.out.println("Usage: Encryptor <Algorithm: AES|DES|DESede>");
			System.out.println();
			return;
		} else {
			cipher = getFirstPart(arg[0]).toLowerCase();
			if (!cipher.equals("aes") && !cipher.equals("des") && !cipher.equals("desded")) {
				System.out.println();
				System.out.println("Usage: Encryptor <Algorithm: AES|DES|DESede>");
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
