package net.ouwan.umipay.android.asynctask.handler;

import net.ouwan.umipay.android.api.UmipaySDKStatusCode;
import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.entry.gson.Gson_Cmd_DeleteAccount;

/**
 * RspHandler_Cmd_DeleteAccount
 *
 * @author zacklpx
 *         date 15-4-30
 *         description
 */
public class RspHandler_Cmd_DeleteAccount extends CommonRspHandler<Gson_Cmd_DeleteAccount> {
	@Override
	public void toHandle(Gson_Cmd_DeleteAccount data) {
		int code = data.getCode();
		String msg = UmipaySDKStatusCode.handlerMessage(code, data.getMessage());
		if (code == UmipaySDKStatusCode.SUCCESS) {
			Debug_Log.d("Delete account success!");
		} else {
			Debug_Log.d("Delete account failed! code = " + code + " msg = " + msg);
		}
	}
}
