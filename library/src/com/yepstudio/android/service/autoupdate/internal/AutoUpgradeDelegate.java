package com.yepstudio.android.service.autoupdate.internal;

import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.Toast;

import com.yepstudio.android.service.autoupdate.AppUpdate;
import com.yepstudio.android.service.autoupdate.AppUpdateService;
import com.yepstudio.android.service.autoupdate.AppUpdateServiceConfiguration;
import com.yepstudio.android.service.autoupdate.AutoUpdateLog;
import com.yepstudio.android.service.autoupdate.AutoUpdateLogFactory;
import com.yepstudio.android.service.autoupdate.ResponseListener;
import com.yepstudio.android.service.autoupdate.Version;

/**
 * 
 * @author zhangzl@gmail.com
 * @create 2014年4月17日
 * @version 1.0, 2014年4月17日
 *
 */
public class AutoUpgradeDelegate implements AppUpdate {
	
	private static AutoUpdateLog log = AutoUpdateLogFactory.getAutoUpdateLog(AutoUpgradeDelegate.class);
	
	private Toast toast;
	protected AtomicBoolean isChecking = new AtomicBoolean(false); 
	protected boolean responseError = false;
	protected boolean parserError = false;

	public void checkUpdate(String module, final Context context, final boolean isAutoUpdate) {
		final AppUpdateServiceConfiguration config = AppUpdateService.getConfiguration(module);
		if (!canStartUpdateCheck(config, context, isAutoUpdate)) {
			log.info("update stop, hasNetwork : false, isAutoUpdate:" + isAutoUpdate);
			return;
		}
		
		// 判断是不是正在检查
		AsyncTask<Void, Integer, Version> task = new AsyncTask<Void, Integer, Version>() {

			@Override
			protected Version doInBackground(Void... args) {
				return checkUpdateInTask(config);
			}

			@Override
			protected void onPostExecute(Version version) {
				processUpdatePolicyOfServer(config, version, isAutoUpdate);
				processResponseListener(config, context, isAutoUpdate, version);
				finishUpdateCheck();
			}
			
		};
		
		startUpdateCheck(config, context, isAutoUpdate);
		if (!isChecking.getAndSet(true)) {//如果没有检查更新则去检查更新
			log.info("execute check update... isAutoUpdate:" + isAutoUpdate);
			task.execute();
		} else {
			log.info("has check update is running, so skip it. isAutoUpdate:" + isAutoUpdate);
		}
	}

	protected boolean canStartUpdateCheck(AppUpdateServiceConfiguration config, Context context, boolean isAutoUpdate) {
		log.trace("canStartUpdateCheck...");
		if (!NetworkUtil.hasNetwork(context)) {
			log.trace("has not Network stop UpdateCheck.");
			if (!isAutoUpdate) {
				String tip = config.getTip(AppUpdateServiceConfiguration.TIP_KEY_NETWORKNOTACTIVATED);
				showToast(context, tip, Toast.LENGTH_LONG);
			}
			return false;
		}
		return true;
	}
	
	protected void startUpdateCheck(AppUpdateServiceConfiguration config, Context context, boolean isAutoUpdate) {
		log.trace("startUpdateCheck...");
		if (!isAutoUpdate) {
			String tip = config.getTip(AppUpdateServiceConfiguration.TIP_KEY_CHECK_NEW_VERSION);
			showToast(context, tip, Toast.LENGTH_LONG);
		}
	}
	
	protected void finishUpdateCheck() {
		log.trace("finishUpdateCheck.");
		isChecking.set(false);
		responseError = false;
		parserError = false;
	}
	
	protected Version checkUpdateInTask(AppUpdateServiceConfiguration config) {
		log.trace("checkUpdateInTask...");
		String response = null;
		try {
			response = config.getResponseDelivery().submitRequest(config.getUpdateUrl(), "GET", config.getUserAgent(), config.getRequestParams());
		} catch (Throwable exp) {
			log.error("requestUpdateCheck error", exp);
			responseError = true;
		}
		Version version = null;
		if (!TextUtils.isEmpty(response)) {
			try {
				version = config.getResponseParser().parser(response);
				log.trace("ResponseParser version success.");
			} catch (Throwable exp) {
				log.error("ResponseParser error", exp);
				parserError = true;
			}
		} else {
			log.info("response isEmpty, no Version to Update");
		}
		
		return version;
	}
	
	protected Version processUpdatePolicyOfServer(AppUpdateServiceConfiguration config, Version version, boolean isAutoUpdate) {
		log.trace("processUpdatePolicyOfServer");
		// 判断是否忽略服务器返回的更新策略
		if (version != null && config.ignoreServerPolicy()) {
			log.info("ignoreServerPolicy");
			version.setUpdatePolicy(config.getUpdatePolicy());
		}
		return version;
	}
	
	protected void processResponseListener(AppUpdateServiceConfiguration config, Context context, boolean isAutoUpdate, Version version) {
		log.trace("processResponseListener");
		ResponseListener resListener = config.getResponseListener();
		String tip;
		if (responseError) {
			if (!resListener.onResponseError(config.getModule(), context, isAutoUpdate)) {
				tip = config.getTip(AppUpdateServiceConfiguration.TIP_KEY_NETWORK_ERROR);
				showToast(context, tip, Toast.LENGTH_LONG);
			}
		}
		if (parserError) {
			if (!resListener.onParserError(config.getModule(), context, isAutoUpdate)) {
				tip = config.getTip(AppUpdateServiceConfiguration.TIP_KEY_PARSER_ERROR);
				showToast(context, tip, Toast.LENGTH_LONG);
			}
		}
		if (!parserError && !parserError) {
			// 返回解析并且解析成功
			if (config.getVersionCompare().compare(config.getModule(), context, version, isAutoUpdate)) {
				if (!resListener.onFoundLatestVersion(config.getModule(), context, version, isAutoUpdate)) {
					config.getDisplayDelegate().showFoundLatestVersion(config.getModule(), context, version, isAutoUpdate);
				}
			} else {
				if (!resListener.onCurrentIsLatest(config.getModule(), context, isAutoUpdate)) {
					tip = config.getTip(AppUpdateServiceConfiguration.TIP_KEY_IS_LATEST_VERSION_LABEL);
					showToast(context, tip, Toast.LENGTH_LONG);
				}
			}
		}
	}
	
	private void showToast(Context context, String text, int duration) {
		if (!TextUtils.isEmpty(text)) {
			if (toast != null) {
				toast.cancel();
			}
			toast = Toast.makeText(context, text, duration);
			toast.show();
		}
	}
}