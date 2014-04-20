package com.yepstudio.android.service.autoupdate;

import java.util.HashMap;
import java.util.Map;

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.yepstudio.android.service.autoupdate.internal.AndroidDownloadDelegate;
import com.yepstudio.android.service.autoupdate.internal.BrowserDownloadDelegate;
import com.yepstudio.android.service.autoupdate.internal.PackageCheckFileDelegate;
import com.yepstudio.android.service.autoupdate.internal.SimpleDisplayDelegate;
import com.yepstudio.android.service.autoupdate.internal.SimpleJSONParser;
import com.yepstudio.android.service.autoupdate.internal.SimpleResponseDelivery;
import com.yepstudio.android.service.autoupdate.internal.SimpleResponseListener;
import com.yepstudio.android.service.autoupdate.internal.SimpleUserOptionsListener;
import com.yepstudio.android.service.autoupdate.internal.SimpleVersionCompare;

/**
 * 
 * @author zhangzl@gmail.com
 * @create 2014年4月17日
 * @version 1.0, 2014年4月17日
 * 
 */
public class AppUpdateServiceConfiguration {
	
	private static AutoUpdateLog log = AutoUpdateLogFactory.getAutoUpdateLog(AppUpdateServiceConfiguration.class);
	
	private String module;
	
	private String updateUrl;
	private String userAgent;
	private Map<String, Object> requestParams;
	private Map<String, String> tip;
	private ResponseDelivery responseDelivery; 
	private ResponseParser responseParser;
	private ResponseListener responseListener;
	private VersionComparer versionCompare;
	private DisplayDelegate displayDelegate;
	private UserOptionsListener userOptionsListener;
	private DownloadDelegate downloadDelegate;
	private CheckFileDelegate checkFileDelegate;
	private UpdatePolicy updatePolicy;
	
	/*** 是否忽略服务器返回的更新策略 ***/
	private boolean ignoreServerPolicy = false;
	/*** 是否忽略Sim卡的信息 ***/
	private boolean ignoreRequestSimInfo = false;
	/*** 是否忽略网络信息 ***/
	private boolean ignoreRequestNetworkInfo = false;
	/*** 是否忽略手机和操作系统的信息 ***/
	private boolean ignoreRequestOSInfo = false;
	/*** 是否忽略App的信息 ***/
	private boolean ignoreRequestAppInfo = false;
	/*** 是否忽略分辨率的信息 ***/
	private boolean ignoreRequestDisplayInfo = false;
	
	/***开始检查新版本的提示**/
	public static final String TIP_KEY_CHECK_NEW_VERSION = "aus__check_new_version";
	/***网络不可用时的提示**/
	public static final String TIP_KEY_NETWORKNOTACTIVATED = "aus__network_not_activated";
	/***请求网络时出错的提示**/
	public static final String TIP_KEY_NETWORK_ERROR = "aus__network_error";
	/***解析请求下载的JSON时出错的提示**/
	public static final String TIP_KEY_PARSER_ERROR = "parser_error";
	/***已经是最新版本的提示**/
	public static final String TIP_KEY_IS_LATEST_VERSION_LABEL = "aus__is_latest_version_label";
	/***有新版本的提示**/
	public static final String TIP_KEY_HAS_NEW_VERSION_LABEL = "aus__has_new_version_label";
	
	
	public static final String PARAM_WIDTH = "width";
	public static final String PARAM_HEIGHT = "height";
	public static final String PARAM_DENSITY = "density";
	public static final String PARAM_DENSITYDPI = "densitydip";
	
	public static final String PARAM_DEVICEID = "deviceId";
	public static final String PARAM_PRODUCT = "product";
	public static final String PARAM_MODEL = "model";
	public static final String PARAM_BRAND = "brand";
	public static final String PARAM_MANUFACTURER = "manufacturer";
	
	public static final String PARAM_OSNAME = "osName";
	public static final String PARAM_SDKRELEASE = "sdkRelease";
	public static final String PARAM_SDKVERSION = "sdkVersion";
	
	public static final String PARAM_NETWORK = "network";
	
	public static final String PARAM_PHONE = "phone";
	public static final String PARAM_PHONETYPE = "phoneType";
	public static final String PARAM_SIMCOUNTRY = "simCountry";
	public static final String PARAM_SIM = "sim";
	public static final String PARAM_SIMNAME = "simName";
	public static final String PARAM_SIMCODE = "simCode";
	
	public static final String PARAM_APPTYPE = "appType";
	public static final String PARAM_APP = "app";
	public static final String PARAM_VERSIONNAME = "versionName";
	public static final String PARAM_VERSIONCODE = "versionCode";
	public static final String PARAM_DEBUG = "debug";
	
	public static final String PARAM_AUTOUPDATE = "autoUpdate";
	
	private AppUpdateServiceConfiguration() {
		requestParams = new HashMap<String, Object>();
		tip = new HashMap<String, String>();
	}
	
	public String getTip(String key) {
		return tip.get(key);
	}
	
	public String getUpdateUrl() {
		return updateUrl;
	}
	
	public String getUserAgent() {
		return userAgent;
	}
	
	public ResponseParser getResponseParser() {
		return responseParser;
	}
	
	public ResponseListener getResponseListener() {
		return responseListener;
	}
	
	public DisplayDelegate getDisplayDelegate() {
		return displayDelegate;
	}
	
	public UserOptionsListener getUserOptionsListener() {
		return userOptionsListener;
	}
	
	public String getModule() {
		return module;
	}
	
	public DownloadDelegate getDownloadDelegate() {
		return downloadDelegate;
	}
	
	public CheckFileDelegate getCheckFileDelegate() {
		return checkFileDelegate;
	}
	
	public Map<String, Object> getRequestParams() {
		return requestParams;
	}
	
	public UpdatePolicy getUpdatePolicy() {
		return updatePolicy;
	}
	
	public boolean ignoreServerPolicy() {
		return ignoreServerPolicy;
	}
	
	public VersionComparer getVersionCompare() {
		return versionCompare;
	}
	
	public void addRequestParam(String key, Object value) {
		if (requestParams == null) {
			requestParams = new HashMap<String, Object>();
		}
		requestParams.put(key, value);
	}
	
	public ResponseDelivery getResponseDelivery() {
		return responseDelivery;
	}

	public static class Build {

		private AppUpdateServiceConfiguration config;
		private boolean useDefaultRequestParams = true;

		public Build() {
			super();
			config = new AppUpdateServiceConfiguration();
		}
		
		public Build setTip(String key, String value) {
			config.tip.put(key, value);
			return this;
		}
		
		public Build setResponseParser(ResponseParser responseParser) {
			config.responseParser = responseParser;
			return this;
		}
		
		public Build setCheckUpdateUrl(String updateUrl) {
			config.updateUrl = updateUrl;
			return this;
		}
		
		public Build setUserAgent(String userAgent) {
			config.userAgent = userAgent;
			return this;
		}
		
		public Build setResponseCallback(ResponseListener responseListener) {
			config.responseListener = responseListener;
			return this;
		}
		
		public Build setDisplayDelegate(DisplayDelegate displayDelegate) {
			config.displayDelegate = displayDelegate;
			return this;
		}
		
		public Build setUserOptionsListener(UserOptionsListener userOptionsListener) {
			config.userOptionsListener = userOptionsListener;
			return this;
		}
		

		public Build setDownloadDelegate(DownloadDelegate downloadDelegate) {
			config.downloadDelegate = downloadDelegate;
			return this;
		}
		
		public Build setCheckFileDelegate(CheckFileDelegate checkFileDelegate) {
			config.checkFileDelegate = checkFileDelegate;
			return this;
		}
		
		public Build setRequestParams(Map<String, Object> requestParams) {
			config.requestParams = requestParams;
			useDefaultRequestParams = false;
			return this;
		}
		
		public Build addRequestParams(String key, String value) {
			config.addRequestParam(key, value);
			return this;
		}
		
		public Build setDefaultRequestParamsIgnore(boolean appInfo, boolean displayInfo, boolean networkInfo, boolean osInfo, boolean simInfo) {
			config.ignoreRequestAppInfo = appInfo;
			config.ignoreRequestDisplayInfo = displayInfo;
			config.ignoreRequestNetworkInfo = networkInfo;
			config.ignoreRequestOSInfo = osInfo;
			config.ignoreRequestSimInfo = simInfo;
			return this;
		}
		
		public Build setResponseDelivery(ResponseDelivery responseDelivery) {
			config.responseDelivery = responseDelivery;
			return this;
		}
		
		public Build setUpdatePolicy(UpdatePolicy updatePolicy) {
			config.updatePolicy = updatePolicy;
			return this;
		}
		
		public Build setVersionCompare(VersionComparer versionCompare) {
			config.versionCompare = versionCompare;
			return this;
		}
		
		public AppUpdateServiceConfiguration create(Context context) {
			if (TextUtils.isEmpty(config.updateUrl)) {
				throw new IllegalArgumentException("AppUpdateServiceConfiguration create fail. updateUrl need be set. setCheckUpdateUrl");
			}
			if (TextUtils.isEmpty(config.userAgent)) {
				setDefaultUserAgent(context);
				log.trace("use default UserAgent : " + config.userAgent);
			}
			
			setDefaultTips(context);
			
			if (config.responseDelivery == null) {
				setResponseDelivery(new SimpleResponseDelivery());
				log.trace("use default ResponseDelivery : " + config.responseDelivery);
			}
			if (config.responseParser == null) {
				setResponseParser(new SimpleJSONParser());
				log.trace("use default ResponseParser : " + config.responseParser);
			}
			if (config.versionCompare == null) {
				setVersionCompare(new SimpleVersionCompare());
				log.trace("use default VersionCompare : " + config.versionCompare);
			}
			if (config.responseListener == null) {
				setResponseCallback(new SimpleResponseListener());
				log.trace("use default ResponseListener : " + config.responseListener);
			}
			if (config.displayDelegate == null) {
				setDisplayDelegate(new SimpleDisplayDelegate());
				log.trace("use default DisplayDelegate : " + config.displayDelegate);
			}
			if (config.userOptionsListener == null) {
				setUserOptionsListener(new SimpleUserOptionsListener());
				log.trace("use default UserOptionsListener : " + config.userOptionsListener);
			}
			if (config.downloadDelegate == null) {
				setDefaultDownloadDelegate(context);
				log.trace("use default DownloadDelegate : " + config.downloadDelegate);
			}
			if (config.checkFileDelegate == null) {
				setCheckFileDelegate(new PackageCheckFileDelegate());
				log.trace("use default CheckFileDelegate : " + config.checkFileDelegate);
			}
			if (useDefaultRequestParams) {
				setDefaultRequestParams(context);
				log.trace("use default UseDefaultRequestParams : " + config.requestParams);
			}
			if (config.updatePolicy == null) {
				setUpdatePolicy(new UpdatePolicy());
				log.trace("default is UpdatePolicy : " + config.updatePolicy);
			}
			return config;
		}
		
		private void setDefaultUserAgent(Context context) {
			String version = "";
			try {
				PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
				version = info.versionName;
			} catch (NameNotFoundException e) {
				log.error("get version name fail");
			}
			setUserAgent(String.format("AppUpdateService(%s,%s)", context.getPackageName(), version));
		}
		
		private void setDefaultRequestParams(Context context) {
			TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			
			//设置分辨率参数
			if (!config.ignoreRequestDisplayInfo) {
				DisplayMetrics dm = context.getResources().getDisplayMetrics();
				config.addRequestParam(PARAM_WIDTH, dm.widthPixels);
				config.addRequestParam(PARAM_HEIGHT, dm.heightPixels);
				config.addRequestParam(PARAM_DENSITY, dm.density);
				config.addRequestParam(PARAM_DENSITYDPI, dm.densityDpi);
			}
			
			//设置手机的信息
			if (!config.ignoreRequestAppInfo) {
				config.addRequestParam(PARAM_DEVICEID, tm.getDeviceId());
				config.addRequestParam(PARAM_PRODUCT, android.os.Build.PRODUCT);
				config.addRequestParam(PARAM_MODEL, android.os.Build.MODEL);
				config.addRequestParam(PARAM_BRAND, android.os.Build.BRAND);
				config.addRequestParam(PARAM_MANUFACTURER, android.os.Build.MANUFACTURER);
			}
			
			//设置操作系统信息
			if (!config.ignoreRequestOSInfo) {
				config.addRequestParam(PARAM_OSNAME, android.os.Build.VERSION.CODENAME);
				config.addRequestParam(PARAM_SDKRELEASE, android.os.Build.VERSION.RELEASE);
				config.addRequestParam(PARAM_SDKVERSION, android.os.Build.VERSION.SDK_INT);
			}
			
			//设置网络信息
			if (!config.ignoreRequestNetworkInfo) {
				config.addRequestParam(PARAM_NETWORK, tm.getNetworkType());
			}
			
			//设置手机号码信息
			if (!config.ignoreRequestSimInfo) {
				config.addRequestParam(PARAM_PHONE, tm.getLine1Number());
				config.addRequestParam(PARAM_PHONETYPE, tm.getPhoneType());
				config.addRequestParam(PARAM_SIMCOUNTRY, tm.getSimCountryIso());
				config.addRequestParam(PARAM_SIM, tm.getSimSerialNumber());
				config.addRequestParam(PARAM_SIMNAME, tm.getSimOperatorName());
				config.addRequestParam(PARAM_SIMCODE, tm.getSimOperator());
			}
			
			//设置App的信息
			if (!config.ignoreRequestAppInfo) {
				config.addRequestParam(PARAM_APPTYPE, "APK");
				config.addRequestParam(PARAM_APP, context.getPackageName());
				try {
					PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
					config.addRequestParam(PARAM_VERSIONCODE, info.versionCode);
					config.addRequestParam(PARAM_VERSIONNAME, info.versionName);
					config.addRequestParam(PARAM_DEBUG, BuildConfig.DEBUG);
				} catch (NameNotFoundException e) {
					log.error("get version name fail");
				}
			}
		}
		
		private void setDefaultTips(Context context) {
			setTipIfEmpty(TIP_KEY_NETWORKNOTACTIVATED, context, R.string.aus__network_not_activated);
			setTipIfEmpty(TIP_KEY_CHECK_NEW_VERSION, context, R.string.aus__check_new_version);
			setTipIfEmpty(TIP_KEY_NETWORK_ERROR, context, R.string.aus__network_error);
			setTipIfEmpty(TIP_KEY_PARSER_ERROR, context, R.string.aus__error_check_update);
			setTipIfEmpty(TIP_KEY_IS_LATEST_VERSION_LABEL, context, R.string.aus__is_latest_version_label);
			setTipIfEmpty(TIP_KEY_HAS_NEW_VERSION_LABEL, context, R.string.aus__has_new_version_label);
		}
		
		@TargetApi(android.os.Build.VERSION_CODES.GINGERBREAD)
		private void setDefaultDownloadDelegate(Context context) {
			DownloadDelegate delegate;
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD) {
				AndroidDownloadDelegate androidDelegate = new AndroidDownloadDelegate();
				delegate = androidDelegate;
				IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
				filter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);
				context.getApplicationContext().registerReceiver(androidDelegate, filter);
			} else {
				delegate = new BrowserDownloadDelegate();
			}
			setDownloadDelegate(delegate);
		}
		
		private void setTipIfEmpty(String key, String defaultTip) {
			if (TextUtils.isEmpty(config.tip.get(key))) {
				log.trace("use default tip for " + key + " => " + defaultTip);
				config.tip.put(key, defaultTip);
			}
		}
		
		private void setTipIfEmpty(String key, Context context, int resId) {
			setTipIfEmpty(key, context.getString(resId));
		}
	}
	
	public void setModule(String module) {
		this.module = module;
	}

}
