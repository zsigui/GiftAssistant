package net.youmi.android.libs.platform.v2.network;

import android.content.Context;

import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.v2.network.core.BaseHttpRequesterModel;
import net.youmi.android.libs.common.v2.network.httpclient.DefaultHttpClientRequester;
import net.youmi.android.libs.platform.PlatformConstant;
import net.youmi.android.libs.platform.global.Global_Runtime_ClientId;

import org.apache.http.client.methods.HttpRequestBase;

/**
 * 官方建议：2.3下使用httpclient
 *
 * @author zhitao
 * @since 2015-09-10 23:31
 */
public class YoumiAdHttpClientRequester extends DefaultHttpClientRequester {

	/**
	 * @param context
	 * @param baseHttpRequesterModel 本次请求的相关参数的自定义数据模型
	 * @throws NullPointerException
	 */
	public YoumiAdHttpClientRequester(Context context, BaseHttpRequesterModel baseHttpRequesterModel)
			throws NullPointerException {
		super(context, baseHttpRequesterModel);
	}

	/**
	 * 在请求之前可以做的一些事情，比如添加额外的头部
	 */
	@Override
	protected void beforeRequest(HttpRequestBase httpRequestBase) {
		super.beforeRequest(httpRequestBase);
		// 设置gzip头启用压缩
		httpRequestBase.addHeader("Accept-Encoding", "gzip, deflate");
		try {
			Global_Runtime_ClientId cid = new Global_Runtime_ClientId(mApplicationContext);
			httpRequestBase.addHeader(PlatformConstant.get_HEADER_CID(), cid.getCid());
		} catch (Throwable e) {
			if (Debug_SDK.isNetLog) {
				Debug_SDK.te(Debug_SDK.mNetTag, this, e);
			}
		}
	}

}
