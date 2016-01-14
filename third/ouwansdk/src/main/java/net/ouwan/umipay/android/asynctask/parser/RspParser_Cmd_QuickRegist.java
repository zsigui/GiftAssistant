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
import net.ouwan.umipay.android.entry.gson.Gson_Cmd_QuickRegist;
import net.ouwan.umipay.android.manager.ListenerManager;
import net.ouwan.umipay.android.manager.LocalPasswordManager;
import net.ouwan.umipay.android.manager.UmipayAccountManager;

/**
 * RspParser_Cmd_QuickRegist
 *
 * @author zacklpx
 *         date 15-4-28
 *         description
 */
public class RspParser_Cmd_QuickRegist extends CommonRspParser<Gson_Cmd_QuickRegist> {
	public RspParser_Cmd_QuickRegist(Context context) {
		super(context);
	}

	@Override
	public void toHandle(Gson_Cmd_QuickRegist result, Bundle... extResponses) {
		if (result.getCode() == UmipaySDKStatusCode.CANCEL) {
			return;
		}
		if (!result.checkData()) {
			result = getErrorRsp(UmipaySDKStatusCode.ERR_14_ERR_UNPACK, "Check data failed");
		}
		postResult(result, extResponses);
	}

	@Override
	public Gson_Cmd_QuickRegist getErrorRsp(int code, String msg) {
		return new Gson_Cmd_QuickRegist(context, code, msg, null);
	}

	@Override
	public Gson_Cmd_QuickRegist fromJson(String jsonString) {
		Gson_Cmd_QuickRegist ret = getErrorRsp(UmipaySDKStatusCode.ERR_14_ERR_UNPACK, null);
		try {
			Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).create();
			ret = gson.fromJson(jsonString, Gson_Cmd_QuickRegist.class);
			ret.setContext(context);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return ret;
	}

	@Override
	public void postResult(int what, Gson_Cmd_QuickRegist result, Bundle... extResponses) {
		int code = result.getCode();
		String msg = result.getMessage();
		UmipayAccount umipayAccount = null;
		Gson_Cmd_QuickRegist.Cmd_QuickRegist_Data cmdQuickRegistData = result.getData();
		if (code == UmipaySDKStatusCode.SUCCESS) {
			umipayAccount = new UmipayAccount(cmdQuickRegistData.getUsername(), cmdQuickRegistData
					.getPassword());

			GameUserInfo userInfo = new GameUserInfo();
			userInfo.setOpenId(cmdQuickRegistData.getOpenid());
			userInfo.setSign(cmdQuickRegistData.getSign());
			userInfo.setTimestamp_s(cmdQuickRegistData.getTs());
			umipayAccount.setGameUserInfo(userInfo);

			umipayAccount.setUid(cmdQuickRegistData.getUid());
			umipayAccount.setSession(cmdQuickRegistData.getSession());
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
			ListenerManager.getCommandRegistListener().onRegist(UmipayAccount.TYPE_QUICK_REGIST, code, msg, umipayAccount);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}
}
