package net.ouwan.umipay.android.handler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.junnet.heepay.ui.activity.WelcomeActivity;
import com.junnet.heepay.ui.base.Constant;

import net.ouwan.umipay.android.debug.Debug_Log;
import net.youmi.android.libs.common.basic.Basic_JSONUtil;
import net.youmi.android.libs.webjs.js.base.handler.Interface_Browser_Handler;
import net.youmi.android.libs.webjs.js.base.handler.Interface_SDK_Handler;
import net.youmi.android.libs.webjs.js.base.handler.JsHandler_abstract_Params_NoPsw_Handler;
import net.youmi.android.libs.webjs.js.logic.Proxy_JS_Callback_Manager;

import org.json.JSONObject;

//处理js请求处理支付宝的handler
public class JsHandler_Pay_With_WECHAT extends JsHandler_abstract_Params_NoPsw_Handler {

	// js callback fn
	String mJsFn;
	// req code
	long mReqCode;
	// 接受回调的页面的url
	String mReceiverCallPageUrl;
	//
	Bundle _payInfoBundle;

	String token_id = "";
	String agent_id = "";
	String bill_no = "";

	public static final String WECHAT_PUKGUIN_PAYEND_ACTION = "com.junnet.heepay.payend.umipay_Action";



	@Override
	protected JSONObject toHandler(Interface_SDK_Handler sdkHandler,
	                               Interface_Browser_Handler browserHandler, JSONObject params) {

		try {
			if (sdkHandler == null) {
				return toSimpleCodeJson(Err_Exception);
			}
			Context context = sdkHandler.getActivity();
			if (context == null) {
				return toSimpleCodeJson(Err_Exception);
			}

			if (params != null) {
				// js callback fn
				mJsFn = Basic_JSONUtil.getString(params, "a", null);// fn

				// req code
				mReqCode = Basic_JSONUtil.getLong(params, "b", 0);

				// 接受回调的页面的url
				mReceiverCallPageUrl = Basic_JSONUtil.getString(params,
						"c", null);

				//订单数据
				token_id = Basic_JSONUtil.getString(params, "d", null);
				//订单数据
				agent_id = Basic_JSONUtil.getString(params, "e", null);
				//订单数据
				bill_no = Basic_JSONUtil.getString(params, "f", null);
			}

			if (token_id == null || agent_id == null || bill_no == null) {
				return toSimpleCodeJson(Err_Params);
			}
			_payInfoBundle = new Bundle();
			_payInfoBundle.putString("tid",token_id);
			_payInfoBundle.putInt("aid",
					Integer.parseInt(agent_id));

			_payInfoBundle.putString("bn", bill_no);
			sdkHandler.getActivity().startActivityForResult(
					new Intent(context, WelcomeActivity.class)
							.putExtras(_payInfoBundle),
					Constant.REQUEST_CODE_INIT);
			JSONObject res = toSimpleCodeJson(OK);
			//监听回调
			IntentFilter filter = new IntentFilter();
			filter.addAction(WECHAT_PUKGUIN_PAYEND_ACTION);
			filter.addCategory(Intent.CATEGORY_DEFAULT);
			context.registerReceiver(new WeChatBroadcastReceiver(context), filter);
			return res;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return toSimpleCodeJson(Err_Exception);
	}

	private class WeChatBroadcastReceiver extends BroadcastReceiver{

		private final Context mAppContext;

		private WeChatBroadcastReceiver(Context appcontext) {
			this.mAppContext = appcontext;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			String code = "01";
			String errorCode = "";
			String result_message = "";
			try{
				if(WECHAT_PUKGUIN_PAYEND_ACTION.equals(action)) {
					code = intent.getExtras().getString("result_code");
					result_message =  intent.getExtras().getString("result_message");
					//交易状态成功:00 交易状态失败:01 交易状态取消:02 交易状态未知:03

					//返回PE007错误码表示未安装微信
					errorCode =  intent.getExtras().getString("error_code");
				}
			}catch (Throwable e) {
				Debug_Log.e(e);
			} finally {
				if (mAppContext != null) {
					mAppContext.unregisterReceiver(this);
					if (mJsFn != null) {
						JSONObject data = new JSONObject();
						try {
							Basic_JSONUtil.putString(data, "a", code);
							Basic_JSONUtil.putString(data, "b", result_message);
							Basic_JSONUtil.putString(data, "c", errorCode);
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
	};
}
