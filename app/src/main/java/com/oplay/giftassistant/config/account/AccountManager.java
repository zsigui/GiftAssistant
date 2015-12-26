package com.oplay.giftassistant.config.account;

import android.text.TextUtils;

import com.oplay.giftassistant.model.UserModel;

/**
 * 管理用户账号信息的管理器 <br/>
 * Created by zsigui on 15-12-25.
 */
public class AccountManager {

	private static AccountManager manager;

	private AccountManager(){}

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

	public void setUser(UserModel user) {
		mUser = user;
	}

	public boolean isLogin() {
		return (mUser != null
				&& !TextUtils.isEmpty(mUser.session)
				&& mUser.sessionExpired > System.currentTimeMillis()
				&& mUser.uid != 0);
	}
}
