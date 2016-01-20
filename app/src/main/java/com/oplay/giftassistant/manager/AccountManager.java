package com.oplay.giftassistant.manager;

import android.content.Context;
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
import com.oplay.giftassistant.util.DateUtil;
import com.oplay.giftassistant.util.NetworkUtil;
import com.oplay.giftassistant.util.ToastUtil;
import com.oplay.giftassistant.util.encrypt.NetDataEncrypt;
import com.socks.library.KLog;

import net.youmi.android.libs.common.global.Global_SharePreferences;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * 管理用户账号信息的管理器 <br/>
 * Created by zsigui on 15-12-25.
 */
public class AccountManager {

	private static AccountManager manager;
	private static Context mContext = AssistantApp.getInstance().getApplicationContext();

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

		if (mUser != null && mUser.userInfo != null
				&& user != null && user.userInfo != null) {
			// 保留用户登录状态
			user.userInfo.loginType = mUser.userInfo.loginType;
		}
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
				if (isLogin()) {
					KLog.e("write session = " + getUserSesion().session);
				}
				String userJson = AssistantApp.getInstance().getGson().toJson(mUser, UserModel.class);
				Global_SharePreferences.saveEncodeStringToSharedPreferences(mContext,
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
					if (!NetworkUtil.isConnected(mContext)) {
						return;
					}
					KLog.e("update User start");
					ToastUtil.showShort("update User start");
					Global.getNetEngine().getUserInfo(new JsonReqBase<String>(null))
							.enqueue(new Callback<JsonRespBase<UserModel>>() {
								@Override
								public void onResponse(Response<JsonRespBase<UserModel>> response, Retrofit retrofit) {
									if (response != null && response.isSuccess()) {
										if (response.body() != null
												&& response.body().getCode() == StatusCode.SUCCESS) {
											UserModel user = getUser();
											user.userInfo = response.body().getData().userInfo;
											setUser(user);
											return;
										}
										if (AppDebugConfig.IS_DEBUG) {
											KLog.e(AppDebugConfig.TAG_MANAGER,
													response.body() == null ? "解析失败" : response.body().error());
										}
									}
								}

								@Override
								public void onFailure(Throwable t) {
									if (AppDebugConfig.IS_DEBUG) {
										KLog.e(AppDebugConfig.TAG_MANAGER, t);
									}
								}
							});
				}
			});
		}
	}

	/**
	 * 同步Cookie
	 */
	public void syncCookie(String baseUrl, ArrayList<String> cookies) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			CookieManager.getInstance().removeAllCookies(null);
			if (cookies != null && !cookies.isEmpty()) {
				for (String cookie : cookies) {
					CookieManager.getInstance().setCookie(baseUrl, cookie);
				}
			}
			CookieManager.getInstance().flush();
		} else {
			CookieSyncManager.createInstance(mContext);
			CookieManager.getInstance().setAcceptCookie(true);
			CookieManager.getInstance().removeAllCookie();
			if (cookies != null && !cookies.isEmpty()) {
				for (String cookie : cookies) {
					CookieManager.getInstance().setCookie(baseUrl, cookie);
				}
			}
			CookieSyncManager.getInstance().sync();
		}
	}


	public void syncCookie() {
		ArrayList<String> cookies = null;
		if (isLogin()) {
			cookies = new ArrayList<>();
			cookies.add("cuid=" + getUserSesion().uid + ";");
			cookies.add("sessionid=" + getUserSesion().session + ";");
		}
		syncCookie(WebViewUrl.URL_BASE, cookies);
	}

	/**
	 * 更新用户的Session
	 */
	public void updateUserSession() {
		if (isLogin()) {
			// 当天登录过，无须再次重新更新 session
			if (DateUtil.isToday(getUserSesion().lastUpdateTime)) {
				return;
			}
			// session只能保持7天，一旦超时，需要重新登录
			if (getUserSesion().lastUpdateTime + 7 * 24 * 60 * 60 * 1000 > System.currentTimeMillis()) {
				KLog.e("lastUpdateTime = " + getUserSesion().lastUpdateTime
						+ ", currentTime = " + System.currentTimeMillis());
				ToastUtil.showShort("登录超时，需要重新登录");
				setUser(null);
				return;
			}
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
			if (NetworkUtil.isConnected(mContext)) {
				Global.getNetEngine().updateSession(new JsonReqBase<String>())
						.enqueue(new Callback<JsonRespBase<UpdateSesion>>() {

							@Override
							public void onResponse(Response<JsonRespBase<UpdateSesion>> response,
							                       Retrofit retrofit) {
								if (response != null && response.isSuccess()) {
									if(response.body() != null && response.body().isSuccess()) {
										mUpdateSessionRetryTime = 0;
										mUser.userSession.session = response.body().getData().session;
										KLog.e("req_new_session = " + mUser.userSession.session);
										setUser(mUser);
										// 请求更新数据
										updateUserInfo();
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
