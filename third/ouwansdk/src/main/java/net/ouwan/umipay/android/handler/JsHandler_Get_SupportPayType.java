package net.ouwan.umipay.android.handler;

import android.content.Context;

import net.ouwan.umipay.android.Utils.Util_SDK_Info;
import net.ouwan.umipay.android.debug.Debug_Log;
import net.youmi.android.libs.webjs.js.base.handler.Interface_Browser_Handler;
import net.youmi.android.libs.webjs.js.base.handler.Interface_SDK_Handler;
import net.youmi.android.libs.webjs.js.base.handler.JsHandler_abstract_NoParams_NoPsw_Handler;

import org.json.JSONObject;

public class JsHandler_Get_SupportPayType extends JsHandler_abstract_NoParams_NoPsw_Handler {

	@Override
	protected JSONObject toHandler(Interface_SDK_Handler sdkHandler,
	                               Interface_Browser_Handler browserHandler) {
		try {

			if (sdkHandler == null) {
				return toSimpleCodeJson(Err_Exception);
			}
			Context context = sdkHandler.getApplicationContext();
			if (context == null) {
				return toSimpleCodeJson(Err_Exception);
			}

			JSONObject rt_jo = toSimpleCodeJson(OK);
			String supportPaytypes = Util_SDK_Info.getSDKSupportPayType(context);

			rt_jo.put("d", supportPaytypes);

			return rt_jo;
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return null;
	}

}
