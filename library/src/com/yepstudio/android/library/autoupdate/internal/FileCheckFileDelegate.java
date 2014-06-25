package com.yepstudio.android.library.autoupdate.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.Context;
import android.text.TextUtils;

import com.yepstudio.android.library.autoupdate.AutoUpdateLog;
import com.yepstudio.android.library.autoupdate.AutoUpdateLogFactory;
import com.yepstudio.android.library.autoupdate.CheckFileDelegate;
import com.yepstudio.android.library.autoupdate.Version;

/**
 * 校验文件完整性，使用Version的MD5或者SHA1进行校验
 * @author zzljob@gmail.com
 * @create 2014年4月17日
 * @version 1.1, 2014年6月16日
 *
 */
public class FileCheckFileDelegate implements CheckFileDelegate {
	
	private static AutoUpdateLog log = AutoUpdateLogFactory.getAutoUpdateLog(FileCheckFileDelegate.class);

	@Override
	public boolean doCheck(Context context, Version version, File file) {
		if (!TextUtils.isEmpty(version.getMD5())) {
			String md5 = getAlgorithmOfFile(file, "MD5");
			log.debug("version.getMD5() = " + version.getMD5() + " ,  file MD5 = " + md5);
			return TextUtils.equals(md5, version.getMD5());
		}
		if (!TextUtils.isEmpty(version.getSHA1())) {
			String sha1 = getAlgorithmOfFile(file, "SHA-1");
			log.debug("version.getSHA1() = " + version.getSHA1() + " ,  file MD5 = " + sha1);
			return TextUtils.equals(sha1, version.getSHA1());
		}
		log.trace("doCheck, no MD5, no SHA-1, so pass : " + file.getAbsolutePath());
		return true;
	}
	
	public static String getAlgorithmOfFile(File file, String algorithm) {
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			
		}
		if (digest == null || file == null || !file.exists()) {
			return null;
		}
		FileInputStream is = null;
		try {
			is = new FileInputStream(file);
			byte[] bytes = new byte[8192];
			int byteCount;
			while ((byteCount = is.read(bytes)) > 0) {
				digest.update(bytes, 0, byteCount);
			}
		} catch (IOException e) {
			return null;
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {

				}
			}
		}
		BigInteger bigInt = new BigInteger(1, digest.digest());
		String hashtext = bigInt.toString(16);
		while (hashtext.length() < 32) {
			hashtext = "0" + hashtext;
		}
		return hashtext;
	}

}
