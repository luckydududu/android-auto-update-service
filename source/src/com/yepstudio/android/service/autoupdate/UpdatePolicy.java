package com.yepstudio.android.service.autoupdate;

import java.io.Serializable;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.DateUtils;

/**
 * 更新策略
 * @author zhangzl@gmail.com
 * @create 2014年4月18日
 * @version 1.0, 2014年4月18日
 *
 */
public class UpdatePolicy implements Serializable {
	
	private static AutoUpdateLog log = AutoUpdateLogFactory.getAutoUpdateLog(UpdatePolicy.class);
	private static final long serialVersionUID = -3662004012924347765L;
	
	/*** 是否忽略自动升级的版本 ***/
	private boolean ignoreAutoUpdate = false;
	/*** 版本是否可以被忽略 ***/
	private boolean versionCanIgnored = true;
	/*** 是否要现在在Wifi下更新，这个只在手机处于3G网的时候使用 ***/
	private boolean hasUpdateInWifi = true;
	/*** 是否自动打开Wifi，这个只在手机处于3G网的时候使用 ***/
	private boolean autoOpenWifi = false;
	/*** 点击忽略的时候，忽略的策略 ***/
	private IgnorePolicy ignorePolicy = JUST_THIS_LAUNCHER;

	public interface IgnorePolicy {
		/**
		 * 判断是否要忽略该版本
		 * @param version
		 * @param isAutoUpdate
		 * @return
		 */
		public boolean isIgnore(Context context, Version version, boolean isAutoUpdate);
		
		/**
		 * 
		 * @param module
		 * @param context
		 * @param version
		 */
		public void notifyIgnore(Context context, Version version);

	}

	public static IgnorePolicy TODAY_NO_SHOW = new IgnorePolicy() {
		
		private static final String SAVE_NAME = "TODAY_NO_SHOW";

		@Override
		public boolean isIgnore(Context context, Version version, boolean isAutoUpdate) {
			boolean result = false;
			if (isAutoUpdate) {
				SharedPreferences sp = getSharedPreferences(context);
				long time = sp.getLong(version.getUniqueIdentity(), 0L);
				result = DateUtils.isToday(time);
			}
			log.debug("IgnorePolicy TODAY_NO_SHOW, isAutoUpdate=" + isAutoUpdate + ", isIgnore=" + result);
			return result;
		}

		@Override
		public void notifyIgnore(Context context, Version version) {
			log.debug("IgnorePolicy TODAY_NO_SHOW  be notifyIgnore, member today Ignore this version.");
			SharedPreferences sp = getSharedPreferences(context);
			sp.edit().putLong(version.getUniqueIdentity(), new Date().getTime()).commit();
		}
		
		private SharedPreferences getSharedPreferences(Context context) {
			if (context != null) {
				return context.getApplicationContext().getApplicationContext().getSharedPreferences(SAVE_NAME, Context.MODE_PRIVATE);
			}
			return null;
		}

	};

	public static IgnorePolicy JUST_THIS_LAUNCHER = new IgnorePolicy() {

		@Override
		public boolean isIgnore(Context context, Version version, boolean isAutoUpdate) {
			log.debug("IgnorePolicy : JUST_THIS_LAUNCHER, do not Ignore.");
			return false;
		}

		@Override
		public void notifyIgnore(Context context, Version version) {
			//do nothing
			log.debug("IgnorePolicy : JUST_THIS_LAUNCHER, notifyIgnore, but do nothing.");
		}

	};

	public boolean isIgnoreAutoUpdate() {
		return ignoreAutoUpdate;
	}

	public void setIgnoreAutoUpdate(boolean ignoreAutoUpdate) {
		this.ignoreAutoUpdate = ignoreAutoUpdate;
	}

	public boolean isVersionCanIgnored() {
		return versionCanIgnored;
	}

	public void setVersionCanIgnored(boolean versionCanIgnored) {
		this.versionCanIgnored = versionCanIgnored;
	}

	public boolean isHasUpdateInWifi() {
		return hasUpdateInWifi;
	}

	public void setHasUpdateInWifi(boolean hasUpdateInWifi) {
		this.hasUpdateInWifi = hasUpdateInWifi;
	}

	public boolean isAutoOpenWifi() {
		return autoOpenWifi;
	}

	public void setAutoOpenWifi(boolean autoOpenWifi) {
		this.autoOpenWifi = autoOpenWifi;
	}

	public IgnorePolicy getIgnorePolicy() {
		return ignorePolicy;
	}

	public void setIgnorePolicy(IgnorePolicy ignorePolicy) {
		this.ignorePolicy = ignorePolicy;
	}

}