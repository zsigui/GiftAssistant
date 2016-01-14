package net.ouwan.umipay.android.handler;

import net.ouwan.umipay.android.api.UmipayBrowser;
import net.youmi.android.libs.webjs.js.base.handler.Interface_Browser_Handler;
import net.youmi.android.libs.webjs.js.base.handler.Interface_SDK_Handler;
import net.youmi.android.libs.webjs.js.base.handler.JsHandler_abstract_NoParams_NoPsw_Handler;

import org.json.JSONObject;

public class JsHandler_Logout_CloseBrowser extends JsHandler_abstract_NoParams_NoPsw_Handler {


	@Override
	protected JSONObject toHandler(Interface_SDK_Handler sdkHandler,
	                               Interface_Browser_Handler browserHandler) {
		// TODO Auto-generated method stub

		try {
			if (sdkHandler == null) {
				return toSimpleCodeJson(Err_Exception);
			}
			if (browserHandler == null) {
				return toSimpleCodeJson(Err_Exception);
			}
			((UmipayBrowser) sdkHandler.getActivity()).logout_CloseBrowser();
			JSONObject res = toSimpleCodeJson(OK);
			return res;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return toSimpleCodeJson(Err_Exception);
	}

}
