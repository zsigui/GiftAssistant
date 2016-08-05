package net.ouwan.umipay.android.handler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.heepay.plugin.api.HeepayPlugin;

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

	// 支付初始化后返回的一个支付码 初始化才返回
	String token_id = "";
	// 商家生成的订单号 初始化才回返回
	String bill_no = "";
	String agent_id = "";
	//支付类型,30为微信支付，22为支付宝支付
	String _payType = "30";

	public static final String WECHAT_PUKGUIN_PAYEND_ACTION = "com.junnet.heepay.payend.umipay_Action";



	@Override
	protected JSONObject toHandler(final Interface_SDK_Handler sdkHandler,
	                               Interface_Browser_Handler browserHandler, JSONObject params) {

		Log.e("gcool_debug_warn", "toHandler start!");
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
				Log.e("gcool_debug_warn", "mJsFn = " + mJsFn +", mReqCode"
				 + mReqCode + ", mReceiverCallPageUrl = " + mReceiverCallPageUrl + ", token_id = " + token_id
				+ ", agent_id = " + agent_id + ", bill_no = " + bill_no);
			}

			if (token_id == null || agent_id == null || bill_no == null) {
				return toSimpleCodeJson(Err_Params);
			}
			final String paramStr = token_id + ","
					+ agent_id + "," + bill_no
					+ "," + _payType;
			sdkHandler.getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Log.e("gcool_debug_warn", "param = " + paramStr + ", activity = " + sdkHandler.getActivity());
					HeepayPlugin.pay(sdkHandler.getActivity(), paramStr);
				}
			});
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
					String respCode = intent.getExtras().getString("respCode");
					//更新后支付结果状态（01成功/00处理中(多数情况下是用户取消，少数情况是掉单)/-1 失败）
					//目前后台交易状态成功:00 交易状态失败:01 交易状态取消:02 交易状态未知:03
					if("01".equals(respCode)){
						code = "00";
					}else if("-1".equals(respCode)){
						code = "01";
					}else if("00".equals(respCode)){
						code = "02";
					}else{
						code = "03";
					}
				}
			}catch (Throwable e) {
				Debug_Log.e(e);
			} finally {
				if (mAppContext != null) {
					mAppContext.unregisterReceiver(this);
					if (mJsFn != null) {
						JSONObject data = new JSONObject();
						try {
							Basic_JSONUtil.put(data, "a", code);
							Basic_JSONUtil.put(data, "b", result_message);
							Basic_JSONUtil.put(data, "c", errorCode);
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
