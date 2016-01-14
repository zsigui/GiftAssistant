package net.ouwan.umipay.android.handler;

import android.content.Context;

import net.ouwan.umipay.android.api.UmipayBrowser;
import net.ouwan.umipay.android.api.UmipaySDKStatusCode;
import net.youmi.android.libs.common.basic.Basic_JSONUtil;
import net.youmi.android.libs.webjs.js.base.handler.Interface_Browser_Handler;
import net.youmi.android.libs.webjs.js.base.handler.Interface_SDK_Handler;
import net.youmi.android.libs.webjs.js.base.handler.JsHandler_abstract_Params_NoPsw_Handler;

import org.json.JSONObject;

//更新支付进度状态
public class JsHandler_Pay_CallBack extends JsHandler_abstract_Params_NoPsw_Handler {

	int code;//0表示订单未完成，1表示订单完成(不等于支付成功)


	@Override
	protected JSONObject toHandler(Interface_SDK_Handler sdkHandler,
	                               Interface_Browser_Handler browserHandler, JSONObject params) {
		// TODO Auto-generated method stub

		try {
			if (sdkHandler == null) {
				return toSimpleCodeJson(Err_Exception);
			}
			Context context = sdkHandler.getApplicationContext();
			if (context == null) {
				return toSimpleCodeJson(Err_Exception);
			}
			if (browserHandler == null) {
				return toSimpleCodeJson(Err_Exception);
			}
			if (params != null) {
				code = Basic_JSONUtil.getInt(params, "a", -1);
			}
			if (code == 1) {
				((UmipayBrowser) sdkHandler.getActivity()).setPayCode(UmipaySDKStatusCode.PAY_FINISH);
				JSONObject res = toSimpleCodeJson(OK);
				return res;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return toSimpleCodeJson(Err_Exception);
	}

}
