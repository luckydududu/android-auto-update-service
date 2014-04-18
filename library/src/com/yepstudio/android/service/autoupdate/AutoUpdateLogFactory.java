package com.yepstudio.android.service.autoupdate;

import com.yepstudio.android.service.autoupdate.internal.LogCatLog;
import com.yepstudio.android.service.autoupdate.internal.Sl4fLog;

/**
 * 
 * @author zhangzl@gmail.com
 * @create 2014年4月18日
 * @version 1.0, 2014年4月18日
 * 
 */
public class AutoUpdateLogFactory {

	public static Class<? extends AutoUpdateLog> LOGCLASS;

	public static AutoUpdateLog getAutoUpdateLog(Class<?> clazz) {
		AutoUpdateLog log = null;
		if (LOGCLASS != null) {
			try {
				log = LOGCLASS.newInstance();
				log.init(clazz);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		if (log == null) {
			try {
				Class<?> slf4j = Class.forName("org.slf4j.LoggerFactory");
				if (slf4j != null) {
					log = new Sl4fLog();
				}
			} catch (Throwable th) {
				log = new LogCatLog();
			}
			log.init(clazz);
		}
		return log;
	}
}
