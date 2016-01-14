package com.oplay.giftassistant.asynctask;

import android.content.Context;
import android.os.AsyncTask;

import com.oplay.giftassistant.config.AppDebugConfig;
import com.oplay.giftassistant.download.ApkDownloadManager;
import com.socks.library.KLog;

/**
 * AsyncTask_InitApplication
 *
 * @author zacklpx
 *         date 16-1-14
 *         description
 */
public class AsyncTask_InitApplication extends AsyncTask<Object, Integer, Void> {
	private Context mContext;

	public AsyncTask_InitApplication(Context context) {
		mContext = context.getApplicationContext();
	}
	@Override
	protected Void doInBackground(Object... params) {
		try {
			//初始化下载列表
			ApkDownloadManager.getInstance(mContext).initDownloadList();
			//TODO异步初始化操作
		}catch (Throwable e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(e);
			}
		}
		return null;
	}
}
