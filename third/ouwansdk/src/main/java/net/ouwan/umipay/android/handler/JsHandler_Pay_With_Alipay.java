package net.ouwan.umipay.android.handler;


import net.youmi.android.libs.webjs.js.base.handler.Interface_Browser_Handler;
import net.youmi.android.libs.webjs.js.base.handler.Interface_SDK_Handler;
import net.youmi.android.libs.webjs.js.base.handler.JsHandler_abstract_Params_NoPsw_Handler;

import org.json.JSONObject;

//处理js请求易联支付的handler
public class JsHandler_Pay_With_Alipay extends JsHandler_abstract_Params_NoPsw_Handler {

	@Override
	protected JSONObject toHandler(Interface_SDK_Handler arg0,
	                               Interface_Browser_Handler arg1, JSONObject arg2) {
		return null;
	}
}
