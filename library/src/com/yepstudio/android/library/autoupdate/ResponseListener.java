package com.yepstudio.android.library.autoupdate;

import android.content.Context;

/**
 * 请求的监听器
 * @author zzljob@gmail.com
 * @create 2014年4月17日
 * @version 1.0, 2014年4月17日
 *
 */
public interface ResponseListener {
	
	/**
	 * 开始检查更新
	 * @param context
	 * @param isAutoUpdate
	 */
	public void onStartUpdateCheck(Context context, boolean isAutoUpdate, boolean hasRunning);

    /**
     * 发现最新版本数据时被调用
     * @param context
     * @param version
     * @param isAutoUpdate
     */
	public void onFoundLatestVersion(Context context, Version version, boolean isAutoUpdate);
	
	/**
	 * 当跳过最新版时执行
	 * @param context
	 * @param version
	 * @param isAutoUpdate
	 */
	public void onSkipLatestVersion(Context context, Version version, boolean isAutoUpdate);
	
	/**
	 * 当前版本已是最新版本
	 * @param module
	 * @param context
	 * @param isAutoUpdate
	 * @return 返回false则继续往下处理，返回true表示处理完成，中断往下的执行
	 */
	public void onCurrentIsLatest(Context context, boolean isAutoUpdate);

	/**
	 * 请求出错时回调
	 * @param module
	 * @param context
	 * @param isAutoUpdate
	 */
	public void onResponseError(Context context, boolean isAutoUpdate);
	
	/**
	 * 解析出错
	 * @param module
	 * @param context
	 * @param isAutoUpdate
	 */
	public void onParserError(Context context, boolean isAutoUpdate);
	
}
