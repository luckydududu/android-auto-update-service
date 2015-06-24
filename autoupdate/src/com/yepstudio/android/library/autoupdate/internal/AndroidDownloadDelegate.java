package com.yepstudio.android.library.autoupdate.internal;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

import com.yepstudio.android.library.autoupdate.AppUpdateService;
import com.yepstudio.android.library.autoupdate.AutoUpdateLog;
import com.yepstudio.android.library.autoupdate.AutoUpdateLogFactory;
import com.yepstudio.android.library.autoupdate.DownloadDelegate;
import com.yepstudio.android.library.autoupdate.R;
import com.yepstudio.android.library.autoupdate.Version;

/**
 * Android自身的DownloadManager下载器
 * @author zhangzl@gmail.com
 * @create 2014年4月17日
 * @version 1.0, 2014年4月17日
 *
 */
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class AndroidDownloadDelegate extends BroadcastReceiver implements DownloadDelegate {
	
	private static AutoUpdateLog log = AutoUpdateLogFactory.getAutoUpdateLog(AndroidDownloadDelegate.class);
	
	private Map<String, Long> taskIdMap = new HashMap<String, Long>();
	private Map<String, Runnable> callbackMap = new HashMap<String, Runnable>();
	
	@Override
	public boolean download(String module, Context context, Version version, Runnable callback, boolean isUserOpt) {
		if (!canUseSDCard()) {
			log.warning("can not Use SDCard, so stop download and toast.");
			if (isUserOpt) {
				AppUpdateService.show(context, R.string.aus__sdcard_not_mounted, Toast.LENGTH_SHORT);
			}
			return false;
		}
		if (isUserOpt) {
			AppUpdateService.show(context, R.string.aus__start_download, Toast.LENGTH_LONG);
		}
		
		DownloadManager downloader = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
		long taskId = getDownloadTaskId(version);
		log.trace("getDownloadTaskId taskId:" + taskId);
		
		if (hasRunning(downloader, taskId)) {
			log.debug("hasRunning for taskId:" + taskId + ", so do nothing");
			//do nothing, just wait
		} else {
			if (taskId > 0) {
				//删除所有的失败和暂停的任务
				log.trace("remove for taskId:" + taskId + " if taskId is exist");
				try {
					downloader.remove(taskId);
				} catch (Throwable e) {
					log.warning("remove for taskId:" + taskId + " has exception");
				}
			}
			//开始下载任务
			DownloadManager.Request task = new DownloadManager.Request(Uri.parse(version.getTargetUrl()));
			task.setTitle(TextUtils.isEmpty(version.getTitle()) ? version.getName() : version.getTitle());
			task.setDescription(version.getDescription());
			task.setVisibleInDownloadsUi(true);
			task.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
			File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
			if (dir != null) {
				if (!dir.exists()) {
					log.trace("it's not exists, mkdirs dir : " + dir.getAbsolutePath());
					dir.mkdirs();
				}
				String apkName = extractName(version);
				task.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, apkName);
				long addTaskId = downloader.enqueue(task);
				log.debug("download dir:" + dir.getAbsolutePath());
				log.info("add download task, taskId:" + addTaskId + ", apkName:" + apkName);
				taskIdMap.put(version.getUniqueIdentity(), addTaskId);
				callbackMap.put(version.getUniqueIdentity(), callback);
			} else {
				log.warning("DIRECTORY_DOWNLOADS is null, so download fail.");
				return false;
			}
		}
		return true;
	}
	
	private boolean hasRunning(DownloadManager downloader, long taskId) {
		if (taskId < 0) {
			return false;
		}
		Query query = new Query();
		query.setFilterById(taskId);
		query.setFilterByStatus(DownloadManager.STATUS_RUNNING);
		boolean result = false;
		Cursor cur = downloader.query(query);
		if (cur != null) {
			if(cur.moveToFirst()) {
				result = true;
			}
			cur.close();
		}
		return result;
	}
	
	private File querySuccessful(DownloadManager downloader, long taskId) {
		if (taskId < 0) {
			return null;
		}
		Query query = new Query();
		query.setFilterById(taskId);
		query.setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL);
		File targetApkFile = null;
		Cursor cur = downloader.query(query);
		if (cur != null) {
			if(cur.moveToFirst()) {
				String uriString = cur.getString(cur.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
				if (!TextUtils.isEmpty(uriString)) {
					targetApkFile = new File(Uri.parse(uriString).getPath());
				}
			}
			cur.close();
		}
		return targetApkFile;
	}
	
	private long getDownloadTaskId(Version version) {
		if (version == null || version.getTargetUrl() == null) {
			return -1;
		}
		Long taskId = taskIdMap.get(version.getUniqueIdentity());
		return taskId == null ? -1 : taskId;
	}
	
	@Override
	public boolean isDownloading(String module, Context context, Version version) {
		Long taskId = taskIdMap.get(version.getUniqueIdentity());
		if (taskId == null || taskId < 0) {
			return false;
		} else {
			DownloadManager downloader = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
			return this.hasRunning(downloader, taskId);
		}
	}

	@Override
	public File getDownloadLocalFile(String module, Context context, Version version) {
		DownloadManager downloader = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
		File targetApkFile = querySuccessful(downloader, getDownloadTaskId(version));
		if (targetApkFile != null && targetApkFile.exists()) {
			return targetApkFile;
		}
		return null;
	}
	
	
	
	private boolean canUseSDCard() {
		return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
	}
	
	private String extractName(Version version) {
		String path = version.getTargetUrl();
		String tempFileName = "_temp@" + path.hashCode();
		boolean fileNameExist = path.substring(path.length() - 5, path.length()).contains(".");
		if (fileNameExist) {
			tempFileName = path.substring(path.lastIndexOf(File.separator) + 1);
		}
		return tempFileName;
	}

	@Override
	public void onReceive(Context paramContext, Intent paramIntent) {
		log.debug("onReceive");
		if (paramIntent == null) {
			return ;
		}
		String action = paramIntent.getAction();
		if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == action) {
			log.debug("action:" + action);
			DownloadManager downloader = (DownloadManager) paramContext.getSystemService(Context.DOWNLOAD_SERVICE);
			Handler handler = new Handler(Looper.getMainLooper());
			for (String key : taskIdMap.keySet()) {
				long taskId = taskIdMap.get(key);
				File file = querySuccessful(downloader, taskId);
				log.trace("file:" + file);
				if (file != null && file.exists()) {
					Runnable run = callbackMap.remove(key);
					log.trace("run:" + run);
					if (run != null) {
						handler.post(run);
					}
				}
			}
		}
		
	}
	
}
