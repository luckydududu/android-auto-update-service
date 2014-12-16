package com.yepstudio.android.library.autoupdate.internal;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

import com.yepstudio.android.library.autoupdate.AppUpdate;
import com.yepstudio.android.library.autoupdate.AppUpdateService;
import com.yepstudio.android.library.autoupdate.AppUpdateServiceConfiguration;
import com.yepstudio.android.library.autoupdate.AutoUpdateLog;
import com.yepstudio.android.library.autoupdate.AutoUpdateLogFactory;
import com.yepstudio.android.library.autoupdate.CheckFileDelegate;
import com.yepstudio.android.library.autoupdate.DisplayDelegate;
import com.yepstudio.android.library.autoupdate.DownloadDelegate;
import com.yepstudio.android.library.autoupdate.R;
import com.yepstudio.android.library.autoupdate.RequestInfo;
import com.yepstudio.android.library.autoupdate.ResponseDelivery;
import com.yepstudio.android.library.autoupdate.ResponseListener;
import com.yepstudio.android.library.autoupdate.UpdatePolicy;
import com.yepstudio.android.library.autoupdate.UserOptionsListener;
import com.yepstudio.android.library.autoupdate.Version;
import com.yepstudio.android.library.autoupdate.VersionComparer;
import com.yepstudio.android.library.autoupdate.VersionPersistent;

/**
 * 
 * @author zzljob@gmail.com
 * @create 2014年4月17日
 * @version 1.1, 2014年6月16日
 *
 */
public class AutoUpgradeDelegate implements AppUpdate {
	
	private static AutoUpdateLog log = AutoUpdateLogFactory.getAutoUpdateLog(AutoUpgradeDelegate.class);
	
	protected AtomicBoolean isChecking = new AtomicBoolean(false); 

	public void checkUpdate(final ContextWrapper wrapper) {
		boolean isAutoUpdate = wrapper.isAutoUpdate();
		if (!canStartUpdateCheck(wrapper)) {
			log.info("update stop, hasNetwork : false, isAutoUpdate:" + isAutoUpdate);
			return;
		}
		
		// 判断是不是正在检查
		AsyncTask<Void, Integer, Version> task = new AsyncTask<Void, Integer, Version>() {
			
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
			}

			@Override
			protected Version doInBackground(Void... args) {
				Version version = checkUpdateInTask(wrapper);
				if (version == null) {
					wrapper.append("no-find-version");
				} else {
					wrapper.append("find-new-version");
					version = processUpdatePolicy(wrapper);
				}
				return version;
			}

			@Override
			protected void onPostExecute(Version version) {
				processResponseListener(wrapper);
				finishUpdateCheck();
			}

		};
		wrapper.append("start-update-check");
		ResponseListener listener = wrapper.getConfiguration().getResponseListener();
		listener.onStartUpdateCheck(wrapper.getContext(), wrapper.isAutoUpdate(), isChecking.get());
		if (!isChecking.getAndSet(true)) {//如果没有检查更新则去检查更新
			log.info("execute check update... isAutoUpdate:" + isAutoUpdate);
			task.execute();
		} else {
			wrapper.append("has-running-stop-check");
			log.info("has check update is running, so skip it. isAutoUpdate:" + isAutoUpdate);
		}
		
	}

	protected boolean canStartUpdateCheck(ContextWrapper wrapper) {
		log.debug("start check canStartUpdateCheck...");
		if (!NetworkUtil.hasNetwork(wrapper.getContext())) {//没有网络，则直接提示不能升级
			log.trace("has not Network stop UpdateCheck.");
			if (!wrapper.isAutoUpdate()) {
				AppUpdateService.show(wrapper.getContext(), R.string.aus__network_not_activated, Toast.LENGTH_SHORT);
			}
			wrapper.append("not-network");
			return false;
		} else {//有网络
			log.trace("canStartUpdateCheck.");
		}
		return true;
	}
	
	protected void finishUpdateCheck() {
		log.debug("finishUpdateCheck.");
		isChecking.set(false);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Version checkUpdateInTask(ContextWrapper wrapper) {
		log.debug("start checkUpdateInTask...");
		Object response = null;
		AppUpdateServiceConfiguration config = wrapper.getConfiguration();
		ResponseDelivery responseDelivery = config.getResponseDelivery();
		RequestInfo info = config.getRequestInfo();
		try {
			wrapper.append("start-request");
			response = responseDelivery.submitRequest(info);
		} catch (Throwable exp) {
			log.error("requestUpdateCheck error", exp);
			wrapper.setRequestError(true);
			wrapper.append("request-error");
		}
		
		Version version = null;
		if (response != null) {
			try {
				wrapper.append("parser-response");
				version = responseDelivery.parserResponse(response);
				log.trace("ResponseParser version success : " + version);
			} catch (Exception exp) {
				log.error("ResponseParser error", exp);
				wrapper.setParserResponseError(true);
				wrapper.append("parser-response");
			}
		} else {
			log.trace("response isEmpty, no Version to Update");
			wrapper.append("no-response");
		}
		wrapper.setVersion(version);
		return version;
	}
	
	protected Version processUpdatePolicy(ContextWrapper wrapper) {
		log.debug("start processUpdatePolicy...");
		// 判断是否忽略服务器返回的更新策略
		Version version = wrapper.getVersion();
		if (version == null) {
			log.trace("version is null, return null.");
			return null;
		}
		AppUpdateServiceConfiguration config = wrapper.getConfiguration();
		if (config.isIgnoreServerPolicy() || wrapper.getVersion().getUpdatePolicy() == null) {
			log.trace("AppUpdateServiceConfiguration ignoreServerPolicy, so version.setUpdatePolicy from config.getUpdatePolicy");
			wrapper.getVersion().setUpdatePolicy(wrapper.getConfiguration().getUpdatePolicy());
			wrapper.append("use-updatepolicy-of-configuration");
		} else {
			log.trace("AppUpdateServiceConfiguration is not ignoreServerPolicy, use UpdatePolicy  when you parser in ResponseDelivery");
			wrapper.append("use-parse-updatepolicy-of-response");
		}
		return wrapper.getVersion();
	}
	
	protected void processResponseListener(ContextWrapper wrapper) {
		AppUpdateServiceConfiguration config = wrapper.getConfiguration();
		log.trace("processResponseListener");
		ResponseListener resListener = config.getResponseListener();
		Context context = wrapper.getContext();
		boolean isAutoUpdate = wrapper.isAutoUpdate();
		if (wrapper.isRequestError()) {
			resListener.onResponseError(context, isAutoUpdate);
			return ;
		}
		if (wrapper.isParserResponseError()) {
			resListener.onParserError(context, isAutoUpdate);
			return ;
		}
		// 返回解析并且解析成功
		VersionComparer comparer = config.getVersionCompare();
		Version version = wrapper.getVersion();
		if (!comparer.compare(context, version)) {
			resListener.onCurrentIsLatest(context, isAutoUpdate);
			return ;
		}
		
		//检查策略
		if (isSkipThisVersion(wrapper)) {
			resListener.onSkipLatestVersion(context, version, isAutoUpdate);
		} else {
			if (!isDownloading(wrapper)) {
				resListener.onFoundLatestVersion(context, version, isAutoUpdate);
				doShowFoundLatestVersion(wrapper);
			} else {
				if (!isAutoUpdate) {
					AppUpdateService.show(context, R.string.aus__update_downloading, Toast.LENGTH_LONG);
				}
			}
		}
	}
	
	protected boolean isDownloading(ContextWrapper wrapper){
		Version version = wrapper.getVersion();
		AppUpdateServiceConfiguration config = wrapper.getConfiguration();
		Context context = wrapper.getContext();
		//判断是否有版本正在下载
		if (config.getDownloadDelegate().isDownloading(wrapper.getModule(), context, version)) {
			log.info("onFoundLatestVersion be ignore. this version isDownloading...");
			log.trace("this version be ignore by isDownloading");
			wrapper.append("skip-version-by-this-version-downloading");
			return true;
		}
		return false;
	}
	
	protected boolean isSkipThisVersion(ContextWrapper wrapper) {
		log.debug("start check isSkipThisVersion...");
		Version version = wrapper.getVersion();
		AppUpdateServiceConfiguration config = wrapper.getConfiguration();
		Context context = wrapper.getContext();
		boolean isAutoUpdate = wrapper.isAutoUpdate();
		UpdatePolicy updatePolicy = version.getUpdatePolicy();
		if (updatePolicy == null) {
			log.trace("Version UpdatePolicy is null, so use  AppUpdateServiceConfiguration's UpdatePolicy.");
			updatePolicy = config.getUpdatePolicy();
		}
		
		log.debug(String.format("isIgnoreAutoUpdate:%s, isAutoUpdate:%s", updatePolicy.isIgnoreAutoUpdate(), isAutoUpdate));
		//是否忽略自动升级的版本
		if (updatePolicy.isIgnoreAutoUpdate() && isAutoUpdate) {
			log.trace("this version be ignore by UpdatePolicy when AutoUpdate");
			wrapper.append("skip-version-by-update-policy-when-auto-update");
			return true;
		}
		
		log.trace(String.format("IgnorePolicy:%s", updatePolicy.getIgnorePolicy()));
		//检查用户是否点击过忽略该版本，如果忽略过，就要检查忽略策略，判断是否要忽略
		if (updatePolicy.getIgnorePolicy().isIgnore(context, version, isAutoUpdate)) {
			if (!isAutoUpdate) {
				AppUpdateService.show(context, R.string.aus__has_new_version_label, Toast.LENGTH_LONG);
			}
			log.trace("this version be ignore by IgnorePolicy of UpdatePolicy");
			wrapper.append("skip-version-by-ignore-policy");
			return true;
		}
		
		//自动更新 并且 不在WIFI情况下
		if(isAutoUpdate && NetworkUtil.getNetworkType(context) != NetworkUtil.WIFI) {
			log.debug("isAutoUpdate and No Wifi, check update is member download when WIFI.");
			//判断下 用户是否选择过稍候在Wifi下面下载
			Version saveVersion = config.getVersionPersistent().load(wrapper.getModule(), context);
			log.trace("saveVersion's UniqueIdentity : " + (saveVersion != null ? saveVersion.getUniqueIdentity() : "null"));
			log.trace("version's UniqueIdentity : " + version.getUniqueIdentity());
			if (saveVersion != null && TextUtils.equals(saveVersion.getUniqueIdentity(), version.getUniqueIdentity())) {
				log.debug("this version be ignore be ignore. this version user opt update in WIFI...");
				wrapper.append("skip-version-by-this-version-user-update-WIFI");
				return true;
			}
		}
		return false;
	}
	
	protected void doShowFoundLatestVersion(final ContextWrapper wrapper) {
		log.debug("start doShowFoundLatestVersion...");
		final DisplayDelegate delegate = wrapper.getConfiguration().getDisplayDelegate();
		final UserOptionsListener userListener = wrapper.getConfiguration().getUserOptionsListener();
		final UserOptionsListener listener = new UserOptionsListener() {

			@Override
			public void doUpdate(boolean laterOnWifi) {
				userListener.doUpdate(laterOnWifi);
				if (laterOnWifi) {
					AutoUpgradeDelegate.this.doUpdateLaterOnWifi(wrapper);
				} else {
					AutoUpgradeDelegate.doUpdate(wrapper);
				}
			}

			@Override
			public void doIgnore() {
				userListener.doIgnore();
				AutoUpgradeDelegate.this.doIgnore(wrapper);
			}
		};
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				try {
					delegate.showFoundLatestVersion(wrapper.getContext(), wrapper.getVersion(), wrapper.isAutoUpdate(), listener);
				} catch (Throwable th) {
					
				}
			}
		});
	}
	
	protected void doUpdateLaterOnWifi(ContextWrapper wrapper) {
		VersionPersistent persistent = wrapper.getConfiguration().getVersionPersistent();
		persistent.save(wrapper.getModule(), wrapper.getContext(), wrapper.getVersion());
		AppUpdateService.show(wrapper.getContext(), R.string.aus__update_version_just_in_wifi, Toast.LENGTH_LONG);
	}

	protected void doIgnore(ContextWrapper wrapper) {
		log.info("doIgnore");
		Version version = wrapper.getVersion();
		UpdatePolicy updatePolicy = version.getUpdatePolicy();
		updatePolicy.getIgnorePolicy().notifyIgnore(wrapper.getContext(), version);
	}
	
	public static void doUpdate(final ContextWrapper wrapper) {
		final DownloadDelegate delegate = wrapper.getConfiguration().getDownloadDelegate();
		final String module = wrapper.getModule();
		final Context context = wrapper.getContext();
		final Version version = wrapper.getVersion();
		
		File file = delegate.getDownloadLocalFile(module, context, version);
		if (file != null && file.exists()) {
			log.debug("getDownloadLocalFile, is not exists");
			checkAndInstallApk(wrapper, file);
		} else {
			Runnable callback = new Runnable() {

				@Override
				public void run() {
					File apkFile = delegate.getDownloadLocalFile(module, context, version);
					checkAndInstallApk(wrapper, apkFile);
				}

			};
			boolean downloadSuccess = false;
			try{
				downloadSuccess = delegate.download(module, context, version, callback, true);
			} catch (Throwable th) {
				
			}
			//如果下载失败就使用浏览器去下载
			if (!downloadSuccess) {
				log.warning("download fail, use BrowserDownloadDelegate to download.");
				new BrowserDownloadDelegate().download(module, context, version, null, false);
			}
		}
	}
	
	public static void checkAndInstallApk(final ContextWrapper wrapper, final File apk) {
		final AppUpdateServiceConfiguration config = wrapper.getConfiguration();
		final Context context=wrapper.getContext();
		final Version version = wrapper.getVersion();
		new AsyncTask<Void, Integer, Boolean>() {

			@Override
			protected Boolean doInBackground(Void... paramArrayOfParams) {
				try {
					log.debug("download success, CheckFile ....");
					CheckFileDelegate delegate = config.getCheckFileDelegate();
					return delegate.doCheck(context, version, apk);
				} catch (Throwable e) {
					log.error("download success, CheckFile fail", e);
					return false;
				}
			}

			@Override
			protected void onPostExecute(Boolean result) {
				if (result != null && result == true) {
					log.info("download success, CheckFile success, start install...");
					installAPK(wrapper, apk);
					config.getVersionPersistent().notifyFinish(wrapper.getModule(), context, version);
				} else {
					if (apk != null) {
						if (!apk.delete()) {
							apk.deleteOnExit();
						}
					}
					log.info("download success, CheckFile not pass, cancel this install, and delete this file.");
					AppUpdateService.show(context, R.string.aus__apk_file_invalid, Toast.LENGTH_LONG);
				}
				super.onPostExecute(result);
			}
			
		}.execute();
		AppUpdateService.show(context, R.string.aus__apk_file_start_valldate, Toast.LENGTH_LONG);
	}
	
	public static void installAPK(ContextWrapper wrapper, File apk) {
		log.info("installAPK : " + apk.getAbsolutePath());
		Context context = wrapper.getContext();
		AppUpdateService.show(context, R.string.aus__start_install, Toast.LENGTH_LONG);
		wrapper.getConfiguration().getInstallExecutor().install(context, apk);
	}
	
}