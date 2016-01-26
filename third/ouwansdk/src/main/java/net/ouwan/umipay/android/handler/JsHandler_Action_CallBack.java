package net.ouwan.umipay.android.handler;

import android.content.Context;

import net.ouwan.umipay.android.api.UmipayBrowser;
import net.youmi.android.libs.common.basic.Basic_JSONUtil;
import net.youmi.android.libs.webjs.js.base.handler.Interface_Browser_Handler;
import net.youmi.android.libs.webjs.js.base.handler.Interface_SDK_Handler;
import net.youmi.android.libs.webjs.js.base.handler.JsHandler_abstract_Params_NoPsw_Handler;

import org.json.JSONObject;

//更新支付进度状态
public class JsHandler_Action_CallBack extends JsHandler_abstract_Params_NoPsw_Handler {

	int code;//0操作未完成，1操作完成


	/**
	 * ACTION_DEFAULT = 0;          默认
	 * ACTION_MODIFY_PSW = 1;       修改密码
	 * ACTION_CHANGE_PHONE = 2;     修改手机
	 * ACTION_BIND_PHONE = 3;       绑定手机
	 * ACTION_BIND_OUWAN = 4;       绑定偶玩账号
	 */
	int action;


	@Override
	protected JSONObject toHandler(Interface_SDK_Handler sdkHandler,
	                               Interface_Browser_Handler browserHandler, JSONObject params) {
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
				action = Basic_JSONUtil.getInt(params, "a", -1);
				code = Basic_JSONUtil.getInt(params, "b", -1);
			}
			if (((UmipayBrowser) sdkHandler.getActivity()).setActionCode(action, code)) {

				return toSimpleCodeJson(OK);
			} else {
				return toSimpleCodeJson(Err_Params);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return toSimpleCodeJson(Err_Exception);
	}

}
