package net.ouwan.umipay.android.Utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/*
 * 定制自己的HttpClient,确保唯一实例，提供全局访问接口
 * 自定义timeout时间等参数
 */
public class MyHttpClient {
	private static final int TIMEOUT = 10000;
	private static final int TIMEOUT_SOCKET = 15000;

	private MyHttpClient() {
	}

	// 每次返回同一实例
	// public static synchronized HttpClient getInstance(Context mContext){
	//
	// if(null == singleStance){
	// singleStance = getNewInstance(mContext);
	// }
	// return singleStance ;
	// }

	// 每次都返回新的HttpClient实例
	public static HttpClient getNewInstance(Context mContext) {
		HttpClient newInstance;

		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params,
				HTTP.DEFAULT_CONTENT_CHARSET);
		HttpProtocolParams.setUseExpectContinue(params, true);

		// 自定义三个timeout参数

		/*
		 * 1.set a timeout for the connection manager,it defines how long we
		 * should wait to get a connection out of the connection pool managed by
		 * the connection manager
		 */
		ConnManagerParams.setTimeout(params, 5000);

		/*
		 * 2.The second timeout value defines how long we should wait to make a
		 * connection over the network to the server on the other end
		 */
		HttpConnectionParams.setConnectionTimeout(params, TIMEOUT);

		/*
		 * 3.we set a socket timeout value to 4 seconds to define how long we
		 * should wait to get data back for our request.
		 */
		HttpConnectionParams.setSoTimeout(params, TIMEOUT_SOCKET);

		SchemeRegistry schReg = new SchemeRegistry();
		schReg.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));
		schReg.register(new Scheme("https",
				SSLSocketFactory.getSocketFactory(), 443));

		ClientConnectionManager conMgr = new ThreadSafeClientConnManager(
				params, schReg);

		newInstance = new DefaultHttpClient(conMgr, params);

		if (isCMWAP(mContext)) {
			// 通过代理解决中国移动GPRS中cmwap无法访问的问题
			HttpHost proxy = new HttpHost("10.0.0.172", 80, "http");
			newInstance.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
					proxy);
		}

		return newInstance;
	}

	public static HttpResponse execute(Context context,
	                                   HttpUriRequest paramHttpUriRequest) throws ClientProtocolException,
			IOException {
		HttpResponse response = getNewInstance(context).execute(
				paramHttpUriRequest);
		return response;
	}

	public static String executeGetRequest(Context context, String baseuri,
	                                       List<NameValuePair> params, Header[] headers) {
		try {
			String fullurl = generateUrl(baseuri, params);
			HttpGet getRequest = new HttpGet(fullurl);
			getRequest.setHeaders(headers);
			HttpResponse response = execute(context, getRequest);
			return toString(response);
		} catch (Throwable e) {
			return null;
		}
	}

	private static String generateUrl(String baseuri, List<NameValuePair> params) {
		if (params == null) {
			return baseuri;
		}

		StringBuilder builder = new StringBuilder();
		for (NameValuePair nameValuePair : params) {
			if (nameValuePair == null) {
				continue;
			}
			builder.append("&" + nameValuePair.getName() + "=" + nameValuePair.getValue());
		}
		String paramsStr = builder.toString();

		if (baseuri != null && !baseuri.contains("?")) {
			baseuri = baseuri + "?";
		}
		return baseuri + paramsStr;
	}

	public static String executePostRequest(Context context, String baseuri,
	                                        List<NameValuePair> params, Header[] headers) {
		try {
			HttpPost httpPost = new HttpPost(baseuri);
			UrlEncodedFormEntity requestentity = new UrlEncodedFormEntity(
					params, "UTF-8");
			httpPost.setEntity(requestentity);
			httpPost.setHeaders(headers);
			HttpResponse response = execute(context, httpPost);
			return toString(response);
		} catch (Throwable e) {
			return null;
		}
	}

	public static String toString(HttpResponse response) {
		try {
			if (response == null) {
				return null;
			}
			if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
				HttpEntity entity = response.getEntity();
				if (null == entity) {
					return null;
				}
				String charset = null;
				try {
					charset = EntityUtils.getContentCharSet(entity);
				} catch (Throwable e) {
					charset = "UTF-8";
				}
				if (charset == null) {
					charset = "UTF-8";
				}

				StringBuilder builder = new StringBuilder();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(entity.getContent(), charset));
				for (String s = reader.readLine(); s != null; s = reader
						.readLine()) {
					builder.append(s);
				}
				reader.close();
				String data = builder.toString();
				return data;
			} else {
				return null;
			}
		} catch (Throwable e) {
			return null;
		}
	}


	/*
	 * 判断当前网络连接类型是否是cmwap还是cmnet
	 */
	public static boolean isCMWAP(Context mContext) {
		ConnectivityManager connectivityManager = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo netWrokInfo = connectivityManager.getActiveNetworkInfo();

		if (netWrokInfo.getTypeName().equalsIgnoreCase("mobile")
				&& netWrokInfo.getExtraInfo().equals("cmwap")) {
			return true;
		}
		return false;
	}

}
