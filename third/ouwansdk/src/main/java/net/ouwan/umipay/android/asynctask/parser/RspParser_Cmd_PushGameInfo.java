package net.ouwan.umipay.android.asynctask.parser;

import android.content.Context;
import android.os.Bundle;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.ouwan.umipay.android.api.UmipaySDKStatusCode;
import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.entry.gson.Gson_Cmd_PushGameInfo;
import net.ouwan.umipay.android.manager.ListenerManager;

/**
 * RspParser_Cmd_DeleteAccount
 *
 * @author zacklpx
 *         date 15-4-30
 *         description
 */
public class RspParser_Cmd_PushGameInfo extends CommonRspParser<Gson_Cmd_PushGameInfo> {
	public RspParser_Cmd_PushGameInfo(Context context) {
		super(context);
	}

	@Override
	public void toHandle(Gson_Cmd_PushGameInfo result, Bundle... extResponses) {
		postResult(result);
	}

	@Override
	public Gson_Cmd_PushGameInfo getErrorRsp(int code, String msg) {
		return new Gson_Cmd_PushGameInfo(context, code, msg, null);
	}

	@Override
	public Gson_Cmd_PushGameInfo fromJson(String jsonString) {
		Gson_Cmd_PushGameInfo ret = getErrorRsp(UmipaySDKStatusCode.ERR_14_ERR_UNPACK, null);
		try {
			Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).create();
			ret = gson.fromJson(jsonString, Gson_Cmd_PushGameInfo.class);
			ret.setContext(context);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return ret;
	}

	@Override
	public void postResult(int what, Gson_Cmd_PushGameInfo result, Bundle... extResponses) {
		ListenerManager.sendMessage(what, result);
	}
}
