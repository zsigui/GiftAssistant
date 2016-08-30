package net.ouwan.umipay.android.asynctask.parser;

import android.content.Context;
import android.os.Bundle;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.ouwan.umipay.android.api.UmipaySDKStatusCode;
import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.entry.gson.Gson_Cmd_Bind_Oauth;
import net.ouwan.umipay.android.manager.ListenerManager;

/**
 * RspParser_Cmd_Login
 *
 * @author jimmy
 *         date 16-8-17
 *         description
 */
public class RspParser_Cmd_Bind_Oauth extends CommonRspParser<Gson_Cmd_Bind_Oauth> {
	public RspParser_Cmd_Bind_Oauth(Context context) {
		super(context);
	}

	@Override
	public void toHandle(Gson_Cmd_Bind_Oauth result, Bundle... extResponses) {
		if (result.getCode() == UmipaySDKStatusCode.CANCEL) {
			return;
		}
		if (!result.checkData()) {
			result = getErrorRsp(UmipaySDKStatusCode.ERR_14_ERR_UNPACK, "Check data failed");
		}
		postResult(result, extResponses);
	}

	@Override
	public Gson_Cmd_Bind_Oauth getErrorRsp(int code, String msg) {
		return new Gson_Cmd_Bind_Oauth(context, code, msg, null);
	}

	@Override
	public Gson_Cmd_Bind_Oauth fromJson(String jsonString) {
		Gson_Cmd_Bind_Oauth ret = getErrorRsp(UmipaySDKStatusCode.ERR_14_ERR_UNPACK, null);
		try {
			Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).create();
			ret = gson.fromJson(jsonString, Gson_Cmd_Bind_Oauth.class);
			ret.setContext(context);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return ret;
	}

	@Override
	public void postResult(int what, Gson_Cmd_Bind_Oauth result, Bundle... extResponses) {
		int code = result.getCode();
		String msg = result.getMessage();
		try {
			ListenerManager.getmCommandBindOauth().onBindOauth(code, msg);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

}
