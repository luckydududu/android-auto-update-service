package com.yepstudio.android.service.autoupdate;

import java.io.IOException;
import java.util.Map;

public interface ResponseDelivery {

	public String submitRequest(String updateUrl, String method, String userAgent, Map<String, Object> requestParams) throws IOException;

}
