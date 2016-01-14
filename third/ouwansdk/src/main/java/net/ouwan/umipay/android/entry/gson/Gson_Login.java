package net.ouwan.umipay.android.entry.gson;

import android.content.Context;

import net.ouwan.umipay.android.entry.UmipayAccount;

/**
 * Gson_Login
 *
 * @author zacklpx
 *         date 15-4-28
 *         description
 */
public class Gson_Login extends Gson_Base<Gson_Login.Login_Data> {
	public Gson_Login(Context context, int code, String message, Login_Data loginData) {
		super(context, code, message, loginData);
	}

	public class Login_Data {

		private int loginType;
		private UmipayAccount account;

		public int getLoginType() {
			return loginType;
		}

		public void setLoginType(int loginType) {
			this.loginType = loginType;
		}

		public UmipayAccount getAccount() {
			return account;
		}

		public void setAccount(UmipayAccount account) {
			this.account = account;
		}
	}
}
