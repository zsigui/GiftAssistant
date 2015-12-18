package net.youmi.android.libs.common.v2.network.core;

import android.content.Context;
import android.os.Build;

import net.youmi.android.libs.common.basic.Basic_StringUtil;
import net.youmi.android.libs.common.debug.DLog;
import net.youmi.android.libs.common.global.Global_Runtime_SystemInfo;
import net.youmi.android.libs.common.v2.network.NetworkStatus;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLConnection;

/**
 * 生成配置好的HttpClient 或者　HttpURLConnection实例的工厂
 * <p/>
 * 参考选择
 * <p/>
 * 在 Froyo(2.2) 之前，HttpURLConnection 有个重大 Bug，调用 close() 函数会影响连接池，导致连接复用失效
 * 所以在 Froyo 之前使用 HttpURLConnection 需要关闭 keepAlive。
 * <p/>
 * 另外在 Gingerbread(2.3) HttpURLConnection 默认开启了 gzip 压缩，提高了 HTTPS 的性能，
 * Ice Cream Sandwich(4.0) HttpURLConnection 支持了请求结果缓存。
 * 再加上 HttpURLConnection 本身 API 相对简单，
 * Retrofit及Volley框架默认在Android Gingerbread(API 9)及以上都是用HttpURLConnection，9以下用HttpClient。
 * <p/>
 * <b>所以对 Android 来说，在 2.3 之后建议使用 HttpURLConnection，之前建议使用 AndroidHttpClient。<b/>
 * <p/>
 */
public class HttpRequesterFactory {

	/**
	 * 从连接池中取连接的超时时间:1秒 (v2版本：5秒 -> 1秒)
	 */

	private final static int TIMEOUT_GET_CONNECTION_FROM_POOL = 1000;

	/**
	 * 设置http超时，即通过网络与服务器建立连接的超时时间:5秒 (v2版本：30秒 -> ５秒)
	 */
	private final static int TCP_CONNECTION_TIME_OUT = 5000;

	/**
	 * 设置socket超时，即即从服务器获取响应数据需要等待的时间:10秒 (v2版本：30秒 -> 10秒)
	 * 假设网速十分慢，只有0.1kb/s　那么如果这里设置为10秒的话，那么在这个情况下只能读取1Kb Max
	 */
	private final static int SOCKET_CONNECTION_TIME_OUT = 10000;

	/**
	 * 默认统一的userAgent
	 */
	private static String mUserAgent;

	/**
	 * 创建HttpURLConnection
	 *
	 * @param context
	 * @param requestUrl 请求url，如果是带有中文字符的url 需要先转码，在传入
	 *
	 * @return
	 */
	public static HttpURLConnection newHttpURLConnection(Context context, String requestUrl) throws IOException {
		return newHttpURLConnection(context, requestUrl, null, TCP_CONNECTION_TIME_OUT, SOCKET_CONNECTION_TIME_OUT);
	}

	/**
	 * 创建HttpURLConnection
	 *
	 * @param context
	 * @param requestUrl 请求url，如果是带有中文字符的url 需要先转码，在传入
	 * @param userAgent  自定义的userAgent，可以不传，不传的话，会用sdk默认构造的userAgent
	 *
	 * @return
	 */
	public static HttpURLConnection newHttpURLConnection(Context context, String requestUrl, String userAgent)
			throws IOException {
		return newHttpURLConnection(context, requestUrl, userAgent, TCP_CONNECTION_TIME_OUT, SOCKET_CONNECTION_TIME_OUT);
	}

	/**
	 * 创建HttpURLConnection
	 *
	 * @param context
	 * @param requestUrl                 请求url，如果是带有中文字符的url 需要先转码，在传入
	 * @param userAgent                  自定义的userAgent，可以不传，不传的话，会用sdk默认构造的userAgent
	 * @param tcpConnectionTimeOut_ms    设置tcp连接超时，即通过网络与服务器建立连接的超时时间(毫秒)
	 * @param socketConnectionTimeout_ms 设置socket超时，即即从服务器获取响应数据需要等待的时间(毫秒)
	 *
	 * @return
	 *
	 * @see http://blog.csdn.net/ccp1994/article/details/23190773
	 */
	public static HttpURLConnection newHttpURLConnection(Context context, String requestUrl, String userAgent,
			int tcpConnectionTimeOut_ms, int socketConnectionTimeout_ms) throws IOException {
		if (Basic_StringUtil.isNullOrEmpty(requestUrl)) {
			return null;
		}

		// 为运营商的wap网络设置代理
		String apn = NetworkStatus.getApn(context);
		if (DLog.isNetLog) {
			DLog.td(DLog.mNetTag, HttpRequesterFactory.class, "构造HttpURLConnection中：当前APN：%s", apn);
		}
		Proxy proxy = null;
		SocketAddress sa = null;
		if (apn.equals(NetworkStatus.APN.APN_CMWAP) || apn.equals(NetworkStatus.APN.APN_3GWAP) ||
		    apn.equals(NetworkStatus.APN.APN_UNIWAP)) {
			sa = new InetSocketAddress("10.0.0.172", 80);
		}
		if (apn.equals(NetworkStatus.APN.APN_CTWAP)) {
			sa = new InetSocketAddress("10.0.0.200", 80);
		}
		if (sa != null) {
			proxy = new Proxy(Proxy.Type.HTTP, sa);
			if (DLog.isNetLog) {
				DLog.td(DLog.mNetTag, HttpRequesterFactory.class, "构造HttpURLConnection中：成功设置 %s 代理 %s", apn, proxy.toString());
			}
		}

		URL url = new URL(requestUrl);
		URLConnection urlConnection;
		if (proxy == null) {
			// url对象的openConnection() 方法返回一个HttpURLConnection 对象,这个对象表示应用程序和url之间的通信连接
			urlConnection = url.openConnection();
		} else {
			urlConnection = url.openConnection(proxy);
		}

		HttpURLConnection httpUrlConnection = (HttpURLConnection) urlConnection;

		// 设置userAgent
		if (Basic_StringUtil.isNullOrEmpty(userAgent)) {
			httpUrlConnection.setRequestProperty("User-Agent", getmUserAgent());
		} else {
			String temp = userAgent.trim();
			httpUrlConnection.setRequestProperty("User-Agent", temp);
		}

		// 设置连接超时
		httpUrlConnection.setConnectTimeout(tcpConnectionTimeOut_ms);

		// 设置socket超时
		httpUrlConnection.setReadTimeout(socketConnectionTimeout_ms);

		// 设置是否向httpUrlConnection输出，默认情况下是false。使用httpUrlConnection.getOutputStream()，把内容输出到远程服务器上。
		// httpUrlConnection.setDoOutput(true);

		// 设置是否从httpUrlConnection读入，默认情况下是true。使用httpUrlConnection.getInputStream()，从远程服务器上得到响应的内容。
		httpUrlConnection.setDoInput(true);

		// 是否使用缓存,POST请求不能用缓存
		httpUrlConnection.setUseCaches(false);

		// 设定传送的内容类型是可序列化的java对象 (如果不设此项,在传送序列化对象时,当WEB服务默认的不是这种类型时可能抛java.io.EOFException)。
		// httpUrlConnection.setRequestProperty("Content-type", "application/x-java-serialized-object");

		// 设定请求的方法为”POST”，默认是GET 。
		// httpUrlConnection.setRequestMethod("POST");

		// 设定是否自动处理重定向请求，默认是会自动处理的
		// httpUrlConnection.setInstanceFollowRedirects(true);

		return httpUrlConnection;
	}

//	/**
//	 * 创建DefaultHttpClient
//	 * 从连接池中取连接的超时时间:1秒 (v2版本：5秒 -> 1秒)
//	 * 设置http超时，即通过网络与服务器建立连接的超时时间:5秒 (v2版本：30秒 -> ５秒)
//	 * 设置socket超时，即即从服务器获取响应数据需要等待的时间:5秒 (v2版本：30秒 -> 10秒)
//	 *
//	 * @param context
//	 *
//	 * @return
//	 */
//	public static DefaultHttpClient newHttpClient(Context context) {
//		return newHttpClient(context, null, TIMEOUT_GET_CONNECTION_FROM_POOL, TCP_CONNECTION_TIME_OUT,
//				SOCKET_CONNECTION_TIME_OUT);
//	}
//
//	/**
//	 * 创建DefaultHttpClient
//	 * 从连接池中取连接的超时时间:1秒 (v2版本：5秒 -> 1秒)
//	 * 设置http超时，即通过网络与服务器建立连接的超时时间:5秒 (v2版本：30秒 -> ５秒)
//	 * 设置socket超时，即即从服务器获取响应数据需要等待的时间:5秒 (v2版本：30秒 -> 10秒)
//	 *
//	 * @param context
//	 * @param userAgent
//	 *
//	 * @return
//	 */
//	public static DefaultHttpClient newHttpClient(Context context, String userAgent) {
//		return newHttpClient(context, userAgent, TIMEOUT_GET_CONNECTION_FROM_POOL, TCP_CONNECTION_TIME_OUT,
//				SOCKET_CONNECTION_TIME_OUT);
//	}
//
//	/**
//	 * 创建DefaultHttpClient
//	 *
//	 * @param context
//	 * @param userAgent                    自定义的userAgent，可以不传，不传的话，会用sdk默认构造的userAgent
//	 * @param getFromConnManagerTimeOut_ms 从连接池中取连接的超时时间(毫秒)
//	 * @param tcpConnectionTimeOut_ms      设置tcp连接超时，即通过网络与服务器建立连接的超时时间(毫秒)
//	 * @param socketConnectionTimeout_ms   设置socket超时，即即从服务器获取响应数据需要等待的时间(毫秒)
//	 *
//	 * @return
//	 */
//	public static DefaultHttpClient newHttpClient(Context context, String userAgent, long getFromConnManagerTimeOut_ms,
//			int tcpConnectionTimeOut_ms, int socketConnectionTimeout_ms) {
//
//		BasicHttpParams params = new BasicHttpParams();
//
//		// 从连接池中取连接的超时时间(毫秒)
//		ConnManagerParams.setTimeout(params, getFromConnManagerTimeOut_ms);
//
//		// 设置http超时，即通过网络与服务器建立连接的超时时间(毫秒)
//		HttpConnectionParams.setConnectionTimeout(params, tcpConnectionTimeOut_ms);
//
//		// 设置socket超时，即即从服务器获取响应数据需要等待的时间(毫秒)
//		HttpConnectionParams.setSoTimeout(params, socketConnectionTimeout_ms);
//
//		// 设置处理自动处理重定向
//		HttpClientParams.setRedirecting(params, true);
//
//		// 设置userAgent
//		if (Basic_StringUtil.isNullOrEmpty(userAgent)) {
//			HttpProtocolParams.setUserAgent(params, getmUserAgent());
//		} else {
//			String temp = userAgent.trim();
//			HttpProtocolParams.setUserAgent(params, temp);
//		}
//
//		// 设置utf-8(待测试)
//		// HttpProtocolParams.setContentCharset(params, "utf-8");
//
//		// 设置utf-8(待测试)
//		// HttpProtocolParams.setHttpElementCharset(params, "utf-8");
//
//		// 为运营商的wap网络设置代理
//		String apn = NetworkStatus.getApn(context);
//		if (DLog.isNetLog) {
//			DLog.td(DLog.mNetTag, HttpRequesterFactory.class, "构造HttpClient中：当前APN：%s", apn);
//		}
//		if (apn.equals(NetworkStatus.APN.APN_CMWAP) || apn.equals(NetworkStatus.APN.APN_3GWAP) ||
//		    apn.equals(NetworkStatus.APN.APN_UNIWAP)) {
//
//			//			当我们使用的是中国移动的手机网络时，下面方法可以直接获取得到10.0.0.172，80端口
//			//			String hostName = Proxy.getDefaultHost();
//			//			int port = Proxy.getDefaultPort();
//			HttpHost proxy = new HttpHost("10.0.0.172", 80, null);
//			params.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
//			if (DLog.isNetLog) {
//				DLog.td(DLog.mNetTag, HttpRequesterFactory.class, "构造HttpClient中：需要设置 %s 代理 %s : %d", apn, proxy.getHostName(),
//						proxy.getPort());
//			}
//		}
//
//		if (apn.equals(NetworkStatus.APN.APN_CTWAP)) {
//			HttpHost proxy = new HttpHost("10.0.0.200", 80, null);
//			params.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
//			if (DLog.isNetLog) {
//				DLog.td(DLog.mNetTag, HttpRequesterFactory.class, "构造HttpClient中：需要设置 %s 代理 %s : %d", apn, proxy.getHostName(),
//						proxy.getPort());
//			}
//		}
//
//		// 设置HttpClient支持HTTP和HTTPS两种模式
//		SchemeRegistry schReg = new SchemeRegistry();
//		schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
//		//		schReg.register(new Scheme("https", SSLSocketFactory
//		//				.getSocketFactory(), 443));
//
//		// 使用线程安全的连接管理来创建HttpClient
//		ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, schReg);
//
//		DefaultHttpClient httpClient = new DefaultHttpClient(ccm, params);
//
//		// 设置重定向处理，目的是获得重定向后的地址
//		httpClient.setRedirectHandler(new RedirectHandler() {
//
//			@Override
//			public boolean isRedirectRequested(HttpResponse response, HttpContext context) {
//				int statusCode = response.getStatusLine().getStatusCode();
//				if (DLog.isNetLog) {
//					DLog.td(DLog.mNetTag, HttpRequesterFactory.class, "StatusCode : %d", statusCode);
//				}
//
//				if (//statusCode == HttpStatus.SC_MULTIPLE_CHOICES ||
//						statusCode == HttpStatus.SC_MOVED_PERMANENTLY ||
//						statusCode == HttpStatus.SC_MOVED_TEMPORARILY ||
//						statusCode == HttpStatus.SC_SEE_OTHER || statusCode == HttpStatus.SC_TEMPORARY_REDIRECT) {
//					// 此处重定向处理
//					return true;
//				}
//				return false;
//			}
//
//			@Override
//			public URI getLocationURI(HttpResponse response, HttpContext context) throws ProtocolException {
//
//				Header header = response.getFirstHeader("Location");
//				if (header == null) {
//					header = response.getFirstHeader("location");
//					if (header == null) {
//						return null;
//					}
//				}
//
//				String url = header.getValue();
//				if (DLog.isNetLog) {
//					DLog.td(DLog.mNetTag, HttpRequesterFactory.class, "处理重定向url，获取Location:%s", url);
//				}
//
//				if (url == null) {
//					return null;
//				}
//				return URI.create(url);
//			}
//		});
//
//		return httpClient;
//	}

	/**
	 * 获取UserAgent
	 *
	 * @return
	 */
	public static String getmUserAgent() {

		// AdLog.e(Build.class.getName());
		// AdLog.e("BOARD:"+Build.BOARD);
		// AdLog.e("BRAND:"+Build.BRAND);
		// AdLog.e("CPU_ABI:"+Build.CPU_ABI);
		// AdLog.e("DEVICE:"+Build.DEVICE);
		// AdLog.e("DISPLAY:"+Build.DISPLAY);
		// AdLog.e("FINGERPRINT:"+Build.FINGERPRINT);
		// AdLog.e("HOST:"+Build.HOST);
		// AdLog.e("ID:"+Build.ID);
		// AdLog.e("MANUFACTURER:"+Build.MANUFACTURER);
		// AdLog.e("MODEL:"+Build.MODEL);
		// AdLog.e("PRODUCT:"+Build.PRODUCT);
		// AdLog.e("TAGS:"+Build.TAGS);
		// AdLog.e("TYPE:"+Build.TYPE);
		// AdLog.e("USER:"+Build.USER);
		// AdLog.e("VERSION.CODENAME:"+Build.VERSION.CODENAME);
		// AdLog.e("VERSION.INCREMENTAL:"+Build.VERSION.INCREMENTAL);
		// AdLog.e("VERSION.RELEASE:"+Build.VERSION.RELEASE);
		// AdLog.e("VERSION.SDK:"+Build.VERSION.SDK);
		// AdLog.e("VERSION.SDK_INT:"+Build.VERSION.SDK_INT+"");
		// AdLog.e("VERSION_CODES.BASE:"+Build.VERSION_CODES.BASE+"");
		// AdLog.e("VERSION_CODES.BASE_1_1:"+Build.VERSION_CODES.BASE_1_1+"");
		// AdLog.e("VERSION_CODES.CUPCAKE:"+Build.VERSION_CODES.CUPCAKE+"");
		// AdLog.e("VERSION_CODES.CUR_DEVELOPMENT:"+Build.VERSION_CODES.CUR_DEVELOPMENT+"");
		// AdLog.e("Build.VERSION_CODES.DONUT:"+Build.VERSION_CODES.DONUT+"");

		if (mUserAgent == null) {

			try {

				StringBuilder sb = new StringBuilder(256);
				sb.append("Mozilla/5.0 (Linux; U; Android ");
				sb.append(Build.VERSION.RELEASE);
				sb.append("; ");
				sb.append(Global_Runtime_SystemInfo.getLocaleLanguage_Country().toLowerCase());
				sb.append("; ");
				sb.append(Global_Runtime_SystemInfo.getDeviceModel());
				sb.append(" Build/");
				sb.append(Build.ID);
				sb.append(") AppleWebkit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1");

				mUserAgent = sb.toString();

			} catch (Throwable e) {
				if (DLog.isNetLog) {
					DLog.te(DLog.mNetTag, HttpRequesterFactory.class, e);
				}
				return "";
			}
		}
		return mUserAgent;
	}

	/**
	 * 设置sdk默认的UserAgent
	 *
	 * @param ua
	 */
	public static void setmUserAgent(String ua) {
		try {
			if (ua != null) {
				ua = ua.trim();
				if (ua.length() > 0) {
					mUserAgent = ua;
				}
			}
		} catch (Throwable e) {
			if (DLog.isNetLog) {
				DLog.te(DLog.mNetTag, HttpRequesterFactory.class, e);
			}
		}
	}

}
