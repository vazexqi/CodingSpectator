/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
@Deprecated
public class Encryptor {

	@SuppressWarnings("serial")
	public static class EncryptionException extends Exception {

		public EncryptionException(Throwable cause) {
			super(cause);
		}

	}

	public static String toMD5(String text) throws EncryptionException {
		MessageDigest digest;
		try {
			digest= MessageDigest.getInstance("MD5");
			byte[] bytesDigest= digest.digest(text.getBytes("UTF-8"));
			String stringDigest= String.format("%032x", new BigInteger(1, bytesDigest));
			return stringDigest;
		} catch (NoSuchAlgorithmException e) {
			throw new EncryptionException(e);
		} catch (UnsupportedEncodingException e) {
			throw new EncryptionException(e);
		}
	}

}
