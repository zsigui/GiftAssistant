package com.oplay.giftcool.download.silent.bean;

import java.io.InputStream;
import java.util.Map;

/**
 * 执行网络请求后响应类
 * <p></p>
 * Created by zsigui on 15-9-2.
 */
public class NetworkResponse {

	private InputStream mContent;
	private int mResponseCode;
	private String mContentType;
	private String mContentEncoding;
	private int mContentLength;
	private long mLastModified;
	private long mDate;
	private long mIfModifiedSince;
	private Map<String, String> mHeaders;

	public InputStream getContent() {
		return mContent;
	}

	public void setContent(InputStream content) {
		mContent = content;
	}

	public int getResponseCode() {
		return mResponseCode;
	}

	public void setResponseCode(int responseCode) {
		mResponseCode = responseCode;
	}

	public String getContentType() {
		return mContentType;
	}

	public void setContentType(String contentType) {
		mContentType = contentType;
	}

	public String getContentEncoding() {
		return mContentEncoding;
	}

	public void setContentEncoding(String contentEncoding) {
		mContentEncoding = contentEncoding;
	}

	public int getContentLength() {
		return mContentLength;
	}

	public void setContentLength(int contentLength) {
		mContentLength = contentLength;
	}

	public long getLastModified() {
		return mLastModified;
	}

	public void setLastModified(long lastModified) {
		mLastModified = lastModified;
	}

	public long getDate() {
		return mDate;
	}

	public void setDate(long date) {
		mDate = date;
	}

	public long getIfModifiedSince() {
		return mIfModifiedSince;
	}

	public void setIfModifiedSince(long ifModifiedSince) {
		mIfModifiedSince = ifModifiedSince;
	}

	public Map<String, String> getHeaders() {
		return mHeaders;
	}

	public void setHeaders(Map<String, String> headers) {
		mHeaders = headers;
	}
}
