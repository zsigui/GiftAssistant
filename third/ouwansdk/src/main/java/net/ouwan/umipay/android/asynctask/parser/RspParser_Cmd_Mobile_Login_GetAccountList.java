package net.ouwan.umipay.android.asynctask.parser;

import android.content.Context;
import android.os.Bundle;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.ouwan.umipay.android.api.UmipaySDKStatusCode;
import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.entry.gson.Gson_Cmd_Mobile_Login_GetAccountList;
import net.ouwan.umipay.android.manager.ListenerManager;

import java.util.List;

/**
 * RspParser_Cmd_Login
 *
 * @author jimmy
 *         date 16-8-17
 *         description
 */
public class RspParser_Cmd_Mobile_Login_GetAccountList extends CommonRspParser<Gson_Cmd_Mobile_Login_GetAccountList> {
	public RspParser_Cmd_Mobile_Login_GetAccountList(Context context) {
		super(context);
	}

	@Override
	public void toHandle(Gson_Cmd_Mobile_Login_GetAccountList result, Bundle... extResponses) {

		int code = result.getCode();
		String msg = result.getMessage();
		String mobile = null;
		String calling_code = null;
		try {
			if (extResponses != null && extResponses.length > 0) {
				mobile = extResponses[0].getString("mobile");
				calling_code = extResponses[0].getString("calling_code");
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		List<Gson_Cmd_Mobile_Login_GetAccountList.Cmd_Account_List_Item_Data> accountList=null;
		if(code == UmipaySDKStatusCode.SUCCESS && result!=null) {
			Gson_Cmd_Mobile_Login_GetAccountList.Cmd_Account_List_Data cmdGetAccountListData = result.getData();
			accountList = cmdGetAccountListData.getAccountList();
			for(Gson_Cmd_Mobile_Login_GetAccountList.Cmd_Account_List_Item_Data item:accountList){
				item.setTs(result.getData().getTs());
				item.setMobile(calling_code);
				item.setMobile(mobile);
			}
		}
		ListenerManager.getCommandGetAccountList().onGetAccountList(code,msg,accountList);
		postResult(result, extResponses);
	}

	@Override
	public Gson_Cmd_Mobile_Login_GetAccountList getErrorRsp(int code, String msg) {
		return new Gson_Cmd_Mobile_Login_GetAccountList(context, code, msg, null);
	}

	@Override
	public Gson_Cmd_Mobile_Login_GetAccountList fromJson(String jsonString) {
		Gson_Cmd_Mobile_Login_GetAccountList ret = getErrorRsp(UmipaySDKStatusCode.ERR_14_ERR_UNPACK, null);
		try {
			Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).create();
			ret = gson.fromJson(jsonString, Gson_Cmd_Mobile_Login_GetAccountList.class);
			ret.setContext(context);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return ret;
	}

	@Override
	public void postResult(int what, Gson_Cmd_Mobile_Login_GetAccountList result, Bundle... extResponses) {
		ListenerManager.sendMessage(what, result);
	}

}
