package net.ouwan.umipay.android.asynctask.handler;

import net.ouwan.umipay.android.api.UmipaySDKStatusCode;
import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.entry.gson.Gson_Cmd_GetAccountList;
import net.ouwan.umipay.android.manager.UmipayAccountManager;

/**
 * RspHandler_Cmd_GetAccountList
 *
 * @author zacklpx
 *         date 15-4-22
 *         description
 */
public class RspHandler_Cmd_GetAccountList extends CommonRspHandler<Gson_Cmd_GetAccountList> {
	@Override
	public void toHandle(Gson_Cmd_GetAccountList data) {
		if (data.getCode() == UmipaySDKStatusCode.SUCCESS) {
			UmipayAccountManager.getInstance(data.getContext()).updateAccountList();
			Debug_Log.d("Load accounts success!");
		} else {
			Debug_Log.d("Load account failed! code = " + data.getCode() + ", msg = " + data.getMessage());
		}
	}
}
