package net.ouwan.umipay.android.asynctask.handler;

import net.ouwan.umipay.android.api.UmipaySDKStatusCode;
import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.manager.ListenerManager;

/**
 * RspHandler_Cmd_Login
 *
 * @author zacklpx
 *         date 15-4-28
 *         description
 */
public class RspHandler_Cmd_Logout extends CommonRspHandler {
	@Override
	public void toHandle(Object data) {
		try {
			ListenerManager.getAccountCallbackListener().onLogout(UmipaySDKStatusCode.SUCCESS, data);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}
}
