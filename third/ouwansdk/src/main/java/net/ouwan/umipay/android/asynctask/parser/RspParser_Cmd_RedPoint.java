package net.ouwan.umipay.android.asynctask.parser;

import android.content.Context;
import android.os.Bundle;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.ouwan.umipay.android.api.UmipaySDKStatusCode;
import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.entry.gson.Gson_Cmd_RedPoint;
import net.ouwan.umipay.android.manager.FloatmenuCacheManager;

/**
 * RspParser_Cmd_RedPoint
 *
 * @author zacklpx
 *         date 15-5-4
 *         description
 */
public class RspParser_Cmd_RedPoint extends CommonRspParser<Gson_Cmd_RedPoint> {
	public RspParser_Cmd_RedPoint(Context context) {
		super(context);
	}

	@Override
	public void toHandle(Gson_Cmd_RedPoint result, Bundle... extResponses) {
		if (!result.checkData()) {
			postResult(getErrorRsp(UmipaySDKStatusCode.ERR_14_ERR_UNPACK, "Check data failed"));
		} else {
			postResult(result);
		}
	}

	@Override
	public Gson_Cmd_RedPoint getErrorRsp(int code, String msg) {
		return new Gson_Cmd_RedPoint(context, code, msg, null);
	}

	@Override
	public Gson_Cmd_RedPoint fromJson(String jsonString) {
		Gson_Cmd_RedPoint ret = getErrorRsp(UmipaySDKStatusCode.ERR_14_ERR_UNPACK, null);
		try {
			Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).create();
			ret = gson.fromJson(jsonString, Gson_Cmd_RedPoint.class);
			//记得补上context值
			ret.setContext(context);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return ret;
	}

	@Override
	public void postResult(int what, Gson_Cmd_RedPoint result, Bundle... extResponses) {
		//不用post了，直接异步处理
		FloatmenuCacheManager.getInstance(context).parseRedPointInfos(result);
	}
}
