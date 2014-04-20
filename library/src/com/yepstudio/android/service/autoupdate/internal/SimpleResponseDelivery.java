package com.yepstudio.android.service.autoupdate.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Map;

import com.yepstudio.android.service.autoupdate.AutoUpdateLog;
import com.yepstudio.android.service.autoupdate.AutoUpdateLogFactory;
import com.yepstudio.android.service.autoupdate.ResponseDelivery;

/**
 * 
 * @author zhangzl@gmail.com
 * @create 2014年4月19日
 * @version 1.0, 2014年4月19日
 *
 */
public class SimpleResponseDelivery implements ResponseDelivery {
	
	private static AutoUpdateLog log = AutoUpdateLogFactory.getAutoUpdateLog(SimpleResponseDelivery.class);

	@Override
	public String submitRequest(String updateUrl, String method, String userAgent, Map<String, Object> requestParams) throws IOException {
		log.trace("requestUpdateCheck");
		
		StringBuilder builder = new StringBuilder(updateUrl);
		if (requestParams != null && requestParams.size() > 0) {
			builder.append(updateUrl.contains("?") ? "&" : "?");
			for (String key : requestParams.keySet()) {
				builder.append(key).append("=").append(String.valueOf(requestParams.get(key)));
				builder.append("&");
			}
			builder.deleteCharAt(builder.length() - 1);
		}
		
		URL targetUrl = new URL(updateUrl);
		log.trace("updateUrl:" + updateUrl);
		
		//设置UserAgent
		HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
		connection.setConnectTimeout(5* 60 * 1000);
		connection.setReadTimeout(5 * 60 * 1000);
		try {
			connection.setRequestMethod(method);
		} catch (ProtocolException e) {
			throw new RuntimeException(e); 
		}
		
		connection.addRequestProperty("User-Agent", userAgent);
		log.trace("userAgent:" + userAgent);
		
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

}
