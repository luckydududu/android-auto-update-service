package com.yepstudio.android.library.autoupdate.internal;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtil {

	public static final int NO_CONNECTION = 0;
	public static final int WIFI = 1;
	public static final int MOBILE = 2;

    /**
     * 获取网络类型
     * @param context Context
     * @return 类型值
     */
	public static int getNetworkType(Context context) {
		ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		int networkType = NO_CONNECTION;
		if(networkInfo != null){
			int type = networkInfo.getType();
			networkType = type == ConnectivityManager.TYPE_WIFI ? WIFI : MOBILE;
		}
		return networkType;
	}
	
	public static boolean hasNetwork(Context context) {
		return NetworkUtil.getNetworkType(context) != NetworkUtil.NO_CONNECTION;
	}
}
