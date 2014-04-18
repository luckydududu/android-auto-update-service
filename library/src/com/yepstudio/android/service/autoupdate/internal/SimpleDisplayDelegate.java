package com.yepstudio.android.service.autoupdate.internal;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import android.app.Dialog;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.TextView;

import com.yepstudio.android.service.autoupdate.AppUpdateService;
import com.yepstudio.android.service.autoupdate.AutoUpdateLog;
import com.yepstudio.android.service.autoupdate.AutoUpdateLogFactory;
import com.yepstudio.android.service.autoupdate.DisplayDelegate;
import com.yepstudio.android.service.autoupdate.R;
import com.yepstudio.android.service.autoupdate.UpdatePolicy;
import com.yepstudio.android.service.autoupdate.UserOptionsListener;
import com.yepstudio.android.service.autoupdate.Version;

/**
 * 
 * @author zhangzl@gmail.com
 * @create 2014年4月17日
 * @version 1.0, 2014年4月17日
 *
 */
public class SimpleDisplayDelegate implements DisplayDelegate {
	
	private static AutoUpdateLog log = AutoUpdateLogFactory.getAutoUpdateLog(SimpleDisplayDelegate.class);
	
	@Override
	public void showFoundLatestVersion(final String module, final Context context, final Version version, boolean isAutoUpdate) {
		final Dialog dialog = new Dialog(context, R.style.dialog);
        dialog.setContentView(R.layout.aus__dialog_found_version);
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        
        TextView title = (TextView) dialog.findViewById(R.id.title);
        TextView feature = (TextView) dialog.findViewById(R.id.feature);
        TextView time = (TextView) dialog.findViewById(R.id.time);
		if (TextUtils.isEmpty(version.getTitle())) {
			title.setText(String.format(context.getResources().getString(R.string.aus__latest_version_title), version.getName()));
		} else {
			title.setText(version.getTitle());
		}
		if (version.getReleaseTime() == null) {
			time.setVisibility(View.GONE);
		} else {
			DateFormat format = SimpleDateFormat.getDateInstance();
			String text = format.format(version.getReleaseTime());
			time.setText(String.format(context.getResources().getString(R.string.aus__latest_version_time), text));
			time.setVisibility(View.VISIBLE);
		}
        feature.setText(version.getDescription());
        
        View ignore = dialog.findViewById(R.id.ignore);
        View update = dialog.findViewById(R.id.update);

        UpdatePolicy updatePolicy = AppUpdateService.getConfiguration(module).getUpdatePolicy();
        
        final CheckBox laterOnWifi = (CheckBox) dialog.findViewById(R.id.only_wifi);
        //非Wifi情况下
		if (NetworkUtil.getNetworkType(context) != NetworkUtil.WIFI) {
			//判断更新策略
			if (!updatePolicy.isHasUpdateInWifi()) {
				log.info("isHasUpdateInWifi : true, so not show UpdateInWifi");
				laterOnWifi.setVisibility(View.GONE);
			} else {
				laterOnWifi.setVisibility(View.VISIBLE);
			}
			//判断更新策略是否自动打开WIFI
			if (updatePolicy.isAutoOpenWifi()) {
				log.info("isAutoOpenWifi : true, OpenWifi...");
				openWifi(context);
			}
		} else {
			laterOnWifi.setVisibility(View.GONE);
		}
        
        ignore.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				UserOptionsListener listener = AppUpdateService.getConfiguration(module).getUserOptionsListener();
				listener.doIgnore(module, context, version);
				dialog.dismiss();
			}
		});
        
        update.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean laterOnWifiFlag = laterOnWifi.isChecked();
				UserOptionsListener listener = AppUpdateService.getConfiguration(module).getUserOptionsListener();
				listener.doUpdate(module, context, version, laterOnWifiFlag);
				dialog.dismiss();
			}
		});
        
        //更新策略 ： 是否显示忽略
        log.info("isVersionCanIgnored : " + updatePolicy.isVersionCanIgnored());
		if (updatePolicy.isVersionCanIgnored()) {
			ignore.setVisibility(View.VISIBLE);
			dialog.setCancelable(true);
		} else {
			dialog.setCancelable(false);
			ignore.setVisibility(View.GONE);
		}
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();                
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);  
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
	    if (height > width) {  
	        lp.width = (int) (width * 0.9);          
	    } else {  
	        lp.width = (int) (width * 0.5);                  
	    }  
	    dialog.getWindow().setAttributes(lp);
		dialog.show();
	}
	
	private void openWifi(Context context) {
		try {
			WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			wm.setWifiEnabled(true);
			log.trace("openWifi success.");
		} catch (Throwable th) {
			log.error("openWifi fail.");
		}
	}

}
