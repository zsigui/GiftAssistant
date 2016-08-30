package net.ouwan.umipay.android.interfaces;

import net.ouwan.umipay.android.entry.gson.Gson_Base;

/**
 * Interface_Mobile_Login_Verificate_SMS_Listener
 *
 * @author jimmy
 *         date 16-8-17
 *         description
 */
public interface Interface_Bind_Oauth_Listener {
	void onBindOauth(int code, String msg);
}
