package com.yepstudio.android.library.autoupdate;

import java.io.File;

import android.content.Context;


/**
 * 下载的接口
 * @author zhangzl@gmail.com
 * @create 2014年4月17日
 * @version 1.0, 2014年4月17日
 *
 */
public interface DownloadDelegate {

  /**
   * 下载的方法，如果有异常需自行处理，抛出来就会崩溃
   * @param module
   * @param context
   * @param version
   * @param callback
   * @return 是否接收了该文件的下载
   */
	public boolean download(String module, Context context, Version version, Runnable callback, boolean isUserOpt);
	
	/**
	 * 是否正在下载
	 * @param module
	 * @param context
	 * @param version
	 * @return
	 */
	public boolean isDownloading(String module, Context context, Version version);
	
	/**
	 * 获取本地下载
	 * @param module
	 * @param context
	 * @param version
	 * @return
	 */
	public File getDownloadLocalFile(String module, Context context, Version version);
	
	
	
}