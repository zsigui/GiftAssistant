package net.ouwan.umipay.android.handler;

import android.content.Context;

import net.ouwan.umipay.android.debug.Debug_Log;
import net.youmi.android.libs.common.basic.Basic_JSONUtil;
import net.youmi.android.libs.common.util.Util_System_ClipboardManager;
import net.youmi.android.libs.webjs.js.base.handler.Interface_Browser_Handler;
import net.youmi.android.libs.webjs.js.base.handler.Interface_SDK_Handler;
import net.youmi.android.libs.webjs.js.base.handler.JsHandler_abstract_Params_NoPsw_Handler;

import org.json.JSONObject;

public class JsHandler_FloatMenu_Copy_To_Clipboar extends JsHandler_abstract_Params_NoPsw_Handler {

	String str;

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
				str = Basic_JSONUtil.getString(params, "a", null);
				if (str == null) {
					return toSimpleCodeJson(Err_Exception);
				}

				if (Util_System_ClipboardManager.setText(context, str)) {
					JSONObject res = toSimpleCodeJson(OK);
					return res;
				}
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return toSimpleCodeJson(Err_Exception);
	}

}
