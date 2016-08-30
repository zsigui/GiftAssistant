package net.ouwan.umipay.android.entry;

import android.content.Context;
import android.text.TextUtils;

import net.ouwan.umipay.android.debug.Debug_Log;
import net.youmi.android.libs.common.basic.Basic_JSONUtil;
import net.youmi.android.libs.common.util.Util_System_Package;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * UmipayCommonAccount
 *
 * @author jimmy
 *         date 15-2-3
 *         description
 */
public class UmipayCommonAccount implements Serializable {


    private static final String KEY_UID = "a";
    private static final String KEY_USERNAME = "aa";
    private static final String KEY_SESSION = "ab";
    private static final String KEY_ORIGIN_APKNAME = "ac";
    private static final String KEY_DEST_PACKAGENAME = "p";
    private static final String KEY_ORIGIN_PACKAGENAME = "q";
    private static final String KEY_TIMESTAMP = "s";


    /**
     * @Fields mDestPackageName : 使用通用登录态的目标包名,也是通用登录态存储时的主键，一个目标包名唯一对应一个通用登录态
     */
    private String mDestPackageName;
    /**
     * @Fields mOriginPackageName : 保存该通用登录态的来源包名,用于判断通用登录态来源、是否用于切换（目标包名和来源包名不一致时用户切换）
     */
    private String mOriginPackageName;
    /**
     * @Fields mOriginApkName :保存该通用登录态的来源应用名称
     */
    private String mOriginApkName;

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
     * @Fields mTimestamp : utc秒数,1970至今秒数。用以游戏服验证登陆时效性。
     */
    private long mTimestamp_s;
    /**
     * 本地缓存key
     */
    private String mCacheKey;


    public UmipayCommonAccount(String json) {
        deserialize(json);
    }

    public UmipayCommonAccount(String destPackageName, String originPackageName, String apkName, long ts) {
        this.mDestPackageName = destPackageName;
        this.mOriginPackageName = originPackageName;
        this.mOriginApkName = apkName;
        this.mTimestamp_s = ts;
    }

    public UmipayCommonAccount(Context context, UmipayAccount account, long ts) {
        if (account == null) {
            return;
        }
        this.mDestPackageName = context.getPackageName();
        this.mOriginPackageName = context.getPackageName();
        this.mOriginApkName = Util_System_Package.getAppNameforCurrentContext(context);
        this.mUid = account.getUid();
        this.mSession = account.getSession();
        this.mUserName = account.getUserName();
        this.mCacheKey = account.getCacheKey();
        this.mTimestamp_s = ts;
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

    public String getDestPackageName() {
        return mDestPackageName;
    }

    public void setDestPackageName(String destPackageName) {
        mDestPackageName = destPackageName;
    }


    public String getOriginApkName() {
        return mOriginApkName;
    }

    public void setOriginApkName(String originApkName) {
        mOriginApkName = originApkName;
    }

    public String getOriginPackageName() {
        return mOriginPackageName;
    }

    public void setOriginPackageName(String originPackageName) {
        mOriginPackageName = originPackageName;
    }

    public long getTimestamp_s() {
        return mTimestamp_s;
    }

    public void setTimestamp_s(long timestamp_s) {
        mTimestamp_s = timestamp_s;
    }

    public String serialize() {
        JSONObject jo = new JSONObject();
        Basic_JSONUtil.put(jo, KEY_UID, mUid);
        Basic_JSONUtil.put(jo, KEY_USERNAME, mUserName);
        Basic_JSONUtil.put(jo, KEY_SESSION, mSession);
        Basic_JSONUtil.put(jo, KEY_ORIGIN_APKNAME, mOriginApkName);
        Basic_JSONUtil.put(jo, KEY_DEST_PACKAGENAME, mDestPackageName);
        Basic_JSONUtil.put(jo, KEY_ORIGIN_PACKAGENAME, mOriginPackageName);
        Basic_JSONUtil.put(jo, KEY_TIMESTAMP, mTimestamp_s);
        return jo.toString();
    }

    public boolean deserialize(String json) {
        try {
            JSONObject jo = Basic_JSONUtil.toJsonObject(json);
            if (jo != null) {
                mUid = Basic_JSONUtil.getInt(jo, KEY_UID, mUid);
                mUserName = Basic_JSONUtil.getString(jo, KEY_USERNAME, mUserName);
                mSession = Basic_JSONUtil.getString(jo, KEY_SESSION, mSession);
                mOriginApkName = Basic_JSONUtil.getString(jo, KEY_ORIGIN_APKNAME, mOriginApkName);
                mDestPackageName = Basic_JSONUtil.getString(jo, KEY_DEST_PACKAGENAME, mDestPackageName);
                mOriginPackageName = Basic_JSONUtil.getString(jo, KEY_ORIGIN_PACKAGENAME, mOriginPackageName);
                mTimestamp_s = Basic_JSONUtil.getLong(jo, KEY_TIMESTAMP, mTimestamp_s);
            }
            return true;
        } catch (Throwable e) {
            Debug_Log.e(e);
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o != null && o instanceof UmipayCommonAccount) {
            UmipayCommonAccount oA = (UmipayCommonAccount) o;
            return mUid == oA.mUid
                    && (!TextUtils.isEmpty(mSession) && mSession.equalsIgnoreCase(oA.mSession))
                    && (!TextUtils.isEmpty(mUserName) && mUserName.equalsIgnoreCase(oA.mUserName))
                    && (!TextUtils.isEmpty(mDestPackageName) && mDestPackageName.equalsIgnoreCase(oA.mDestPackageName))
                    && (!TextUtils.isEmpty(mOriginPackageName)
                    && mOriginPackageName.equalsIgnoreCase(oA.mOriginPackageName));
        } else
            return false;
    }
}
