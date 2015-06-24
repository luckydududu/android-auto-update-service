package com.yepstudio.android.library.autoupdate;

import android.util.Log;

import com.yepstudio.android.library.autoupdate.internal.LogCatLog;
import com.yepstudio.android.library.autoupdate.internal.Slf4jLog;

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
			try {
				Class<?> slf4jClazz = Class.forName("org.slf4j.LoggerFactory");
				if (slf4jClazz != null) {
					LOGCLASS = Slf4jLog.class;
					Log.i("AutoUpdateService", "find has Slf4j, use Slf4jLog for log.");
				}
			} catch (Throwable e) {
				LOGCLASS = LogCatLog.class;
			}
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
