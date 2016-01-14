package net.ouwan.umipay.android.asynctask.parser;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.ouwan.umipay.android.api.UmipaySDKStatusCode;
import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.entry.UmipayAccount;
import net.ouwan.umipay.android.entry.gson.Gson_Cmd_GetAccountList;
import net.ouwan.umipay.android.manager.ListenerManager;
import net.ouwan.umipay.android.manager.LocalPasswordManager;
import net.ouwan.umipay.android.manager.UmipayAccountManager;

import java.util.List;

/**
 * RspParser_CMD_GETACCOUNTLIST
 *
 * @author zacklpx
 *         date 15-4-22
 *         description
 */
public class RspParser_Cmd_GetAccount extends CommonRspParser<Gson_Cmd_GetAccountList> {
	public RspParser_Cmd_GetAccount(Context context) {
		super(context);
	}

	@Override
	public void toHandle(Gson_Cmd_GetAccountList result, Bundle... extResponses) {
		int code = result.getCode();
		String msg = result.getMessage();
		Gson_Cmd_GetAccountList.Cmd_GetAccountList_Data cmdGetAccountListData = result.getData();
		if (code == UmipaySDKStatusCode.SUCCESS && cmdGetAccountListData != null && cmdGetAccountListData
				.getAccountList() != null) {
			List<String> accountList = cmdGetAccountListData.getAccountList();
			UmipayAccountManager accountManager = UmipayAccountManager.getInstance(context);
			for (int i = 0; i < accountList.size(); i++) {
				String username = accountList.get(i);
				if (!TextUtils.isEmpty(username) && accountManager.getAccountByUserName(username) ==
						null) {
					UmipayAccount account = new UmipayAccount(username,
							LocalPasswordManager.getInstance(context).getPassword(username));
					accountManager.saveAccount(account);
				}
			}
		}
		postResult(result);
	}

	@Override
	public Gson_Cmd_GetAccountList getErrorRsp(int code, String msg) {
		return new Gson_Cmd_GetAccountList(context, code, msg, null);
	}

	@Override
	public Gson_Cmd_GetAccountList fromJson(String jsonString) {
		Gson_Cmd_GetAccountList ret = getErrorRsp(UmipaySDKStatusCode.ERR_14_ERR_UNPACK, null);
		try {
			Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).create();
			ret = gson.fromJson(jsonString, Gson_Cmd_GetAccountList.class);
			//记得补上context值
			ret.setContext(context);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return ret;
	}

	@Override
	public void postResult(int what, Gson_Cmd_GetAccountList result, Bundle... extResponses) {
		ListenerManager.sendMessage(what, result);
	}
}
