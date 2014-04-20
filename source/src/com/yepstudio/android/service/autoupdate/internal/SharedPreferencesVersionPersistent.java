package com.yepstudio.android.service.autoupdate.internal;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.yepstudio.android.service.autoupdate.AutoUpdateLog;
import com.yepstudio.android.service.autoupdate.AutoUpdateLogFactory;
import com.yepstudio.android.service.autoupdate.Version;
import com.yepstudio.android.service.autoupdate.VersionPersistent;

/**
 * 使用SharedPreferences去存储
 * @author zzljob@gmail.com
 * @create 2014年4月20日
 * @version 1.0，2014年4月20日
 *
 */
public class SharedPreferencesVersionPersistent implements VersionPersistent {
	
	private static AutoUpdateLog log = AutoUpdateLogFactory.getAutoUpdateLog(SharedPreferencesVersionPersistent.class); 
	
	public static final String VERSION_PERSISTENT_NAME = "auto_update_version";
	
	public static final String key_App = "App";
	public static final String key_Module = "Module";
	public static final String key_Code = "Code";
	public static final String key_Name = "Name";
	public static final String key_Title = "Title";
	public static final String key_Description = "Description";
	public static final String key_TargetUrl = "TargetUrl";
	public static final String key_ReleaseTime = "ReleaseTime";
	public static final String key_MD5 = "MD5";
	public static final String key_SHA1 = "SHA1";
	public static final String key_Remark = "Remark";

	@Override
	public void save(String module, Context context, Version version) {
		log.trace("save, module : " + module);
		try {
			JSONObject json = new JSONObject();
			json.put(key_App, version.getApp());
			json.put(key_Module, version.getModule());
			json.put(key_Code, version.getCode());
			json.put(key_Name, version.getName());
			json.put(key_Title, version.getTitle());
			json.put(key_Description, version.getDescription());
			json.put(key_TargetUrl, version.getTargetUrl());
			if (version.getReleaseTime() == null) {
				json.put(key_ReleaseTime, -1L);
			} else {
				json.put(key_ReleaseTime, version.getReleaseTime().getTime());
			}
			json.put(key_MD5, version.getMD5());
			json.put(key_SHA1, version.getSHA1());
			json.put(key_Remark, version.getRemark());
			
			SharedPreferences sp = context.getApplicationContext().getSharedPreferences(VERSION_PERSISTENT_NAME, Context.MODE_PRIVATE);
			sp.edit().putString(module, json.toString()).commit();
			log.trace("json:" + json.toString());
		} catch (Exception e) {
			log.error("save Version failed", e);
		}
	}

	@Override
	public Version load(String module, Context context) {
		log.trace("load, module : " + module);
		SharedPreferences sp = context.getApplicationContext().getSharedPreferences(VERSION_PERSISTENT_NAME, Context.MODE_PRIVATE);
		String text = sp.getString(module, "");
		if (TextUtils.isEmpty(text)) {
			return null;
		}
		Version version = null;
		try {
			log.trace("json:" + text);
			JSONObject json = new JSONObject(text);
			version = new Version();
			version.setApp(getString(json, key_App));
			version.setModule(getString(json, key_Module));
			version.setCode(getInt(json, key_Code));
			version.setName(getString(json, key_Name));
			version.setTitle(getString(json, key_Title));
			version.setDescription(getString(json, key_Description));
			version.setTargetUrl(getString(json, key_TargetUrl));
			long time = getLong(json, key_ReleaseTime);
			if (time >= 0) {
				version.setReleaseTime(new Date(time));
			}
			version.setMD5(getString(json, key_MD5));
			version.setSHA1(getString(json, key_SHA1));
			version.setRemark(getString(json, key_Remark));
		} catch (Exception e) {
			log.error("load Version failed", e);
		}
		return version;
	}
	
	private String getString(JSONObject json, String key) throws JSONException {
		if (json.has(key) && !json.isNull(key)) {
			return json.getString(key);
		}
		return null;
	}
	
	private int getInt(JSONObject json, String key) throws JSONException {
		if (json.has(key) && !json.isNull(key)) {
			return json.getInt(key);
		}
		return 0;
	}
	
	private long getLong(JSONObject json, String key) throws JSONException {
		if (json.has(key) && !json.isNull(key)) {
			return json.getLong(key);
		}
		return 0L;
	}

	@Override
	public void notifyFinish(String module, Context context, Version version) {
		log.trace("notifyFinish, module : " + module);
		SharedPreferences sp = context.getApplicationContext().getSharedPreferences(VERSION_PERSISTENT_NAME, Context.MODE_PRIVATE);
		sp.edit().putString(module, "").commit();
	}

}
