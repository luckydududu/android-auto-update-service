package com.yepstudio.android.service.autoupdate;

import android.content.Context;

/**
 * 请求的监听器，用途在于可以中断请求结束后的继续执行
 * @author zhangzl@gmail.com
 * @create 2014年4月17日
 * @version 1.0, 2014年4月17日
 *
 */
public interface ResponseListener {

    /**
     * 发现最新版本数据时被调用
     * @param module
     * @param context
     * @param version
     * @param isAutoUpdate
     * @return 返回false则继续往下处理，返回true表示处理完成，中断往下的执行
     */
	public boolean onFoundLatestVersion(String module, Context context, Version version, boolean isAutoUpdate);
	
	/**
	 * 当前版本已是最新版本
	 * @param module
	 * @param context
	 * @param isAutoUpdate
	 * @return 返回false则继续往下处理，返回true表示处理完成，中断往下的执行
	 */
	public boolean onCurrentIsLatest(String module, Context context, boolean isAutoUpdate);

	/**
	 * 请求出错时回调
	 * @param module
	 * @param context
	 * @param isAutoUpdate
	 * @return 返回false则继续往下处理，返回true表示处理完成，中断往下的执行
	 */
	public boolean onResponseError(String module, Context context, boolean isAutoUpdate);
	
	/**
	 * 解析出错
	 * @param module
	 * @param context
	 * @param isAutoUpdate
	 * @return 返回false则继续往下处理，返回true表示处理完成，中断往下的执行
	 */
	public boolean onParserError(String module, Context context, boolean isAutoUpdate);
	
}
