package net.youmi.android.libs.common.v2.network;

import android.content.Context;
import android.net.Uri;
import android.os.Build;

import net.youmi.android.libs.common.basic.Basic_StringUtil;
import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.dns.Message;
import net.youmi.android.libs.common.dns.SimpleResolver;
import net.youmi.android.libs.common.v2.download.core.DownloadUtil;
import net.youmi.android.libs.common.v2.network.core.BaseHttpRequesterModel;
import net.youmi.android.libs.common.v2.network.core.HttpRequesterFactory;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 从网络地址中提取出文件的相关描述信息
 */
public class NetworkUtil {

	/**
	 * 获取最终的url地址(按照http 1.0协议，最大循环循环遍历5次，如果5次之后都没有获取到的话就返回原来的地址)
	 *
	 * @param rawUrl
	 * @return
	 */
	public static String getFinalDestUrl(Context context, String rawUrl) {
		if (Build.VERSION.SDK_INT < 9) {
			return getFinalDestUrlByHttpClient(context, rawUrl, 5);
		} else {
			return getFinalDestUrlByHttpURLConnection(context, rawUrl, 5);
		}
	}

	/**
	 * 获取最终的url地址(循环遍历301/302)
	 *
	 * @param rawUrl
	 * @param loopMaxTimes 最大遍历次数，不然坑爹一点的话会无限循环重定向 (A->B->A)
	 * @return
	 */
	public static String getFinalDestUrlByHttpClient(Context context, String rawUrl, int loopMaxTimes) {
		if (Basic_StringUtil.isNullOrEmpty(rawUrl)) {
			return null;
		}
		int count = 0;
		if (Debug_SDK.isNetLog) {
			Debug_SDK.td(Debug_SDK.mNetTag, NetworkUtil.class, "原始url:%s", rawUrl);
		}

		DefaultHttpClient httpClient = null;
		HttpGet httpget = null;

		try {
			// 因为这里生成的httpclient是已经开启了重定向处理的，所以需要传入一个值来获取其中的状态
			httpClient = HttpRequesterFactory.newHttpClient(context);
			httpget = new HttpGet(rawUrl);

			// httpContext会在execute中传入，记录请求中的一些状态信息，如：记录重定向的信息
			HttpContext httpContext = new BasicHttpContext();
			httpClient.execute(httpget, httpContext);

			HttpHost targetHost = (HttpHost) httpContext.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
			HttpUriRequest realRequest = (HttpUriRequest) httpContext.getAttribute(ExecutionContext.HTTP_REQUEST);
			String finalUrl = targetHost.getHostName() + realRequest.getURI().toString();
			if (!finalUrl.startsWith("http://")) {
				finalUrl = "http://" + finalUrl;
			}
			if (Debug_SDK.isNetLog) {
				Debug_SDK.td(Debug_SDK.mNetTag, NetworkUtil.class, "最终url:%s", finalUrl);
			}
			return finalUrl;

		} catch (Exception e) {
			if (Debug_SDK.isNetLog) {
				Debug_SDK.te(Debug_SDK.mNetTag, NetworkUtil.class, e);
			}
		} finally {
			try {
				if (httpget != null) {
					httpget.abort();
				}
			} catch (Exception e) {
				if (Debug_SDK.isNetLog) {
					Debug_SDK.te(Debug_SDK.mNetTag, NetworkUtil.class, e);
				}
			}
			try {
				// 至此，HttpClient的实例已经不再需要时，可以使用连接管理器关闭
				if (httpClient != null) {
					httpClient.getConnectionManager().shutdown();
				}
			} catch (Exception e) {
				if (Debug_SDK.isNetLog) {
					Debug_SDK.te(Debug_SDK.mNetTag, NetworkUtil.class, e);
				}

			}
		}

		return rawUrl;
	}

	/**
	 * 获取最终的url地址(循环遍历301/302)
	 *
	 * @param rawUrl
	 * @param loopMaxTimes 最大遍历次数，不然坑爹一点的话会无限循环重定向 (A->B->A)
	 * @return
	 */
	public static String getFinalDestUrlByHttpURLConnection(Context context, String rawUrl, int loopMaxTimes) {
		if (Basic_StringUtil.isNullOrEmpty(rawUrl)) {
			return null;
		}
		int count = 0;
		if (Debug_SDK.isNetLog) {
			Debug_SDK.td(Debug_SDK.mNetTag, NetworkUtil.class, "原始url:%s", rawUrl);
		}
		String currentUrl = rawUrl;
		while (count < loopMaxTimes) {
			HttpURLConnection httpURLConnection = null;
			try {
				httpURLConnection = HttpRequesterFactory.newHttpURLConnection(context, currentUrl);
				httpURLConnection.setRequestMethod(BaseHttpRequesterModel.REQUEST_TYPE_GET);

				// 关闭自动跟踪重定向
				httpURLConnection.setInstanceFollowRedirects(false);

				// 只要connection就可以获取到头部信息，而不用getInputStream(发送请求)
				httpURLConnection.connect();

				switch (httpURLConnection.getResponseCode()) {
					case HttpURLConnection.HTTP_MULT_CHOICE://300
					case HttpURLConnection.HTTP_MOVED_TEMP: //301
					case HttpURLConnection.HTTP_MOVED_PERM: //302
					case HttpURLConnection.HTTP_SEE_OTHER:  //303
					case 307:                               //307
						currentUrl = httpURLConnection.getHeaderField("Location");
						if (Debug_SDK.isNetLog) {
							Debug_SDK.td(Debug_SDK.mNetTag, NetworkUtil.class, "中途url:%s", currentUrl);
						}
						break;
					default:
						httpURLConnection.disconnect();
						if (Debug_SDK.isNetLog) {
							Debug_SDK.td(Debug_SDK.mNetTag, NetworkUtil.class, "最终url:%s", currentUrl);
						}
						return currentUrl;
				}
			} catch (Exception e) {
				if (Debug_SDK.isNetLog) {
					Debug_SDK.te(Debug_SDK.mNetTag, NetworkUtil.class, e);
				}
			} finally {
				try {
					if (httpURLConnection != null) {
						httpURLConnection.disconnect();
					}
				} catch (Exception e) {
					if (Debug_SDK.isNetLog) {
						Debug_SDK.te(Debug_SDK.mNetTag, NetworkUtil.class, e);
					}
				}
			}

		}
		return rawUrl;
	}

	/**
	 * 从描述中提取出文件名字
	 *
	 * @param contentDisposition
	 * @return
	 */
	public static String getFileNameFromContentDisposition(String contentDisposition) {
		try {
			if (contentDisposition == null) {
				return null;
			}
			Matcher matcher1 = Pattern.compile("filename=\"(.*?)\"").matcher(contentDisposition);
			if (matcher1.find()) {
				return matcher1.group(matcher1.groupCount());
			}
			Matcher matcher2 = Pattern.compile("filename='(.*?)'").matcher(contentDisposition);
			if (matcher2.find()) {
				return matcher2.group(matcher2.groupCount());
			}
		} catch (Throwable e) {
			if (Debug_SDK.isNetLog) {
				Debug_SDK.te(Debug_SDK.mNetTag, NetworkUtil.class, e);
			}
		}
		return null;
	}

	/**
	 * 从http 请求结果中提取出文件名字
	 *
	 * @param response
	 * @param destUrl
	 * @return
	 */
	public static String getFileNameFromHttpResponse(HttpResponse response, String destUrl) {
		String fileName = null;
		try {
			// 提取文件名等等
			Header[] headers = response.getHeaders("Content-Disposition");

			// 从Content-Disposition中获取fileName
			if (headers != null && headers.length > 0) {
				for (Header header : headers) {
					if (header != null) {
						fileName = getFileNameFromContentDisposition(header.getValue());
						if (!Basic_StringUtil.isNullOrEmpty(fileName)) {
							break;
						}
						fileName = null;
					}
				}
			}

			// 如果不行就从请求的url中获取
			if (fileName == null) {
				fileName = getFileNameFromHttpUrl(destUrl);
			}

		} catch (Throwable e) {
			if (Debug_SDK.isNetLog) {
				Debug_SDK.te(Debug_SDK.mNetTag, NetworkUtil.class, e);
			}
		}
		return fileName;
	}

	/**
	 * 从http请求url中获取文件名字(仅仅是截获最后一个'/'后的字符串)
	 *
	 * @param destUrl
	 * @return
	 */
	public static String getFileNameFromHttpUrl(String destUrl) {
		try {
			if (Basic_StringUtil.isNullOrEmpty(destUrl)) {
				return null;
			}
			Uri uri = Uri.parse(destUrl);
			String path = uri.getPath();

			int index = path.lastIndexOf('/');
			if (index > -1) {
				return path.substring(index + 1);
			}
		} catch (Throwable e) {
			if (Debug_SDK.isNetLog) {
				Debug_SDK.te(Debug_SDK.mNetTag, NetworkUtil.class, e);
			}
		}
		return null;
	}

	/**
	 * 从html页面中获取字符编码，用于对付顽固gb2312
	 *
	 * @param html
	 * @return
	 */
	public static String getCharsetFromHtml(String html) {
		try {
			if (Basic_StringUtil.isNullOrEmpty(html)) {
				return null;
			}
			// Pattern
			// pattern=Pattern.compile("<meta.*http-equiv=.*Content-Type.*content=.*text/html;.*charset=(.*?)\"|'
			// .*/>",Pattern
			// .CASE_INSENSITIVE);
			Pattern pattern =
					Pattern.compile("<meta.*content.*text/html;.*charset=(.*?)\"|'.*/>", Pattern.CASE_INSENSITIVE);//
					// 保险一点的做法
			Matcher matcher = pattern.matcher(html);
			if (matcher.find()) {
				return matcher.group(matcher.groupCount());
			}
		} catch (Throwable e) {
			if (Debug_SDK.isNetLog) {
				Debug_SDK.te(Debug_SDK.mNetTag, NetworkUtil.class, e);
			}
		}
		return null;
	}

	//	/**
	//	 * 判断两个url的路径是否相同
	//	 *
	//	 * @param urlA
	//	 * @param urlB
	//	 *
	//	 * @return
	//	 */
	//	public static boolean isUrlMatchWithPath(String urlA, String urlB) {
	//		try {
	//			if (Basic_StringUtil.isNullOrEmpty(urlA) || Basic_StringUtil.isNullOrEmpty(urlB)) {
	//				return false;
	//			}
	//
	//			urlA = urlA.trim();
	//			urlB = urlB.trim();
	//			if (urlA.equalsIgnoreCase(urlB)) {
	//				return true;
	//			}
	//
	//			URL uriA = new URL(urlA);
	//			URL uriB = new URL(urlB);
	//
	//			String hostA = uriA.getHost();
	//			String hostB = uriB.getHost();
	//			if ((hostA.equals(hostB)) && (uriA.getProtocol().equals(uriB.getProtocol())) && (uriA.getPort() ==
	// uriB.getPort
	// ())) {
	//
	//				String pathA = uriA.getPath();
	//				String pathB = uriB.getPath();
	//				if (pathA.equalsIgnoreCase(pathB)) {
	//					return true;
	//				}
	//
	//				if (pathA.length() == pathB.length()) {
	//					// 长度一致，但内容不一致
	//					return false;
	//				}
	//
	//				pathA = pathA.replace('/', ' ').trim();
	//				pathB = pathB.replace('/', ' ').trim();
	//
	//				if (pathA.equalsIgnoreCase(pathB)) {
	//					return true;
	//				}
	//			}
	//
	//		} catch (Throwable e) {
	//			if (Debug_SDK.isNetLog) {
	//				Debug_SDK.te(Debug_SDK.mNetTag, Util_Network_HttpUtil.class, e);
	//			}
	//		}
	//		return false;
	//	}
	//
	//	/**
	//	 * 判断指定的url是不是在某个url列表上面
	//	 *
	//	 * @param urls 多个url的字符串，用逗号分隔
	//	 * @param url  指定的url，可带查询串，但最终判断时会去掉查询串
	//	 *
	//	 * @return
	//	 */
	//	public static boolean isUrlsContainsWithDestUrl(String urls, String url) {
	//		try {
	//
	//			if (urls == null || url == null) {
	//				return false;
	//			}
	//
	//			urls = urls.trim();
	//			url = url.trim();
	//
	//			if (urls.length() == 0 || url.length() == 0) {
	//				return false;
	//			}
	//
	//			if (urls.equalsIgnoreCase(url)) {
	//				return true;
	//			}
	//
	//			Uri uriDestUrl = Uri.parse(url);
	//
	//			String destUrlPath = uriDestUrl.getPath();
	//
	//			if (urls.contains(destUrlPath) && urls.contains(uriDestUrl.getHost())) {
	//				return true;
	//			}
	//
	//		} catch (Throwable e) {
	//			if (Debug_SDK.isNetLog) {
	//				Debug_SDK.te(Debug_SDK.mNetTag, Util_Network_HttpUtil.class, e);
	//			}
	//		}
	//		return false;
	//	}

	public static long getContentLength(Context context, String url) {
		if (Build.VERSION.SDK_INT < 9) {
			return getContentLengthByHttpClient(context, url);
		} else {
			return getContentLengthByHttpURLConnection(context, url, 5);
		}

	}

	/**
	 * 获取服务器上目标文件的长度(支持传入从重定向地址)
	 *
	 * @param context
	 * @param url
	 * @return
	 */
	public static long getContentLengthByHttpClient(Context context, String url) {
		DefaultHttpClient client = null;
		HttpGet get = null;
		try {
			if (url == null) {
				return -1;
			}
			client = HttpRequesterFactory.newHttpClient(context);
			get = new HttpGet(url);
			HttpResponse response = client.execute(get);
			int code = response.getStatusLine().getStatusCode();
			if (code >= 200 && code < 300) {
				return response.getEntity().getContentLength();
			}

		} catch (Throwable e) {
			if (Debug_SDK.isNetLog) {
				Debug_SDK.te(Debug_SDK.mNetTag, NetworkUtil.class, e);
			}
		} finally {
			try {
				if (get != null) {
					get.abort();
				}
			} catch (Throwable e) {
				if (Debug_SDK.isNetLog) {
					Debug_SDK.te(Debug_SDK.mNetTag, NetworkUtil.class, e);
				}
			}

			try {
				if (client != null) {
					client.getConnectionManager().shutdown();
				}
			} catch (Throwable e) {
				if (Debug_SDK.isNetLog) {
					Debug_SDK.te(Debug_SDK.mNetTag, NetworkUtil.class, e);
				}
			}
		}
		return -1;
	}

	/**
	 * 获取服务器上目标文件的长度(支持传入从重定向地址)
	 *
	 * @param rawUrl
	 * @param loopMaxTimes 最大遍历次数，不然坑爹一点的话会无限循环重定向 (A->B->A)
	 * @return
	 */
	public static long getContentLengthByHttpURLConnection(Context context, String rawUrl, int loopMaxTimes) {
		if (Basic_StringUtil.isNullOrEmpty(rawUrl)) {
			return -1;
		}
		int count = 0;
		if (Debug_SDK.isNetLog) {
			Debug_SDK.td(Debug_SDK.mNetTag, NetworkUtil.class, "原始url:%s", rawUrl);
		}
		String currentUrl = rawUrl;
		while (count < loopMaxTimes) {
			HttpURLConnection httpURLConnection = null;
			try {
				httpURLConnection = HttpRequesterFactory.newHttpURLConnection(context, currentUrl);
				httpURLConnection.setRequestMethod(BaseHttpRequesterModel.REQUEST_TYPE_GET);

				// 关闭自动跟踪重定向
				httpURLConnection.setInstanceFollowRedirects(false);

				// 只要connection就可以获取到头部信息，而不用getInputStream(发送请求)
				httpURLConnection.connect();

				switch (httpURLConnection.getResponseCode()) {
					case HttpURLConnection.HTTP_MULT_CHOICE://300
					case HttpURLConnection.HTTP_MOVED_TEMP: //301
					case HttpURLConnection.HTTP_MOVED_PERM: //302
					case HttpURLConnection.HTTP_SEE_OTHER:  //303
					case 307:                               //307
						currentUrl = httpURLConnection.getHeaderField("Location");
						if (Debug_SDK.isNetLog) {
							Debug_SDK.td(Debug_SDK.mNetTag, NetworkUtil.class, "中途url:%s", currentUrl);
						}
						break;
					default:
						long contentLength = httpURLConnection.getContentLength();
						httpURLConnection.disconnect();
						if (Debug_SDK.isNetLog) {
							Debug_SDK.td(Debug_SDK.mNetTag, NetworkUtil.class, "最终url:%s", currentUrl);
							Debug_SDK.td(Debug_SDK.mNetTag, NetworkUtil.class, "最终ContentLength:%d", contentLength);
						}
						return contentLength;
				}
			} catch (Exception e) {
				if (Debug_SDK.isNetLog) {
					Debug_SDK.te(Debug_SDK.mNetTag, NetworkUtil.class, e);
				}
			} finally {
				try {
					if (httpURLConnection != null) {
						httpURLConnection.disconnect();
					}
				} catch (Exception e) {
					if (Debug_SDK.isNetLog) {
						Debug_SDK.te(Debug_SDK.mNetTag, NetworkUtil.class, e);
					}
				}
			}

		}
		return -1;
	}

	/**
	 * 判断指定的url是不是在某个url列表上面
	 *
	 * @param urls 多个url的字符串，用逗号分隔
	 * @param url  指定的url，可带查询串，但最终判断时会去掉查询串
	 * @return
	 */
	public static boolean isUrlsContainsWithDestUrl(String urls, String url) {
		try {

			if (urls == null || url == null) {
				return false;
			}

			urls = urls.trim();
			url = url.trim();

			if (urls.length() == 0 || url.length() == 0) {
				return false;
			}

			if (urls.equalsIgnoreCase(url)) {
				return true;
			}

			Uri uriDestUrl = Uri.parse(url);

			String destUrlPath = uriDestUrl.getPath();

			if (urls.contains(destUrlPath) && urls.contains(uriDestUrl.getHost())) {
				return true;
			}

		} catch (Throwable e) {
			if (Debug_SDK.isNetLog) {
				Debug_SDK.te(Debug_SDK.mNetTag, NetworkUtil.class, e);
			}
		}
		return false;
	}

	/**
	 * 获取url域名所对应的所有ip
	 * <p/>
	 * 通过请求DNS，获取传入url所对应的所有ip[这是一个耗时的方法]
	 *
	 * @return 形如下面的格式 "127.0.0.1;...;...;..." or null
	 */
	public static String request4HostIp(String url) {
		try {
			String host = new URI(url).getHost();
			Map<String, List<String>> map = new HashMap<String, List<String>>();
			if (!Basic_StringUtil.isNullOrEmpty(host)) {
				try {
					InetAddress[] ipArray = InetAddress.getAllByName(host);
					for (InetAddress ip : ipArray) {
						if (ip instanceof Inet4Address) {
							if (!(map.containsKey("ipv4"))) {
								map.put("ipv4", new ArrayList<String>());
							}
							map.get("ipv4").add(ip.getHostAddress());
						} else if (ip instanceof Inet6Address) {
							if (!(map.containsKey("ipv6"))) {
								map.put("ipv6", new ArrayList<String>());
							}
							map.get("ipv6").add(ip.getHostAddress());
						}
					}
					if (map.isEmpty()) {
						return null;
					}
					StringBuilder sb = new StringBuilder();
					if (map.containsKey("ipv4")) {
						for (String ip : map.get("ipv4")) {
							sb.append(ip).append(";");
						}
					}
					if (map.containsKey("ipv6")) {
						for (String ip : map.get("ipv6")) {
							sb.append(ip).append(";");
						}
					}
					return sb.substring(0, sb.length() - 1);
				} catch (Exception e) {
					if (Debug_SDK.isNetLog) {
						Debug_SDK.te(Debug_SDK.mNetTag, NetworkUtil.class, e);
					}

				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isNetLog) {
				Debug_SDK.te(Debug_SDK.mNetTag, NetworkUtil.class, e);
			}
		}
		return null;
	}

	/**
	 * 通过阿里云DNS解析服务器，获取本次请求域名的实际IP，然后替换域名为实际的ip[耗时的操作]
	 *
	 * @return
	 */
	public static String getReallyIpAddress(String url) {
		try {
			SimpleResolver sr = new SimpleResolver("223.5.5.5");
			String host = new URI(url).getHost();
			Message query = new Message(host);
			String ip = InetAddress.getByAddress(sr.send(query).getAddr()).getHostAddress();
			return url.replaceFirst(host, ip);
		} catch (Exception e) {
			if (Debug_SDK.isNetLog) {
				Debug_SDK.te(Debug_SDK.mNetTag, DownloadUtil.class, e);
			}
		}
		return null;
	}
}
