package com.yepstudio.android.service.autoupdate.internal;

import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.text.TextUtils;

import com.yepstudio.android.service.autoupdate.AutoUpdateLog;
import com.yepstudio.android.service.autoupdate.AutoUpdateLogFactory;
import com.yepstudio.android.service.autoupdate.CheckFileDelegate;
import com.yepstudio.android.service.autoupdate.Version;

/**
 * 验证签名
 * 
 * @author zhangzl@gmail.com
 * @create 2014年4月18日
 * @version 1.0, 2014年4月18日
 * 
 */
public class SignatureCheckFileDelegate implements CheckFileDelegate {
	
	private static AutoUpdateLog log = AutoUpdateLogFactory.getAutoUpdateLog(SignatureCheckFileDelegate.class); 

	@Override
	public boolean doCheck(Context context, Version version, File file) {
		if (file == null || !file.exists()) {
			return false;
		}
		log.trace("doCheck : " + file.getAbsolutePath());
		String dexPath = file.getAbsolutePath();
		PackageInfo info = context.getPackageManager().getPackageArchiveInfo(dexPath, PackageManager.GET_SIGNATURES);
		if (info == null) {
			return false;
		}
		Signature[] apkSigns = info.signatures;
		String apkMD5 = getSignatureMD5(apkSigns);
		log.debug("download apk Signature MD5 : " + apkMD5);

		PackageManager pm = context.getPackageManager();
		PackageInfo packageInfo = null;
		try {
			packageInfo = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
		} catch (NameNotFoundException e) {
			throw new RuntimeException(e);
		}
		Signature[] selfSigns = packageInfo.signatures;
		String selfMd5 = getSignatureMD5(selfSigns);
		log.debug("self Signature MD5 : " + selfMd5);
		
		return TextUtils.equals(apkMD5, selfMd5);
	}

	public static String getSignatureMD5(Signature[] signs) {
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {

		}
		if (digest == null || signs == null) {
			return null;
		}
		for (Signature signature : signs) {
			digest.update(signature.toByteArray());
		}
		byte[] md5byte = digest.digest();
		BigInteger bigInt = new BigInteger(1, md5byte);
		String hashtext = bigInt.toString(16);
		while (hashtext.length() < 32) {
			hashtext = "0" + hashtext;
		}
		return hashtext;
	}

}
