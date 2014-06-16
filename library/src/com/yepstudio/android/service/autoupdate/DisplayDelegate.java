package com.yepstudio.android.service.autoupdate;

import android.content.Context;


/**
 * 有新版本了，如何提示给用户，让用户自己选择要不要升级
 * @author zzljob@gmail.com
 * @create 2014年4月17日
 * @version 1.1, 2014年6月16日
 *
 */
public interface DisplayDelegate {

    /**
     * 显示发现最新版本数据
     * @param version 版本数据
     */
	public void showFoundLatestVersion(Context context, Version version, boolean isAutoUpdate, UserOptionsListener listener);
	
	

}