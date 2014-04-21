package com.yepstudio.android.service.autoupdate.internal;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import com.yepstudio.android.service.autoupdate.AutoUpdateLog;
import com.yepstudio.android.service.autoupdate.AutoUpdateLogFactory;
import com.yepstudio.android.service.autoupdate.ResponseParser;
import com.yepstudio.android.service.autoupdate.Version;

/**
 * 
 * @author zhangzl@gmail.com
 * @create 2014年4月18日
 * @version 1.0, 2014年4月18日
 * 
 */
public class SimpleJSONParser implements ResponseParser {

	public static final String ROOT_NODE = "response";
	private static AutoUpdateLog log = AutoUpdateLogFactory.getAutoUpdateLog(SimpleJSONParser.class);

	@Override
	public Version parser(String response) {
		Version version = null;
		try {
			response = response.substring(response.indexOf("{"), response.lastIndexOf("}") + 1);
			log.trace("json:" + response);
			JSONObject json = new JSONObject(response);

			if (json.has(ROOT_NODE) && !json.isNull(ROOT_NODE)) {
				log.trace("has ROOT_NODE");
				json = json.getJSONObject(ROOT_NODE);
			}
			
			version = new Version();
			version.setApp(json.getString("app"));
			version.setCode(json.getInt("version"));
			version.setName(json.getString("name"));
			version.setTitle(json.getString("title"));
			version.setDescription(json.getString("description"));
			version.setReleaseTime(new Date(json.getLong("release")));
			version.setTargetUrl(json.getString("url"));
			version.setMD5(json.getString("md5"));
			version.setSHA1(json.getString("sha1"));

		} catch (JSONException e) {
			log.warning("has exception", e);
			version = null;
		}
		return version;
	}

}
