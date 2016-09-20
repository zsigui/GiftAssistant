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
import net.ouwan.umipay.android.entry.gson.Gson_Cmd_Mobile_Login;
import net.ouwan.umipay.android.manager.ListenerManager;
import net.ouwan.umipay.android.manager.UmipayAccountManager;
import net.ouwan.umipay.android.manager.UmipayCommonAccountCacheManager;

/**
 * RspParser_Cmd_Login
 *
 * @author jimmy
 *         date 16-8-17
 *         description
 */
public class RspParser_Cmd_Mobile_Login extends CommonRspParser<Gson_Cmd_Mobile_Login> {
	public RspParser_Cmd_Mobile_Login(Context context) {
		super(context);
	}

	@Override
	public void toHandle(Gson_Cmd_Mobile_Login result, Bundle... extResponses) {
		if (result.getCode() == UmipaySDKStatusCode.CANCEL) {
			return;
		}
		if (!result.checkData()) {
			result = getErrorRsp(UmipaySDKStatusCode.ERR_14_ERR_UNPACK, "Check data failed");
		}
		postResult(result, extResponses);
	}

	@Override
	public Gson_Cmd_Mobile_Login getErrorRsp(int code, String msg) {
		return new Gson_Cmd_Mobile_Login(context, code, msg, null);
	}

	@Override
	public Gson_Cmd_Mobile_Login fromJson(String jsonString) {
		Gson_Cmd_Mobile_Login ret = getErrorRsp(UmipaySDKStatusCode.ERR_14_ERR_UNPACK, null);
		try {
			Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).create();
			ret = gson.fromJson(jsonString, Gson_Cmd_Mobile_Login.class);
			ret.setContext(context);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return ret;
	}

	@Override
	public void postResult(int what, Gson_Cmd_Mobile_Login result, Bundle... extResponses) {

		String username = "";
		String psw = "";
		boolean isRememberPsw = false;

		int code = result.getCode();
		String msg = result.getMessage();
		UmipayAccount  umipayAccount = null;
		Gson_Cmd_Mobile_Login.Cmd_Mobile_Login_Data cmdLoginData = result.getData();
		if (code == UmipaySDKStatusCode.SUCCESS) {
			umipayAccount = new UmipayAccount(username, psw);
			umipayAccount.setUserName(cmdLoginData.getUserName());
			umipayAccount.setBindOauth(cmdLoginData.getBindOauth());
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
			umipayAccount.setOauthID(String.valueOf(cmdLoginData.getUid()));
			umipayAccount.setOauthToken(cmdLoginData.getSession());
			umipayAccount.setOauthType(UmipayAccount.TYPE_MOBILE);

			UmipayAccountManager.getInstance(context).saveAccount(umipayAccount);
			UmipayAccountManager.getInstance(context).setCurrentAccount(umipayAccount);
			UmipayAccountManager.getInstance(context).setLogin(true);
			long ts = 0;
			try{
				ts = System.currentTimeMillis();
			}catch (Throwable e){
				Debug_Log.e(e);
			}
			UmipayCommonAccountCacheManager.getInstance(context).addCommonAccount(new UmipayCommonAccount(context,umipayAccount,ts),UmipayCommonAccountCacheManager.COMMON_ACCOUNT);
		}
		try {
			ListenerManager.getCommandLoginListener().onLogin(code, msg, umipayAccount);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

}