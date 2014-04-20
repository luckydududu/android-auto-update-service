package com.yepstudio.android.service.autoupdate;

import java.io.File;

import android.content.Context;

/**
 * 
 * @author zzljob@gmail.com
 * @create 2014年4月20日
 * @version 1.0，2014年4月20日
 *
 */
public interface InstallExecutor {
	
	public void install(String module, Context context, File file);
	
}
