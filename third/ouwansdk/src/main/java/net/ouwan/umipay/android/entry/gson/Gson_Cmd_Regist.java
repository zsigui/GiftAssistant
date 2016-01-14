package net.ouwan.umipay.android.entry.gson;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import net.ouwan.umipay.android.api.UmipaySDKStatusCode;

/**
 * Gson_Cmd_Regist
 *
 * @author zacklpx
 *         date 15-4-28
 *         description
 */
public class Gson_Cmd_Regist extends Gson_Base<Gson_Cmd_Regist.Cmd_Regist_Data> {

	public Gson_Cmd_Regist(Context context, int code, String message, Cmd_Regist_Data cmd_regist_data) {
		super(context, code, message, cmd_regist_data);
	}

	@Override
	public boolean checkData() {
		if (mCode == UmipaySDKStatusCode.SUCCESS && super.checkData()) {
			return !(mData.getUid() <= 0 || TextUtils.isEmpty(mData.getOpenid()) || TextUtils.isEmpty(mData
					.getSession())
					|| TextUtils.isEmpty(mData.getSign()));
		} else {
			return super.checkData();
		}
	}

	public class Cmd_Regist_Data {
		@SerializedName("uid")
		int uid;
		@SerializedName("openid")
		String openid;
		@SerializedName("session")
		String session;
		@SerializedName("ts")
		int ts;
		@SerializedName("url")
		String url;
		@SerializedName("sign")
		String sign;

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
	}
}
