package net.ouwan.umipay.android.entry.gson;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import net.ouwan.umipay.android.api.UmipaySDKStatusCode;

/**
 * Gson_Cmd_ThirdLogin
 *
 * @author zacklpx
 *         date 15-4-29
 *         description
 */
public class Gson_Cmd_OauthLogin extends Gson_Base<Gson_Cmd_OauthLogin.Cmd_ThirdLogin_Data> {

	public Gson_Cmd_OauthLogin(Context context, int code, String message, Cmd_ThirdLogin_Data cmd_thirdLogin_data) {
		super(context, code, message, cmd_thirdLogin_data);
	}

	@Override
	public boolean checkData() {
		if (mCode == UmipaySDKStatusCode.SUCCESS && super.checkData()) {
			return !(mData.getUid() <= 0 || TextUtils.isEmpty(mData.getOpenid()) || TextUtils.isEmpty(mData.getSession
					()) || TextUtils.isEmpty(mData.getSign()) || TextUtils.isEmpty(mData.getUsername()));
		} else {
			return super.checkData();
		}
	}

	public class Cmd_ThirdLogin_Data {
		@SerializedName("uid")
		private int uid;
		@SerializedName("openid")
		private String openid;
		@SerializedName("session")
		private String session;
		@SerializedName("ts")
		private int ts;
		@SerializedName("url")
		private String url;
		@SerializedName("sign")
		private String sign;
		@SerializedName("username")
		private String username;
		@SerializedName("bindoauth")
		private int bindoauth;

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

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
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

		public int getBindoauth() {
			return bindoauth;
		}

		public void setBindoauth(int bindoauth) {
			this.bindoauth = bindoauth;
		}
	}
}