package com.oplay.giftcool.manager;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.SPConfig;
import com.oplay.giftcool.config.StatusCode;
import com.oplay.giftcool.config.UserTypeUtil;
import com.oplay.giftcool.config.WebViewUrl;
import com.oplay.giftcool.model.MobileInfoModel;
import com.oplay.giftcool.model.data.resp.UpdateSession;
import com.oplay.giftcool.model.data.resp.UserInfo;
import com.oplay.giftcool.model.data.resp.UserModel;
import com.oplay.giftcool.model.data.resp.UserSession;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.util.DateUtil;
import com.oplay.giftcool.util.NetworkUtil;
import com.oplay.giftcool.util.SPUtil;
import com.oplay.giftcool.util.ToastUtil;
import com.oplay.giftcool.util.encrypt.NetDataEncrypt;
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

	public static AccountManager getInstance() {
		if (manager == null) {
			manager = new AccountManager();
		}
		return manager;
	}

	private UserModel mUser;
	private int lastLoginType = UserTypeUtil.TYPE_POHNE;

	public boolean isPhoneLogin() {
		return lastLoginType == UserTypeUtil.TYPE_POHNE;
	}

	public UserModel getUser() {
		return mUser;
	}

	/**
	 * 设置当前用户，会引起监听该变化的接口调用进行通知
	 */
	public void setUser(UserModel user) {
		if (mUser != null && mUser.userInfo != null) {
			lastLoginType = mUser.userInfo.loginType;
			if (user != null && user.userInfo != null) {
				// 保留用户登录状态
				user.userInfo.loginType = mUser.userInfo.loginType;
			}
		} else {
			lastLoginType = UserTypeUtil.TYPE_POHNE;
		}
		mUser = user;
		// 当用户变化，需要进行通知
		ObserverManager.getInstance().notifyUserUpdate();
		ObserverManager.getInstance().notifyGiftUpdate(ObserverManager.STATUS.GIFT_UPDATE_ALL);


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

		Global.THREAD_POOL.execute(new Runnable() {
			@Override
			public void run() {
				// 写入SP中
				String userJson = AssistantApp.getInstance().getGson().toJson(mUser, UserModel.class);
				Global_SharePreferences.saveEncodeStringToSharedPreferences(mContext,
						SPConfig.SP_USER_INFO_FILE, SPConfig.KEY_USER_INFO, userJson, SPConfig.SALT_USER_INFO);
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
					Global.getNetEngine().getUserInfo(new JsonReqBase<Void>())
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
										// 登录状态失效，原因包括: 已在其他地方登录，更新失败
										sessionFailed(response.body());
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

	private void sessionFailed(JsonRespBase response) {
		if (response != null && response.getCode() == StatusCode.ERR_UN_LOGIN) {
			setUser(null);
			ToastUtil.showShort(mContext.getResources().getString(R.string.st_hint_un_login));
		}
	}

	/**
	 * 更新用户部分信息: 偶玩豆，积分，礼包数
	 */
	public void updatePartUserInfo() {
		if (isLogin()) {
			Global.THREAD_POOL.execute(new Runnable() {
				@Override
				public void run() {
					if (!NetworkUtil.isConnected(mContext)) {
						return;
					}

					Global.getNetEngine().getUserPartInfo(new JsonReqBase<Void>())
							.enqueue(new Callback<JsonRespBase<UserInfo>>() {
								@Override
								public void onResponse(Response<JsonRespBase<UserInfo>> response, Retrofit retrofit) {
									if (response != null && response.isSuccess()) {
										if (response.body() != null
												&& response.body().getCode() == StatusCode.SUCCESS) {
											UserInfo info = response.body().getData();
											UserModel user = getUser();
											user.userInfo.score = info.score;
											user.userInfo.bean = info.bean;
											user.userInfo.giftCount = info.giftCount;
											KLog.d(AppDebugConfig.TAG_MANAGER, "金额= " + info.score + ", " + info.bean
													+ ", " + info.giftCount);
											setUser(user);
											return;
										}
										if (AppDebugConfig.IS_DEBUG) {
											KLog.e(AppDebugConfig.TAG_MANAGER,
													response.body() == null ? "解析失败" : response.body().error());
										}
										sessionFailed(response.body());
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
			cookies = new ArrayList<String>();
			String expiredDate = DateUtil.getGmtDate(48);
			cookies.add(String.format("cuid=%s;Domain=%s;Expires=%s;Path=/;HttpOnly",
					getUserSesion().uid, WebViewUrl.URL_DOMAIN, expiredDate));
			cookies.add(String.format("sessionid=%s;Domain=%s;Expires=%s;Path=/;HttpOnly",
					getUserSesion().session, WebViewUrl.URL_DOMAIN, expiredDate));
			cookies.add(String.format("chnid=%d;Domain=%s;Expires=%s;Path=/;HttpOnly",
					AssistantApp.getInstance().getChannelId(), WebViewUrl.URL_DOMAIN, expiredDate));
			cookies.add(String.format("cid=%s;Domain=%s;Expires=%s;Path=/;HttpOnly",
					MobileInfoModel.getInstance().getCid(), WebViewUrl.URL_DOMAIN, expiredDate));
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
				// 只更新用户信息
				updateUserInfo();
				return;
			}
			// session只能保持7天，一旦超时，需要重新登录
			if (getUserSesion().lastUpdateTime + 7 * 24 * 60 * 60 * 1000 > System.currentTimeMillis()) {
				ToastUtil.showShort("登录超时，需要重新登录");
				setUser(null);
				return;
			}
			NetDataEncrypt.getInstance().initDecryptDataModel(getUserSesion().uid, getUserSesion().session);
			updateSessionNetRequest();
		}
	}

	private void updateSessionNetRequest() {
		if (NetworkUtil.isConnected(mContext)) {
			Global.getNetEngine().updateSession(new JsonReqBase<String>())
					.enqueue(new Callback<JsonRespBase<UpdateSession>>() {

						@Override
						public void onResponse(Response<JsonRespBase<UpdateSession>> response,
						                       Retrofit retrofit) {
							if (response != null && response.isSuccess()) {
								if (response.body() != null) {
									if (response.body().isSuccess()) {
										mUser.userSession.session = response.body().getData().session;
										if (AppDebugConfig.IS_DEBUG) {
											KLog.d("req_new_session = " + mUser.userSession.session);
										}
										setUser(mUser);
										// 请求更新数据
										updateUserInfo();
										return;
									}
									if (response.body().getCode() == StatusCode.ERR_UN_LOGIN) {
										// 更新session不同步
										if (AppDebugConfig.IS_DEBUG) {
											KLog.d("session is not sync, err msg = " + response.body().getMsg());
										}
										// 重置登录信息，表示未登录
										setUser(null);
										return;
									}
								}
							}
							if (AppDebugConfig.IS_DEBUG) {
								KLog.e(AppDebugConfig.TAG_MANAGER, "failed to update session");
							}
						}

						@Override
						public void onFailure(Throwable t) {
							if (AppDebugConfig.IS_DEBUG) {
								KLog.e(AppDebugConfig.TAG_MANAGER, t);
								KLog.e(AppDebugConfig.TAG_MANAGER, "failed to update session");
							}
						}
					});
		}
	}

	public void logout() {
		if (!isLogin()) {
			return;
		}
		Global.THREAD_POOL.execute(new Runnable() {
			@Override
			public void run() {
				Global.getNetEngine().logout(new JsonReqBase<Object>()).enqueue(new Callback<Void>() {
					@Override
					public void onResponse(Response<Void> response, Retrofit retrofit) {
						if (AppDebugConfig.IS_FRAG_DEBUG) {
							KLog.e(response == null ? "login response null" : response.code());
						}
					}

					@Override
					public void onFailure(Throwable t) {
						if (AppDebugConfig.IS_FRAG_DEBUG) {
							KLog.e(t);
						}
					}
				});
			}
		});
		AccountManager.getInstance().setUser(null);
	}

	public ArrayList<String> obtainPhoneAccount() {
		ArrayList<String> result = new ArrayList<String>();
		return result;
	}

	public ArrayList<String> obtainOuwanAccount() {
		return null;
	}

	public void writePhoneAccount(String val, ArrayList<String> history, boolean isRemove) {
		writeToHistory(val, SPConfig.KEY_LOGIN_PHONE, history, isRemove);
	}

	public void writeOuwanAccount(String val, ArrayList<String> history, boolean isRemove) {
		writeToHistory(val, SPConfig.KEY_LOGIN_OUWAN, history, isRemove);
	}

	public ArrayList<String> readPhoneAccount() {
		return readFromHistory(SPConfig.KEY_LOGIN_PHONE);
	}

	public ArrayList<String> readOuwanAccount() {
		return readFromHistory(SPConfig.KEY_LOGIN_OUWAN);
	}

	private void writeToHistory(String value, String key, ArrayList<String> history, boolean isRemove) {
		if (TextUtils.isEmpty(value) || (value.contains(",") && value.indexOf(",") == 0)) {
			return;
		}
		if (history == null) {
			history = new ArrayList<>();
		}
		if (isRemove) {
			history.remove(value);
		} else {
			for (int i = history.size() - 1; i >= 0; i--) {
				String s = history.get(i);
				if (s.split(",")[0].equals(value.split(",")[0])) {
					history.remove(i);
					break;
				}
			}
			if (history.size() > 15) {
				history.remove(14);
			}
			history.add(0, value);
		}
		StringBuilder historyStr = new StringBuilder();
		for (String s : history) {
			historyStr.append(s).append(":");
		}
		if (historyStr.length() > 0) {
			historyStr.deleteCharAt(historyStr.length() - 1);
		}
		SPUtil.putString(mContext, SPConfig.SP_LOGIN_FILE, key, historyStr.toString());
	}

	private ArrayList<String> readFromHistory(String key) {
		String history = SPUtil.getString(mContext, SPConfig.SP_LOGIN_FILE, key, null);
		ArrayList<String> result = null;
		if (history != null) {
			String[] names = history.split(":");
			result = new ArrayList<String>();
			for (String n : names) {
				if (!TextUtils.isEmpty(n) && (!n.contains(",") || n.indexOf(",") != 0)) {
					result.add(n);
				}
			}
		}
		return result;
	}
}
