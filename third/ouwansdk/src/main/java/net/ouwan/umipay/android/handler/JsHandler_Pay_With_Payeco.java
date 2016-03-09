package net.ouwan.umipay.android.handler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.payeco.android.plugin.PayecoPluginLoadingActivity;

import net.ouwan.umipay.android.api.GameParamInfo;
import net.ouwan.umipay.android.debug.Debug_Log;
import net.youmi.android.libs.common.basic.Basic_JSONUtil;
import net.youmi.android.libs.webjs.js.base.handler.Interface_Browser_Handler;
import net.youmi.android.libs.webjs.js.base.handler.Interface_SDK_Handler;
import net.youmi.android.libs.webjs.js.base.handler.JsHandler_abstract_Params_NoPsw_Handler;
import net.youmi.android.libs.webjs.js.logic.Proxy_JS_Callback_Manager;

import org.json.JSONObject;

//处理js请求处理支付宝的handler
public class JsHandler_Pay_With_Payeco extends JsHandler_abstract_Params_NoPsw_Handler {
	private static final String PAYECO_PLUGIN_PAYEND_ACTION = "com.payeco.plugin.payend.umipay_Action";

	// js callback fn
	String mJsFn;
	// req code
	long mReqCode;
	// 接受回调的页面的url
	String mReceiverCallPageUrl;

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

			String orderdata = null;
//			JSONObject joJsCallBack = Basic_JSONUtil.getJsonObject(params, "a",
//					null);
			if (params != null) {
				// js callback fn
				mJsFn = Basic_JSONUtil.getString(params, "a", null);// fn

				// req code
				mReqCode = Basic_JSONUtil.getLong(params, "b", 0);

				// 接受回调的页面的url
				mReceiverCallPageUrl = Basic_JSONUtil.getString(params,
						"c", null);

				//订单数据
				orderdata = Basic_JSONUtil.getString(params, "d", null);
			}
			if (orderdata == null) {
				return toSimpleCodeJson(Err_Params);
			}

			Intent intent = new Intent(sdkHandler.getActivity(),
					PayecoPluginLoadingActivity.class);
			intent.putExtra("upPay.Req", orderdata);
			intent.putExtra("Broadcast", PAYECO_PLUGIN_PAYEND_ACTION); //广播接收地址
			String testMode = GameParamInfo.getInstance(context).isTestMode() ? "00" : "01";
			intent.putExtra("Environment", testMode); // 00: 测试环境, 01: 生产环境
			sdkHandler.getActivity().startActivity(intent);

			JSONObject res = toSimpleCodeJson(OK);

			//监听回调
			IntentFilter filter = new IntentFilter();
			filter.addAction(PAYECO_PLUGIN_PAYEND_ACTION);
			filter.addCategory(Intent.CATEGORY_DEFAULT);
			context.registerReceiver(new PayecoBroadcastReceiver(context), filter);
			return res;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return toSimpleCodeJson(Err_Exception);
	}

	final private class PayecoBroadcastReceiver extends BroadcastReceiver {
		private final Context mAppContext;

		public PayecoBroadcastReceiver(Context appcontext) {
			this.mAppContext = appcontext;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			Debug_Log.dd(intent.toString());
			String action = intent.getAction();
			int code = -1;
			String respCode = "";
			String respDesc = "返回结果出错";
			try {
				if (PAYECO_PLUGIN_PAYEND_ACTION.equals(action)) {
					String payResp = intent.getExtras().getString("upPay.Rsp");
					JSONObject json = new JSONObject(payResp);
					respDesc = json.getString("respDesc");
					respCode = json.getString("respCode");
					if ("0000".equals(respCode)) {
						code = 0;
					}
					respDesc = respCode + ":" + respDesc;
				}
			} catch (Throwable e) {
				Debug_Log.e(e);
			} finally {
				if (mAppContext != null) {
					mAppContext.unregisterReceiver(this);
					if (mJsFn != null) {
						JSONObject data = new JSONObject();
						try {
							Basic_JSONUtil.put(data, "a", code);
							Basic_JSONUtil.put(data, "b", respDesc);
						} catch (Exception ignored) {
						}
						String dataJson = data.toString();
						StringBuilder sb = new StringBuilder(data.length() * 2);

						sb.append("javascript:").append(mJsFn).append("(")
								.append(mReqCode).append(",").append(dataJson)
								.append(")");
						String jsondata = sb.toString();
						//回调js
						Proxy_JS_Callback_Manager.getInstance().onSendCallBackToJs(
								mReceiverCallPageUrl, jsondata);
					}
				}
			}
		}
	}
}
