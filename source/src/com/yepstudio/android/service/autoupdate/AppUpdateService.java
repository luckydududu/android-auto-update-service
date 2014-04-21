package com.yepstudio.android.service.autoupdate;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.yepstudio.android.service.autoupdate.internal.AutoUpgradeDelegate;
import com.yepstudio.android.service.autoupdate.internal.NetworkUtil;

/**
 * 
 * @author zhangzl@gmail.com
 * @create 2014年4月17日
 * @version 1.0, 2014年4月17日
 *
 */
public class AppUpdateService {
	
	private static AutoUpdateLog log = AutoUpdateLogFactory.getAutoUpdateLog(AppUpdateService.class);
	private static Map<String, AppUpdateServiceConfiguration> configurationMap;
	private static Class<? extends AppUpdate> clazz;
	private static Map<String, SoftReference<AppUpdate>> appUpdateMap;
	private static BroadcastReceiver networkStateReceiver;
	
	public static void setAutoUpgradeDelegate(Class<? extends AppUpdate> delegate) {
		clazz = delegate;
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
		config.setModule(module);
		configurationMap.put(module, config);
		if (networkStateReceiver == null) {
			networkStateReceiver = new NetworkStateReceiver();
			IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
			context.getApplicationContext().registerReceiver(networkStateReceiver, filter);
		}
	}
	
	public static void checkUpdate(Context context, String module, boolean isAutoUpdate) {
		log.info("checkUpdate module : " + module + ", isAutoUpdate : " + isAutoUpdate);
		if(appUpdateMap == null) {
			appUpdateMap = new HashMap<String, SoftReference<AppUpdate>>();
		}
		AppUpdate appUpdate = null;
		SoftReference<AppUpdate> value = appUpdateMap.get(module);
		if (value != null && value.get() != null) {
			appUpdate = value.get();
		}
		if (appUpdate == null) {
			if (clazz == null) {
				clazz = AutoUpgradeDelegate.class;
			}
			try {
				appUpdate = clazz.newInstance();
			} catch (InstantiationException e) {
				throw new RuntimeException("clazz can not be  Instance.");
			} catch (IllegalAccessException e) {
				throw new RuntimeException("clazz can not be  Instance.");
			}
			AppUpdateServiceConfiguration config = getConfiguration(module);
			if (config == null) {
				throw new RuntimeException("AppUpdateService.init be call for this module.");
			}
			config.addRequestParam(AppUpdateServiceConfiguration.PARAM_AUTOUPDATE, isAutoUpdate);
			appUpdateMap.put(module, new SoftReference<AppUpdate>(appUpdate));
		}
		appUpdate.checkUpdate(module, context.getApplicationContext(), isAutoUpdate);
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
		@Override
		public void onReceive(Context context, Intent intent) {
			log.info("NetworkStateReceiver onReceive");
			if (intent == null) {
				return;
			}
			if (NetworkUtil.getNetworkType(context) == NetworkUtil.WIFI) {// 只有在WIFI的情况下才会去执行
				log.debug("has WIFI, start download...");
				Toast toast = null;
				String tip;
				for (String key : configurationMap.keySet()) {
					AppUpdateServiceConfiguration config = getConfiguration(key);
					tip = config.getTip(AppUpdateServiceConfiguration.TIP_KEY_LATER_UPDATE_TIP);
					if (toast != null) {
						toast.cancel();
					}
					toast = Toast.makeText(context, tip, Toast.LENGTH_LONG);
					toast.show();
					Version version = config.getVersionPersistent().load(key, context);
					if (config.getVersionCompare().compare(key, context, version)) {
						config.getUserOptionsListener().doUpdate(key, context, version, false);
						config.getVersionPersistent().notifyFinish(config.getModule(), context, version);
					}
				}
			}
		}
	}
	
}
