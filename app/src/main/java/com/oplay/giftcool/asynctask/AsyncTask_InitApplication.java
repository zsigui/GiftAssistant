package com.oplay.giftcool.asynctask;

import android.content.Context;
import android.os.AsyncTask;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.SPConfig;
import com.oplay.giftcool.download.ApkDownloadManager;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.manager.OuwanSDKManager;
import com.oplay.giftcool.model.MobileInfoModel;
import com.oplay.giftcool.model.data.resp.UserModel;
import com.oplay.giftcool.ui.activity.MainActivity;
import com.oplay.giftcool.util.CommonUtil;
import com.oplay.giftcool.util.DateUtil;
import com.oplay.giftcool.util.SPUtil;
import com.socks.library.KLog;

import net.youmi.android.libs.common.global.Global_SharePreferences;

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
			//TODO异步初始化操作
			doInit();
			// 初始化下载列表
			ApkDownloadManager.getInstance(mContext).initDownloadList();
		}catch (Throwable e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(e);
			}
		}
		return null;
	}


	/**
	 * 判断是否今日首次登录
	 */
	public void judgeFirstOpenToday() {
		long lastOpenTime = SPUtil.getLong(mContext, SPConfig.SP_USER_INFO_FILE, SPConfig.KEY_LAST_OPEN_TIME, 0);
		// 首次打开APP 或者 今日首次登录
		MainActivity.sIsTodayFirstOpen = (lastOpenTime == 0 || !DateUtil.isToday(lastOpenTime));
		// 写入当前时间
		SPUtil.putLong(mContext, SPConfig.SP_USER_INFO_FILE, SPConfig.KEY_LAST_OPEN_TIME, System.currentTimeMillis());
	}

	/**
	 * 需要在此完成一些APP全局常量初始化的获取工作
	 */
	private void doInit() {
		AssistantApp assistantApp = AssistantApp.getInstance();
		if (assistantApp.isGlobalInit()) {
			return;
		}

		// 初始化网络下载模块
		assistantApp.initRetrofit();
		// 初始配置加载列表
		assistantApp.initLoadingView();

		// 初始化设备配置
		assistantApp.initAppConfig();
		// 初始化设备状态
		if (!MobileInfoModel.getInstance().isInit()) {
			CommonUtil.initMobileInfoModel(mContext);
		}
		// 判断是否金立首次打开APP
		judgeFirstOpenToday();

		try {
			OuwanSDKManager.getInstance().init();
		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(AppDebugConfig.TAG_APP, e);
			}
		}
		// 获取用户信息
		// 该信息使用salt加密存储再SharedPreference中
		UserModel user = null;
		try {
			String userJson = Global_SharePreferences.getStringFromSharedPreferences(mContext,
					SPConfig.SP_USER_INFO_FILE, SPConfig.KEY_SESSION, SPConfig.SALT_USER_INFO, null);
			if (AppDebugConfig.IS_DEBUG) {
				KLog.d(AppDebugConfig.TAG_APP, "get from sp: user = " + userJson);
			}
			user = assistantApp.getGson().fromJson(userJson, UserModel.class);
		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(AppDebugConfig.TAG_APP, e);
			}
		}
		AccountManager.getInstance().setUser(user);
		// 每次登录请求一次更新用户状态和数据
		AccountManager.getInstance().updateUserSession();

		assistantApp.setGlobalInit(true);
	}
}
