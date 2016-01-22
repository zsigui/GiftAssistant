package com.oplay.giftcool.model;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

/**
 * Created by zsigui on 16-1-22.
 */
public class ShareInfo implements Serializable {

	private CharSequence mAppName;
	private String mPackageName;
	private String mActivityName;
	private transient Drawable mIconDrawable;

	public ShareInfo() {
	}

	public ShareInfo(CharSequence name, String pn, String an, Drawable icon) {
		mAppName = name;
		mPackageName = pn;
		mActivityName = an;
		mIconDrawable = icon;
	}

	public CharSequence getAppName() {
		return mAppName;
	}

	public void setAppName(CharSequence appName) {
		mAppName = appName;
	}

	public String getPackageName() {
		return mPackageName;
	}

	public void setPackageName(String packageName) {
		mPackageName = packageName;
	}

	public String getActivityName() {
		return mActivityName;
	}

	public void setActivityName(String activityName) {
		mActivityName = activityName;
	}

	public Drawable getIconDrawable() {
		return mIconDrawable;
	}

	public void setIconDrawable(Drawable iconDrawable) {
		mIconDrawable = iconDrawable;
	}
}
