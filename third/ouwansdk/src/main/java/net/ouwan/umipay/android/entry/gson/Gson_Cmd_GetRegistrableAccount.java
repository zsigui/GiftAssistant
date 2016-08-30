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
public class Gson_Cmd_GetRegistrableAccount extends Gson_Base<Gson_Cmd_GetRegistrableAccount.Cmd_GetRegistrableAccount_Data> {
	public Gson_Cmd_GetRegistrableAccount(Context context, int code, String message, Cmd_GetRegistrableAccount_Data cmd_getRegistAccount_data) {
		super(context, code, message, cmd_getRegistAccount_data);
	}

	@Override
	public boolean checkData() {
		if (mCode == UmipaySDKStatusCode.SUCCESS && super.checkData()) {
			return !( TextUtils.isEmpty(mData.getUsername()) || TextUtils
					.isEmpty(mData.getPassword()));
		} else {
			return super.checkData();
		}
	}

	public class Cmd_GetRegistrableAccount_Data {
		@SerializedName("username")
		private String username;
		@SerializedName("password")
		private String password;

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
