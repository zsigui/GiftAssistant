package net.ouwan.umipay.android.handler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.unionpay.UPPayAssistEx;
import com.unionpay.uppay.PayActivity;

import net.youmi.android.libs.common.basic.Basic_JSONUtil;
import net.youmi.android.libs.webjs.js.base.handler.Interface_Browser_Handler;
import net.youmi.android.libs.webjs.js.base.handler.Interface_SDK_Handler;
import net.youmi.android.libs.webjs.js.base.handler.JsHandler_abstract_Params_NoPsw_Handler;
import net.youmi.android.libs.webjs.js.logic.Proxy_JS_Callback_Manager;

import org.json.JSONObject;

//处理js请求处理银联的handler
public class JsHandler_Pay_With_UPMP extends JsHandler_abstract_Params_NoPsw_Handler {
	public static final String UPMP_PLUGIN_PAYEND_ACTION = "com.unionpay.plugin.payend.umipay_Action";
	private static final String mode = "00"; //01测试，00正式

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

			String tn = null;
			if (params != null) {
				// js callback fn
				mJsFn = Basic_JSONUtil.getString(params, "a", null);// fn

				// req code
				mReqCode = Basic_JSONUtil.getLong(params, "b", 0);

				// 接受回调的页面的url
				mReceiverCallPageUrl = Basic_JSONUtil.getString(params,
						"c", null);

				//订单数据
				tn = Basic_JSONUtil.getString(params, "d", null);
			}

			if (tn == null) {
				return toSimpleCodeJson(Err_Params);
			}

			UPPayAssistEx.startPayByJAR(sdkHandler.getActivity(), PayActivity.class, null, null, tn, mode);
			JSONObject res = toSimpleCodeJson(OK);

			//监听回调
			IntentFilter filter = new IntentFilter();
			filter.addAction(UPMP_PLUGIN_PAYEND_ACTION);
			context.registerReceiver(new UnionpayBroadcastReceiver(context), filter);
			return res;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return toSimpleCodeJson(Err_Exception);
	}

	final private class UnionpayBroadcastReceiver extends BroadcastReceiver {
		private final Context mAppContext;

		public UnionpayBroadcastReceiver(Context appcontext) {
			this.mAppContext = appcontext;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			int payResultCode = -1;
			String payResultMsg = "default";
			try {
				if (UPMP_PLUGIN_PAYEND_ACTION.equals(action)) {
					payResultMsg = intent.getExtras().getString("msg");
					if (payResultMsg.equalsIgnoreCase("success")) {
						payResultCode = 0;
					}
					if (payResultMsg.equalsIgnoreCase("fail")) {
						payResultCode = 1;
					}
					if (payResultMsg.equalsIgnoreCase("cancel")) {
						payResultCode = 2;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (mAppContext != null) {
					mAppContext.unregisterReceiver(this);
					if (mJsFn != null) {
						JSONObject data = new JSONObject();
						try {
							data.put("a", payResultCode);
							data.put("b", payResultMsg);
						} catch (Exception e2) {
						}
						StringBuilder sb = new StringBuilder();
						sb.append("javascript:").append(mJsFn).append("(")
								.append(mReqCode).append(",").append(data)
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
