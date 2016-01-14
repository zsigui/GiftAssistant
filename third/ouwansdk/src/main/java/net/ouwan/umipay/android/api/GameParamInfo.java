package net.ouwan.umipay.android.api;

import android.content.Context;

import net.ouwan.umipay.android.debug.Debug_Log;
import net.youmi.android.libs.common.basic.Basic_JSONUtil;
import net.youmi.android.libs.common.cache.Interface_Serializable;
import net.youmi.android.libs.common.cache.Proxy_DB_Cache_Helper;
import net.youmi.android.libs.common.cache.Proxy_Serializable_CacheManager;
import net.youmi.android.libs.common.coder.Coder_Md5;

import org.json.JSONObject;

/**
 * GameParamInfo
 *
 * @author zacklpx
 *         date 15-1-28
 *         description
 */
public class GameParamInfo implements Interface_Serializable {
	private static final String CACHE_KEY_GAMEPREF = "dfddf7812";
	private final static String GAMEPREF_DB_NAME = "NMrY528p";
	private final static String GAMEPREF_DB_PSW = "5cSdssCy";
	private final static int GAMEPREF_DB_VERSION = 1;

	private static final String KEY_APPID = "a";
	private static final String KEY_APPSEC = "b";
	private static final String KEY_CHANNELID = "f";
	private static final String KEY_CHILDCHANNELID = "g";
	private static final String KEY_TESTMODE = "h";
	private static final String KEY_SINGLEPAY = "i";
	private static final String KEY_MINFEE = "k";
	//没用的去掉了 v3.50
//	private static final String KEY_CPID = "c";
//	private static final String KEY_SVRID = "d";
//	private static final String KEY_AREADID = "e";
//	private static final String KEY_SDKCALLBACK = "j";

	private String mCacheKey;
	private Proxy_Serializable_CacheManager mSerializableManager;

	private static GameParamInfo mInstance;

	/**
	 * @Fields mGameId : 游戏appid
	 */
	private String mAppId;

	/**
	 * @Fields mAppSecret : 游戏app secret
	 */
	private String mAppSecret;

	/**
	 * @Fields mChannalId : 渠道id
	 */
	private String mChannelId;

	/**
	 * @Fields mSubChannel :
	 */
	private String mSubChannelId;

	/**
	 * @Fields mTestMode : 测试模式
	 */
	private boolean mTestMode;

	/**
	 * false:支付完成会允许继续充值；
	 * true： 支付完成后关闭支付界面,不能继续充值（）
	 */
	private boolean mSinglePayMode;

	/**
	 * 充值时选择的最小金额,低于最小金额的无法支付
	 */
	private int mMinFee = 0;

	public GameParamInfo() {
	}

	GameParamInfo(Context context) {
		Proxy_DB_Cache_Helper dbhelp = new Proxy_DB_Cache_Helper(context, GAMEPREF_DB_NAME, GAMEPREF_DB_VERSION);
		mSerializableManager = new Proxy_Serializable_CacheManager(context, GAMEPREF_DB_PSW, dbhelp);
	}

	public static GameParamInfo getInstance(Context context) {
		if (mInstance == null) {
			mInstance = read(context);
		}
		return mInstance;
	}

	private static GameParamInfo read(Context context) {
		GameParamInfo res = new GameParamInfo(context);
		try {
			res.mSerializableManager.getCache(res);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return res;
	}

	public static void copy(Context appContext, GameParamInfo gameparams) {
		if (gameparams == null) {
			return;
		}
		if (mInstance == null) {
			mInstance = new GameParamInfo(appContext);
		}
		String json = gameparams.serialize();
		mInstance.deserialize(json);
	}

	public String getAppId() {
		return mAppId;
	}

	public void setAppId(String appId) {
		mAppId = appId;
	}

	public String getAppSecret() {
		return mAppSecret;
	}

	public void setAppSecret(String appSecret) {
		mAppSecret = appSecret;
	}

	public String getChannelId() {
		return mChannelId;
	}

	@Deprecated
	public void setChannelId(String channelId) {
		mChannelId = channelId;
	}

	public String getSubChannelId() {
		return mSubChannelId;
	}

	@Deprecated
	public void setSubChannelId(String subChannelId) {
		mSubChannelId = subChannelId;
	}

	public boolean isTestMode() {
		return mTestMode;
	}

	public void setTestMode(boolean testMode) {
		mTestMode = testMode;
	}

	public boolean isSinglePayMode() {
		return mSinglePayMode;
	}

	public void setSinglePayMode(boolean singlePayMode) {
		mSinglePayMode = singlePayMode;
	}

	public int getMinFee() {
		return mMinFee;
	}

	public void setMinFee(int minFee) {
		mMinFee = minFee;
	}

	@Override
	public String serialize() {
		try {
			JSONObject jO = new JSONObject();
			Basic_JSONUtil.putString(jO, KEY_APPID, mAppId);
			Basic_JSONUtil.putString(jO, KEY_APPSEC, mAppSecret);
			Basic_JSONUtil.putString(jO, KEY_CHANNELID, mChannelId);
			Basic_JSONUtil.putString(jO, KEY_CHILDCHANNELID, mSubChannelId);
			Basic_JSONUtil.putBoolean(jO, KEY_TESTMODE, mTestMode);
			Basic_JSONUtil.putBoolean(jO, KEY_SINGLEPAY, mSinglePayMode);
			Basic_JSONUtil.putInt(jO, KEY_MINFEE, mMinFee);
			return jO.toString();
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return null;
	}

	@Override
	public boolean deserialize(String json) {
		try {
			JSONObject jo = Basic_JSONUtil.toJsonObject(json);
			if (jo != null) {
				mAppId = Basic_JSONUtil.getString(jo, KEY_APPID, mAppId);
				mAppSecret = Basic_JSONUtil.getString(jo, KEY_APPSEC, mAppSecret);
				mChannelId = Basic_JSONUtil.getString(jo, KEY_CHANNELID, mChannelId);
				mSubChannelId = Basic_JSONUtil.getString(jo, KEY_CHILDCHANNELID, mSubChannelId);
				mTestMode = Basic_JSONUtil.getBoolean(jo, KEY_TESTMODE, mTestMode);
				mSinglePayMode = Basic_JSONUtil.getBoolean(jo, KEY_SINGLEPAY, mSinglePayMode);
				mMinFee = Basic_JSONUtil.getInt(jo, KEY_MINFEE, mMinFee);
				return true;
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return false;
	}

	@Override
	public long getValidCacheTime_ms() {
		return -1;
	}

	@Override
	public String getCacheKey() {
		if (mCacheKey == null) {
			mCacheKey = Coder_Md5.md5(CACHE_KEY_GAMEPREF);
		}
		return mCacheKey;
	}

	public void save() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					mSerializableManager.saveCache(mInstance);
				} catch (Throwable e) {
					Debug_Log.e(e);
				}
			}
		}).start();
	}
}
