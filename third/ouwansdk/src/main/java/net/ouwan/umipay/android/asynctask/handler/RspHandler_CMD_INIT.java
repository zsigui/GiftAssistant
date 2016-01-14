package net.ouwan.umipay.android.asynctask.handler;

import android.content.Context;

import net.ouwan.umipay.android.api.UmipayBrowser;
import net.ouwan.umipay.android.api.UmipaySDKStatusCode;
import net.ouwan.umipay.android.config.SDKConstantConfig;
import net.ouwan.umipay.android.entry.gson.Gson_Cmd_Init;
import net.ouwan.umipay.android.manager.ListenerManager;

/**
 * RspHandler_CMD_INIT
 *
 * @author zacklpx
 *         date 15-4-20
 *         description
 */
public class RspHandler_CMD_INIT extends CommonRspHandler<Gson_Cmd_Init> {
	@Override
	public void toHandle(Gson_Cmd_Init data) {
		int code = data.getCode();
		Context context = data.getContext();
		String msg = data.getMessage();
		if (code == UmipaySDKStatusCode.SUCCESS) {
			ListenerManager.callbackInitFinish(code, msg);
			UmipayBrowser.preLoadUrl(context, SDKConstantConfig.get_CACHE_URL(context));
		} else {
			ListenerManager.callbackInitFinish(code, UmipaySDKStatusCode.handlerMessage(code, msg));
		}
	}
}
