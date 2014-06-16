package com.yepstudio.android.service.autoupdate;

import com.yepstudio.android.service.autoupdate.internal.ContextWrapper;

/**
 * 自动更新服务的操作接口
 * 
 * @author zhangzl@gmail.com
 * @create 2014年4月17日
 * @version 1.1, 2014年6月16日
 * 
 */
public interface AppUpdate {

	/**
	 * 检查更新
	 * @param module
	 * @param context Context内容
	 * @param configuration AppUpdateServiceConfiguration配置
	 * @param isAutoUpdate 是否自动更新的，true自动更新的，false用户点击了检查更新 
	 */
	public void checkUpdate(ContextWrapper Wrapper);
	
}
