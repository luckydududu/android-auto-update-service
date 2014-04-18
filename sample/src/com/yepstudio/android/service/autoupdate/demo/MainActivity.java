package com.yepstudio.android.service.autoupdate.demo;

import java.util.Date;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.yepstudio.android.service.autoupdate.AppUpdateService;
import com.yepstudio.android.service.autoupdate.AppUpdateServiceConfiguration;
import com.yepstudio.android.service.autoupdate.ResponseParser;
import com.yepstudio.android.service.autoupdate.Version;
import com.yepstudio.android.service.autoupdate.demo.R;

public class MainActivity extends Activity {

	final static String UPDATE_URL = "http://rebirth.duapp.com/app/check?app=com.yepstudio.geekpark&appType=APK";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		AppUpdateServiceConfiguration.Build config = new AppUpdateServiceConfiguration.Build();
		config.setCheckUpdateUrl(UPDATE_URL);
		config.setResponseParser(new ResponseParser() {
			
			@Override
			public Version parser(String module, String response) {
				Version version = new Version();
				version.setApp(MainActivity.this.getPackageName());
				version.setCode(2);
				version.setTitle("发现新版本了");
				version.setDescription("不晓得阿德啊");
				version.setTargetUrl("http://tools.fund123.cn/MobileClient/smbandroid.apk");
				version.setReleaseTime(new Date());
				return version;
			}
		});
		AppUpdateService.init(this, config.create(this));
		
		View check = findViewById(R.id.check);
		check.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// 检查最新版本，并弹出窗口
				AppUpdateService.checkUpdate(MainActivity.this, true);
			}
		});
		
		View download = findViewById(R.id.download);
		download.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// 无须提示，直接升级
				AppUpdateService.checkUpdate(MainActivity.this, false);
			}
		});
	}

}
