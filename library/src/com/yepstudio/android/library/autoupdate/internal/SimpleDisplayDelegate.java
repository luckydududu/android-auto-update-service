package com.yepstudio.android.library.autoupdate.internal;

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

import com.yepstudio.android.library.autoupdate.AutoUpdateLog;
import com.yepstudio.android.library.autoupdate.AutoUpdateLogFactory;
import com.yepstudio.android.library.autoupdate.DisplayDelegate;
import com.yepstudio.android.library.autoupdate.R;
import com.yepstudio.android.library.autoupdate.UpdatePolicy;
import com.yepstudio.android.library.autoupdate.UserOptionsListener;
import com.yepstudio.android.library.autoupdate.Version;

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
	public void showFoundLatestVersion(final Context context, final Version version, final boolean isAutoUpdate, final UserOptionsListener listener) {
		final Dialog dialog = new Dialog(context, R.style.aus__dialog);
        dialog.setContentView(R.layout.aus__dialog_found_version);
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        
        TextView title = (TextView) dialog.findViewById(R.id.aus__title);
        TextView feature = (TextView) dialog.findViewById(R.id.aus__feature);
        TextView time = (TextView) dialog.findViewById(R.id.aus__time);
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
        
        View ignore = dialog.findViewById(R.id.aus__ignore);
        View update = dialog.findViewById(R.id.aus__update);

        UpdatePolicy updatePolicy = version.getUpdatePolicy();
        
        final CheckBox laterOnWifi = (CheckBox) dialog.findViewById(R.id.aus__only_wifi);
        //非Wifi情况下
		if (NetworkUtil.getNetworkType(context) != NetworkUtil.WIFI) {
			//判断更新策略
			if (updatePolicy.isHasUpdateInWifi()) {
				log.debug("isHasUpdateInWifi : true, so show UpdateInWifi");
				laterOnWifi.setVisibility(View.VISIBLE);
			} else {
				log.debug("isHasUpdateInWifi : false, so not show UpdateInWifi");
				laterOnWifi.setVisibility(View.GONE);
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
				listener.doIgnore();
				dialog.dismiss();
			}
		});
        
        update.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean laterOnWifiFlag = laterOnWifi.isChecked();
				listener.doUpdate(laterOnWifiFlag);
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
