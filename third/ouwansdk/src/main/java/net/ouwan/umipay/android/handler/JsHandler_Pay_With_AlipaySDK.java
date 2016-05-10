package net.ouwan.umipay.android.handler;

import android.content.Context;

import com.alipay.sdk.app.PayTask;

import net.ouwan.umipay.android.debug.Debug_Log;
import net.youmi.android.libs.common.basic.Basic_JSONUtil;
import net.youmi.android.libs.webjs.js.base.handler.Interface_Browser_Handler;
import net.youmi.android.libs.webjs.js.base.handler.Interface_SDK_Handler;
import net.youmi.android.libs.webjs.js.base.handler.JsHandler_abstract_Params_NoPsw_Handler;
import net.youmi.android.libs.webjs.js.logic.Proxy_JS_Callback_Manager;

import org.json.JSONObject;

//处理js请求新版支付宝支付的handler
public class JsHandler_Pay_With_AlipaySDK extends JsHandler_abstract_Params_NoPsw_Handler {

	// js callback fn
	String mJsFn;
	// req code
	long mReqCode;
	// 接受回调的页面的url
	String mReceiverCallPageUrl;

	//订单信息
	String orderInfo;

	private static final int SDK_PAY_FLAG = 1;

	@Override
	protected JSONObject toHandler(final Interface_SDK_Handler sdkHandler,
	                               Interface_Browser_Handler browserHandler, JSONObject params) {

		try {
			if (sdkHandler == null) {
				return toSimpleCodeJson(Err_Exception);
			}
			Context context = sdkHandler.getApplicationContext();
			if (context == null) {
				return toSimpleCodeJson(Err_Exception);
			}

			orderInfo = null;
			if (params != null) {
				// js callback fn
				mJsFn = Basic_JSONUtil.getString(params, "a", null);// fn

				// req code
				mReqCode = Basic_JSONUtil.getLong(params, "b", 0);

				// 接受回调的页面的url
				mReceiverCallPageUrl = Basic_JSONUtil.getString(params,
						"c", null);

				//订单数据
				orderInfo = Basic_JSONUtil.getString(params, "d", null);
			}

			if (orderInfo == null) {
				return toSimpleCodeJson(Err_Params);
			}

			Runnable payRunnable = new Runnable() {
				@Override
				public void run() {
					try {
						PayTask alipay = new PayTask(sdkHandler.getActivity());
						String result = alipay.pay(orderInfo, true);
						callBack(result);
					} catch (Throwable e) {
						Debug_Log.e(e);
					}
				}
			};
			Thread payThread = new Thread(payRunnable);
			payThread.start();
			JSONObject res = toSimpleCodeJson(OK);
			return res;
		} catch (Exception e) {
			Debug_Log.e(e);
		}
		return toSimpleCodeJson(Err_Exception);
	}

	public void callBack(String rawResult) {
		String resultStatus = "";
		String result = "";
		String mono = "";
		try {
			Result resultObj = new Result(rawResult);
			resultStatus = resultObj.resultStatus;
			result = resultObj.result;
			mono = resultObj.memo;

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (mJsFn != null) {
				JSONObject data = new JSONObject();
				try {
					data.put("a", orderInfo);
					data.put("b", resultStatus);
					data.put("c", result);
					data.put("d", mono);
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

	class Result {
		String resultStatus;
		String result;
		String memo;

		public Result(String rawResult) {
			try {
				String[] resultParams = rawResult.split(";");
				for (String resultParam : resultParams) {
					if (resultParam.startsWith("resultStatus")) {
						resultStatus = gatValue(resultParam, "resultStatus");
					}
					if (resultParam.startsWith("result")) {
						result = gatValue(resultParam, "result");
					}
					if (resultParam.startsWith("memo")) {
						memo = gatValue(resultParam, "memo");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public String toString() {
			return "resultStatus={" + resultStatus + "};memo={" + memo
					+ "};result={" + result + "}";
		}

		private String gatValue(String content, String key) {
			String prefix = key + "={";
			return content.substring(content.indexOf(prefix) + prefix.length(),
					content.lastIndexOf("}"));
		}
	}

}
