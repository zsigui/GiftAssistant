package net.ouwan.umipay.android.handler;

import android.content.Context;

import net.ouwan.umipay.android.manager.FloatmenuCacheManager;
import net.youmi.android.libs.common.basic.Basic_JSONUtil;
import net.youmi.android.libs.webjs.js.base.handler.Interface_Browser_Handler;
import net.youmi.android.libs.webjs.js.base.handler.Interface_SDK_Handler;
import net.youmi.android.libs.webjs.js.base.handler.JsHandler_abstract_Params_NoPsw_Handler;

import org.json.JSONObject;

public class JsHandler_FloatMenu_Save_Readed extends JsHandler_abstract_Params_NoPsw_Handler {

	String mReadOrWrite;//"0"表示读取，"1"表示写入
	String mType;
	String mId;

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
				mReadOrWrite = Basic_JSONUtil.getString(params, "a", null);
				mType = Basic_JSONUtil.getString(params, "b", null);
				mId = Basic_JSONUtil.getString(params, "c", null);
				if (mReadOrWrite == null || mType == null) {
					return toSimpleCodeJson(Err_Exception);
				}
				if (mReadOrWrite.equals("1") && mId != null) {
					if (!FloatmenuCacheManager.getInstance(context).consume(mType, mId)) {
						//读取失败就返回错误
						return toSimpleCodeJson(Err_Exception);
					}
				}

				//不管读还是写都返回该类型的所有已读id
				JSONObject res = toSimpleCodeJson(OK);
				String ids = FloatmenuCacheManager.getInstance(context).getConsumedList(mType);
				res.put("d", ids);
				return res;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return toSimpleCodeJson(Err_Exception);
	}

}
