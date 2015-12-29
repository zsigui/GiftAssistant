package net.youmi.android.libs.common.cache;

import android.content.Context;

import net.youmi.android.libs.common.debug.Debug_SDK;

/**
 * 缓存代理工具类 这些数据库的名字先不要改动
 *
 * @author 林秋明 created on 2012-7-4
 * @author zhitaocai edit on 2014-6-27
 */
public class Proxy_DB_Cache_Helper extends Base_DB_Cache_Helper {

	public Proxy_DB_Cache_Helper(Context context, String dbName, int dbVersion, String tbName) {
		super(context, dbName, dbVersion, tbName);
	}

	public Proxy_DB_Cache_Helper(Context context, String dbName, int dbVersion) {
		super(context, dbName, dbVersion);
	}

	// -------------------------------------------------
	// 公共缓存数据库
	static final String DB_NAME_COMMON = "jq".trim() + "Iq".trim() + "JY".trim() + "OT".trim() + "3J".trim() + "pT";

	private static final int DB_VERSION_COMMON = 2;

	private static Proxy_DB_Cache_Helper mInstance_Common;

	public synchronized static Proxy_DB_Cache_Helper getCommonDBInstance(Context context) {
		try {
			if (mInstance_Common == null) {
				mInstance_Common = new Proxy_DB_Cache_Helper(context, DB_NAME_COMMON, DB_VERSION_COMMON);
			}
		} catch (Throwable e) {
			if (Debug_SDK.isCacheLog) {
				Debug_SDK.te(Debug_SDK.mCacheTag, Proxy_DB_Cache_Helper.class, e);
			}
		}
		return mInstance_Common;
	}

}
