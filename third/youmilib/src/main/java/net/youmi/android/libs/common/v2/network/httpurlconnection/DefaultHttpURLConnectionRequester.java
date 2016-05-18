package net.youmi.android.libs.common.v2.network.httpurlconnection;

import android.content.Context;

import net.youmi.android.libs.common.v2.network.core.BaseHttpRequesterModel;

import java.net.HttpURLConnection;

/**
 * @author zhitao
 * @since 2015-09-15 09:31
 */
public class DefaultHttpURLConnectionRequester extends AbsHttpURLConnectionRequester {

	/**
	 * @param context
	 * @param baseHttpRequesterModel 本次请求的相关参数的自定义数据模型
	 * @throws NullPointerException
	 */
	public DefaultHttpURLConnectionRequester(Context context, BaseHttpRequesterModel baseHttpRequesterModel)
			throws NullPointerException {
		super(context, baseHttpRequesterModel);
	}

	@Override
	protected void beforeRequest(HttpURLConnection httpURLConnection) {

	}
}
