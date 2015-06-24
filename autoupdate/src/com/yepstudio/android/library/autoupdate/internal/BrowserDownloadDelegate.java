package com.yepstudio.android.library.autoupdate.internal;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.yepstudio.android.library.autoupdate.DownloadDelegate;
import com.yepstudio.android.library.autoupdate.R;
import com.yepstudio.android.library.autoupdate.Version;

/**
 * 浏览器下载器
 * @author zhangzl@gmail.com
 * @create 2014年4月17日
 * @version 1.0, 2014年4月17日
 *
 */
public class BrowserDownloadDelegate implements DownloadDelegate {

	@Override
	public boolean download(String module, Context context, Version version, Runnable callback, boolean isUserOpt) {
		if (isUserOpt) {
			Toast.makeText(context, R.string.aus__start_download, Toast.LENGTH_LONG).show();
		}
		Uri uri = Uri.parse(version.getTargetUrl());
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
		return true;
	}

	@Override
	public File getDownloadLocalFile(String module, Context context, Version version) {
		return null;
	}

	@Override
	public boolean isDownloading(String module, Context context, Version version) {
		return false;
	}

}
