package net.youmi.android.libs.common.v2.network.httpclient;//package net.youmi.android.libs.common.v2.network.httpclient;
//
//import android.content.Context;
//
//import net.youmi.android.libs.common.debug.DLog;
//import net.youmi.android.libs.common.v2.network.core.AbsHttpRequester;
//import net.youmi.android.libs.common.v2.network.core.BaseHttpRequesterModel;
//import net.youmi.android.libs.common.v2.network.core.HttpRequestExceptionCode;
//import net.youmi.android.libs.common.v2.network.core.HttpRequesterFactory;
//
//import org.apache.http.Header;
//import org.apache.http.HttpEntity;
//import org.apache.http.HttpResponse;
//import org.apache.http.NameValuePair;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.entity.UrlEncodedFormEntity;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.client.methods.HttpRequestBase;
//import org.apache.http.conn.ConnectTimeoutException;
//import org.apache.http.conn.ConnectionPoolTimeoutException;
//import org.apache.http.conn.HttpHostConnectException;
//import org.apache.http.entity.ByteArrayEntity;
//import org.apache.http.message.BasicHeader;
//import org.apache.http.message.BasicNameValuePair;
//
//import java.io.ByteArrayOutputStream;
//import java.io.InputStream;
//import java.net.SocketException;
//import java.net.SocketTimeoutException;
//import java.net.UnknownHostException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Locale;
//import java.util.Map;
//import java.util.zip.GZIPInputStream;
//
///**
// * HttpClient网络请求基础抽象类
// */
//public abstract class AbsHttpClientRequester extends AbsHttpRequester {
//
//	protected HttpRequestBase mHttpRequestBase;
//
//	protected HttpClient mHttpClient;
//
//	protected boolean mIsRunning;
//
//	/**
//	 * @param context
//	 * @param baseHttpRequesterModel 本次请求的相关参数的自定义数据模型
//	 *
//	 * @throws NullPointerException
//	 */
//	public AbsHttpClientRequester(Context context, BaseHttpRequesterModel baseHttpRequesterModel) throws NullPointerException {
//		super(context, baseHttpRequesterModel);
//	}
//
//	/**
//	 * 请求之前的钩子，如:子类可以为 httpRequestBase 添加额外的头部
//	 *
//	 * @param httpRequestBase
//	 */
//	protected abstract void beforeRequest(HttpRequestBase httpRequestBase);
//
//	/**
//	 * 舍弃当前的请求
//	 */
//	@Override
//	public void abort() {
//		mIsRunning = false;
//		//　标识请求已经舍弃了
//		mBaseHttpResponseModel.setIsFinishResponse(mIsRunning);
//		try {
//			//　在舍弃的同时回收这个httpClient实例
//			if (mHttpClient != null) {
//				mHttpClient.getConnectionManager().shutdown();
//			}
//		} catch (Throwable e) {
//			if (DLog.isNetLog) {
//				DLog.te(DLog.mNetTag, this, e);
//			}
//		}
//		try {
//			if (mHttpRequestBase != null) {
//				mHttpRequestBase.abort();
//			}
//		} catch (Throwable e) {
//			if (DLog.isNetLog) {
//				DLog.te(DLog.mNetTag, this, e);
//			}
//		}
//
//	}
//
//	@Override
//	protected void newHttpRequest() {
//		ByteArrayOutputStream baos = null;
//		InputStream inputStream = null;
//		mIsRunning = true;
//		//　一开始就标识请求是能正常跑的，而不是被主动打断的
//		mBaseHttpResponseModel.setIsFinishResponse(mIsRunning);
//		try {
//			mHttpClient = HttpRequesterFactory.newHttpClient(mApplicationContext);
//
//			if (mHttpClient == null) {
//				throw new NullPointerException("http client is null");
//			}
//
//			//在请求之前做的一些操作，比如子类在添加一些额外的头部信息
//			beforeRequest(mHttpRequestBase);
//
//			// 添加额外的http请求头部数据duanqinei
//			try {
//				Map<String, List<String>> map = mBaseHttpRequesterModel.getExtraHeaders();
//				if (map != null && !map.isEmpty()) {
//					for (Map.Entry<String, List<String>> entry : map.entrySet()) {
//						for (String value : entry.getValue()) {
//							Header header = new BasicHeader(entry.getKey(), value);
//							mHttpRequestBase.addHeader(header);
//						}
//					}
//				}
//			} catch (Throwable e) {
//				if (DLog.isNetLog) {
//					DLog.te(DLog.mNetTag, this, e);
//				}
//			}
//
//			// 根据请求参数设置请求类型
//			// get
//			if (BaseHttpRequesterModel.REQUEST_TYPE_GET.equals(mBaseHttpRequesterModel.getRequestType())) {
//				mHttpRequestBase = new HttpGet(mBaseHttpRequesterModel.getRequestUrl());
//			}
//			// post
//			else if (BaseHttpRequesterModel.REQUEST_TYPE_POST.equals(mBaseHttpRequesterModel.getRequestType())) {
//
//				// 优先以NameValuePair的post请求
//				if (mBaseHttpRequesterModel.getPostDataMap() != null && !mBaseHttpRequesterModel.getPostDataMap().isEmpty()) {
//					HttpPost post = new HttpPost(mBaseHttpRequesterModel.getRequestUrl());
//					List<NameValuePair> postData = new ArrayList<NameValuePair>();
//					for (Map.Entry<String, String> entry : mBaseHttpRequesterModel.getPostDataMap().entrySet()) {
//						postData.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
//					}
//					HttpEntity httpEntity = new UrlEncodedFormEntity(postData, mBaseHttpRequesterModel.getEncodingCharset());
//					post.setEntity(httpEntity);
//					mHttpRequestBase = post;
//				}
//
//				// 然后才是二进制的post data
//				else if (mBaseHttpRequesterModel.getPostDataByteArray() != null &&
//				         mBaseHttpRequesterModel.getPostDataByteArray().length > 0) {
//					HttpPost post = new HttpPost(mBaseHttpRequesterModel.getRequestUrl());
//					post.setEntity(new ByteArrayEntity(mBaseHttpRequesterModel.getPostDataByteArray()));
//					mHttpRequestBase = post;
//				}
//			}
//
//			if (mHttpRequestBase == null) {
//				throw new NullPointerException("request is null");
//			}
//
//			if (DLog.isNetLog) {
//				DLog.td(DLog.mNetTag, this, "[%s] %s", mBaseHttpRequesterModel.getRequestType(),
//						mBaseHttpRequesterModel.getRequestUrl());
//			}
//
//			// 如果在发起请求之前就舍弃的话就不发起请求
//			if (!mIsRunning) {
//				return;
//			}
//
//			HttpResponse httpResponse = mHttpClient.execute(mHttpRequestBase);
//			HttpEntity entity = httpResponse.getEntity();
//			if (httpResponse == null) {
//				if (DLog.isNetLog) {
//					DLog.td(DLog.mNetTag, this, "请求结果为空");
//				}
//				throw new NullPointerException("response is null");
//			}
//
//			try {
//				mBaseHttpResponseModel.setHttpCode(httpResponse.getStatusLine().getStatusCode());
//			} catch (Throwable e) {
//				if (DLog.isNetLog) {
//					DLog.te(DLog.mNetTag, this, e);
//				}
//			}
//			try {
//				mBaseHttpResponseModel.setHttpReasonPhrase(httpResponse.getStatusLine().getReasonPhrase());
//			} catch (Throwable e) {
//				if (DLog.isNetLog) {
//					DLog.te(DLog.mNetTag, this, e);
//				}
//			}
//			try {
//				Header[] headers = httpResponse.getAllHeaders();
//				Map<String, List<String>> headerMap = new HashMap<String, List<String>>();
//				for (Header header : headers) {
//					if (header != null) {
//						String name = header.getName();
//						String value = header.getValue();
//						if (!headerMap.containsKey(name)) {
//							List<String> temp = new ArrayList<String>();
//							temp.add(value);
//							headerMap.put(name, temp);
//						} else {
//							headerMap.get(name).add(value);
//						}
//					}
//				}
//				mBaseHttpResponseModel.setHeaders(headerMap);
//			} catch (Throwable e) {
//				if (DLog.isNetLog) {
//					DLog.te(DLog.mNetTag, this, e);
//				}
//			}
//			try {
//				if (DLog.isNetLog) {
//					DLog.td(DLog.mNetTag, this, "从Entity中获取的contentLength：%d", entity.getContentLength());
//				}
//				mBaseHttpResponseModel.setContentLength(entity.getContentLength());
//			} catch (Throwable e) {
//				if (DLog.isNetLog) {
//					DLog.te(DLog.mNetTag, this, e);
//				}
//			}
//
//			// getContent方法只能调用一次,因为流获取完之后不会在有
//			if (httpResponse.getStatusLine().getStatusCode() >= 200 && httpResponse.getStatusLine().getStatusCode() < 300) {
//
//				inputStream = entity.getContent();
//
//				try {
//					// 获取contentEncoding来获取inputStream
//					Header header = entity.getContentEncoding();
//					if (header != null && header.getValue().toLowerCase(Locale.US).contains("gzip") && inputStream != null) {
//						inputStream = new GZIPInputStream(inputStream);
//						if (DLog.isNetLog) {
//							DLog.td(DLog.mNetTag, this, "%s : %s，初始化inputStrrem为GZIPInputStream", header.getName(),
//									header.getValue());
//						}
//					}
//				} catch (Throwable e) {
//					if (DLog.isNetLog) {
//						DLog.te(DLog.mNetTag, this, e);
//					}
//				}
//
//				if (inputStream == null) {
//					throw new NullPointerException("InputStream is null");
//				}
//
//				baos = new ByteArrayOutputStream();
//				byte[] buff = new byte[1024];
//				int len = 0;
//				// 如果当前请求有效就不断读取其中的内容
//				while ((len = inputStream.read(buff)) > 0) {
//					if (mIsRunning) {
//						baos.write(buff, 0, len);
//						//						if (Debug_SDK.isNetLog) {
//						//							DLog.ti(DLog.mNetTag, this, "已读取%d ....", len);
//						//						}
//						//						Thread.sleep(1000);
//					} else {
//						return;
//					}
//				}
//				baos.flush();
//
//				byte[] buffer = baos.toByteArray();
//				mBaseHttpResponseModel.setBodyLength(buffer.length);
//
//				String rspString = new String(buffer, mBaseHttpRequesterModel.getEncodingCharset());
//				mBaseHttpResponseModel.setResponseString(rspString);
//
//				if (DLog.isNetLog) {
//					DLog.ti(DLog.mNetTag, this, "返回字符串[长度:%d]:%s", buffer.length, rspString);
//				}
//
//			}
//		} catch (ConnectionPoolTimeoutException e) {
//			// 从ConnectionManager管理的连接池中取出连接超时
//			setException(HttpRequestExceptionCode.ConnectionPoolTimeoutException, e);
//
//		} catch (ConnectTimeoutException e) {
//			// 网络与服务器建立连接超时
//			setException(HttpRequestExceptionCode.ConnectTimeoutException, e);
//
//		} catch (SocketTimeoutException e) {
//			// 请求超时
//			setException(HttpRequestExceptionCode.SocketTimeoutException, e);
//
//		} catch (UnknownHostException e) {
//			// 网络没有打开或者没有配置网络权限的时候会1抛出这个异常
//			setException(HttpRequestExceptionCode.UnknownHostException, e);
//
//		} catch (HttpHostConnectException e) {
//			// 网络没有打开或者没有配置网络权限的时候会1抛出这个异常
//			setException(HttpRequestExceptionCode.HttpHostConnectException, e);
//
//		} catch (SocketException e) {
//			// 请求被关闭
//			setException(HttpRequestExceptionCode.SocketException, e);
//
//		} catch (Exception e) {
//			// 这里如果有其他的异常的话先统一标记为-99 ，这个-99会在异常上报中反馈
//			setException(HttpRequestExceptionCode.UnknownException, e);
//
//		} finally {
//			// 请求结束时回收HttpClient实例
//			try {
//				if (mHttpClient != null) {
//					mHttpClient.getConnectionManager().shutdown();
//				}
//			} catch (Throwable e) {
//				if (DLog.isNetLog) {
//					DLog.te(DLog.mNetTag, this, e);
//				}
//			}
//			mHttpClient = null; // 这句话必须要要有
//
//			// 清除请求
//			try {
//				if (mHttpRequestBase != null) {
//					mHttpRequestBase.abort();
//				}
//			} catch (Throwable e) {
//				if (DLog.isNetLog) {
//					DLog.te(DLog.mNetTag, this, e);
//				}
//			}
//			try {
//				if (baos != null) {
//					baos.close();
//				}
//			} catch (Throwable e) {
//				if (DLog.isNetLog) {
//					DLog.te(DLog.mNetTag, this, e);
//				}
//			}
//			try {
//				if (inputStream != null) {
//					inputStream.close();
//				}
//			} catch (Throwable e) {
//				if (DLog.isNetLog) {
//					DLog.te(DLog.mNetTag, this, e);
//				}
//			}
//
//		}
//	}
//}
