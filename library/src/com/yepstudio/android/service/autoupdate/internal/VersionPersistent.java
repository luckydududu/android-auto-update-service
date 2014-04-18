package com.yepstudio.android.service.autoupdate.internal;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.yepstudio.android.service.autoupdate.Version;

public class VersionPersistent {

	public static final String VERSION_PERSISTENT_NAME = "auto_update_version";
	
	private SharedPreferences shared;
	
	public VersionPersistent(Context context) {
		shared = context.getSharedPreferences(VERSION_PERSISTENT_NAME, Context.MODE_PRIVATE);
	}
	
	public void save(Version version) {
		if (version == null) {
			return;
		}
		Editor editor = shared.edit();
		editor.clear();
//		editor.putInt(Version.VERSION_CODE, version.code);
//		editor.putInt(Version.VERSION_BUILD, version.build);
//		editor.putInt(Version.VERSION_LEVEL, version.level);
//		editor.putString(Version.VERSION_TITLE, version.title);
//		editor.putString(Version.VERSION_NAME, version.name);
//		editor.putString(Version.VERSION_FEATURE, version.feature);
//		editor.putString(Version.VERSION_URL, version.targetUrl);
//		editor.putString(Version.VERSION_TIME, version.releaseTime);
//		editor.putString(Version.VERSION_APP, version.app);
		editor.commit();
	}
	
	public void clear() {
		Editor editor = shared.edit();
		editor.clear();
		editor.commit();
	}
	
	public Version load() {
//		if (shared.contains(Version.VERSION_CODE)) {
//			int code = shared.getInt(Version.VERSION_CODE, 0);
//			int build = shared.getInt(Version.VERSION_BUILD, 0);
//			int level = shared.getInt(Version.VERSION_LEVEL, 0);
//			String name = shared.getString(Version.VERSION_NAME, null);
//			String title = shared.getString(Version.VERSION_TITLE, null);
//			String feature = shared.getString(Version.VERSION_FEATURE, null);
//			String url = shared.getString(Version.VERSION_URL, null);
//			String time = shared.getString(Version.VERSION_TIME, null);
//			String app = shared.getString(Version.VERSION_APP, null);
//			if (name == null || url == null) {
//				return null;
//			}
//			return new Version(code, build, level, app, name, title, feature, url, time);
//		}
		return null;
	}
	
}
