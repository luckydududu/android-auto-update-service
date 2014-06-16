package com.yepstudio.android.service.autoupdate;

import java.io.IOException;

/**
 * 
 * @author zzljob@gmail.com
 * @create 2014年6月16日
 * @version 1.0, 2014年6月16日
 *
 * @param <T>
 */
public interface ResponseDelivery<T> {

	public T submitRequest(RequestInfo requestInfo) throws IOException;

	public Version parserResponse(T before) throws Exception;

}
