package com.yepstudio.android.service.autoupdate.internal;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

import com.yepstudio.android.service.autoupdate.AppUpdateService;
import com.yepstudio.android.service.autoupdate.AppUpdateServiceConfiguration;
import com.yepstudio.android.service.autoupdate.AutoUpdateLog;
import com.yepstudio.android.service.autoupdate.AutoUpdateLogFactory;
import com.yepstudio.android.service.autoupdate.CheckFileDelegate;
import com.yepstudio.android.service.autoupdate.DownloadDelegate;
import com.yepstudio.android.service.autoupdate.UserOptionsListener;
import com.yepstudio.android.service.autoupdate.Version;

/**
 * 
 * @author zhangzl@gmail.com
 * @create 2014年4月17日
 * @version 1.0, 2014年4月17日
 *
 */
public class SimpleUserOptionsListener implements UserOptionsListener {
	
	private static AutoUpdateLog log = AutoUpdateLogFactory.getAutoUpdateLog(SimpleUserOptionsListener.class);
	
	@Override
	public void doUpdate(final String module, final Context context, final Version version, boolean laterOnWifi) {
		final AppUpdateServiceConfiguration config = AppUpdateService.getConfiguration(module);
		log.debug("doUpdate, laterOnWifi:" + laterOnWifi);
		final DownloadDelegate delegate = config.getDownloadDelegate();
		if (!laterOnWifi) {
			File file = delegate.getDownloadLocalFile(module, context, version);
			if (file != null && file.exists()) {
				log.debug("getDownloadLocalFile, is not exists");
				checkAndInstallApk(config, context, version, file);
			} else {
				Runnable callback = new Runnable() {

					@Override
					public void run() {
						File apkFile = delegate.getDownloadLocalFile(module, context, version);
						checkAndInstallApk(config, context, version, apkFile);
					}
					
				};
				if (!delegate.download(module, context, version, callback)) {
					log.warning("download fail, use BrowserDownloadDelegate to download.");
					new BrowserDownloadDelegate().download(module, context, version, null);
				}
			}

		} else {
			new VersionPersistent(context).save(version);
		}
	}

	@Override
	public void doIgnore(String module, Context context, Version version) {
		log.info("doIgnore");
	}
	
	private void checkAndInstallApk(final AppUpdateServiceConfiguration config, final Context context, final Version version, final File apk) {
		new AsyncTask<Void, Integer, Boolean>() {

			@Override
			protected Boolean doInBackground(Void... paramArrayOfParams) {
				try {
					CheckFileDelegate delegate = config.getCheckFileDelegate();
					return delegate.doCheck(config.getModule(), context, version, apk);
				} catch (Throwable e) {
					
				}
				return false;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				if (result != null && result == true) {
					installAPK(context, apk);
				}
				super.onPostExecute(result);
			}
			
		}.execute();
	}
	
	private void installAPK(Context context, File apk) {
		Intent installIntent = new Intent();
		installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		installIntent.setAction(android.content.Intent.ACTION_VIEW);
		installIntent.setDataAndType(Uri.fromFile(apk), "application/vnd.android.package-archive");
		context.startActivity(installIntent);
	}

}
