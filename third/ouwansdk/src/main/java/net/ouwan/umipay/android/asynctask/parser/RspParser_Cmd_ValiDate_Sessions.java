package net.ouwan.umipay.android.asynctask.parser;

import android.content.Context;
import android.os.Bundle;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.ouwan.umipay.android.api.UmipaySDKStatusCode;
import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.entry.gson.Gson_Base;
import net.ouwan.umipay.android.entry.gson.Gson_Cmd_ValiDate_Sessions;
import net.ouwan.umipay.android.manager.UmipayCommonAccountCacheManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * RspParser_Cmd_Login
 *
 * @author jimmy
 *         date 16-8-17
 *         description
 */
public class RspParser_Cmd_ValiDate_Sessions extends CommonRspParser<Gson_Base> {
	public RspParser_Cmd_ValiDate_Sessions(Context context) {
		super(context);
	}

	@Override
	public void toHandle(Gson_Base result, Bundle... extResponses) {
		if (result.getCode() == UmipaySDKStatusCode.CANCEL) {
			return;
		}
		postResult(result, extResponses);
	}

	@Override
	public Gson_Cmd_ValiDate_Sessions getErrorRsp(int code, String msg) {
		return new Gson_Cmd_ValiDate_Sessions(context, code, msg, null);
	}

	@Override
	public Gson_Cmd_ValiDate_Sessions fromJson(String jsonString) {
		Gson_Cmd_ValiDate_Sessions ret = getErrorRsp(UmipaySDKStatusCode.ERR_14_ERR_UNPACK, null);
		try {
			Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).create();
			ret = gson.fromJson(jsonString, Gson_Cmd_ValiDate_Sessions.class);
			ret.setContext(context);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return ret;
	}

	@Override
	public void postResult(int what, Gson_Base result, Bundle... extResponses) {
		int code = result.getCode();
		String msg = result.getMessage();
		JSONArray ja = (JSONArray) result.getData();
		if (code == UmipaySDKStatusCode.SUCCESS) {
				try {
					ArrayList uidList = new ArrayList();
				if(ja != null){
					for(int i=0;i<ja.length();i++){
						JSONObject jo = ja.getJSONObject(i);
						int uid = jo.getInt("uid");
						uidList.add(uid);
					}
				}
				UmipayCommonAccountCacheManager.getInstance(context).valiDateCommonAccount(uidList);
			} catch (JSONException e) {
				Debug_Log.e(e);
			}
		}
	}

}
