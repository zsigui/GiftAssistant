package net.ouwan.umipay.android.asynctask.parser;//package net.ouwan.umipay.android.asynctask.parser;
//
//import android.content.Context;
//import android.os.Bundle;
//
//import com.google.gson.FieldNamingPolicy;
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//
//import net.ouwan.umipay.android.Utils.Util_Package;
//import net.ouwan.umipay.android.api.UmipaySDKStatusCode;
//import net.ouwan.umipay.android.debug.Debug_Log;
//import net.ouwan.umipay.android.entry.UmipayCommonAccount;
//import net.ouwan.umipay.android.entry.UmipayConvertAccount;
//import net.ouwan.umipay.android.entry.gson.Gson_Cmd_Session_Convert;
//import net.ouwan.umipay.android.manager.UmipayCommonAccountCacheManager;
//
///**
// * RspParser_Cmd_Login
// *
// * @author jimmy
// *         date 16-8-17
// *         description
// */
//public class RspParser_Cmd_Session_Convert extends CommonRspParser<Gson_Cmd_Session_Convert> {
//	public RspParser_Cmd_Session_Convert(Context context) {
//		super(context);
//	}
//
//	@Override
//	public void toHandle(Gson_Cmd_Session_Convert result, Bundle... extResponses) {
//		if (result.getCode() == UmipaySDKStatusCode.CANCEL) {
//			return;
//		}
//
//		if (!result.checkData()) {
//			result = getErrorRsp(UmipaySDKStatusCode.ERR_14_ERR_UNPACK, "Check data failed");
//		}
//		postResult(result, extResponses);
//	}
//
//	@Override
//	public Gson_Cmd_Session_Convert getErrorRsp(int code, String msg) {
//		return new Gson_Cmd_Session_Convert(context, code, msg, null);
//	}
//
//	@Override
//	public Gson_Cmd_Session_Convert fromJson(String jsonString) {
//		Gson_Cmd_Session_Convert ret = getErrorRsp(UmipaySDKStatusCode.ERR_14_ERR_UNPACK, null);
//		try {
//			Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).create();
//			ret = gson.fromJson(jsonString, Gson_Cmd_Session_Convert.class);
//			ret.setContext(context);
//		} catch (Throwable e) {
//			Debug_Log.e(e);
//		}
//		return ret;
//	}
//
//	@Override
//	public void postResult(int what, Gson_Cmd_Session_Convert result, Bundle... extResponses) {
//		String d_packageName = "";
//		String o_packageName = "";
//
//		try {
//			if (extResponses != null && extResponses.length > 0) {
//				d_packageName = extResponses[0].getString("package_name");
//			}
//			o_packageName = context.getPackageName();
//		} catch (Throwable e) {
//			Debug_Log.e(e);
//		}
//
//		int code = result.getCode();
//		String msg = result.getMessage();
//		UmipayCommonAccount umipayAccount = null;
//		Gson_Cmd_Session_Convert.Cmd_Session_Convert_Data cmdLoginData = result.getData();
//		if (code == UmipaySDKStatusCode.SUCCESS) {
//			umipayAccount = new UmipayCommonAccount(d_packageName, o_packageName);
//			umipayAccount.setUserName(cmdLoginData.getUserName());
//			umipayAccount.setTimestamp_s(cmdLoginData.getTs());
//
//			umipayAccount.setUid(cmdLoginData.getUid());
//			umipayAccount.setSession(cmdLoginData.getSession());
//			umipayAccount.setBindMobile(cmdLoginData.getBindmobile());
//			umipayAccount.setLastLoginTime_ms(System.currentTimeMillis());
//			umipayAccount.setDestPackageName(context.getPackageName());
//			umipayAccount.setOriginPackageName(context.getPackageName());
//			UmipayCommonAccountCacheManager.getInstance(context).addCommonAccount(umipayAccount);
//			Util_Package.startUmiApp(context,d_packageName);
//		}
//	}
//
//}
