package net.ouwan.umipay.android.handler;

import android.content.Context;

import net.ouwan.umipay.android.api.UmipayBrowser;
import net.youmi.android.libs.common.basic.Basic_JSONUtil;
import net.youmi.android.libs.webjs.js.base.handler.Interface_Browser_Handler;
import net.youmi.android.libs.webjs.js.base.handler.Interface_SDK_Handler;
import net.youmi.android.libs.webjs.js.base.handler.JsHandler_abstract_Params_NoPsw_Handler;

import org.json.JSONObject;

public class JsHandler_CloseView_SetVisibility extends JsHandler_abstract_Params_NoPsw_Handler {

	int mVisibility;

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
				mVisibility = Basic_JSONUtil.getInt(params, "a", 1);
			}
			((UmipayBrowser) sdkHandler.getActivity()).setCloseViewVisibility(mVisibility);
			JSONObject res = toSimpleCodeJson(OK);

			return res;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return toSimpleCodeJson(Err_Exception);
	}

}
