package net.ouwan.umipay.android.asynctask.handler;

import android.app.Activity;

import net.ouwan.umipay.android.api.UmipayBrowser;
import net.youmi.android.libs.common.basic.Basic_JSONUtil;
import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.webjs.js.base.handler.Interface_Browser_Handler;
import net.youmi.android.libs.webjs.js.base.handler.Interface_SDK_Handler;
import net.youmi.android.libs.webjs.js.base.handler.JsHandler_abstract_Params_NoPsw_Handler;

import org.json.JSONObject;

/**
 * 通过全屏Activity的方式创建一个新的"内置浏览器"实例，并加载指定的url。
 * 
 * @author jen
 * 
 */
final public class JsHandler_LoadUrlInNewBrowser extends JsHandler_abstract_Params_NoPsw_Handler {

	@Override
	protected JSONObject toHandler(Interface_SDK_Handler sdkHandler, Interface_Browser_Handler browserHandler,
	                               JSONObject jo_) {
		try {
			if (sdkHandler == null) {
				return toSimpleCodeJson(Err_Exception);
			}

			Activity activity = sdkHandler.getActivity();
			if (activity == null) {
				return toSimpleCodeJson(Err_Exception);
			}

			// 获取url
			String url = Basic_JSONUtil.getString(jo_, "a", null);

			String title = Basic_JSONUtil.getString(jo_, "b", null);


			if (url == null) {
				return toSimpleCodeJson(Err_Params);
			}


			// 目标浏览器所有页面待加载的js代码
			String allPageLoadJsCode = Basic_JSONUtil.getString(jo_, "c", null);

			// 目标浏览器所有页面待加载的js file的url
			String allPageLoadJsFileUrl = Basic_JSONUtil.getString(jo_, "d", null);

			// 获取目标浏览器配置flags
			int flags = Basic_JSONUtil.getInt(jo_, "e", 0);

			// post的数据，格式是a=1&b=2&c=3,如果这个不为空，则采用post的方式加载url，并且将此参数传给服务器，请自行解决urlencode的问题。
			// {基础类库版本>=2013012400 }
			// postData
			String postData = Basic_JSONUtil.getString(jo_, "f", null);
			UmipayBrowser.postUrl(sdkHandler.getActivity(),title,url,postData,flags,allPageLoadJsCode,allPageLoadJsFileUrl,0);
			JSONObject res = toSimpleCodeJson(OK);
			return res;

		} catch (Throwable e) {
			if (Debug_SDK.isJsLog) {
				Debug_SDK.te(Debug_SDK.mJsTag, this, e);
			}
		}
		return null;
	}

}
