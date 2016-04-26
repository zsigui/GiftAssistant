package com.oplay.giftcool.asynctask;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;
import android.os.Build;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.config.AppConfig;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.SPConfig;
import com.oplay.giftcool.download.ApkDownloadManager;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.manager.OuwanSDKManager;
import com.oplay.giftcool.manager.PushMessageManager;
import com.oplay.giftcool.manager.StatisticsManager;
import com.oplay.giftcool.model.MobileInfoModel;
import com.oplay.giftcool.model.data.req.AppBaseInfo;
import com.oplay.giftcool.model.data.req.ReqInitApp;
import com.oplay.giftcool.model.data.req.ReqReportedInfo;
import com.oplay.giftcool.model.data.resp.InitAppResult;
import com.oplay.giftcool.model.data.resp.UserModel;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.activity.MainActivity;
import com.oplay.giftcool.util.AppInfoUtil;
import com.oplay.giftcool.util.CommonUtil;
import com.oplay.giftcool.util.DateUtil;
import com.oplay.giftcool.util.SPUtil;
import com.socks.library.KLog;

import net.youmi.android.libs.common.global.Global_SharePreferences;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


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
		} catch (Throwable e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(e);
			}
		}
		return null;
	}

	/**
	 * 在此处进行对旧版的处理操作
	 */
	public void doClearWorkForOldVer() {
		int oldVer = SPUtil.getInt(mContext, SPConfig.SP_APP_CONFIG_FILE, SPConfig.KEY_STORE_VER, AppConfig.SDK_VER);
		if (oldVer != AppConfig.SDK_VER) {
			if (oldVer < 3) {
				// 清除旧版的账号存储信息
				SPUtil.putString(mContext, SPConfig.SP_USER_INFO_FILE, SPConfig.KEY_LOGIN_PHONE, "");
				SPUtil.putString(mContext, SPConfig.SP_USER_INFO_FILE, SPConfig.KEY_LOGIN_OUWAN, "");
			}
			// 写入最新版本信息
			SPUtil.putInt(mContext, SPConfig.SP_APP_CONFIG_FILE, SPConfig.KEY_STORE_VER, AppConfig.SDK_VER);
			// 清空今日登录状态
			SPUtil.putLong(mContext, SPConfig.SP_USER_INFO_FILE, SPConfig.KEY_LOGIN_LAST_OPEN_TIME, 0);
			MainActivity.sIsTodayFirstOpenForBroadcast = MainActivity.sIsTodayFirstOpen = true;
		}
	}


	/**
	 * 判断是否今日首次登录
	 */
	public boolean judgeFirstOpenToday() {
		long lastOpenTime = SPUtil.getLong(mContext, SPConfig.SP_USER_INFO_FILE, SPConfig.KEY_LOGIN_LAST_OPEN_TIME, 0);
		// 首次打开APP 或者 今日首次登录
		// 写入当前时间
		SPUtil.putLong(mContext, SPConfig.SP_USER_INFO_FILE, SPConfig.KEY_LOGIN_LAST_OPEN_TIME, System
				.currentTimeMillis());
		return (lastOpenTime == 0 || !DateUtil.isToday(lastOpenTime));
	}

	/**
	 * 需要在此完成一些APP全局常量初始化的获取工作
	 */
	private void doInit() {
		AssistantApp assistantApp = AssistantApp.getInstance();
		if (assistantApp.isGlobalInit()) {
			return;
		}

		// 存储打开APP时间
		SPUtil.putLong(assistantApp, SPConfig.SP_APP_CONFIG_FILE,
				SPConfig.KEY_LAST_OPEN_APP_TIME, System.currentTimeMillis());

		// 初始化统计工具
		StatisticsManager.getInstance().init(assistantApp, assistantApp.getChannelId());
		// 初始化推送SDK
		PushMessageManager.getInstance().initPush(assistantApp);


		// 初始化设备配置
		assistantApp.initAppConfig();
		// 初始化设备状态
		if (!MobileInfoModel.getInstance().isInit()) {
			CommonUtil.initMobileInfoModel(mContext);
		}

		// 初始化网络下载模块, 需要在initMobileInfoModel后设置
		assistantApp.initRetrofit();
		Global.resetNetEngine();
		// 初始化配置，获取更新信息
		if (!initAndCheckUpdate()) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e("initAndCheckUpdate failed!");
			}
		}

		doClearWorkForOldVer();
		// 判断是否今日首次打开APP
		if (judgeFirstOpenToday()) {
			// 进行应用信息上报
			reportedAppInfo();
		}

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
					SPConfig.SP_USER_INFO_FILE, SPConfig.KEY_USER_INFO, SPConfig.SALT_USER_INFO, null);
			if (AppDebugConfig.IS_DEBUG) {
				KLog.d(AppDebugConfig.TAG_APP, "get from sp: user = " + userJson);
			}
			user = assistantApp.getGson().fromJson(userJson, UserModel.class);
			if (user != null && user.userInfo != null) {
				// 将首次登录状态清掉，再次获取已经不属于首次登录
				user.userInfo.isFirstLogin = false;
			}
		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(AppDebugConfig.TAG_APP, e);
			}
		}
		AccountManager.getInstance().notifyUserAll(user);
		// 每次登录请求一次更新用户状态和数据
		AccountManager.getInstance().updateUserSession();


		assistantApp.setGlobalInit(true);
	}

	private boolean initAndCheckUpdate() {
		ReqInitApp data = new ReqInitApp();
		data.curVersionCode = AppInfoUtil.getAppVerCode(mContext);
		JsonReqBase<ReqInitApp> reqData = new JsonReqBase<>(data);
		try {
			Response<JsonRespBase<InitAppResult>> response = Global.getNetEngine().initAPP(reqData).execute();
			if (response != null && response.isSuccessful()) {
				if (response.body() != null && response.body().isSuccess()) {
					InitAppResult initData = response.body().getData();
					if (initData != null) {
						if (initData.initAppConfig != null) {
							AssistantApp.getInstance().setAllowDownload(initData.initAppConfig
									.isShowDownload);
							AssistantApp.getInstance().setQQInfo(initData.initAppConfig.qqInfo);
							AssistantApp.getInstance().setStartImg(initData.initAppConfig
									.startImgUrl);
							AssistantApp.getInstance().setBroadcastBanner(initData.initAppConfig
									.broadcastBanner);
						}
						if (initData.updateInfo != null) {
							AssistantApp.getInstance().setUpdateInfo(initData.updateInfo);
						}
						return true;
					}
				}
				KLog.d(AppDebugConfig.TAG_WARN, response.body() == null ? "解析失败" : response.body().error());
				return false;
			}
			KLog.d(AppDebugConfig.TAG_WARN, response == null ? "返回失败" : response.message());
		} catch (Throwable e) {
			if (AppDebugConfig.IS_DEBUG) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * 上报应用信息
	 */
	private void reportedAppInfo() {
		Global.THREAD_POOL.execute(new Runnable() {
			@Override
			public void run() {
				ReqReportedInfo info = new ReqReportedInfo();
				info.brand = Build.MODEL;
				info.osVersion = Build.VERSION.RELEASE;
				info.sdkVersion = String.valueOf(Build.VERSION.SDK_INT);
				info.appInfos = getAppInfos(AssistantApp.getInstance().getApplicationContext());
				final JsonReqBase<ReqReportedInfo> reqData = new JsonReqBase<ReqReportedInfo>(info);
				Global.getNetEngine().reportedAppInfo(reqData)
						.enqueue(new Callback<JsonRespBase<Void>>() {
							@Override
							public void onResponse(Call<JsonRespBase<Void>> call, Response<JsonRespBase<Void>>
									response) {
								if (response != null && response.isSuccessful()
										&& response.body() != null && response.body().isSuccess()) {
									// 执行上报成功
									if (AppDebugConfig.IS_DEBUG) {
										KLog.d(AppDebugConfig.TAG_APP, "信息上报成功");
									}
								}
								AppDebugConfig.warnResp(AppDebugConfig.TAG_UTIL, response);
//								ThreadUtil.runOnUiThread(new Runnable() {
//									@Override
//									public void run() {
//										reportedAppInfo();
//									}
//								}, 30 * 1000);
							}

							@Override
							public void onFailure(Call<JsonRespBase<Void>> call, Throwable t) {
								AppDebugConfig.warn(AppDebugConfig.TAG_UTIL, t);
								// 上报失败，等待30秒后继续执行
//								ThreadUtil.runOnUiThread(new Runnable() {
//									@Override
//									public void run() {
//										reportedAppInfo();
//									}
//								}, 30 * 1000);
							}
						});
			}
		});
	}

	/**
	 * 获取已安装应用信息
	 */
	private ArrayList<AppBaseInfo> getAppInfos(Context context) {
		ArrayList<AppBaseInfo> result = new ArrayList<>();
		List<PackageInfo> packages = context.getPackageManager().getInstalledPackages(0);
		for (int i = 0; i < packages.size(); i++) {
			try {
				final PackageInfo packageInfo = packages.get(i);
				final AppBaseInfo info = new AppBaseInfo();
				info.name = packageInfo.applicationInfo.loadLabel(context.getPackageManager()).toString();
				info.pkg = packageInfo.packageName;
				info.vc = String.valueOf(packageInfo.versionCode);
				info.vn = packageInfo.versionName;
				result.add(info);
			} catch (Exception e) {
				if (AppDebugConfig.IS_DEBUG) {
					KLog.d(AppDebugConfig.TAG_UTIL, e);
				}
			}
		}
		return result;
	}
}
