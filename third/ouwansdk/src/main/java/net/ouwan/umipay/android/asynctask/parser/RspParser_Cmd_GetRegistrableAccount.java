package net.ouwan.umipay.android.asynctask.parser;

import android.content.Context;
import android.os.Bundle;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.ouwan.umipay.android.api.UmipaySDKStatusCode;
import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.entry.UmipayAccount;
import net.ouwan.umipay.android.entry.gson.Gson_Cmd_GetRegistrableAccount;
import net.ouwan.umipay.android.manager.ListenerManager;

/**
 * RspParser_Cmd_QuickRegist
 *
 * @author zacklpx
 *         date 15-4-28
 *         description
 */
public class RspParser_Cmd_GetRegistrableAccount extends CommonRspParser<Gson_Cmd_GetRegistrableAccount> {
	public RspParser_Cmd_GetRegistrableAccount(Context context) {
		super(context);
	}

	@Override
	public void toHandle(Gson_Cmd_GetRegistrableAccount result, Bundle... extResponses) {
		if (result.getCode() == UmipaySDKStatusCode.CANCEL) {
			return;
		}
		if (!result.checkData()) {
			result = getErrorRsp(UmipaySDKStatusCode.ERR_14_ERR_UNPACK, "Check data failed");
		}
		postResult(result, extResponses);
	}

	@Override
	public Gson_Cmd_GetRegistrableAccount getErrorRsp(int code, String msg) {
		return new Gson_Cmd_GetRegistrableAccount(context, code, msg, null);
	}

	@Override
	public Gson_Cmd_GetRegistrableAccount fromJson(String jsonString) {
		Gson_Cmd_GetRegistrableAccount ret = getErrorRsp(UmipaySDKStatusCode.ERR_14_ERR_UNPACK, null);
		try {
			Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).create();
			ret = gson.fromJson(jsonString, Gson_Cmd_GetRegistrableAccount.class);
			ret.setContext(context);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return ret;
	}

	@Override
	public void postResult(int what, Gson_Cmd_GetRegistrableAccount result, Bundle... extResponses) {
		int code = result.getCode();
		String msg = result.getMessage();
		UmipayAccount umipayAccount = null;
		Gson_Cmd_GetRegistrableAccount.Cmd_GetRegistrableAccount_Data cmdGetRegistrableAccountData = result.getData();
		if (code == UmipaySDKStatusCode.SUCCESS) {
			umipayAccount = new UmipayAccount(cmdGetRegistrableAccountData.getUsername(), cmdGetRegistrableAccountData
					.getPassword());
		}
		try {
			ListenerManager.getCommandGetRegistrableAccountListener().onGetRegistrableAccount(UmipayAccount.TYPE_QUICK_REGIST, code, msg, umipayAccount);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}
}
