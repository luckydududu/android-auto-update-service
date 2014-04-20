package com.yepstudio.android.service.autoupdate;

import android.content.Context;

/**
 * 用户操作的抽象接口
 * @author zhangzl@gmail.com
 * @create 2014年4月17日
 * @version 1.0, 2014年4月17日
 *
 */
public interface UserOptionsListener {
	
	/**
	 * 当用户选择更新时调用
	 * @param module
	 * @param context
	 * @param version
	 * @param laterOnWifi
	 */
	void doUpdate(String module, Context context, Version version, boolean laterOnWifi);

	/**
	 * 当用户选择忽略时调用
	 * @param module
	 * @param context
	 * @param version
	 */
	void doIgnore(String module, Context context, Version version);
}
