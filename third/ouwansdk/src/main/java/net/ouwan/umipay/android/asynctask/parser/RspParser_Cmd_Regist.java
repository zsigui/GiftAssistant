package net.ouwan.umipay.android.asynctask.parser;

import android.content.Context;
import android.os.Bundle;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.ouwan.umipay.android.api.GameUserInfo;
import net.ouwan.umipay.android.api.UmipaySDKStatusCode;
import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.entry.UmipayAccount;
import net.ouwan.umipay.android.entry.gson.Gson_Cmd_Regist;
import net.ouwan.umipay.android.manager.ListenerManager;
import net.ouwan.umipay.android.manager.LocalPasswordManager;
import net.ouwan.umipay.android.manager.UmipayAccountManager;

/**
 * RspParser_Cmd_Regist
 *
 * @author zacklpx
 *         date 15-4-28
 *         description
 */
public class RspParser_Cmd_Regist extends CommonRspParser<Gson_Cmd_Regist> {
	public RspParser_Cmd_Regist(Context context) {
		super(context);
	}

	@Override
	public void toHandle(Gson_Cmd_Regist result, Bundle... extResponses) {
		if (result.getCode() == UmipaySDKStatusCode.CANCEL) {
			return;
		}
		if (!result.checkData()) {
			result = getErrorRsp(UmipaySDKStatusCode.ERR_14_ERR_UNPACK, "Check data failed");
		}
		postResult(result, extResponses);
	}

	@Override
	public Gson_Cmd_Regist getErrorRsp(int code, String msg) {
		return new Gson_Cmd_Regist(context, code, msg, null);
	}

	@Override
	public Gson_Cmd_Regist fromJson(String jsonString) {
		Gson_Cmd_Regist ret = getErrorRsp(UmipaySDKStatusCode.ERR_14_ERR_UNPACK, null);
		try {
			Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).create();
			ret = gson.fromJson(jsonString, Gson_Cmd_Regist.class);
			ret.setContext(context);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return ret;
	}

	@Override
	public void postResult(int what, Gson_Cmd_Regist result, Bundle... extResponses) {
		String username = "";
		String psw = "";
		try {
			if (extResponses != null && extResponses.length > 0) {
				username = extResponses[0].getString("username");
				psw = extResponses[0].getString("psw");
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}

		int code = result.getCode();
		String msg = result.getMessage();
		UmipayAccount umipayAccount = null;
		Gson_Cmd_Regist.Cmd_Regist_Data cmdRegistData = result.getData();
		if (code == UmipaySDKStatusCode.SUCCESS) {
			umipayAccount = UmipayAccountManager.getInstance(context).getAccountByUserName
					(username);
			if (umipayAccount == null) {
				umipayAccount = new UmipayAccount(username, psw);
			} else {
				umipayAccount.setPsw(psw);
			}
			GameUserInfo userInfo = new GameUserInfo();
			userInfo.setOpenId(cmdRegistData.getOpenid());
			userInfo.setSign(cmdRegistData.getSign());
			userInfo.setTimestamp_s(cmdRegistData.getTs());
			umipayAccount.setGameUserInfo(userInfo);

			umipayAccount.setUid(cmdRegistData.getUid());
			umipayAccount.setSession(cmdRegistData.getSession());
			umipayAccount.setBindMobile(0);
			umipayAccount.setLastLoginTime_ms(System.currentTimeMillis());
			umipayAccount.setRemenberPsw(true);

			UmipayAccountManager.getInstance(context).saveAccount(umipayAccount);
			UmipayAccountManager.getInstance(context).setCurrentAccount(umipayAccount);
			UmipayAccountManager.getInstance(context).setLogin(true);
			if (umipayAccount.isRemenberPsw()) {
				LocalPasswordManager.getInstance(context).putPassword(umipayAccount.getUserName(),
						umipayAccount.getPsw());
			} else {
				LocalPasswordManager.getInstance(context).removePassword(umipayAccount.getUserName());
			}
		}
		try {
			ListenerManager.getCommandRegistListener().onRegist(UmipayAccount.TYPE_REGIST, code, msg, umipayAccount);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}
}
