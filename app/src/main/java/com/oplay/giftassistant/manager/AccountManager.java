package com.oplay.giftassistant.manager;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.oplay.giftassistant.AssistantApp;
import com.oplay.giftassistant.config.Global;
import com.oplay.giftassistant.model.UserModel;
import com.oplay.giftassistant.model.json.base.JsonReqBase;
import com.oplay.giftassistant.model.json.base.JsonRespBase;
import com.oplay.giftassistant.util.NetworkUtil;
import com.oplay.giftassistant.util.ToastUtil;
import com.oplay.giftassistant.util.encrypt.NetDataEncrypt;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * 管理用户账号信息的管理器 <br/>
 * Created by zsigui on 15-12-25.
 */
public class AccountManager {

	private static AccountManager manager;

	private AccountManager(){}

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
		// 如果再更新状态过程中用户退出登录，直接取消重试
		mHandler.removeCallbacks(mUpdateSessionTask);
	}

	public boolean isLogin() {
		return (mUser != null
				&& !TextUtils.isEmpty(mUser.session)
				&& mUser.uid != 0);
	}

	public void updateUserSession() {
		if (isLogin()) {
			NetDataEncrypt.getInstance().initDecryptDataModel(mUser.uid, mUser.session);
			mHandler.postAtFrontOfQueue(mUpdateSessionTask);
		}
	}

	private int mUpdateSessionRetryTime = 0;
	private Runnable mUpdateSessionTask = new Runnable() {
		@Override
		public void run() {
			if (NetworkUtil.isConnected(AssistantApp.getInstance().getApplicationContext())) {
				Global.getNetEngine().updateSession(new JsonReqBase<Object>())
						.enqueue(new Callback<JsonRespBase<UserModel>>() {

							@Override
							public void onResponse(Response<JsonRespBase<UserModel>> response,
							                       Retrofit retrofit) {
								if (response != null && response.isSuccess()) {
									mUpdateSessionRetryTime = 0;
									setUser(response.body().getData());
								} else {
									retryJudge();
								}
							}

							@Override
							public void onFailure(Throwable t) {
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
		ToastUtil.showShort("网络连接失败");
		if (mUpdateSessionRetryTime < 3) {
			// 请求失败, 5秒后再请求
			mHandler.postAtTime(mUpdateSessionTask, 5000);
		} else if (mUpdateSessionRetryTime < 10){
			// 10次以内, 10秒后再请求
			mHandler.postAtTime(mUpdateSessionTask, 10 * 1000);
		} else if (mUpdateSessionRetryTime < 60) {
			// 60次，1分钟后请求
			mHandler.postAtTime(mUpdateSessionTask, 60 * 1000);
		}
		mUpdateSessionRetryTime++;
	}
}
