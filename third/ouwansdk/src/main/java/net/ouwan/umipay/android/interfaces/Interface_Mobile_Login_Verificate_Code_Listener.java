package net.ouwan.umipay.android.interfaces;

import net.ouwan.umipay.android.entry.gson.Gson_Cmd_Mobile_Login_GetCode;

/**
 * Interface_Mobile_Login_Verificate_SMS_Listener
 *
 * @author jimmy
 *         date 16-8-17
 *         description
 */
public interface Interface_Mobile_Login_Verificate_Code_Listener {

	void onMobileLoginGetCode(Gson_Cmd_Mobile_Login_GetCode gsonCmdMobileLoginVerificateCode);
}
