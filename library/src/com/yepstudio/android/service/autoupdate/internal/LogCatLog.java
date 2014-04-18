package com.yepstudio.android.service.autoupdate.internal;

import android.util.Log;

import com.yepstudio.android.service.autoupdate.AutoUpdateLog;

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
		Log.v(tag, msg);
	}

	@Override
	public void trace(String msg, Throwable t) {
		Log.v(tag, msg, t);
	}

	@Override
	public void debug(String msg) {
		Log.d(tag, msg);
	}

	@Override
	public void debug(String msg, Throwable t) {
		Log.d(tag, msg, t);
	}

	@Override
	public void info(String msg) {
		Log.i(tag, msg);
	}

	@Override
	public void info(String msg, Throwable t) {
		Log.i(tag, msg, t);

	}

	@Override
	public void warning(String msg) {
		Log.w(tag, msg);
	}

	@Override
	public void warning(String msg, Throwable t) {
		Log.w(tag, msg, t);

	}

	@Override
	public void error(String msg) {
		Log.e(tag, msg);
	}

	@Override
	public void error(String msg, Throwable t) {
		Log.e(tag, msg, t);
	}

	@Override
	public void init(Class<?> clazz) {
		if (clazz != null) {
			tag = clazz.getName();
		}
	}

}
