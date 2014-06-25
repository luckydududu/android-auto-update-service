package com.yepstudio.android.library.autoupdate;

import java.util.HashMap;
import java.util.Map;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import com.yepstudio.android.library.autoupdate.BuildConfig;

/**
 * 
 * @author zzljob@gmail.com
 * @create 2014年6月16日
 * @version 1.0, 2014年6月16日
 *
 */
public class RequestInfo {
	
	public static final String HEADER_UserAgent = "User-Agent";
	
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
	
	public static final String PARAM_WIFIMAC= "wifiMac";
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
	
	private String updateUrl;
	private String method;
	private Map<String, Object> requestHeaders;
	private Map<String, Object> requestParams;
	
	public void applyAppInfo(Context context) {
		getRequestParams().put(PARAM_APPTYPE, "APK");
		getRequestParams().put(PARAM_APP, context.getPackageName());
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			getRequestParams().put(PARAM_VERSIONCODE, info.versionCode);
			getRequestParams().put(PARAM_VERSIONNAME, info.versionName);
			getRequestParams().put(PARAM_DEBUG, BuildConfig.DEBUG);
		} catch (NameNotFoundException e) {
			
		}
	}
	
	public void applyDeviceInfo(Context context) {
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		getRequestParams().put(PARAM_DEVICEID, tm.getDeviceId());
		getRequestParams().put(PARAM_PRODUCT, android.os.Build.PRODUCT);
		getRequestParams().put(PARAM_MODEL, android.os.Build.MODEL);
		getRequestParams().put(PARAM_BRAND, android.os.Build.BRAND);
		getRequestParams().put(PARAM_MANUFACTURER, android.os.Build.MANUFACTURER);
	}

	public void applyAndroidInfo(Context context) {
		getRequestParams().put(PARAM_OSNAME, android.os.Build.VERSION.CODENAME);
		getRequestParams().put(PARAM_SDKRELEASE, android.os.Build.VERSION.RELEASE);
		getRequestParams().put(PARAM_SDKVERSION, android.os.Build.VERSION.SDK_INT);
	}
	
	public void applySimInfo(Context context) {
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		getRequestParams().put(PARAM_PHONE, tm.getLine1Number());
		getRequestParams().put(PARAM_PHONETYPE, tm.getPhoneType());
		getRequestParams().put(PARAM_SIMCOUNTRY, tm.getSimCountryIso());
		getRequestParams().put(PARAM_SIM, tm.getSimSerialNumber());
		getRequestParams().put(PARAM_SIMNAME, tm.getSimOperatorName());
		getRequestParams().put(PARAM_SIMCODE, tm.getSimOperator());	
	}
	
	public void applyNetworkInfo(Context context) {
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		getRequestParams().put(PARAM_NETWORK, tm.getNetworkType());
		
		WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	    WifiInfo info = wifi.getConnectionInfo();
	    getRequestParams().put(PARAM_WIFIMAC, info.getMacAddress());	
	}
	
	public void applyDisplayInfo(Context context) {
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		getRequestParams().put(PARAM_WIDTH, dm.widthPixels);
		getRequestParams().put(PARAM_HEIGHT, dm.heightPixels);
		getRequestParams().put(PARAM_DENSITY, dm.density);
		getRequestParams().put(PARAM_DENSITYDPI, dm.densityDpi);
	}

	public String getUpdateUrl() {
		return updateUrl;
	}

	public String getMethod() {
		return method;
	}

	public Map<String, Object> getRequestHeaders() {
		if(requestHeaders == null){
			requestHeaders = new HashMap<String, Object>();
		}
		return requestHeaders;
	}

	public Map<String, Object> getRequestParams() {
		if(requestParams == null){
			requestParams = new HashMap<String, Object>();
		}
		return requestParams;
	}

	public void setUpdateUrl(String updateUrl) {
		this.updateUrl = updateUrl;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public void setRequestHeaders(Map<String, Object> requestHeaders) {
		this.requestHeaders = requestHeaders;
	}

	public void setRequestParams(Map<String, Object> requestParams) {
		this.requestParams = requestParams;
	}

}
