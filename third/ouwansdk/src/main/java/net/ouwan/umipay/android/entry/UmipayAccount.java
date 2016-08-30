package net.ouwan.umipay.android.entry;

import net.ouwan.umipay.android.api.GameUserInfo;
import net.ouwan.umipay.android.debug.Debug_Log;
import net.youmi.android.libs.common.basic.Basic_JSONUtil;
import net.youmi.android.libs.common.cache.Interface_Serializable;
import net.youmi.android.libs.common.coder.Coder_Md5;

import org.json.JSONObject;

/**
 * UmipayAccount
 *
 * @author zacklpx
 *         date 15-2-3
 *         description
 */
public class UmipayAccount implements Interface_Serializable {
	public static final int TYPE_NORMAL = 0;
	public static final int TYPE_QQ = 1;
	public static final int TYPE_SINA = 2;
	public static final int TYPE_VISITOR = 3;
	public static final int TYPE_SLIENT = 4;
	public static final int TYPE_QUICKREGISTER = 5;
	public static final int TYPE_GIFT_COOL = 8;
	public static final int TYPE_MOBILE = 9;

	public static final int TYPE_REGIST = 10;
	public static final int TYPE_QUICK_REGIST = 11;

	private static final String CACHE_KEY_PREFIX_BEFORE_UID = "dfjkl7812";

	private static final String KEY_UID = "a";
	private static final String KEY_USERNAME = "aa";
	private static final String KEY_LAST_LOGIN_TIME = "o";
	private static final String KEY_PSW = "p";
	private static final String KEY_TOKEN = "q";
	private static final String KEY_OAUTH_TYPE = "r";
	private static final String KEY_OAUTH_ID = "s";
	private static final String KEY_OAUTH_EXPIRE = "ss";
	private static final String KEY_OAUTH_TOKEN = "y";
	private static final String KEY_REMENBER_PSW = "z";

	/**
	 * @Fields mUid : 用户id
	 */
	private int mUid;
	/**
	 * @Fields mSession : 登录态session
	 */
	private String mSession;
	/**
	 * @Fields mUserName : 用户登录账号名
	 */
	private String mUserName;
	/**
	 * @Fields mPsw : 账户密码
	 */
	private String mPsw;

	private String mToken;
	/**
	 * @Fields mRemenberPsw : 是否记住密码
	 */
	private boolean mRemenberPsw;
	/**
	 * @Fields mLastLoginTime_ms : 用户最后登陆时间
	 */
	private long mLastLoginTime_ms;
	/**
	 * @Fields mBindMobile : 绑定手机
	 */
	private int mBindMobile = 0;
	/**
	 * @Fields mBindOauth : 绑定偶玩账号
	 */
	private int mBindOauth = 0;
	/**
	 * 本地缓存key
	 */
	private String mCacheKey;

	/**
	 * @Fields mOauthType : 第三方登录类型
	 */
	private int mOauthType = 0;
	private int mOauthExpire;
	private String mOauthID;
	private String mOauthToken;


	private GameUserInfo mGameUserInfo;

	public UmipayAccount(String cachekey) {
		mCacheKey = cachekey;
	}

	public UmipayAccount(String username, String psw) {
		this.mUserName = username;
		this.mPsw = psw;
		this.mRemenberPsw = true;
	}

	public UmipayAccount(String oauthid, String oauthtoken, int type) {
		this.mOauthID = oauthid;
		this.mOauthToken = oauthtoken;
		this.mOauthType = type;
	}

	public UmipayAccount(UmipayAccount account) {
		if (account == null) {
			return;
		}
		this.mUid = account.getUid();
		this.mSession = account.getSession();
		this.mUserName = account.getUserName();
		this.mPsw = account.getPsw();
		this.mToken = account.getToken();
		this.mRemenberPsw = account.isRemenberPsw();
		this.mLastLoginTime_ms = account.getLastLoginTime_ms();
		this.mOauthID = account.getOauthID();
		this.mOauthType = account.getOauthType();
		this.mCacheKey = account.getCacheKey();
		this.mGameUserInfo = account.getGameUserInfo();
		this.mBindMobile = account.getBindMobile();
		this.mBindOauth = account.getBindOauth();
	}

	public int getUid() {
		return mUid;
	}

	public void setUid(int uid) {
		mUid = uid;
	}

	public String getSession() {
		return mSession;
	}

	public void setSession(String session) {
		mSession = session;
	}

	public String getUserName() {
		return mUserName;
	}

	public void setUserName(String userName) {
		mUserName = userName;
	}

	public String getPsw() {
		return mPsw;
	}

	public void setPsw(String psw) {
		mPsw = psw;
	}

	public String getToken() {
		return mToken;
	}

	public void setToken(String token) {
		mToken = token;
	}

	public boolean isRemenberPsw() {
		return mRemenberPsw;
	}

	public void setRemenberPsw(boolean remenberPsw) {
		mRemenberPsw = remenberPsw;
	}

	public long getLastLoginTime_ms() {
		return mLastLoginTime_ms;
	}

	public void setLastLoginTime_ms(long lastLoginTime_ms) {
		mLastLoginTime_ms = lastLoginTime_ms;
	}

	public int getOauthType() {
		return mOauthType;
	}

	public void setOauthType(int oauthType) {
		mOauthType = oauthType;
	}

	public String getOauthID() {
		return mOauthID;
	}

	public void setOauthID(String oauthID) {
		mOauthID = oauthID;
	}

	public int getOauthExpire() {
		return mOauthExpire;
	}

	public void setOauthExpire(int oauthExpire) {
		mOauthExpire = oauthExpire;
	}

	public String getOauthToken() {
		return mOauthToken;
	}

	public void setOauthToken(String oauthToken) {
		mOauthToken = oauthToken;
	}

	public GameUserInfo getGameUserInfo() {
		return mGameUserInfo;
	}

	public void setGameUserInfo(GameUserInfo gameUserInfo) {
		mGameUserInfo = gameUserInfo;
	}

	public int getBindMobile() {
		return mBindMobile;
	}

	public void setBindMobile(int bindMobile) {
		mBindMobile = bindMobile;
	}

	public int getBindOauth() {
		return mBindOauth;
	}

	public void setBindOauth(int bindOauth) {
		mBindOauth = bindOauth;
	}
	@Override
	public String serialize() {
		JSONObject jo = new JSONObject();
		Basic_JSONUtil.put(jo, KEY_UID, mUid);
		Basic_JSONUtil.put(jo, KEY_USERNAME, mUserName);
		Basic_JSONUtil.put(jo, KEY_PSW, mPsw);
		Basic_JSONUtil.put(jo, KEY_TOKEN, mToken);
		Basic_JSONUtil.put(jo, KEY_REMENBER_PSW, mRemenberPsw);
		Basic_JSONUtil.put(jo, KEY_LAST_LOGIN_TIME, mLastLoginTime_ms);
		Basic_JSONUtil.put(jo, KEY_OAUTH_ID, mOauthID);
		Basic_JSONUtil.put(jo, KEY_OAUTH_TYPE, mOauthType);
		Basic_JSONUtil.put(jo, KEY_OAUTH_EXPIRE, mOauthExpire);
		Basic_JSONUtil.put(jo, KEY_OAUTH_TOKEN, mOauthToken);
		return jo.toString();
	}

	@Override
	public boolean deserialize(String json) {
		try {
			JSONObject jo = Basic_JSONUtil.toJsonObject(json);
			if (jo != null) {
				mUid = Basic_JSONUtil.getInt(jo, KEY_UID, mUid);
				mUserName = Basic_JSONUtil.getString(jo, KEY_USERNAME, mUserName);
				mPsw = Basic_JSONUtil.getString(jo, KEY_PSW, mPsw);
				mToken = Basic_JSONUtil.getString(jo, KEY_TOKEN, mToken);
				mRemenberPsw = Basic_JSONUtil.getBoolean(jo, KEY_REMENBER_PSW, mRemenberPsw);
				mLastLoginTime_ms = Basic_JSONUtil.getLong(jo, KEY_LAST_LOGIN_TIME, mLastLoginTime_ms);
				mOauthID = Basic_JSONUtil.getString(jo, KEY_OAUTH_ID, mOauthID);
				mOauthType = Basic_JSONUtil.getInt(jo, KEY_OAUTH_TYPE, mOauthType);
				mOauthExpire = Basic_JSONUtil.getInt(jo, KEY_OAUTH_EXPIRE, mOauthExpire);
				mOauthToken = Basic_JSONUtil.getString(jo, KEY_OAUTH_TOKEN, mOauthToken);
			}
			return true;
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
			if (mOauthType == 1 || mOauthType == 2) {
				mCacheKey = Coder_Md5.md5(String.format("%s_%s",
						CACHE_KEY_PREFIX_BEFORE_UID, mOauthID));
			} else {
				mCacheKey = Coder_Md5.md5(String.format("%s_%s",
						CACHE_KEY_PREFIX_BEFORE_UID, mUserName));
			}
		}
		return mCacheKey;
	}
}
