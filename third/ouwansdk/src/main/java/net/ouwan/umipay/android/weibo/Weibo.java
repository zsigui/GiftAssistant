package net.ouwan.umipay.android.weibo;

import android.content.Context;

import net.ouwan.umipay.android.Utils.Util_SinaUtility;
import net.ouwan.umipay.android.debug.Debug_Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

public class Weibo {
	private static Weibo mWeiboInstance;

	public static final String encoding = "utf-8"; // URL编码方式

	//sina api
	public static String URL_OAUTH2_ACCESS_AUTHORIZE = "https://api.weibo.com/oauth2/authorize";
	private static String mAppKey;
	private static String mRedirectUrl;

	private Weibo(){
	};
	public static synchronized Weibo getInstance(String appKey, String redirectUrl) {
		if(mWeiboInstance == null) {
			mWeiboInstance = new Weibo();
		}
		mAppKey = appKey;
		mRedirectUrl = redirectUrl;
		return mWeiboInstance;
	}


	/**
	 * 微博oauth2.0协议登录
	 *
	 * @param context
	 */

	public void login(Context context,WeiboAuthListener listener) {
		try {
			List<NameValuePair> parameters = new ArrayList<NameValuePair>();
			parameters.add(new BasicNameValuePair("client_id", mAppKey));
			parameters.add(new BasicNameValuePair("response_type",  "token"));
			parameters.add(new BasicNameValuePair("redirect_uri", mRedirectUrl));
			parameters.add(new BasicNameValuePair("display", "mobile"));

			String url = URL_OAUTH2_ACCESS_AUTHORIZE + "?" + Util_SinaUtility.encodeUrl(parameters);
			(new WeiboDialog(context,url,listener)).show();
		}catch (Throwable e){
			Debug_Log.e(e);
		}
	}
}
