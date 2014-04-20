package com.yepstudio.android.service.autoupdate.internal;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.yepstudio.android.service.autoupdate.AppUpdateService;
import com.yepstudio.android.service.autoupdate.AppUpdateServiceConfiguration;
import com.yepstudio.android.service.autoupdate.AutoUpdateLog;
import com.yepstudio.android.service.autoupdate.AutoUpdateLogFactory;
import com.yepstudio.android.service.autoupdate.ResponseListener;
import com.yepstudio.android.service.autoupdate.UpdatePolicy;
import com.yepstudio.android.service.autoupdate.Version;

/**
 * 
 * @author zhangzl@gmail.com
 * @create 2014年4月17日
 * @version 1.0, 2014年4月17日
 *
 */
public class SimpleResponseListener implements ResponseListener {
	
	private static AutoUpdateLog log = AutoUpdateLogFactory.getAutoUpdateLog(SimpleResponseListener.class);

	@Override
	public boolean onFoundLatestVersion(String module, Context context, Version version, boolean isAutoUpdate) {
		AppUpdateServiceConfiguration config = AppUpdateService.getConfiguration(module);
		UpdatePolicy updatePolicy = version.getUpdatePolicy();
		log.trace("use version UpdatePolicy.");
		if (updatePolicy == null) {
			log.debug("version UpdatePolicy is null, so use  AppUpdateServiceConfiguration's UpdatePolicy.");
			updatePolicy = config.getUpdatePolicy();
		}
		
		String text = String.format("isIgnoreAutoUpdate:%s, isAutoUpdate:%s", updatePolicy.isIgnoreAutoUpdate(), isAutoUpdate);
		log.debug(text);
		//是否忽略自动升级的版本
		if (updatePolicy.isIgnoreAutoUpdate()) {
			if (isAutoUpdate) {
				log.info("onFoundLatestVersion be ignore IgnoreAutoUpdate.");
				return true;
			}
		}
		
		text = String.format("IgnorePolicy:%s", updatePolicy.getIgnorePolicy());
		log.trace(text);
		//检查用户是否点击过忽略该版本，如果忽略过，就要检查忽略策略，判断是否要忽略
		if (updatePolicy.getIgnorePolicy().isIgnore(context, version, isAutoUpdate)) {
			if (!isAutoUpdate) {
				text = config.getTip(AppUpdateServiceConfiguration.TIP_KEY_HAS_NEW_VERSION_LABEL);
				Toast.makeText(context, text, Toast.LENGTH_LONG).show();
			}
			log.info("onFoundLatestVersion be ignore by IgnorePolicy.");
			return true;
		}
		
		//判断是否有版本正在下载
		if (config.getDownloadDelegate().isDownloading(module, context, version)) {
			log.info("onFoundLatestVersion be ignore. this version isDownloading...");
			if (!isAutoUpdate) {
				text = config.getTip(AppUpdateServiceConfiguration.TIP_KEY_NEW_VERSION_DOWNLOADING);
				Toast.makeText(context, text, Toast.LENGTH_LONG).show();
			}
			return true;
		}
		
		//自动更新 并且 不在WIFI情况下
		if(isAutoUpdate && NetworkUtil.getNetworkType(context) != NetworkUtil.WIFI) {
			log.debug("isAutoUpdate and No Wifi, check update is member download when WIFI.");
			//判断下 用户是否选择过稍候在Wifi下面下载
			Version saveVersion = config.getVersionPersistent().load(module, context);
			log.trace("version's UniqueIdentity : " + version.getUniqueIdentity());
			log.trace("saveVersion's UniqueIdentity : " + (saveVersion != null ? saveVersion.getUniqueIdentity() : "null"));
			if (saveVersion != null && TextUtils.equals(saveVersion.getUniqueIdentity(), version.getUniqueIdentity())) {
				log.info("onFoundLatestVersion be ignore. this version user opt update in WIFI...");
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public boolean onCurrentIsLatest(String module, Context context, boolean isAutoUpdate) {
		return isAutoUpdate;
	}

	@Override
	public boolean onResponseError(String module, Context context, boolean isAutoUpdate) {
		return isAutoUpdate;
	}

	@Override
	public boolean onParserError(String module, Context context, boolean isAutoUpdate) {
		return isAutoUpdate;
	}

}
