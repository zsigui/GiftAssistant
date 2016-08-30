package net.ouwan.umipay.android.entry.gson;

import android.content.Context;
import android.text.TextUtils;

import net.ouwan.umipay.android.api.UmipaySDKStatusCode;

/**
 * Gson_Cmd_VerificateSMS
 *
 * @author zacklpx
 *         date 15-4-27
 *         description
 */
public class Gson_Cmd_Bind_Oauth extends Gson_Base {
	public Gson_Cmd_Bind_Oauth(Context context, int code, String message, Object o) {
		super(context, code, message, o);
	}
	public boolean checkData() {
		//绑定偶玩账号不返回data
		return true;
	}
}
