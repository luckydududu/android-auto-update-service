package com.yepstudio.android.library.autoupdate.internal;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.yepstudio.android.library.autoupdate.InstallExecutor;

/**
 * 
 * @author zzljob@gmail.com
 * @create 2014年4月20日
 * @version 1.0，2014年4月20日
 *
 */
public class ApkInstallExecutor implements InstallExecutor {

	@Override
	public void install(Context context, File file) {
		Intent installIntent = new Intent();
		installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		installIntent.setAction(android.content.Intent.ACTION_VIEW);
		installIntent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
		context.startActivity(installIntent);
	}

}
