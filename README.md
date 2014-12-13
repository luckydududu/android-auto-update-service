auto-update-service
============

An Android Service, provide a easy way to update app automatically.

Android App自动更新服务（非Service）。

## 简单的使用例子

``` java

public class MainActivity extends Activity {
	
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
				AppUpdateService.checkUpdate(MainActivity.this, true);
			}
		});
		
		View download = findViewById(R.id.download);
		download.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// 在执行检查操作后，用户取消下载，可以通过此方法，下载最新版本。
				AppUpdateService.checkUpdate(MainActivity.this, false);
			}
		});
	}
	

}

```

----

## Copyright and License

```
Copyright 2013 zzljob@gmail.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
````
