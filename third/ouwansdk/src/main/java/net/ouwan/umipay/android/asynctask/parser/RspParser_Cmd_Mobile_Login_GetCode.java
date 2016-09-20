package net.ouwan.umipay.android.asynctask.parser;

import android.content.Context;
import android.os.Bundle;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.ouwan.umipay.android.api.UmipaySDKStatusCode;
import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.entry.gson.Gson_Cmd_Mobile_Login_GetCode;
import net.ouwan.umipay.android.manager.ListenerManager;

/**
 * RspParser_Cmd_Mobile_Login_GetCode
 *
 * @author jimmy
 *         date 16-8-17
 *         description
 */
public class RspParser_Cmd_Mobile_Login_GetCode extends CommonRspParser<Gson_Cmd_Mobile_Login_GetCode> {
	public RspParser_Cmd_Mobile_Login_GetCode(Context context) {
		super(context);
	}

	@Override
	public void toHandle(Gson_Cmd_Mobile_Login_GetCode result, Bundle... extResponses) {
		postResult(result);
	}

	@Override
	public Gson_Cmd_Mobile_Login_GetCode getErrorRsp(int code, String msg) {
		return new Gson_Cmd_Mobile_Login_GetCode(context, code, msg, null);
	}

	@Override
	public Gson_Cmd_Mobile_Login_GetCode fromJson(String jsonString) {
		Gson_Cmd_Mobile_Login_GetCode ret = getErrorRsp(UmipaySDKStatusCode.ERR_14_ERR_UNPACK, null);
		try {
			Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).create();
			ret = gson.fromJson(jsonString, Gson_Cmd_Mobile_Login_GetCode.class);
			ret.setContext(context);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return ret;
	}

	@Override
	public void postResult(int what, Gson_Cmd_Mobile_Login_GetCode result, Bundle... extResponses) {
		try {
			ListenerManager.getCommandMobileLoginGetCodeListener().onMobileLoginGetCode(result);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}
}