package com.oplay.giftcool.listener.impl;

import android.content.Context;

import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.util.ThreadUtil;
import com.socks.library.KLog;

import java.util.Set;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

/**
 * Created by zsigui on 16-3-3.
 */
public class JPushTagsAliasCallback implements TagAliasCallback {

	private Context mContext;
	private Runnable mRunnable;
	private int count = 0;

	public JPushTagsAliasCallback(Context context) {
		mContext = context.getApplicationContext();
	}

	@Override
	public void gotResult(int code, final String alias, Set<String> tag) {
		if (code == 0) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.d(AppDebugConfig.TAG_JPUSH, "set alias success : " + alias);
			}
		} else {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.d(AppDebugConfig.TAG_JPUSH, "set alias failed : " + alias);
			}
			if (mRunnable == null) {
				mRunnable = new Runnable() {
					@Override
					public void run() {
						JPushInterface.setAlias(mContext, alias, JPushTagsAliasCallback.this);
					}
				};
			}
			if (count++ < 3)
				if (AppDebugConfig.IS_DEBUG) {
					KLog.d(AppDebugConfig.TAG_JPUSH, "set alias failed, wait for 5s to run again! ");
				}
				ThreadUtil.runInUIThread(mRunnable, 5000);
		}
	}
}
