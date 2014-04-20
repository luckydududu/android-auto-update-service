package com.yepstudio.android.service.autoupdate.internal;

import java.io.File;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.yepstudio.android.service.autoupdate.AppUpdateService;
import com.yepstudio.android.service.autoupdate.AppUpdateServiceConfiguration;
import com.yepstudio.android.service.autoupdate.AutoUpdateLog;
import com.yepstudio.android.service.autoupdate.AutoUpdateLogFactory;
import com.yepstudio.android.service.autoupdate.CheckFileDelegate;
import com.yepstudio.android.service.autoupdate.DownloadDelegate;
import com.yepstudio.android.service.autoupdate.R;
import com.yepstudio.android.service.autoupdate.UpdatePolicy;
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
				if (!delegate.download(module, context, version, callback, true)) {
					log.warning("download fail, use BrowserDownloadDelegate to download.");
					new BrowserDownloadDelegate().download(module, context, version, null, false);
				}
			}
		} else {
			config.getVersionPersistent().save(module, context, version);
			Toast.makeText(context, R.string.aus__update_version_just_in_wifi, Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void doIgnore(String module, Context context, Version version) {
		log.info("doIgnore");
		AppUpdateServiceConfiguration config = AppUpdateService.getConfiguration(module);
		UpdatePolicy updatePolicy = version.getUpdatePolicy();
		if (updatePolicy == null) {
			updatePolicy = config.getUpdatePolicy();
		}
		updatePolicy.getIgnorePolicy().notifyIgnore(context, version);
	}
	
	private void checkAndInstallApk(final AppUpdateServiceConfiguration config, final Context context, final Version version, final File apk) {
		new AsyncTask<Void, Integer, Boolean>() {

			@Override
			protected Boolean doInBackground(Void... paramArrayOfParams) {
				try {
					log.debug("download success, CheckFile ....");
					CheckFileDelegate delegate = config.getCheckFileDelegate();
					return delegate.doCheck(config.getModule(), context, version, apk);
				} catch (Throwable e) {
					log.error("download success, CheckFile fail", e);
					return false;
				}
			}

			@Override
			protected void onPostExecute(Boolean result) {
				if (result != null && result == true) {
					log.info("download success, CheckFile success, start install...");
					installAPK(config, context, apk);
				} else {
					if (apk != null) {
						if (!apk.delete()) {
							apk.deleteOnExit();
						}
					}
					log.info("download success, CheckFile not pass, cancel this install, and delete this file.");
					Toast.makeText(context, R.string.aus__apk_file_invalid, Toast.LENGTH_LONG).show();
				}
				super.onPostExecute(result);
			}
			
		}.execute();
		Toast.makeText(context, R.string.aus__apk_file_start_valldate, Toast.LENGTH_LONG).show();
	}
	
	private void installAPK(AppUpdateServiceConfiguration config, Context context, File apk) {
		log.info("installAPK : " + apk.getAbsolutePath());
		Toast.makeText(context, R.string.aus__start_install, Toast.LENGTH_LONG).show();
		config.getInstallExecutor().install(config.getModule(), context, apk);
	}

}
