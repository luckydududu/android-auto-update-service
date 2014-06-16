package com.yepstudio.android.service.autoupdate.internal;

import android.util.Log;

import com.yepstudio.android.service.autoupdate.AutoUpdateLog;
import com.yepstudio.android.service.autoupdate.BuildConfig;

/**
 * 
 * @author zhangzl@gmail.com
 * @create 2014年4月18日
 * @version 1.0, 2014年4月18日
 * 
 */
public class LogCatLog implements AutoUpdateLog {

	private String tag = "";

	@Override
	public void trace(String msg) {
		if (BuildConfig.DEBUG) {
			Log.v(tag, msg);
		}
	}

	@Override
	public void trace(String msg, Throwable t) {
		if (BuildConfig.DEBUG) {
			Log.v(tag, msg, t);
		}
	}

	@Override
	public void debug(String msg) {
		if (BuildConfig.DEBUG) {
			Log.d(tag, msg);
		}
	}

	@Override
	public void debug(String msg, Throwable t) {
		if (BuildConfig.DEBUG) {
			Log.d(tag, msg, t);
		}
	}

	@Override
	public void info(String msg) {
		if (BuildConfig.DEBUG) {
			Log.i(tag, msg);
		}
	}

	@Override
	public void info(String msg, Throwable t) {
		if (BuildConfig.DEBUG) {
			Log.i(tag, msg, t);
		}
	}

	@Override
	public void warning(String msg) {
		if (BuildConfig.DEBUG) {
			Log.w(tag, msg);
		}
	}

	@Override
	public void warning(String msg, Throwable t) {
		if (BuildConfig.DEBUG) {
			Log.w(tag, msg, t);
		}
	}

	@Override
	public void error(String msg) {
		if (BuildConfig.DEBUG) {
			Log.e(tag, msg);
		}
	}

	@Override
	public void error(String msg, Throwable t) {
		if (BuildConfig.DEBUG) {
			Log.e(tag, msg, t);
		}
	}

	@Override
	public void init(Class<?> clazz) {
		if (clazz != null) {
			tag = clazz.getName();
		}
	}

}
