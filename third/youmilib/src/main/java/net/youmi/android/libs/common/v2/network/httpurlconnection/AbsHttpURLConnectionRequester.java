package net.youmi.android.libs.common.v2.network.httpurlconnection;

import android.content.Context;

import net.youmi.android.libs.common.basic.Basic_StringUtil;
import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.global.Global_Charsets;
import net.youmi.android.libs.common.v2.network.core.AbsHttpRequester;
import net.youmi.android.libs.common.v2.network.core.BaseHttpRequesterModel;
import net.youmi.android.libs.common.v2.network.core.HttpRequestExceptionCode;
import net.youmi.android.libs.common.v2.network.core.HttpRequesterFactory;

import org.apache.http.conn.ConnectTimeoutException;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * @author zhitao
 * @since 2015-09-15 09:31
 */
public abstract class AbsHttpURLConnectionRequester extends AbsHttpRequester {

	protected HttpURLConnection mHttpURLConnection;

	protected boolean mIsRunning;

	/**
	 * @param context
	 * @param baseHttpRequesterModel 本次请求的相关参数的自定义数据模型
	 * @throws NullPointerException
	 */
	public AbsHttpURLConnectionRequester(Context context, BaseHttpRequesterModel baseHttpRequesterModel)
			throws NullPointerException {
		super(context, baseHttpRequesterModel);
	}

	/**
	 * 请求之前的钩子，如:子类可以为 httpRequestBase 添加额外的头部
	 */
	protected abstract void beforeRequest(HttpURLConnection httpURLConnection);

	@Override
	public void abort() {
		mIsRunning = false;
		//　标识请求已经舍弃了
		mBaseHttpResponseModel.setIsFinishResponse(mIsRunning);

		try {
			mHttpURLConnection.disconnect();
		} catch (Throwable e) {
			if (Debug_SDK.isNetLog) {
				Debug_SDK.te(Debug_SDK.mNetTag, this, e);
			}
		}
	}

	@Override
	protected void newHttpRequest() {

		InputStream inputStream = null;
		ByteArrayOutputStream baos = null;
		mIsRunning = true;
		//　一开始就标识请求是能正常跑的，而不是被主动打断的
		mBaseHttpResponseModel.setIsFinishResponse(mIsRunning);
		try {

			// 创建默认的HttpURLConnection
			mHttpURLConnection =
					HttpRequesterFactory.newHttpURLConnection(mApplicationContext, mBaseHttpRequesterModel
							.getRequestUrl());

			if (mHttpURLConnection == null) {
				throw new NullPointerException();
			}

			// 请求之前的钩子,子类可以在这里添加额外的header之类
			beforeRequest(mHttpURLConnection);

			// 添加额外的http请求头部数据
			try {
				Map<String, List<String>> map = mBaseHttpRequesterModel.getExtraHeaders();
				if (map != null && !map.isEmpty()) {
					for (Map.Entry<String, List<String>> entry : map.entrySet()) {
						for (String value : entry.getValue()) {
							mHttpURLConnection.addRequestProperty(entry.getKey(), value);
						}
					}
				}
			} catch (Throwable e) {
				if (Debug_SDK.isNetLog) {
					Debug_SDK.te(Debug_SDK.mNetTag, this, e);
				}
			}

			// 设置请求方式 GET OR POST OR OTHER
			mHttpURLConnection.setRequestMethod(mBaseHttpRequesterModel.getRequestType());

			// 根据请求参数设置与请求类型相关的其他参数
			// get
			if (BaseHttpRequesterModel.REQUEST_TYPE_GET.equals(mBaseHttpRequesterModel.getRequestType())) {

			}
			// post
			else if (BaseHttpRequesterModel.REQUEST_TYPE_POST.equals(mBaseHttpRequesterModel.getRequestType())) {

				// 设置是否向httpUrlConnection输出，默认情况下是false。使用httpUrlConnection.getOutputStream()，把内容输出到远程服务器上。
				mHttpURLConnection.setDoOutput(true);

				OutputStream os = mHttpURLConnection.getOutputStream();

				// 优先以NameValuePair的post请求
				if (mBaseHttpRequesterModel.getPostDataMap() != null && !mBaseHttpRequesterModel.getPostDataMap()
						.isEmpty()) {

					StringBuilder sb = new StringBuilder();
					for (Map.Entry<String, String> entry : mBaseHttpRequesterModel.getPostDataMap().entrySet()) {
						sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
					}
					String params = sb.toString().substring(0, sb.length() - 1);
					String afterURLEncode = URLEncoder.encode(params, Global_Charsets.UTF_8);
					if (Debug_SDK.isNetLog) {
						Debug_SDK.td(Debug_SDK.mNetTag, this, "[POST]原始请求参数:%s urlencode后:%s", params, afterURLEncode);
					}

					os.write(afterURLEncode.getBytes(mBaseHttpRequesterModel.getEncodingCharset()));
					os.flush();
				}

				// 然后才是二进制的post data
				else if (mBaseHttpRequesterModel.getPostDataByteArray() != null &&
						mBaseHttpRequesterModel.getPostDataByteArray().length > 0) {
					os.write(mBaseHttpRequesterModel.getPostDataByteArray());
					os.flush();
				}

				if (os != null) {
					os.close();
				}
			}

			if (Debug_SDK.isNetLog) {
				Debug_SDK.td(Debug_SDK.mNetTag, this, "[%s] %s", mBaseHttpRequesterModel.getRequestType(),
						mBaseHttpRequesterModel.getRequestUrl());
			}

			// 如果在发起请求之前就舍弃的话就不发起请求
			if (!mIsRunning) {
				return;
			}
			//			inputStream =mHttpURLConnection.getInputStream();
			if (mHttpURLConnection.getResponseCode() >= 200 && mHttpURLConnection.getResponseCode() < 300) {
				inputStream = mHttpURLConnection.getInputStream();
			} else {
				inputStream = mHttpURLConnection.getErrorStream();
			}

			try {
				// 设置httpcode
				mBaseHttpResponseModel.setHttpCode(mHttpURLConnection.getResponseCode());
			} catch (Throwable e) {
				if (Debug_SDK.isNetLog) {
					Debug_SDK.te(Debug_SDK.mNetTag, this, e);
				}
			}
			try {
				// 设置状态信息
				mBaseHttpResponseModel.setHttpReasonPhrase(mHttpURLConnection.getResponseMessage());
			} catch (Throwable e) {
				if (Debug_SDK.isNetLog) {
					Debug_SDK.te(Debug_SDK.mNetTag, this, e);
				}
			}
			try {
				// 设置返回头部信息
				mBaseHttpResponseModel.setHeaders(mHttpURLConnection.getHeaderFields());
			} catch (Throwable e) {
				if (Debug_SDK.isNetLog) {
					Debug_SDK.te(Debug_SDK.mNetTag, this, e);
				}
			}

			try {
				// 设置返回结果的contentlength
				if (Debug_SDK.isNetLog) {
					Debug_SDK.td(Debug_SDK.mNetTag, this, "ContentLength:%d, ContentType:%s, ContentEncoding:%s",
							mHttpURLConnection.getContentLength(), mHttpURLConnection.getContentType(),
							mHttpURLConnection.getContentEncoding());
				}
				mBaseHttpResponseModel.setContentLength(mHttpURLConnection.getContentLength());
			} catch (Throwable e) {
				if (Debug_SDK.isNetLog) {
					Debug_SDK.te(Debug_SDK.mNetTag, this, e);
				}
			}

			try {
				// 获取contentEncoding来获取inputStream
				String contentEncoding = mHttpURLConnection.getContentEncoding();
				if (!Basic_StringUtil.isNullOrEmpty(contentEncoding) &&
						contentEncoding.toLowerCase(Locale.US).contains("gzip") && inputStream != null) {
					inputStream = new GZIPInputStream(inputStream);
					if (Debug_SDK.isNetLog) {
						Debug_SDK.td(Debug_SDK.mNetTag, this, "初始化inputStrrem为GZIPInputStream");
					}
				}
			} catch (Throwable e) {
				if (Debug_SDK.isNetLog) {
					Debug_SDK.te(Debug_SDK.mNetTag, this, e);
				}
			}

			if (inputStream == null) {
				throw new NullPointerException("InputStream is null");
			}

			baos = new ByteArrayOutputStream();
			byte[] buff = new byte[1024];
			int len = 0;

			while ((len = inputStream.read(buff)) > 0) {
				if (mIsRunning) {
					baos.write(buff, 0, len);
				} else {
					return;
				}
			}
			baos.flush();

			byte[] buffer = baos.toByteArray();
			mBaseHttpResponseModel.setBodyLength(buffer.length);

			String rspString = new String(buffer, mBaseHttpRequesterModel.getEncodingCharset());
			mBaseHttpResponseModel.setResponseString(rspString);

			if (Debug_SDK.isNetLog) {
				Debug_SDK.ti(Debug_SDK.mNetTag, this, "返回字符串[长度:%d]:%s", buffer.length, rspString);
			}

		} catch (ConnectTimeoutException e) {
			// 网络与服务器建立连接超时
			setException(HttpRequestExceptionCode.ConnectTimeoutException, e);

		} catch (SocketTimeoutException e) {
			// 请求超时
			setException(HttpRequestExceptionCode.SocketTimeoutException, e);

		} catch (UnknownHostException e) {
			// 网络没有打开或者没有配置网络权限的时候会1抛出这个异常
			setException(HttpRequestExceptionCode.UnknownHostException, e);

		} catch (SocketException e) {
			// 请求被关闭
			setException(HttpRequestExceptionCode.SocketException, e);

		} catch (Exception e) {
			// 这里如果有其他的异常的话先统一标记为-99 ，这个-99会在异常上报中反馈
			setException(HttpRequestExceptionCode.UnknownException, e);
		} finally {

			try {
				if (mHttpURLConnection != null) {
					mHttpURLConnection.disconnect();
				}
			} catch (Throwable e) {
				if (Debug_SDK.isNetLog) {
					Debug_SDK.te(Debug_SDK.mNetTag, this, e);
				}
			}
			try {
				if (baos != null) {
					baos.close();
				}
			} catch (Throwable e) {
				if (Debug_SDK.isNetLog) {
					Debug_SDK.te(Debug_SDK.mNetTag, this, e);
				}
			}
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (Throwable e) {
				if (Debug_SDK.isNetLog) {
					Debug_SDK.te(Debug_SDK.mNetTag, this, e);
				}
			}
		}

	}

}
