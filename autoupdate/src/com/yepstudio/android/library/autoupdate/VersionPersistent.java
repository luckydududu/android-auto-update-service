package com.yepstudio.android.library.autoupdate;

import android.content.Context;

/**
 * 版本存储
 * @author zzljob@gmail.com
 * @create 2014年4月20日
 * @version 1.0，2014年4月20日
 *
 */
public interface VersionPersistent {

	public void save(String module, Context context, Version version);

	public Version load(String module, Context context);

	public void notifyFinish(String module, Context context, Version version);

}
