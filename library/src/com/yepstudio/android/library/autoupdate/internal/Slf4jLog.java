package com.yepstudio.android.library.autoupdate.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yepstudio.android.library.autoupdate.AutoUpdateLog;

/**
 * 
 * @author zzljob@gmail.com
 * @create 2014年4月20日
 * @version 1.0，2014年4月20日
 * 
 */
public class Slf4jLog implements AutoUpdateLog {
	private Logger logger;

	@Override
	public void init(Class<?> clazz) {
		logger = LoggerFactory.getLogger(clazz);
	}

	@Override
	public void trace(String msg) {
		if (logger != null) {
			logger.trace(msg);
		}
	}

	@Override
	public void trace(String msg, Throwable t) {
		if (logger != null) {
			logger.trace(msg, t);
		}
	}

	@Override
	public void debug(String msg) {
		if (logger != null) {
			logger.debug(msg);
		}
	}

	@Override
	public void debug(String msg, Throwable t) {
		if (logger != null) {
			logger.debug(msg, t);
		}
	}

	@Override
	public void info(String msg) {
		if (logger != null) {
			logger.info(msg);
		}
	}

	@Override
	public void info(String msg, Throwable t) {
		if (logger != null) {
			logger.info(msg, t);
		}
	}

	@Override
	public void warning(String msg) {
		if (logger != null) {
			logger.warn(msg);
		}
	}

	@Override
	public void warning(String msg, Throwable t) {
		if (logger != null) {
			logger.warn(msg, t);
		}
	}

	@Override
	public void error(String msg) {
		if (logger != null) {
			logger.error(msg);
		}
	}

	@Override
	public void error(String msg, Throwable t) {
		if (logger != null) {
			logger.error(msg, t);
		}
	}

}