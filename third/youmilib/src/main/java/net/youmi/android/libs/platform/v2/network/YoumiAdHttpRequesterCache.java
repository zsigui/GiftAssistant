package net.youmi.android.libs.platform.v2.network;

import android.util.SparseArray;

import net.youmi.android.libs.common.basic.Basic_StringUtil;
import net.youmi.android.libs.common.v2.network.core.AbsHttpRequester;

/**
 * 请求缓存，每次请求都会写入到这里，请求结束后会从这里的缓存中移除
 *
 * @author zhitao
 * @since 2015-09-16 09:14
 */
public class YoumiAdHttpRequesterCache {

	public final static String SYNCHRONIZED = "sync";

	public static SparseArray<AbsHttpRequester> mSparseArray;

	/**
	 * 将请求加入到缓存中
	 *
	 * @param identify
	 * @param requester
	 */
	public static void addRequestToCache(String identify, AbsHttpRequester requester) {
		if (identify != null) {
			addRequestToCache(identify.hashCode(), requester);
		}
	}

	/**
	 * 将请求加入到缓存中
	 *
	 * @param identify
	 * @param requester
	 */
	private static void addRequestToCache(int identify, AbsHttpRequester requester) {
		if (mSparseArray == null) {
			mSparseArray = new SparseArray<AbsHttpRequester>();
		}

		synchronized (SYNCHRONIZED) {
			mSparseArray.put(identify, requester);
		}
	}

	/**
	 * 获取上一个请求
	 *
	 * @param identify
	 * @return
	 */
	public static AbsHttpRequester getLastRequest(String identify) {
		if (mSparseArray == null || mSparseArray.size() == 0) {
			return null;
		}
		if (Basic_StringUtil.isNullOrEmpty(identify)) {
			return null;
		}
		synchronized (SYNCHRONIZED) {
			return mSparseArray.get(identify.hashCode());
		}
	}

	public static void removeThisRequest(String identify) {
		if (mSparseArray == null || mSparseArray.size() == 0) {
			return;
		}
		if (Basic_StringUtil.isNullOrEmpty(identify)) {
			return;
		}
		synchronized (SYNCHRONIZED) {
			mSparseArray.remove(identify.hashCode());
		}
	}

	public static void removeLastRequest(String identify) {
		if (mSparseArray == null || mSparseArray.size() == 0) {
			return;
		}
		if (Basic_StringUtil.isNullOrEmpty(identify)) {
			return;
		}
		synchronized (SYNCHRONIZED) {
			AbsHttpRequester requester = mSparseArray.get(identify.hashCode());
			if (requester != null) {
				requester.abort();
			}
		}
	}
}
