package com.oplay.giftassistant.manager;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.oplay.giftassistant.AssistantApp;
import com.oplay.giftassistant.config.AppDebugConfig;
import com.oplay.giftassistant.config.Global;
import com.oplay.giftassistant.config.SPConfig;
import com.oplay.giftassistant.config.StatusCode;
import com.oplay.giftassistant.config.WebViewUrl;
import com.oplay.giftassistant.model.data.resp.UpdateSesion;
import com.oplay.giftassistant.model.data.resp.UserInfo;
import com.oplay.giftassistant.model.data.resp.UserModel;
import com.oplay.giftassistant.model.data.resp.UserSession;
import com.oplay.giftassistant.model.json.base.JsonReqBase;
import com.oplay.giftassistant.model.json.base.JsonRespBase;
import com.oplay.giftassistant.util.NetworkUtil;
import com.oplay.giftassistant.util.encrypt.NetDataEncrypt;
import com.socks.library.KLog;

import net.youmi.android.libs.common.global.Global_SharePreferences;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * 管理用户账号信息的管理器 <br/>
 * Created by zsigui on 15-12-25.
 */
public class AccountManager {

	private static AccountManager manager;

	private AccountManager() {
	}

	private Handler mHandler = new Handler(Looper.getMainLooper());

	public static AccountManager getInstance() {
		if (manager == null) {
			manager = new AccountManager();
		}
		return manager;
	}

	private UserModel mUser;

	public UserModel getUser() {
		return mUser;
	}

	/**
	 * 设置当前用户，会引起监听该变化的接口调用进行通知
	 */
	public void setUser(UserModel user) {
		mUser = user;
		// 当用户变化，需要进行通知
		ObserverManager.getInstance().notifyUserUpdate();
		ObserverManager.getInstance().notifyGiftUpdate();

		// 同步cookie
		syncCookie();
		if (user != null) {
			NetDataEncrypt.getInstance().initDecryptDataModel(getUserSesion().uid, getUserSesion().session);
		} else {
			NetDataEncrypt.getInstance().initDecryptDataModel(0, "");
		}

		if (isLogin()) {
			OuwanSDKManager.getInstance().login();
		} else {
			OuwanSDKManager.getInstance().logout();
		}

		// 如果再更新状态过程中用户退出登录，直接取消重试
		mHandler.removeCallbacks(mUpdateSessionTask);
		Global.THREAD_POOL.execute(new Runnable() {
			@Override
			public void run() {
				// 写入SP中
				String userJson = AssistantApp.getInstance().getGson().toJson(mUser, UserModel.class);
				Global_SharePreferences.saveEncodeStringToSharedPreferences(AssistantApp.getInstance().getApplicationContext(),
						SPConfig.SP_USER_INFO_FILE, SPConfig.KEY_SESSION, userJson, SPConfig.SALT_USER_INFO);
				if (AppDebugConfig.IS_DEBUG) {
					KLog.d(AppDebugConfig.TAG_MANAGER, "save to sp : " + userJson);
				}
			}
		});
	}

	public boolean isLogin() {
		return (mUser != null
				&& !TextUtils.isEmpty(getUserSesion().session)
				&& getUserInfo().uid != 0);
	}

	public UserInfo getUserInfo() {
		return mUser == null ? null : mUser.userInfo;
	}

	public UserSession getUserSesion() {
		return mUser == null ? null : mUser.userSession;
	}


	public void updateUserInfo() {
		if (isLogin()) {
			NetDataEncrypt.getInstance().initDecryptDataModel(getUserSesion().uid, getUserSesion().session);
			Global.THREAD_POOL.execute(new Runnable() {
				@Override
				public void run() {

				}
			});
		}
	}

	/**
	 * 同步Cookie
	 */
	public void syncCookie(String baseUrl, String cookieVal) {
		CookieManager.getInstance().setAcceptCookie(true);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			CookieManager.getInstance().setCookie(baseUrl, cookieVal);
			CookieManager.getInstance().flush();
		} else {
			CookieManager.getInstance().setCookie(baseUrl, cookieVal);
			CookieSyncManager.getInstance().sync();
		}
	}

	public void syncCookie() {
		if (isLogin()) {
			String cookie = "cuid=" + getUserSesion().uid + ";" +
					"sessionid=" +getUserSesion().session + ";";
			KLog.e("cookie", cookie);
			syncCookie(WebViewUrl.BASE_URL, cookie);
		} else {
			syncCookie(WebViewUrl.BASE_URL, "");
		}
	}

	/**
	 * 更新用户的Session
	 */
	public void updateUserSession() {
		if (isLogin()) {
			NetDataEncrypt.getInstance().initDecryptDataModel(getUserSesion().uid, getUserSesion().session);
			mHandler.postAtFrontOfQueue(mUpdateSessionTask);
		}
	}
	/**
	 * 重试更新Session
	 */
	private int mUpdateSessionRetryTime = 0;
	private Runnable mUpdateSessionTask = new Runnable() {
		@Override
		public void run() {
			if (NetworkUtil.isConnected(AssistantApp.getInstance().getApplicationContext())) {
				Global.getNetEngine().updateSession(new JsonReqBase<String>("0"))
						.enqueue(new Callback<JsonRespBase<UpdateSesion>>() {

							@Override
							public void onResponse(Response<JsonRespBase<UpdateSesion>> response,
							                       Retrofit retrofit) {
								if (response != null && response.isSuccess()) {
									if(response.body() != null && response.body().getCode() == StatusCode.SUCCESS) {
										mUpdateSessionRetryTime = 0;
										mUser.userSession.session = response.body().getData().session;
										return;
									}

								}
								retryJudge();
							}

							@Override
							public void onFailure(Throwable t) {
								if (AppDebugConfig.IS_DEBUG) {
									KLog.e(AppDebugConfig.TAG_MANAGER, t);
								}
								retryJudge();
							}
						});
			} else {
				// 暂时无网络
				retryJudge();
			}
		}
	};

	private void retryJudge() {
		// 只重试3次
		if (mUpdateSessionRetryTime < 3) {
			// 请求失败, 5秒后再请求
			mHandler.postAtTime(mUpdateSessionTask, 5000);
		}
		mUpdateSessionRetryTime++;
	}
}
