package net.youmi.android.libs.platform.v2.network;

import android.content.Context;

import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.v2.network.core.BaseHttpRequesterModel;
import net.youmi.android.libs.common.v2.network.httpurlconnection.DefaultHttpURLConnectionRequester;
import net.youmi.android.libs.platform.PlatformConstant;
import net.youmi.android.libs.platform.global.Global_Runtime_ClientId;

import java.net.HttpURLConnection;

/**
 * 官方建议：2.3之后使用HttpURLConnection
 * @author zhitao
 * @since 2015-09-10 23:31
 */
public class YoumiAdHttpURLConnectionRequester extends DefaultHttpURLConnectionRequester {

	/**
	 * @param context
	 * @param baseHttpRequesterModel 本次请求的相关参数的自定义数据模型
	 *
	 * @throws NullPointerException
	 */
	public YoumiAdHttpURLConnectionRequester(Context context, BaseHttpRequesterModel baseHttpRequesterModel)
			throws NullPointerException {
		super(context, baseHttpRequesterModel);
	}

	@Override
	protected void beforeRequest(HttpURLConnection httpURLConnection) {
		super.beforeRequest(httpURLConnection);
		// 设置gzip头启用压缩
		httpURLConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");
		try {
			Global_Runtime_ClientId cid = new Global_Runtime_ClientId(mApplicationContext);
			httpURLConnection.addRequestProperty(PlatformConstant.get_HEADER_CID(), cid.getCid());
		} catch (Throwable e) {
			if (Debug_SDK.isNetLog) {
				Debug_SDK.te(Debug_SDK.mNetTag, this, e);
			}
		}
	}

}
