package net.youmi.android.libs.common.v2.network.exception;

import android.content.Context;

import net.youmi.android.libs.common.basic.Basic_JSONUtil;
import net.youmi.android.libs.common.basic.Basic_StringUtil;
import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.global.Global_Runtime_SystemInfo;
import net.youmi.android.libs.common.v2.network.NetworkStatus;
import net.youmi.android.libs.common.v2.network.NetworkUtil;
import net.youmi.android.libs.common.v2.network.core.BaseHttpRequesterModel;
import net.youmi.android.libs.common.v2.network.core.BaseHttpResponseModel;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * 异常上报模块类，如果其他产品的异常上报需要有自己的上报参数，请新起子类继承该父类。
 *
 * @author bintou
 */
public abstract class AbsExceptionReporter implements Runnable {

	protected Context mContext;

	protected BaseHttpRequesterModel mBaseHttpRequesterModel;

	protected BaseHttpResponseModel mBaseHttpResponseModel;

	protected NetworkExceptionConfig mNetworkExceptionConfig;

	/**
	 * @param context
	 * @param baseHttpRequesterModel http请求参数模型
	 * @param baseHttpResponseModel  http请求结果参数模型
	 * @param networkExceptionConfig 异常上报信息
	 */
	public AbsExceptionReporter(Context context, BaseHttpRequesterModel baseHttpRequesterModel,
	                            BaseHttpResponseModel baseHttpResponseModel, NetworkExceptionConfig
			                            networkExceptionConfig) {
		mContext = context;
		mBaseHttpRequesterModel = baseHttpRequesterModel;
		mBaseHttpResponseModel = baseHttpResponseModel;
		mNetworkExceptionConfig = networkExceptionConfig;
	}

	/**
	 * 添加额外的参数到本方法的传入来的json参数
	 *
	 * @param jo
	 */
	protected abstract void addExtendParamsToThisJson(JSONObject jo);

	@Override
	public void run() {
		try {

			// 改为先判断是否能发送然后才构造发送的参数
			ExceptionSocketSender sender = new ExceptionSocketSender(mContext);
			if (sender.isNeedToSend()) {

				JSONObject jo = new JSONObject();

				Basic_JSONUtil.putLong(jo, "rt", System.currentTimeMillis());
				Basic_JSONUtil.putInt(jo, "code", mBaseHttpResponseModel.getHttpCode());
				Basic_JSONUtil.putLong(jo, "ert", mBaseHttpResponseModel.getStartRequestTimestamp_ms());
				Basic_JSONUtil.putInt(jo, "sv", mNetworkExceptionConfig.getSdkVersion());
				Basic_JSONUtil.putString(jo, "apn", NetworkStatus.getApn(mContext));
				Basic_JSONUtil.putString(jo, "r".trim() + "host", mBaseHttpRequesterModel.getHostString());
				Basic_JSONUtil.putString(jo, "r".trim() + "path", mBaseHttpRequesterModel.getPathString());
				Basic_JSONUtil.putString(jo, "r".trim() + "query", mBaseHttpRequesterModel.getQueryString());
				Basic_JSONUtil.putLong(jo, "response", mBaseHttpResponseModel.getResponseTimestamp_ms());
				Basic_JSONUtil.putLong(jo, "process", mBaseHttpResponseModel.getTotalTimes_ms());
				try {
					JSONObject reqObj = new JSONObject();
					Map<String, List<String>> map = mBaseHttpRequesterModel.getExtraHeaders();
					if (map != null && !map.isEmpty()) {
						for (Map.Entry<String, List<String>> entry : map.entrySet()) {
							if (!Basic_StringUtil.isNullOrEmpty(entry.getKey())) {
								for (String value : entry.getValue()) {
									reqObj.put(entry.getKey(), value);
								}
							}
						}
						Basic_JSONUtil.putString(jo, "req-header", reqObj.toString());
					}
				} catch (Throwable e) {
					if (Debug_SDK.isNetLog) {
						Debug_SDK.te(Debug_SDK.mNetTag, this, e);
					}
				}

				try {
					JSONObject rspObj = new JSONObject();
					Map<String, List<String>> map = mBaseHttpResponseModel.getHeaders();
					if (map != null && !map.isEmpty()) {
						for (Map.Entry<String, List<String>> entry : map.entrySet()) {
							if (!Basic_StringUtil.isNullOrEmpty(entry.getKey())) {
								for (String value : entry.getValue()) {
									rspObj.put(entry.getKey(), value);
								}
							}
						}
						Basic_JSONUtil.putString(jo, "rsp-header", rspObj.toString());
					}
				} catch (Throwable e) {
					if (Debug_SDK.isNetLog) {
						Debug_SDK.te(Debug_SDK.mNetTag, this, e);
					}
				}
				Basic_JSONUtil.putInt(jo, "exception", mBaseHttpResponseModel.getClientException());
				Basic_JSONUtil.putString(jo, "req_type", mBaseHttpRequesterModel.getRequestType());
				String hostIp = NetworkUtil.request4HostIp(mBaseHttpRequesterModel.getRequestUrl());
				Basic_JSONUtil.putString(jo, "service-ip", Basic_StringUtil.isNullOrEmpty(hostIp) ? "-1" : hostIp);
				Basic_JSONUtil.putString(jo, "caller", mNetworkExceptionConfig.getCaller());
				Basic_JSONUtil.putString(jo, "product", mNetworkExceptionConfig.getProductType());
				Basic_JSONUtil.putInt(jo, "pid", 3);
				Basic_JSONUtil.putString(jo, "tag", mNetworkExceptionConfig.getTag());
				Basic_JSONUtil.putString(jo, "sysv", Global_Runtime_SystemInfo.getDeviceOsRelease());
				Basic_JSONUtil.putString(jo, "pm",
						Global_Runtime_SystemInfo.getManufacturerInfo() + " " + Global_Runtime_SystemInfo
								.getDeviceModel());
				Basic_JSONUtil.putLong(jo, "bl", mBaseHttpResponseModel.getBodyLength());

				// 协议中已经没有了所以不用在传
				// post-data优先以NameValuePair 然后才是二进制数据
				//				List<NameValuePair> postDataNameValuePair = mBaseHttpRequesterModel
				// .getPostDataNameValuePair();
				//				if (postDataNameValuePair != null && !postDataNameValuePair.isEmpty()) {
				//					JSONObject postObj = new JSONObject();
				//					for (int i = 0; i < postDataNameValuePair.size(); i++) {
				//						NameValuePair nameValuePair = postDataNameValuePair.get(i);
				//						postObj.put(nameValuePair.getName(), nameValuePair.getValue());
				//					}
				//					Basic_JSONUtil.putString(jo, "post-data", postObj.toString());
				//				} else {
				//					byte[] postDataByteArray = mBaseHttpRequesterModel.getPostDataByteArray();
				//					if (postDataByteArray != null && postDataByteArray.length > 0) {
				//						Basic_JSONUtil.putString(jo, "post-data", new String(postDataByteArray,
				// Global_Charsets
				// .UTF_8));
				//					}
				//				}
				addExtendParamsToThisJson(jo);
				sender.send(jo);
			}

		} catch (Throwable e) {
			if (Debug_SDK.isNetLog) {
				Debug_SDK.te(Debug_SDK.mNetTag, this, e);
			}
		}
	}

}
