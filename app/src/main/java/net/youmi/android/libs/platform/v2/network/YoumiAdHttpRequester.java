package net.youmi.android.libs.platform.v2.network;

import android.content.Context;

import net.youmi.android.libs.common.basic.Basic_StringUtil;
import net.youmi.android.libs.common.debug.DLog;
import net.youmi.android.libs.common.global.Global_Charsets;
import net.youmi.android.libs.common.v2.global.GlobalCacheExecutor;
import net.youmi.android.libs.common.v2.network.NetworkUtil;
import net.youmi.android.libs.common.v2.network.core.AbsHttpRequester;
import net.youmi.android.libs.common.v2.network.core.BaseHttpRequesterModel;
import net.youmi.android.libs.common.v2.network.exception.NetworkExceptionConfig;
import net.youmi.android.libs.platform.SDKBuild;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * TODO: 视情况看看是否需要在实现所有的请求统一在请求时缓存，请求后清除
 * TODO: 视情况看看是否应该提供一个方法可以清除所有的请求
 */
public class YoumiAdHttpRequester {

	/**
	 * 请求时，会将相同标识的上一个请求舍弃（上一个请求会返回null），
	 *
	 * @param context
	 * @param url
	 * @param identify 　请求标识
	 * @param nec
	 *
	 * @return
	 */
	public static String requestGetIgnoreLastRequest(Context context, String url, String identify, NetworkExceptionConfig nec) {
		return requestGet(context, url, identify, Global_Charsets.UTF_8, null, nec);
	}

	/**
	 * 请求时，会将相同标识的上一个请求舍弃（上一个请求会返回null），
	 *
	 * @param context
	 * @param url
	 * @param identify     　请求标识
	 * @param extraHeaders
	 * @param nec
	 *
	 * @return
	 */
	public static String requestGetIgnoreLastRequest(Context context, String url, String identify,
			Map<String, List<String>> extraHeaders, NetworkExceptionConfig nec) {
		return requestGet(context, url, identify, Global_Charsets.UTF_8, extraHeaders, nec);
	}

	public static String requestGet(Context context, String url) {
		return requestGet(context, url, null, Global_Charsets.UTF_8, null, null);
	}

	public static String requestGet(Context context, String url, NetworkExceptionConfig nec) {
		return requestGet(context, url, null, Global_Charsets.UTF_8, null, nec);
	}

	public static String requestGet(Context context, String url, Map<String, List<String>> extraHeaders,
			NetworkExceptionConfig nec) {
		return requestGet(context, url, null, Global_Charsets.UTF_8, extraHeaders, nec);
	}

	/**
	 * 发起Get请求
	 *
	 * @param context
	 * @param url          请求url 如果是带有中文信息的url，请先urlencode
	 * @param identify     本次请求的标识，可以为空
	 *                     使用场合主要为，如果短时间内重复发起了多个相同的请求，那么会根据这个标识，舍弃前面的所有请求，仅仅关注最后的那次请求
	 * @param charset      请求字符集
	 * @param extraHeaders 额外的头部
	 * @param nec          异常上报信息
	 *
	 * @return
	 */
	public static String requestGet(Context context, String url, String identify, String charset,
			Map<String, List<String>> extraHeaders, NetworkExceptionConfig nec) {
		try {

			// 如果有传入请求标识的话，
			// 那么在执行本次请求之前，先舍弃以前的请求（如果有的话），
			// 然后讲本次请求加入到标识中
			if (!Basic_StringUtil.isNullOrEmpty(identify)) {
				YoumiAdHttpRequesterCache.removeLastRequest(identify);
			}

			BaseHttpRequesterModel baseHttpRequesterModel = new BaseHttpRequesterModel();
			baseHttpRequesterModel.setRequestUrl(url);
			baseHttpRequesterModel.setRequsetType(BaseHttpRequesterModel.REQUEST_TYPE_GET);
			baseHttpRequesterModel.setExtraHeaders(extraHeaders);
			baseHttpRequesterModel.setEncodingCharset(charset);

			AbsHttpRequester requester = new YoumiAdHttpURLConnectionRequester(context, baseHttpRequesterModel);

			// 如果本次请求有传入标识，那么发起请求之前，将本次请求加入到缓存列表中
			if (!Basic_StringUtil.isNullOrEmpty(identify)) {
				YoumiAdHttpRequesterCache.addRequestToCache(identify, requester);
			}

			// 发起请求
			requester.request();

			// 判断请求结果
			if (requester.getBaseHttpResponseModel().getResponseString() != null) {
				return requester.getBaseHttpResponseModel().getResponseString();
			} else {

				// 如果请求失败了
				// 1. 检查请求是否是被主动终止而导致的失败，如果是的话就不用执行下面的步骤了
				// 2. 检查hosts文件是否被篡改，如果是的话就用阿里云DNS解释出最终的ip然后重新请求一次　
				// 3. 如果还是失败了，就发起异常上报（注意这里需要异步）

				// 1. 如果请求被主动终止如调用了abort方法，那么这里就会返回false，这个时候不该重新发起请求或者异常上报
				if (!requester.getBaseHttpResponseModel().isFinishResponse()) {
					return null;
				}

				// 到这里就表示是请求失败了，需要检查一下是不是hosts文件被篡改而引起的，如果是的话，就尝试采用阿里云DNS解释出真实的ip，然后重新请求
				// 如果请求成功，那么就返回结果，不发异常上报
				// 如果请求失败，那么返回null，发起异常上报

				// 如果是127开头，并且能成功找到该域名所对应的真实IP就再次发起请求
				String hostIp = NetworkUtil.request4HostIp(baseHttpRequesterModel.getRequestUrl());
				if (!Basic_StringUtil.isNullOrEmpty(hostIp) && hostIp.trim().startsWith("127.") &&
				    requester.getBaseHttpRequesterModel().replaceHostStringWithReallyIpAddress()) {

					// 再次发起请求
					requester.request();

					if (requester.getBaseHttpResponseModel().getResponseString() != null) {
						return requester.getBaseHttpResponseModel().getResponseString();
					}
				}

				// 3.到这里就标识需要发起异常上报了
				if (nec == null) {
					nec = new NetworkExceptionConfig();
					nec.setSdkVersion(SDKBuild.getLargeVersionCode());
				}

				if (!nec.isPushExcepiton()) {
					return null;
				}

				// 异步发起异常上报
				GlobalCacheExecutor.execute(
						new YoumiAdExceptionReporter(context, baseHttpRequesterModel, requester.getBaseHttpResponseModel(),
								nec));
			}
		} catch (Throwable e) {
			if (DLog.isNetLog) {
				DLog.te(DLog.mNetTag, YoumiAdHttpRequester.class, e);
			}
		}

		return null;
	}

	public static String requestPost(Context context, String url, Map<String, String> postDataMap, NetworkExceptionConfig nec) {
		return requestPost(context, url, postDataMap, null, Global_Charsets.UTF_8, null, nec);
	}

	public static String requestPost(Context context, String url, Map<String, String> postDataMap,
			Map<String, List<String>> extraHeaders, NetworkExceptionConfig nec) {
		return requestPost(context, url, postDataMap, null, Global_Charsets.UTF_8, extraHeaders, nec);
	}

	public static String requestPost(Context context, String url, byte[] postDataByteArray, NetworkExceptionConfig nec) {
		return requestPost(context, url, null, postDataByteArray, Global_Charsets.UTF_8, null, nec);
	}

	public static String requestPost(Context context, String url, byte[] postDataByteArray,
			Map<String, List<String>> extraHeaders, NetworkExceptionConfig nec) {
		return requestPost(context, url, null, postDataByteArray, Global_Charsets.UTF_8, extraHeaders, nec);
	}

	/**
	 * 发起Post请求
	 *
	 * @param context
	 * @param url               请求url 如果是带有中文信息的url，请先urlencode
	 * @param postDataMap       post请求kev-value对 (如果同时存在key-value对和二进制数据，那么仅仅会发送key-value对的数据)
	 * @param postDataByteArray post请求数据的二进制数据 (如果同时存在key-value对和二进制数据，那么仅仅会发送key-value对的数据，
	 *                          只有key-value对为空，而且这个不为空，才会用这个)
	 * @param charset           请求字符集
	 * @param extraHeaders      额外的头部
	 * @param nec               异常上报信息
	 *
	 * @return
	 */
	public static String requestPost(Context context, String url, Map<String, String> postDataMap, byte[] postDataByteArray,
			String charset, Map<String, List<String>> extraHeaders, NetworkExceptionConfig nec) {

		try {

			BaseHttpRequesterModel baseHttpRequesterModel = new BaseHttpRequesterModel();
			baseHttpRequesterModel.setRequestUrl(url);
			baseHttpRequesterModel.setRequsetType(BaseHttpRequesterModel.REQUEST_TYPE_POST);
			baseHttpRequesterModel.setExtraHeaders(extraHeaders);
			baseHttpRequesterModel.setEncodingCharset(charset);
			baseHttpRequesterModel.setPostDataByteArray(postDataByteArray);
			baseHttpRequesterModel.setPostDataMap(postDataMap);

			AbsHttpRequester requester = new YoumiAdHttpURLConnectionRequester(context, baseHttpRequesterModel);

			// 发起请求
			requester.request();

			// 判断请求结果
			if (requester.getBaseHttpResponseModel().getResponseString() != null) {
				return requester.getBaseHttpResponseModel().getResponseString();
			} else {

				// 如果请求失败了
				// 1. 检查请求是否是被主动终止而导致的失败，如果是的话就不用执行下面的步骤了
				// 2. 检查hosts文件是否被篡改，如果是的话就用阿里云DNS解释出最终的ip然后重新请求一次　
				// 3. 如果还是失败了，就发起异常上报（注意这里需要异步）

				// 1. 如果请求被主动终止如调用了abort方法，那么这里就会返回false，这个时候不该重新发起请求或者异常上报
				if (!requester.getBaseHttpResponseModel().isFinishResponse()) {
					return null;
				}

				// 到这里就表示是请求失败了，需要检查一下是不是hosts文件被篡改而引起的，如果是的话，就尝试采用阿里云DNS解释出真实的ip，然后重新请求
				// 如果请求成功，那么就返回结果，不发异常上报
				// 如果请求失败，那么返回null，发起异常上报

				// 如果是127开头，并且能成功找到该域名所对应的真实IP就再次发起请求
				String hostIp = NetworkUtil.request4HostIp(baseHttpRequesterModel.getRequestUrl());
				if (!Basic_StringUtil.isNullOrEmpty(hostIp) && hostIp.trim().startsWith("127.") &&
				    requester.getBaseHttpRequesterModel().replaceHostStringWithReallyIpAddress()) {

					// 再次发起请求
					requester.request();

					if (requester.getBaseHttpResponseModel().getResponseString() != null) {
						return requester.getBaseHttpResponseModel().getResponseString();
					}
				}

				// 3.到这里就标识需要发起异常上报了
				if (nec == null) {
					nec = new NetworkExceptionConfig();
					nec.setSdkVersion(SDKBuild.getLargeVersionCode());
				}

				if (!nec.isPushExcepiton()) {
					return null;
				}

				// 异步发起异常上报
				GlobalCacheExecutor.execute(
						new YoumiAdExceptionReporter(context, baseHttpRequesterModel, requester.getBaseHttpResponseModel(),
								nec));

			}
		} catch (Throwable e) {
			if (DLog.isNetLog) {
				DLog.te(DLog.mNetTag, YoumiAdHttpRequester.class, e);
			}
		}
		return null;
	}

	/**
	 * 快捷将header放入到map中
	 *
	 * @param map
	 * @param headerName
	 * @param headerVaule
	 */
	public static void putHeaderToMap(Map<String, List<String>> map, String headerName, String headerVaule) {
		if (map == null) {
			return;
		}
		try {
			List<String> headerValueList = map.get(headerName);
			if (headerValueList == null || headerValueList.isEmpty()) {
				headerValueList = new ArrayList<String>();
			}
			headerValueList.add(headerVaule);
			map.put(headerName, headerValueList);
		} catch (Throwable e) {
			if (DLog.isNetLog) {
				DLog.te(DLog.mNetTag, YoumiAdHttpRequester.class, e);
			}
		}
	}
}
