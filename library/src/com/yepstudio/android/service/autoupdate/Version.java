package com.yepstudio.android.service.autoupdate;

import java.io.Serializable;
import java.util.Date;

import android.text.TextUtils;

/**
 * 
 * @author zhangzl@gmail.com
 * @create 2014年4月17日
 * @version 1.0, 2014年4月17日
 * 
 */
public class Version implements Serializable {

	private static final long serialVersionUID = -6966499247225833389L;

	/** app包名 **/
	private String app;
	/** app更新的模块名，默认就是包名 **/
	private String module;
	/** 版本号 e.g: 13 **/
	private int code;
	/** 版本名 e.g: 1.0.9 **/
	private String name;
	/** 更新提示标题 e.g: 最新版 **/
	private String title;
	/** 更新提示描述 **/
	private String description;
	/** 此版本APK下载地址 * */
	private String targetUrl;
	/** 发布时间 */
	private Date releaseTime;
	/** MD5值 */
	private String MD5;
	/** SHA1值 */
	private String SHA1;
	/** 备注 */
	private String remark;
	/*** 更新策略 **/
	private UpdatePolicy updatePolicy;

	private String unique;

	/**
	 * 获取唯一的标识
	 * @return
	 */
	public String getUniqueIdentity() {
		if (TextUtils.isEmpty(unique)) {
			StringBuilder builder = new StringBuilder();
			builder.append(app).append("_");
			builder.append(module).append("_");
			builder.append(code).append("_");
			builder.append(name).append("_");
			builder.append(targetUrl);
			unique = builder.toString();
		}
		return unique;
	}

	public String getApp() {
		return app;
	}

	public void setApp(String app) {
		unique = null;
		this.app = app;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		unique = null;
		this.module = module;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		unique = null;
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		unique = null;
		this.name = name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTargetUrl() {
		unique = null;
		return targetUrl;
	}

	public void setTargetUrl(String targetUrl) {
		this.targetUrl = targetUrl;
	}

	public Date getReleaseTime() {
		return releaseTime;
	}

	public void setReleaseTime(Date releaseTime) {
		this.releaseTime = releaseTime;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getMD5() {
		return MD5;
	}

	public void setMD5(String mD5) {
		MD5 = mD5;
	}

	public String getSHA1() {
		return SHA1;
	}

	public void setSHA1(String sHA1) {
		SHA1 = sHA1;
	}

	public UpdatePolicy getUpdatePolicy() {
		return updatePolicy;
	}

	public void setUpdatePolicy(UpdatePolicy updatePolicy) {
		this.updatePolicy = updatePolicy;
	}

}
