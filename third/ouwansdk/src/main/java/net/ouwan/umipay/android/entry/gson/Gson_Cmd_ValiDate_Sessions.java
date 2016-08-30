package net.ouwan.umipay.android.entry.gson;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import net.ouwan.umipay.android.api.UmipaySDKStatusCode;

import org.json.JSONArray;

import java.util.ArrayList;

/**
 * Gson_Cmd_ValiDate_Sessions
 *
 * @author jimmy
 *         date 15-4-24
 *         description
 */
public class Gson_Cmd_ValiDate_Sessions extends Gson_Base<JSONArray> {

	public Gson_Cmd_ValiDate_Sessions(Context context, int code, String message, JSONArray cmd_session_convert_data) {
		super(context, code, message, cmd_session_convert_data);
	}

	@Override
	public boolean checkData() {
		if (mCode == UmipaySDKStatusCode.SUCCESS && super.checkData()) {
			return !(mData == null);
		} else {
			return super.checkData();
		}
	}

	public class Cmd_ValiDate_Sessions_Data {
		@SerializedName("uid")
		private int uid;
		@SerializedName("sid")
		private String sid;
		@SerializedName("type")
		private int type;
		@SerializedName("username")
		private String username;

		public int getUid() {
			return uid;
		}

		public void setUid(int uid) {
			this.uid = uid;
		}

		public String getSid() {
			return sid;
		}

		public void setSid(String session) {
			this.sid = session;
		}

		public int getType() {
			return type;
		}

		public void setType(int type) {
			this.type = type;
		}

		public String getUserName() {
			return this.username;
		}

		public void setUserName(String username) {
			this.username = username;
		}
	}
}
