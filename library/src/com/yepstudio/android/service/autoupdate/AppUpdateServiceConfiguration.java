package com.yepstudio.android.service.autoupdate;

import java.util.HashMap;
import java.util.Map;

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;

import com.yepstudio.android.service.autoupdate.internal.AndroidDownloadDelegate;
import com.yepstudio.android.service.autoupdate.internal.ApkInstallExecutor;
import com.yepstudio.android.service.autoupdate.internal.AutoUpgradeDelegate;
import com.yepstudio.android.service.autoupdate.internal.BrowserDownloadDelegate;
import com.yepstudio.android.service.autoupdate.internal.FileCheckFileDelegate;
import com.yepstudio.android.service.autoupdate.internal.SharedPreferencesVersionPersistent;
import com.yepstudio.android.service.autoupdate.internal.SimpleDisplayDelegate;
import com.yepstudio.android.service.autoupdate.internal.SimpleResponseDelivery;
import com.yepstudio.android.service.autoupdate.internal.SimpleResponseListener;
import com.yepstudio.android.service.autoupdate.internal.SimpleUserOptionsListener;
import com.yepstudio.android.service.autoupdate.internal.SimpleVersionCompare;

/**
 * 
 * @author zzljob@gmail.com
 * @create 2014年4月17日
 * @version 1.0, 2014年4月17日
 * 
 */
public class AppUpdateServiceConfiguration {
	
	private static AutoUpdateLog log = AutoUpdateLogFactory.getAutoUpdateLog(AppUpdateServiceConfiguration.class);
	
	private final AppUpdate appUpdate;
	private final RequestInfo requestInfo;
	private final ResponseDelivery<?> responseDelivery;
	private final ResponseListener responseListener;
	private final VersionComparer versionCompare;
	private final DisplayDelegate displayDelegate;
	private final UserOptionsListener userOptionsListener;
	private final DownloadDelegate downloadDelegate;
	private final CheckFileDelegate checkFileDelegate;
	private final VersionPersistent versionPersistent;
	private final UpdatePolicy updatePolicy;
	private final InstallExecutor installExecutor;
	/*** 是否忽略服务器返回的更新策略 ***/
	private final boolean ignoreServerPolicy;

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
	/***有新版本的提示**/
	public static final String TIP_KEY_NEW_VERSION_DOWNLOADING = "tip_key_new_version_downloading";
	/***在有网络的时候突然进行更新的提示**/
	public static final String TIP_KEY_LATER_UPDATE_TIP = "tip_key_later_update_tip";
	
	private AppUpdateServiceConfiguration(RequestInfo requestInfo,
			ResponseDelivery<?> responseDelivery, AppUpdate appUpdate,
			ResponseListener responseListener, VersionComparer versionCompare,
			DisplayDelegate displayDelegate,
			UserOptionsListener userOptionsListener,
			DownloadDelegate downloadDelegate,
			CheckFileDelegate checkFileDelegate,
			VersionPersistent versionPersistent, UpdatePolicy updatePolicy,
			InstallExecutor installExecutor, boolean ignoreServerPolicy) {
		super();
		this.requestInfo = requestInfo;
		this.responseDelivery = responseDelivery;
		this.appUpdate = appUpdate;
		this.responseListener = responseListener;
		this.versionCompare = versionCompare;
		this.displayDelegate = displayDelegate;
		this.userOptionsListener = userOptionsListener;
		this.downloadDelegate = downloadDelegate;
		this.checkFileDelegate = checkFileDelegate;
		this.versionPersistent = versionPersistent;
		this.updatePolicy = updatePolicy;
		this.installExecutor = installExecutor;
		this.ignoreServerPolicy = ignoreServerPolicy;
	}

	public static class Build {
		
		private String updateUrl;
		private String userAgent;
		private String method;
		private Map<String, Object> requestParams;

		private AppUpdate appUpdate;
		private RequestInfo requestInfo;
		private ResponseDelivery<?> responseDelivery;
		private ResponseListener responseListener;
		private VersionComparer versionCompare;
		private DisplayDelegate displayDelegate;
		private UserOptionsListener userOptionsListener;
		private DownloadDelegate downloadDelegate;
		private CheckFileDelegate checkFileDelegate;
		private VersionPersistent versionPersistent;
		private UpdatePolicy updatePolicy;
		private InstallExecutor installExecutor;
		
		private boolean ignoreServerPolicy;

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
		
		protected RequestInfo getDefaultRequestInfo(Context context) {
			RequestInfo info = new RequestInfo();
			if (TextUtils.isEmpty(updateUrl)) {
				throw new IllegalArgumentException("AppUpdateServiceConfiguration create fail. updateUrl need be set. setCheckUpdateUrl");
			}
			info.setUpdateUrl(updateUrl);

			if (TextUtils.isEmpty(userAgent)) {
				userAgent = getDefaultUserAgent(context);
				log.trace("use default userAgent : " + userAgent);
			}
			info.getRequestHeaders().put(RequestInfo.HEADER_UserAgent, userAgent);
			
			if (TextUtils.isEmpty(method)) {
				method = "GET";
				log.trace("use default Method : " + method);
			}
			info.setMethod(method);
			if (requestParams == null) {
				requestParams = new HashMap<String, Object>();
			}
			info.setRequestParams(requestParams);
			info.applyDeviceInfo(context);
			if (!ignoreRequestSimInfo) {
				info.applySimInfo(context);
			}
			if (!ignoreRequestNetworkInfo) {
				info.applyNetworkInfo(context);
			}
			if (!ignoreRequestOSInfo) {
				info.applyAndroidInfo(context);
			}
			if (!ignoreRequestAppInfo) {
				info.applyAppInfo(context);
			}
			if (!ignoreRequestDisplayInfo) {
				info.applyDisplayInfo(context);
			}
			return info;
		}
		
		public AppUpdateServiceConfiguration create(Context context) {
			if (requestInfo == null) {
				requestInfo = getDefaultRequestInfo(context);
			}
			if (appUpdate == null) {
				appUpdate = new AutoUpgradeDelegate();
			}
			if (responseDelivery == null) {
				responseDelivery = new SimpleResponseDelivery();
			}
			if (versionCompare == null) {
				versionCompare = new SimpleVersionCompare();
			}
			if (responseListener == null) {
				responseListener = new SimpleResponseListener();
			}
			if (displayDelegate == null) {
				displayDelegate = new SimpleDisplayDelegate();
			}
			if (userOptionsListener == null) {
				userOptionsListener = new SimpleUserOptionsListener();
			}
			if (downloadDelegate == null) {
				downloadDelegate = getDefaultDownloadDelegate(context);
			}
			if (checkFileDelegate == null) {
				checkFileDelegate = new FileCheckFileDelegate();
			}
			if (updatePolicy == null) {
				updatePolicy = new UpdatePolicy();
			}
			if (versionPersistent == null) {
				versionPersistent = new SharedPreferencesVersionPersistent();
			}
			if (installExecutor == null) {
				installExecutor = new ApkInstallExecutor();
			}
			return new AppUpdateServiceConfiguration(requestInfo,
					responseDelivery, appUpdate, responseListener,
					versionCompare, displayDelegate, userOptionsListener,
					downloadDelegate, checkFileDelegate, versionPersistent,
					updatePolicy, installExecutor, ignoreServerPolicy);
		}
		
		public Build setAppUpdate(AppUpdate appUpdate) {
			this.appUpdate = appUpdate;
			return this;
		}
		
		public Build setCheckUpdateUrl(String updateUrl) {
			this.updateUrl = updateUrl;
			return this;
		}
		
		public Build setUserAgent(String userAgent) {
			this.userAgent = userAgent;
			return this;
		}

		public Build setMethod(String method) {
			this.method = method;
			return this;
		}
		
		public Build addRequestParams(String key, Object value) {
			if (requestParams == null) {
				requestParams = new HashMap<String, Object>();
			}
			requestParams.put(key, value);
			return this;
		}
		
		public Build setResponseCallback(ResponseListener responseListener) {
			this.responseListener = responseListener;
			return this;
		}
		
		public Build setDisplayDelegate(DisplayDelegate displayDelegate) {
			this.displayDelegate = displayDelegate;
			return this;
		}
		
		public Build setUserOptionsListener(UserOptionsListener userOptionsListener) {
			this.userOptionsListener = userOptionsListener;
			return this;
		}
		

		public Build setDownloadDelegate(DownloadDelegate downloadDelegate) {
			this.downloadDelegate = downloadDelegate;
			return this;
		}
		
		public Build setCheckFileDelegate(CheckFileDelegate checkFileDelegate) {
			this.checkFileDelegate = checkFileDelegate;
			return this;
		}
		
		public Build setDefaultRequestParamsIgnore(boolean appInfo, boolean displayInfo, boolean networkInfo, boolean osInfo, boolean simInfo) {
			this.ignoreRequestAppInfo = appInfo;
			this.ignoreRequestDisplayInfo = displayInfo;
			this.ignoreRequestNetworkInfo = networkInfo;
			this.ignoreRequestOSInfo = osInfo;
			this.ignoreRequestSimInfo = simInfo;
			return this;
		}
		
		public Build setResponseDelivery(ResponseDelivery<?> responseDelivery) {
			this.responseDelivery = responseDelivery;
			return this;
		}
		
		public Build setUpdatePolicy(UpdatePolicy updatePolicy) {
			this.updatePolicy = updatePolicy;
			return this;
		}
		
		public Build setVersionCompare(VersionComparer versionCompare) {
			this.versionCompare = versionCompare;
			return this;
		}
		
		public Build setVersionPersistent(VersionPersistent versionPersistent) {
			this.versionPersistent = versionPersistent;
			return this;
		}
		
		public Build setInstallExecutor(InstallExecutor installDelegate) {
			this.installExecutor = installDelegate;
			return this;
		}
		
		private String getDefaultUserAgent(Context context) {
			String version = "";
			try {
				PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
				version = info.versionName;
			} catch (NameNotFoundException e) {
				log.error("get version name fail");
			}
			return String.format("AppUpdateService(%s,%s)", context.getPackageName(), version);
		}
		
		@TargetApi(android.os.Build.VERSION_CODES.GINGERBREAD)
		private DownloadDelegate getDefaultDownloadDelegate(Context context) {
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
			return delegate;
		}
		
	}

	public AppUpdate getAppUpdate() {
		return appUpdate;
	}

	public RequestInfo getRequestInfo() {
		return requestInfo;
	}

	public ResponseDelivery<?> getResponseDelivery() {
		return responseDelivery;
	}

	public ResponseListener getResponseListener() {
		return responseListener;
	}

	public VersionComparer getVersionCompare() {
		return versionCompare;
	}

	public DisplayDelegate getDisplayDelegate() {
		return displayDelegate;
	}

	public UserOptionsListener getUserOptionsListener() {
		return userOptionsListener;
	}

	public DownloadDelegate getDownloadDelegate() {
		return downloadDelegate;
	}

	public CheckFileDelegate getCheckFileDelegate() {
		return checkFileDelegate;
	}

	public VersionPersistent getVersionPersistent() {
		return versionPersistent;
	}

	public UpdatePolicy getUpdatePolicy() {
		return updatePolicy;
	}

	public InstallExecutor getInstallExecutor() {
		return installExecutor;
	}

	public boolean isIgnoreServerPolicy() {
		return ignoreServerPolicy;
	}

}
