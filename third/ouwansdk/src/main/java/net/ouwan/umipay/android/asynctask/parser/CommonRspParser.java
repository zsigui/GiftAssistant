package net.ouwan.umipay.android.asynctask.parser;

import android.content.Context;
import android.os.Bundle;

import net.ouwan.umipay.android.api.UmipaySDKStatusCode;
import net.ouwan.umipay.android.asynctask.CommandResponse;
import net.ouwan.umipay.android.debug.Debug_Log;

import org.json.JSONObject;

/**
 * Interface_RespHandler
 *
 * @author zacklpx
 *         date 15-4-10
 *         description
 */
public abstract class CommonRspParser<GsonRsp> {

	public Context context;

	protected int cmd;

	public CommonRspParser(Context context) {
		this.context = context;
	}

	public void parseResponse(CommandResponse response, Bundle... extResponses) {
		cmd = response.getCmd();
		int code = response.getCode();
		String msg = response.getMsg();
		JSONObject resultJson = (JSONObject) response.getResult();

		if (code != UmipaySDKStatusCode.SUCCESS) {
			toHandle(getErrorRsp(code, msg));
			return;
		}

		if (resultJson == null) {
			toHandle(getErrorRsp(UmipaySDKStatusCode.ERR_14_ERR_UNPACK, null));
			return;
		}
		Debug_Log.dd(resultJson.toString());
		toHandle(fromJson(resultJson.toString()), extResponses);
	}

	public abstract void toHandle(GsonRsp result, Bundle... extResponses);

	public abstract GsonRsp getErrorRsp(int code, String msg);

	public abstract GsonRsp fromJson(String jsonString);

	public void postResult(GsonRsp result, Bundle... extResponses) {
		postResult(cmd, result, extResponses);
	}

	public abstract void postResult(int what, GsonRsp result, Bundle... extResponses);
}
