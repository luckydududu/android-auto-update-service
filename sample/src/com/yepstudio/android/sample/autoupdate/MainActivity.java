package com.yepstudio.android.sample.autoupdate;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import com.yepstudio.android.library.autoupdate.AppUpdateService;
import com.yepstudio.android.sample.autoupdate.R;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		View check = findViewById(R.id.check);
		check.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AppUpdateService.checkUpdate(MainActivity.this, false);
			}
		});
		
		View download = findViewById(R.id.download);
		download.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AppUpdateService.checkUpdate(MainActivity.this, true);
			}
		});
	}

}
