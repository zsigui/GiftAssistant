package net.youmi.android.libs.common.v2.network.core;

import net.youmi.android.libs.common.debug.DLog;
import net.youmi.android.libs.common.dns.Message;
import net.youmi.android.libs.common.dns.SimpleResolver;
import net.youmi.android.libs.common.global.Global_Charsets;
import net.youmi.android.libs.common.v2.network.NetworkUtil;

import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 封装了http请求的相关数据，注意本类不能引入apache的包
 *
 * @author zhitao
 * @since 2015-09-15 15:31
 */
public class BaseHttpRequesterModel {

	public final static String REQUEST_TYPE_GET = "GET";

	public final static String REQUEST_TYPE_POST = "POST";

	/**
	 * 请求url
	 */
	protected String mRequestUrl;

	/**
	 * 请求类型，post和get
	 */
	protected String mRequsetType;

	/**
	 * 请求头
	 */
	protected Map<String, List<String>> mExtraHeaders;

	/**
	 * post data , 如果为空，看看是不是需要二进制post，如果都不用就，就使用get方法请求
	 */
	protected Map<String, String> mPostDataMap;

	/**
	 * post data , post二进制数据
	 */
	protected byte[] mPostDataByteArray;

	/**
	 * 编码格式
	 */
	private String mEncodingCharset;

	/**
	 * 设置请求url
	 *
	 * @param requestUrl
	 */
	public void setRequestUrl(String requestUrl) {
		mRequestUrl = requestUrl;
	}

	public String getRequestUrl() {
		return mRequestUrl;
	}

	/**
	 * 设置请求类型
	 *
	 * @param mRequsetType {@link #REQUEST_TYPE_GET} or {@link #REQUEST_TYPE_POST} or "delete" or ....
	 */
	public void setRequsetType(String mRequsetType) {
		this.mRequsetType = mRequsetType;
	}

	/**
	 * 如果没有调用set方法，那么默认是get 请求
	 *
	 * @return
	 */
	public String getRequestType() {
		if (mRequsetType == null) {
			return REQUEST_TYPE_GET;
		}
		return mRequsetType;
	}

	/**
	 * 设置请求头
	 *
	 * @param extraHeaders
	 */
	public void setExtraHeaders(Map<String, List<String>> extraHeaders) {
		mExtraHeaders = extraHeaders;
	}

	public Map<String, List<String>> getExtraHeaders() {
		return mExtraHeaders;
	}

	/**
	 * 设置post 二进制数据
	 *
	 * @param bytes
	 */
	public void setPostDataByteArray(byte[] bytes) {
		mPostDataByteArray = bytes;
	}

	public byte[] getPostDataByteArray() {
		return mPostDataByteArray;
	}

	/**
	 * 设置post NameValuePair 数据
	 *
	 * @return
	 */
	public void setPostDataMap(Map<String, String> postDataMap) {
		mPostDataMap = postDataMap;
	}

	public Map<String, String> getPostDataMap() {
		return mPostDataMap;
	}

	/**
	 * 设置请求编码
	 *
	 * @param encoding
	 */
	public void setEncodingCharset(String encoding) {
		mEncodingCharset = encoding;
	}

	public String getEncodingCharset() {
		if (mEncodingCharset == null) {
			return Global_Charsets.UTF_8;
		}
		return mEncodingCharset;
	}

	/**
	 * 通过请求获取服务器IP[这是一个耗时的方法]
	 *
	 * @return 形如下面的格式 "127.0.0.1;...,...,..." or null
	 */
	private String request4HostIp() {
		return NetworkUtil.request4HostIp(mRequestUrl);
	}

	public String getHostString() {
		try {
			return new URI(mRequestUrl).getHost();
			//			URI uri = new URI(mRequestUrl);
			//			if (Debug_SDK.isNetLog) {
			//				DLog.te(DLog.mNetTag, this, "getHost() : %s", uri.getHost());
			//				DLog.te(DLog.mNetTag, this, "getRawPath() : %s", uri.getRawPath());
			//				DLog.te(DLog.mNetTag, this, "getPath() : %s", uri.getPath());
			//				DLog.te(DLog.mNetTag, this, "getQuery() : %s", uri.getQuery());
			//				DLog.te(DLog.mNetTag, this, "getRawQuery() : %s", uri.getRawQuery());
			//				DLog.te(DLog.mNetTag, this, "getScheme() : %s", uri.getScheme());
			//				DLog.te(DLog.mNetTag, this, "getSchemeSpecificPart() : %s", uri.getSchemeSpecificPart());
			//				DLog.te(DLog.mNetTag, this, "getRawSchemeSpecificPart() : %s", uri
			// .getRawSchemeSpecificPart());
			//			}

			//			if (!Basic_StringUtil.isNullOrEmpty(mRequestUrl)) {
			//				String host = "";
			//				host = mRequestUrl.replace("http://", "");
			//				int end = host.indexOf("/");
			//				if (end > 0) {
			//					host = host.substring(0, end);
			//					return host;
			//				}
			//			}
		} catch (Exception e) {
			if (DLog.isDebug) {
				e.printStackTrace();
			}
		}
		return "";
	}

	public String getPathString() {
		try {
			return new URI(mRequestUrl).getRawPath();

			//			if (!Basic_StringUtil.isNullOrEmpty(mRequestUrl)) {
			//				String path = "";
			//				path = mRequestUrl.replace("http://", "");
			//				int start = path.indexOf("/");
			//				int end = path.indexOf("?");
			//				if (start >= 0 && end > 0 && end > start) {
			//					path = path.substring(start, end);
			//					return path;
			//				}
			//			}
		} catch (Exception e) {
			if (DLog.isDebug) {
				e.printStackTrace();
			}
		}
		return "";

	}

	public String getQueryString() {
		try {
			return new URI(mRequestUrl).getRawQuery();

			//			if (!Basic_StringUtil.isNullOrEmpty(mRequestUrl)) {
			//				String query = "";
			//				int start = mRequestUrl.indexOf("?");
			//				int end = mRequestUrl.length();
			//				if (start >= 0 && end > 0 && end > start) {
			//					query = mRequestUrl.substring(start, end);
			//					return query;
			//				}
			//			}
		} catch (Exception e) {
			if (DLog.isDebug) {
				e.printStackTrace();
			}
		}
		return "";
	}

	/**
	 * 通过阿里云DNS解析服务器，获取本次请求域名的实际IP，然后替换域名为实际的ip
	 * 耗时的操作
	 */
	public boolean replaceHostStringWithReallyIpAddress() {
		try {
			SimpleResolver sr = new SimpleResolver("223.5.5.5");
			final String host = getHostString();
			Message query = new Message(host);
			InetAddress addr = InetAddress.getByAddress(sr.send(query).getAddr());

			if (mExtraHeaders == null) {
				mExtraHeaders = new HashMap<String, List<String>>();
			}
			if (!mExtraHeaders.containsKey("host")) {
				List<String> temp = new ArrayList<String>();
				temp.add(host);
				mExtraHeaders.put("host", temp);
			} else {
				mExtraHeaders.get("host").add(host);
			}

			String ip = addr.getHostAddress();
			mRequestUrl = mRequestUrl.replaceFirst(host, ip);
			return true;

		} catch (Exception e) {
			if (DLog.isNetLog) {
				DLog.te(DLog.mNetTag, this, e);
			}
		}

		return false;
	}

	@Override
	public String toString() {

		if (DLog.isNetLog) {
			try {
				final StringBuilder sb = new StringBuilder("BaseHttpRequesterModel {\n");
				sb.append("  mRequestUrl=\"").append(mRequestUrl).append('\"').append("\n");
				sb.append("  mRequsetType=\"").append(mRequsetType).append('\"').append("\n");
				sb.append("  mExtraHeaders=").append(mExtraHeaders).append("\n");
				sb.append("  mPostDataMap=").append(mPostDataMap).append("\n");
				sb.append("  mPostDataByteArray=").append(Arrays.toString(mPostDataByteArray)).append("\n");
				sb.append("  mEncodingCharset=\"").append(mEncodingCharset).append('\"').append("\n");
				sb.append("  request4HostIp()=\"").append(request4HostIp()).append('\"').append("\n");
				sb.append("  getHostString()=\"").append(getHostString()).append('\"').append("\n");
				sb.append("  getPathString()=\"").append(getPathString()).append('\"').append("\n");
				sb.append("  getQueryString()=\"").append(getQueryString()).append('\"').append("\n");
				sb.append('}');
				return sb.toString();
			} catch (Exception e) {
				if (DLog.isNetLog) {
					DLog.te(DLog.mNetTag, this, e);
				}
			}
		}

		return super.toString();
	}
}
