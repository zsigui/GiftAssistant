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
import net.ouwan.umipay.android.entry.UmipayCommonAccount;
import net.ouwan.umipay.android.entry.gson.Gson_Cmd_Login;
import net.ouwan.umipay.android.manager.ListenerManager;
import net.ouwan.umipay.android.manager.LocalPasswordManager;
import net.ouwan.umipay.android.manager.UmipayAccountManager;
import net.ouwan.umipay.android.manager.UmipayCommonAccountCacheManager;

/**
 * RspParser_Cmd_Login
 *
 * @author jimmy
 *         date 16-8-17
 *         description
 */
public class RspParser_Cmd_AutoLogin extends CommonRspParser<Gson_Cmd_Login> {
	public RspParser_Cmd_AutoLogin(Context context) {
		super(context);
	}

	@Override
	public void toHandle(Gson_Cmd_Login result, Bundle... extResponses) {
		if (result.getCode() == UmipaySDKStatusCode.CANCEL) {
			return;
		}
		if (!result.checkData()) {
			result = getErrorRsp(UmipaySDKStatusCode.ERR_14_ERR_UNPACK, "Check data failed");
		}
		postResult(result, extResponses);
	}

	@Override
	public Gson_Cmd_Login getErrorRsp(int code, String msg) {
		return new Gson_Cmd_Login(context, code, msg, null);
	}

	@Override
	public Gson_Cmd_Login fromJson(String jsonString) {
		Gson_Cmd_Login ret = getErrorRsp(UmipaySDKStatusCode.ERR_14_ERR_UNPACK, null);
		try {
			Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).create();
			ret = gson.fromJson(jsonString, Gson_Cmd_Login.class);
			ret.setContext(context);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return ret;
	}

	@Override
	public void postResult(int what, Gson_Cmd_Login result, Bundle... extResponses) {

		String username = "";
		String psw = "";
		boolean isRememberPsw = false;
		try {
			if (extResponses != null && extResponses.length > 0) {
				username = extResponses[0].getString("username");
				psw = extResponses[0].getString("psw");
				isRememberPsw = extResponses[0].getBoolean("isRememberPsw");
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}

		int code = result.getCode();
		String msg = result.getMessage();
		UmipayAccount  umipayAccount = null;
		Gson_Cmd_Login.Cmd_Login_Data cmdLoginData = result.getData();
		if (code == UmipaySDKStatusCode.SUCCESS) {
			umipayAccount = UmipayAccountManager.getInstance(context).getAccountByUserName
					(username);
			if (umipayAccount == null) {
				umipayAccount = new UmipayAccount(username, psw);
			} else {
				umipayAccount.setPsw(psw);
			}
			if (!isRememberPsw) {
				umipayAccount.setPsw("");
			}
			GameUserInfo userInfo = new GameUserInfo();
			userInfo.setOpenId(cmdLoginData.getOpenid());
			userInfo.setSign(cmdLoginData.getSign());
			userInfo.setTimestamp_s(cmdLoginData.getTs());
			umipayAccount.setGameUserInfo(userInfo);

			umipayAccount.setUid(cmdLoginData.getUid());
			umipayAccount.setSession(cmdLoginData.getSession());
			umipayAccount.setBindMobile(cmdLoginData.getBindmobile());
			umipayAccount.setLastLoginTime_ms(System.currentTimeMillis());
			umipayAccount.setRemenberPsw(isRememberPsw);

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
		long ts = 0;
		try{
			ts = System.currentTimeMillis();
		}catch (Throwable e){
			Debug_Log.e(e);
		}
		UmipayCommonAccountCacheManager.getInstance(context).addCommonAccount(new UmipayCommonAccount(context,umipayAccount,ts),UmipayCommonAccountCacheManager.COMMON_ACCOUNT);

		try {
			ListenerManager.getCommandLoginListener().onLogin(code, msg, umipayAccount);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

}
