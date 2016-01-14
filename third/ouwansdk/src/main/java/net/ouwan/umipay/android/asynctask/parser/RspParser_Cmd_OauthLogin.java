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
import net.ouwan.umipay.android.entry.gson.Gson_Cmd_OauthLogin;
import net.ouwan.umipay.android.manager.ListenerManager;
import net.ouwan.umipay.android.manager.LocalPasswordManager;
import net.ouwan.umipay.android.manager.UmipayAccountManager;

/**
 * RspParser_Cmd_ThirdLogin
 *
 * @author zacklpx
 *         date 15-4-29
 *         description
 */
public class RspParser_Cmd_OauthLogin extends CommonRspParser<Gson_Cmd_OauthLogin> {
	public RspParser_Cmd_OauthLogin(Context context) {
		super(context);
	}

	@Override
	public void toHandle(Gson_Cmd_OauthLogin result, Bundle... extResponses) {
		if (result.getCode() == UmipaySDKStatusCode.CANCEL) {
			return;
		}
		if (!result.checkData()) {
			result = getErrorRsp(UmipaySDKStatusCode.ERR_14_ERR_UNPACK, "Check data failed");
		}
		postResult(result, extResponses);
	}

	@Override
	public Gson_Cmd_OauthLogin getErrorRsp(int code, String msg) {
		return new Gson_Cmd_OauthLogin(context, code, msg, null);
	}

	@Override
	public Gson_Cmd_OauthLogin fromJson(String jsonString) {
		Gson_Cmd_OauthLogin ret = getErrorRsp(UmipaySDKStatusCode.ERR_14_ERR_UNPACK, null);
		try {
			Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).create();
			ret = gson.fromJson(jsonString, Gson_Cmd_OauthLogin.class);
			ret.setContext(context);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return ret;
	}

	@Override
	public void postResult(int what, Gson_Cmd_OauthLogin result, Bundle... extResponses) {

		String openid = "";
		String token = "";
		int type = 0;
		int expire = 0;
		try {
			if (extResponses != null && extResponses.length > 0) {
				openid = extResponses[0].getString("openid");
				token = extResponses[0].getString("token");
				type = extResponses[0].getInt("type");
				expire = extResponses[0].getInt("expire");
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}

		int code = result.getCode();
		String msg = result.getMessage();
		UmipayAccount umipayAccount = null;
		Gson_Cmd_OauthLogin.Cmd_ThirdLogin_Data cmdThirdLoginData = result.getData();
		if (code == UmipaySDKStatusCode.SUCCESS) {
			umipayAccount = UmipayAccountManager.getInstance(context).getAccountByOauthId
					(openid, type);
			if (umipayAccount == null) {
				umipayAccount = new UmipayAccount(openid, token, type);
			} else {
				umipayAccount.setOauthToken(token);
			}
			umipayAccount.setOauthExpire(expire);

			GameUserInfo userInfo = new GameUserInfo();
			userInfo.setOpenId(cmdThirdLoginData.getOpenid());
			userInfo.setSign(cmdThirdLoginData.getSign());
			userInfo.setTimestamp_s(cmdThirdLoginData.getTs());
			umipayAccount.setGameUserInfo(userInfo);

			umipayAccount.setUserName(cmdThirdLoginData.getUsername());
			umipayAccount.setUid(cmdThirdLoginData.getUid());
			umipayAccount.setSession(cmdThirdLoginData.getSession());
			umipayAccount.setLastLoginTime_ms(System.currentTimeMillis());

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
			ListenerManager.getCommandLoginListener().onLogin(code, msg, umipayAccount);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}
}
