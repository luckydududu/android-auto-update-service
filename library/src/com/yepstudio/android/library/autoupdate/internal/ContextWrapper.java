package com.yepstudio.android.library.autoupdate.internal;

import android.content.Context;

import com.yepstudio.android.library.autoupdate.AppUpdateServiceConfiguration;
import com.yepstudio.android.library.autoupdate.Version;

public class ContextWrapper {

	private final Context context;
	private final AppUpdateServiceConfiguration configuration;
	private final String module;
	private final boolean isAutoUpdate;
	private final StringBuffer buffer;
	
	private boolean requestError;
	private boolean parserResponseError;
	private Version version;

	public ContextWrapper(AppUpdateServiceConfiguration configuration, String module,
			Context context, boolean isAutoUpdate) {
		super();
		this.module = module;
		this.configuration = configuration;
		this.context = context;
		this.isAutoUpdate = isAutoUpdate;
		this.buffer = new StringBuffer();
	}

	public Context getContext() {
		return context;
	}

	public AppUpdateServiceConfiguration getConfiguration() {
		return configuration;
	}

	public boolean isAutoUpdate() {
		return isAutoUpdate;
	}

	public StringBuffer getBuffer() {
		return buffer;
	}

	public void append(String text) {
		buffer.append(text).append("\n");
	}

	public boolean isRequestError() {
		return requestError;
	}

	public void setRequestError(boolean requestError) {
		this.requestError = requestError;
	}

	public boolean isParserResponseError() {
		return parserResponseError;
	}

	public void setParserResponseError(boolean parserResponseError) {
		this.parserResponseError = parserResponseError;
	}

	public Version getVersion() {
		return version;
	}

	public void setVersion(Version version) {
		this.version = version;
	}

	public String getModule() {
		return module;
	}
}
