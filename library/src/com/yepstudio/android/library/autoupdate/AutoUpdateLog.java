package com.yepstudio.android.library.autoupdate;

/**
 * 日志输出接口
 * 
 * @author zhangzl@gmail.com
 * @create 2014年4月18日
 * @version 1.0, 2014年4月18日
 * 
 */
public interface AutoUpdateLog {

	public void init(Class<?> clazz);

	public void trace(String msg);

	public void trace(String msg, Throwable t);

	public void debug(String msg);

	public void debug(String msg, Throwable t);

	public void info(String msg);

	public void info(String msg, Throwable t);

	public void warning(String msg);

	public void warning(String msg, Throwable t);

	public void error(String msg);

	public void error(String msg, Throwable t);

}