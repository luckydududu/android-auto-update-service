package com.yepstudio.android.service.autoupdate.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.yepstudio.android.service.autoupdate.AutoUpdateLog;
import com.yepstudio.android.service.autoupdate.AutoUpdateLogFactory;
import com.yepstudio.android.service.autoupdate.RequestInfo;
import com.yepstudio.android.service.autoupdate.ResponseDelivery;
import com.yepstudio.android.service.autoupdate.Version;

/**
 * 
 * @author zzljob@gmail.com
 * @create 2014年4月19日
 * @version 1.1, 2014年6月16日
 *
 */
public class SimpleResponseDelivery implements ResponseDelivery<String> {
	
	private static AutoUpdateLog log = AutoUpdateLogFactory.getAutoUpdateLog(SimpleResponseDelivery.class);
	private static String CHARSET_NAME = "UTF-8";
	
	@Override
	public String submitRequest(RequestInfo requestInfo) throws IOException {
		log.debug("start submitRequest...");
		
		String updateUrl = requestInfo.getUpdateUrl();
		StringBuilder builder = new StringBuilder(updateUrl);
		Map<String, Object> requestParams = requestInfo.getRequestParams();
		if (requestParams != null && requestParams.size() > 0) {
			builder.append(updateUrl.contains("?") ? "&" : "?");
			for (String key : requestParams.keySet()) {
				builder.append(URLEncoder.encode(key, CHARSET_NAME));
				builder.append("=");
				builder.append(URLEncoder.encode(String.valueOf(requestParams.get(key)), CHARSET_NAME));
				builder.append("&");
			}
			builder.deleteCharAt(builder.length() - 1);
		}
		
		URL targetUrl = new URL(builder.toString());
		log.trace("updateUrl:" + builder.toString());
		
		//设置UserAgent
		HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
		connection.setDefaultUseCaches(false);
		connection.setConnectTimeout(5* 60 * 1000);
		connection.setReadTimeout(5 * 60 * 1000);
		
		try {
			connection.setRequestMethod(requestInfo.getMethod());
		} catch (ProtocolException e) {
			throw new RuntimeException(e); 
		}
		Map<String, Object> headers = requestInfo.getRequestHeaders();
		if (headers != null) {
			for (String key : headers.keySet()) {
				connection.addRequestProperty(key, String.valueOf(headers.get(key)));
			}
		}
		
		InputStream is = connection.getInputStream();
		String response = toStringBuffer(is).toString();
		log.trace("response:" + response);
		is.close();
		connection.disconnect();
		return response;
	}
	
	/**
	 * 读取输出流
	 * @param is InputStream
	 * @return
	 * @throws IOException
	 */
	private StringBuffer toStringBuffer(InputStream is) throws IOException {
		if (null == is) {
			return null;
		}
		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		StringBuffer buffer = new StringBuffer();
		String line = null;
		while ((line = in.readLine()) != null) {
			buffer.append(line).append("\n");
		}
		is.close();
		return buffer;
	}
	
	public static final String ROOT_NODE = "response";
	
	public Version parserResponse(String response) throws Exception {
		log.debug("start parserResponse...");
		Version version = null;
		try {
			response = response.substring(response.indexOf("{"), response.lastIndexOf("}") + 1);
			//log.trace("json:" + response);
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
