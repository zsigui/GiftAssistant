package net.ouwan.umipay.android.entry.gson;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import net.ouwan.umipay.android.api.UmipaySDKStatusCode;

/**
 * Gson_Cmd_QuickRegist
 *
 * @author zacklpx
 *         date 15-4-28
 *         description
 */
public class Gson_Cmd_QuickRegist extends Gson_Base<Gson_Cmd_QuickRegist.Cmd_QuickRegist_Data> {
	public Gson_Cmd_QuickRegist(Context context, int code, String message, Cmd_QuickRegist_Data cmd_quickRegist_data) {
		super(context, code, message, cmd_quickRegist_data);
	}

	@Override
	public boolean checkData() {
		if (mCode == UmipaySDKStatusCode.SUCCESS && super.checkData()) {
			return !(mData.getUid() <= 0 || TextUtils.isEmpty(mData.getOpenid()) || TextUtils.isEmpty(mData.getSession
					()) || TextUtils.isEmpty(mData.getSign()) || TextUtils.isEmpty(mData.getUsername()) || TextUtils
					.isEmpty(mData.getPassword()));
		} else {
			return super.checkData();
		}
	}

	public class Cmd_QuickRegist_Data {
		@SerializedName("uid")
		private int uid;
		@SerializedName("openid")
		private String openid;
		@SerializedName("session")
		private String session;
		@SerializedName("ts")
		private int ts;
		@SerializedName("sign")
		private String sign;
		@SerializedName("username")
		private String username;
		@SerializedName("password")
		private String password;

		public int getUid() {
			return uid;
		}

		public void setUid(int uid) {
			this.uid = uid;
		}

		public String getOpenid() {
			return openid;
		}

		public void setOpenid(String openid) {
			this.openid = openid;
		}

		public String getSession() {
			return session;
		}

		public void setSession(String session) {
			this.session = session;
		}

		public int getTs() {
			return ts;
		}

		public void setTs(int ts) {
			this.ts = ts;
		}

		public String getSign() {
			return sign;
		}

		public void setSign(String sign) {
			this.sign = sign;
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}
	}
}
