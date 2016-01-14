package net.ouwan.umipay.android.asynctask.handler;

import net.ouwan.umipay.android.api.UmipaySDKStatusCode;
import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.entry.gson.Gson_Cmd_PushGameInfo;

/**
 * RspHandler_Cmd_DeleteAccount
 *
 * @author zacklpx
 *         date 15-4-30
 *         description
 */
public class RspHandler_Cmd_PushGameInfo extends CommonRspHandler<Gson_Cmd_PushGameInfo> {
	@Override
	public void toHandle(Gson_Cmd_PushGameInfo data) {
		int code = data.getCode();
		String msg = UmipaySDKStatusCode.handlerMessage(code, data.getMessage());
		if (code == UmipaySDKStatusCode.SUCCESS) {
			Debug_Log.d("Push game info success!");
		} else {
			Debug_Log.d("Push game info failed! code = " + code + " msg = " + msg);
		}
	}
}
