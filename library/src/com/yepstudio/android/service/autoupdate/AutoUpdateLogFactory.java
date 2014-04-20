package com.yepstudio.android.service.autoupdate;

import com.yepstudio.android.service.autoupdate.internal.LogCatLog;

/**
 * 
 * @author zhangzl@gmail.com
 * @create 2014年4月18日
 * @version 1.0, 2014年4月18日
 * 
 */
public class AutoUpdateLogFactory {

	private static Class<? extends AutoUpdateLog> LOGCLASS;

	public static AutoUpdateLog getAutoUpdateLog(Class<?> clazz) {
		if (LOGCLASS == null) {
			LOGCLASS = LogCatLog.class;
		}
		AutoUpdateLog log = null;
		try {
			log = LOGCLASS.newInstance();
			log.init(clazz);
		} catch (Throwable e) {
			throw new RuntimeException();
		}
		return log;
	}

	public static void setAutoUpdateLogClass(Class<? extends AutoUpdateLog> clazz) {
		LOGCLASS = clazz;
	}

}
