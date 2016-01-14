package net.ouwan.umipay.android.asynctask.parser;

import android.content.Context;
import android.os.Bundle;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.ouwan.umipay.android.api.GameParamInfo;
import net.ouwan.umipay.android.api.UmipaySDKManager;
import net.ouwan.umipay.android.api.UmipaySDKStatusCode;
import net.ouwan.umipay.android.config.SDKCacheConfig;
import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.entry.gson.Gson_Cmd_Init;
import net.ouwan.umipay.android.manager.ListenerManager;

/**
 * RespHandler_CMD_INIT
 *
 * @author zacklpx
 *         date 15-4-10
 *         description
 */
public class RspParser_Cmd_Init extends CommonRspParser<Gson_Cmd_Init> {

	public RspParser_Cmd_Init(Context context) {
		super(context);
	}

	@Override
	public void toHandle(Gson_Cmd_Init result, Bundle... extResponses) {

		if (!result.checkData()) {
			postResult(getErrorRsp(UmipaySDKStatusCode.ERR_14_ERR_UNPACK, "Check data failed"));
		} else {
			try {
				int code = result.getCode();
				String msg = result.getMessage();
				if (code == UmipaySDKStatusCode.SUCCESS) {
					Gson_Cmd_Init.Cmd_Init_Data cmdInitData = result.getData();
					Gson_Cmd_Init.Cmd_Init_Data_Config cmdInitDataConfig = cmdInitData.getConfig();
					Gson_Cmd_Init.Cmd_Init_Data_Config_Ouwan cmdInitDataConfigOuwan = cmdInitDataConfig.getOuwan();
					SDKCacheConfig cacheConfig = SDKCacheConfig.getInstance(context);
					//自动登陆，记住密码，明文密码，显示第三方登陆以用户设置为准，首次使用以服务器为准
					if (!cacheConfig.isHasLocalCache()) {
						cacheConfig.setAutoLogin(cmdInitDataConfig.getIseal() == 1);
						cacheConfig.setRemenberPsw(cmdInitDataConfig.getIserp() == 1);
						cacheConfig.setEnableViewPsw(cmdInitDataConfig.getIsevp() == 1);
						cacheConfig.setEnableOtherLogin(cmdInitDataConfig.getShow3rd() == 1);
					}
					cacheConfig.setEnableQuickReg(cmdInitDataConfig.getIseqr() == 1);
					cacheConfig.setEnableVisitorMode(cmdInitDataConfig.getIsevm() == 1);
					cacheConfig.setEnableFloatMemu(cmdInitDataConfig.getIsefm() == 1);
					cacheConfig.setEnableHttps(cmdInitDataConfig.getHttps() == 1);

					//测试，暂时全部设置为都显示
					cacheConfig.setShowGift(cmdInitDataConfig.getShowgift() == 1);
					cacheConfig.setShowMsg(cmdInitDataConfig.getShowmsg() == 1);
					cacheConfig.setShowBbs(cmdInitDataConfig.getShowbbs() == 1);
					cacheConfig.setShowHelp(cmdInitDataConfig.getShowhelp() == 1);
					cacheConfig.setShowAccount(cmdInitDataConfig.getShowaccount() == 1);

					cacheConfig.setRedpointTime(cmdInitDataConfig.getRedpointtime());
					cacheConfig.setEpayIdentify(cmdInitDataConfig.getEpayidentify());
					cacheConfig.setOuwanPackageName(cmdInitDataConfigOuwan.getPackageName());
					cacheConfig.setOuwanCommunityUrl(cmdInitDataConfigOuwan.getCommunityUrl());
					cacheConfig.setOuwanDownloadUrl(cmdInitDataConfigOuwan.getDownloadUrl());
					cacheConfig.setExitDialogCommunityBtnText(cmdInitDataConfigOuwan.getCommunityText());
					cacheConfig.setExitDialogDownloadBtnText(cmdInitDataConfigOuwan.getDownloadText());
					cacheConfig.save();
				}
				postResult(result);
			} catch (Throwable e) {
				postResult(getErrorRsp(UmipaySDKStatusCode.ERR_RUNTIME, e.getMessage()));
			}
		}
	}


	@Override
	public Gson_Cmd_Init getErrorRsp(int code, String msg) {
		return new Gson_Cmd_Init(context, code, msg, null);
	}

	@Override
	public Gson_Cmd_Init fromJson(String jsonString) {
		Gson_Cmd_Init ret = getErrorRsp(UmipaySDKStatusCode.ERR_14_ERR_UNPACK, null);
		try {
			Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).create();
			ret = gson.fromJson(jsonString, Gson_Cmd_Init.class);
			ret.setContext(context);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return ret;
	}

	@Override
	public void postResult(int what, Gson_Cmd_Init result, Bundle... extResponses) {
		ListenerManager.sendMessage(what, result);
	}
}
