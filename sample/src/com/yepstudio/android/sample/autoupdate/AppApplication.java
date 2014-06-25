package com.yepstudio.android.sample.autoupdate;

import java.util.Date;

import com.yepstudio.android.library.autoupdate.AppUpdateService;
import com.yepstudio.android.library.autoupdate.AppUpdateServiceConfiguration;
import com.yepstudio.android.library.autoupdate.Version;
import com.yepstudio.android.library.autoupdate.internal.SimpleResponseDelivery;

import android.app.Application;

public class AppApplication extends Application {
	
	private final static String UPDATE_URL = "http://rebirth.duapp.com/app/check?app=com.yepstudio.geekpark&appType=APK";

	@Override
	public void onCreate() {
		super.onCreate();
		AppUpdateServiceConfiguration.Build config = new AppUpdateServiceConfiguration.Build();
		config.setCheckUpdateUrl(UPDATE_URL);
		config.setResponseDelivery(new SimpleResponseDelivery() {

			@Override
			public Version parserResponse(String response) {
				Version version = new Version();
				version.setApp(getPackageName());
				version.setCode(2);
				version.setTitle("发现新版本了");
				version.setDescription("不晓得阿德啊");
				version.setTargetUrl("http://tools.fund123.cn/MobileClient/smbandroid.apk");
				version.setReleaseTime(new Date());
				return version;
			}
		});
		AppUpdateService.init(this, config.create(this));
	}

}
