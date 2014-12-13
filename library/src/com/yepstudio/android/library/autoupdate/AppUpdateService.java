package com.yepstudio.android.library.autoupdate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

import com.yepstudio.android.library.autoupdate.R;
import com.yepstudio.android.library.autoupdate.internal.AutoUpgradeDelegate;
import com.yepstudio.android.library.autoupdate.internal.ContextWrapper;
import com.yepstudio.android.library.autoupdate.internal.NetworkUtil;

/**
 * 
 * @author zzljob@gmail.com
 * @create 2014年4月17日
 * @version 1.0, 2014年4月17日
 *
 */
public class AppUpdateService {
	
	private static AutoUpdateLog log = AutoUpdateLogFactory.getAutoUpdateLog(AppUpdateService.class);
	private static Map<String, AppUpdateServiceConfiguration> configurationMap;
	private static BroadcastReceiver networkStateReceiver;
	private static Toast toast;
	
	public static void show(Context context, int msgResId, int duration) {
		show(context, context.getString(msgResId), duration);
	}
	
	public static void show(final Context context, final String msg, final int duration) {
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				if (toast == null) {
					toast = Toast.makeText(context, msg, duration);
				}
				toast.setText(msg);
				toast.setDuration(duration);
				toast.show();
			}
		});
	}
	
	public static void init(Context context, AppUpdateServiceConfiguration config) {
		String module = null;
		if (context != null) {
			module = context.getPackageName();
		}
		init(context, module, config);
	}
	
	public static void checkUpdate(Context context, boolean isAutoUpdate) {
		String module = null;
		if (context != null) {
			module = context.getPackageName();
		}
		checkUpdate(context, module, isAutoUpdate);
	}
	
	public static void init(Context context, String module, AppUpdateServiceConfiguration config) {
		if(context == null || TextUtils.isEmpty(module) || config == null) {
			throw new IllegalArgumentException("AppUpdateService init fail. Context，module，config can not be initialized with null Or Empty.");
		}
		log.info("init module : " + module);
		if (configurationMap == null) {
			configurationMap = new ConcurrentHashMap<String, AppUpdateServiceConfiguration>(3);
		}
		if (configurationMap.get(module) != null) {
			throw new IllegalArgumentException("AppUpdateService init just one time.");
		}
		configurationMap.put(module, config);
		if (networkStateReceiver == null) {
			networkStateReceiver = new NetworkStateReceiver();
			IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
			context.getApplicationContext().registerReceiver(networkStateReceiver, filter);
		}
	}
	
	public static void checkUpdate(Context context, String module, boolean isAutoUpdate) {
		log.info("checkUpdate module : " + module + ", isAutoUpdate : " + isAutoUpdate);
		AppUpdateServiceConfiguration configuration = configurationMap.get(module);
		ContextWrapper wrapper = new ContextWrapper(configuration, module, context, isAutoUpdate);
		AppUpdate appUpdate = configuration.getAppUpdate();
		appUpdate.checkUpdate(wrapper);
	}
	
	public static AppUpdateServiceConfiguration getConfiguration(String module) {
		if (TextUtils.isEmpty(module)) {
			return null;
		}
		if (configurationMap != null) {
			return configurationMap.get(module);
		}
		return null;
	}
	
	private static class NetworkStateReceiver extends BroadcastReceiver {
		private static AutoUpdateLog logger = AutoUpdateLogFactory.getAutoUpdateLog(NetworkStateReceiver.class);
		
		@Override
		public void onReceive(Context context, Intent intent) {
			log.info("NetworkStateReceiver onReceive");
			if (intent == null) {
				return;
			}
			if (NetworkUtil.getNetworkType(context) == NetworkUtil.WIFI) {// 只有在WIFI的情况下才会去执行
				logger.debug("has WIFI, start download...");
				for (String module : configurationMap.keySet()) {
					AppUpdateServiceConfiguration config = getConfiguration(module);
					ContextWrapper wrapper = new ContextWrapper(config, module, context, false);
					
					Version version = config.getVersionPersistent().load(module, context);
					wrapper.setVersion(version);
					
					if (config.getVersionCompare().compare(context, version)) {
						AutoUpgradeDelegate.doUpdate(wrapper);
						show(context, R.string.aus__later_update_tip, Toast.LENGTH_LONG);
						config.getVersionPersistent().notifyFinish(module, context, version);
					}
				}
			}
		}
	}
	
}
