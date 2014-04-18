package com.yepstudio.android.service.autoupdate.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
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
	private AtomicBoolean isChecking = new AtomicBoolean(false); 
	private boolean responseError = false;
	private boolean parserError = false;

	public void checkUpdate(String module, final Context context, final boolean isAutoUpdate) {
		final AppUpdateServiceConfiguration config = AppUpdateService.getConfiguration(module);
		String tip;
		//如果没有网络
		if (!NetworkUtil.hasNetwork(context)) {
			if (!isAutoUpdate) {
				tip = config.getTip(AppUpdateServiceConfiguration.TIP_KEY_NETWORKNOTACTIVATED);
				showToast(context, tip, Toast.LENGTH_LONG);
			}
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
				processResponseListener(config, context, isAutoUpdate, version);
				isChecking.set(false);
				responseError = false;
				parserError = false;
			}
			
		};
		
		if (!isChecking.getAndSet(true)) {//如果没有检查更新则去检查更新
			log.info("execute check update... isAutoUpdate:" + isAutoUpdate);
			task.execute();
		} else {
			log.info("has check update is running, so skip it. isAutoUpdate:" + isAutoUpdate);
		}
		
		if (!isAutoUpdate) {
			tip = config.getTip(AppUpdateServiceConfiguration.TIP_KEY_CHECK_NEW_VERSION);
			showToast(context, tip, Toast.LENGTH_LONG);
		}
	}
	
	private Version checkUpdateInTask(AppUpdateServiceConfiguration config) {
		String response = null;
		try {
			response = requestUpdateCheck(config.getUpdateUrl(), config.getUserAgent(), config.getRequestParams());
		} catch (Throwable exp) {
			log.error("requestUpdateCheck error", exp);
			responseError = true;
		}
		Version version = null;
		if (!TextUtils.isEmpty(response)) {
			try {
				version = config.getResponseParser().parser(config.getModule(), response);
				log.trace("ResponseParser version success.");
			} catch (Throwable exp) {
				log.error("ResponseParser error", exp);
				parserError = true;
			}
		} else {
			log.info("response isEmpty, no Version to Update");
		}
		//判断是否忽略服务器返回的更新策略
		if (version != null && config.ignoreServerPolicy()) {
			log.info("ignoreServerPolicy");
			version.setUpdatePolicy(config.getUpdatePolicy());
		}
		return version;
	}
	
	private void processResponseListener(AppUpdateServiceConfiguration config, Context context, boolean isAutoUpdate, Version version) {
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
	
	/**
	 * 请求
	 * @param checkUrl
	 * @param userAgent
	 * @return
	 * @throws IOException
	 */
	private String requestUpdateCheck(String updateUrl, String userAgent, Map<String, Object> requestParams) throws IOException {
		log.trace("requestUpdateCheck");
		log.trace("updateUrl:" + updateUrl);
		log.trace("userAgent:" + userAgent);
		log.trace("requestParams:" + requestParams);
		
		URL targetUrl = new URL(updateUrl);
		//设置UserAgent
		HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
		InputStream is = connection.getInputStream();
		String response = toStringBuffer(is).toString();
		log.trace("response:" + response);
		is.close();
		connection.disconnect();
		return response;
	}
	
	/**
	 * 读取输出流
	 * @param is InputStream
	 * @return
	 * @throws IOException
	 */
	private StringBuffer toStringBuffer(InputStream is) throws IOException {
		if (null == is) {
			return null;
		}
		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		StringBuffer buffer = new StringBuffer();
		String line = null;
		while ((line = in.readLine()) != null) {
			buffer.append(line).append("\n");
		}
		is.close();
		return buffer;
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