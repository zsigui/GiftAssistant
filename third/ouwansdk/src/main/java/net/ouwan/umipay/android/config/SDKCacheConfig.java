package net.ouwan.umipay.android.config;

import android.content.Context;

import net.ouwan.umipay.android.debug.Debug_Log;
import net.youmi.android.libs.common.basic.Basic_JSONUtil;
import net.youmi.android.libs.common.cache.Interface_Serializable;
import net.youmi.android.libs.common.cache.Proxy_DB_Cache_Helper;
import net.youmi.android.libs.common.cache.Proxy_Serializable_CacheManager;

import org.json.JSONObject;

/**
 * SDKCacheConfig
 *
 * @author zacklpx
 *         date 15-3-3
 *         description  应用程序的本地配置
 */
public class SDKCacheConfig implements Interface_Serializable {
	private static final String FILE_NAME_APP_PREFERENCE = "elI9vvm7vdsfRik";
	private static final String PSW_DB = "xadb41x1";
	private static final int VERSION_CODE = 1;
	private static SDKCacheConfig mInstance;
	private Proxy_Serializable_CacheManager mCacheManager;
	/**
	 * @Fields mAutoLogin : 是否允许自动登陆
	 */
	private boolean mAutoLogin = true;
	/**
	 * @Fields mRemenberPsw : 是否允许记住密码
	 */
	private boolean mRemenberPsw = true;
	/**
	 * @Fields mEnableQuickReg : 是否允许一键注册
	 */
	private boolean mEnableQuickReg = false;
	/**
	 * @Fields mEnableViewPsw : 是否显示密码
	 */
	private boolean mEnableViewPsw = false;
	/**
	 * @Fields mEnableVisitorMode : 是否支持游客试玩模式
	 */
	private boolean mEnableVisitorMode = true;
	/**
	 * @Fields mEnableHttps : 是否https*
	 */
	private boolean mEnableHttps = false;
	/**
	 * @Fields mEnableOtherLogin : 是否显示其他登录模式
	 */
	private boolean mEnableOtherLogin = false;//-1表示未有值，此时由服务器传回参数作默认值,0表示隐藏,1表示显示
	/**
	 * @Fields mEnableFloatMemu : 是否显示浮动菜单
	 */
	private boolean mEnableFloatMemu = true;
	/**
	 * 小红点是否显示礼包功能
	 */
	private boolean mIsShowGift = true;
	/**
	 * 小红点是否显示消息功能
	 */
	private boolean mIsShowMsg = true;
	/**
	 * 小红点是否显示社区功能
	 */
	private boolean mIsShowBbs = true;
	/**
	 * 小红点是否显示帮助功能
	 */
	private boolean mIsShowHelp = true;
	/**
	 * 小红点是否显示账户功能
	 */
	private boolean mIsShowAccount = true;

	/**
	 * @Fields mIsShowDialog :是否显示社区功能上的小红点
	 */
	private boolean mEnableBbsRedPoint = true;
	/**
	 * @Fields mIsShowDialog :是否显示小红点隐藏提示对话框
	 */
	private boolean mEnableFloatMemuDialog = true;
	/**
	 * @Fields mIsInitEpaySDK :是否已经初始化过宜支付SDK
	 */
	private boolean mIsInitEpaySDK = false;
	/**
	 * @Fields mEnableErrorReport :是否允许错误上报
	 */
	private boolean mEnableErrorReport = false;
	/**
	 * @Fields mLastInitEpaySDKIMSI :上一次初始化宜支付SDK时的imei
	 */
	private String mLastInitEpaySDKIMSI;

	private String mEpayIdentify;

	private int mRedpointTime;
	/**
	 * @Fields mOuwanPackageName :偶玩客户端包名
	 */
	private String mOuwanPackageName;
	/**
	 * @Fields mOuwanCommunityUrl :启动偶玩客户端并跳转到社区页面的url
	 */
	private String mOuwanCommunityUrl;
	/**
	 * @Fields mOuwanDownloadUrl :偶玩客户端下载地址
	 */
	private String mOuwanDownloadUrl;
	/**
	 * @Fields mExitDialogDownloadBtnText:退屏退出到偶玩客户端下载页面按钮文字
	 */
	private String mExitDialogDownloadBtnText;
	/**
	 * @Fields mExitDialogCommunityBtnText:退屏普通退出到偶玩客户端社区按钮文字
	 */
	private String mExitDialogCommunityBtnText;

	/**
	 * @Fields mEnableExitToCommunity:是否允许退屏显示跳转偶玩社区按钮
	 */
	private boolean mEnableExitToCommunity = false;

	/**
	 * @Fields mIsLightSDK:是否轻sdk
	 */
	private boolean mIsLightSDK = false;

	/**
	 * @Fields mIsMobileLogin:是否默认手机登录
	 */
	private boolean mIsMobileLogin = true;

	private boolean mHasLocalCache = false;
	/**
	 *  @Fields mHasAnnouncement:是否有公告
	 */
	private boolean mShowBoard = false;

	private SDKCacheConfig(Context context) {
		read(context);
	}

	/**
	 * @return 返回 mInstance 的值
	 */
	public synchronized static SDKCacheConfig getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new SDKCacheConfig(context);
		}
		return mInstance;
	}

	private SDKCacheConfig read(Context context) {
		try {
			if (mCacheManager == null) {
				mCacheManager = new Proxy_Serializable_CacheManager(context, PSW_DB, new Proxy_DB_Cache_Helper(context,
						FILE_NAME_APP_PREFERENCE, VERSION_CODE));
			}
			mHasLocalCache = mCacheManager.getCache(this);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return this;
	}


	@Override
	public String serialize() {
		JSONObject object = new JSONObject();
		Basic_JSONUtil.put(object, "mAutoLogin", mAutoLogin);
		Basic_JSONUtil.put(object, "mRemenberPsw", mRemenberPsw);
		Basic_JSONUtil.put(object, "mEnableQuickReg", mEnableQuickReg);
		Basic_JSONUtil.put(object, "mEnableViewPsw", mEnableViewPsw);
		Basic_JSONUtil.put(object, "mEnableVisitorMode", mEnableVisitorMode);
		Basic_JSONUtil.put(object, "mEnableHttps", mEnableHttps);
		Basic_JSONUtil.put(object, "mEnableFloatMemu", mEnableFloatMemu);
		Basic_JSONUtil.put(object, "mEnableOtherLogin", mEnableOtherLogin);
		Basic_JSONUtil.put(object, "mIsShowAccount", mIsShowAccount);
		Basic_JSONUtil.put(object, "mIsShowGift", mIsShowGift);
		Basic_JSONUtil.put(object, "mIsShowMsg", mIsShowMsg);
		Basic_JSONUtil.put(object, "mIsShowBbs", mIsShowBbs);
		Basic_JSONUtil.put(object, "mIsShowHelp", mIsShowHelp);
		Basic_JSONUtil.put(object, "mEnableBbsRedPoint", mEnableBbsRedPoint);
		Basic_JSONUtil.put(object, "mEnableFloatMemuDialog", mEnableFloatMemuDialog);
		Basic_JSONUtil.put(object, "mIsInitEpaySDK", mIsInitEpaySDK);
		Basic_JSONUtil.put(object, "mEnableErrorReport", mEnableErrorReport);
		Basic_JSONUtil.put(object, "mLastInitEpaySDKIMSI", mLastInitEpaySDKIMSI);
		Basic_JSONUtil.put(object, "mEpayIdentify", mEpayIdentify);
		Basic_JSONUtil.put(object, "mRedpointTime", mRedpointTime);
		Basic_JSONUtil.put(object, "mOuwanPackageName", mOuwanPackageName);
		Basic_JSONUtil.put(object, "mOuwanCommunityUrl", mOuwanCommunityUrl);
		Basic_JSONUtil.put(object, "mOuwanDownloadUrl", mOuwanDownloadUrl);
		Basic_JSONUtil.put(object, "mExitDialogDownloadBtnText", mExitDialogDownloadBtnText);
		Basic_JSONUtil.put(object, "mExitDialogCommunityBtnText", mExitDialogCommunityBtnText);
		Basic_JSONUtil.put(object, "mEnableExitToCommunity", mEnableExitToCommunity);
		Basic_JSONUtil.put(object, "mIsLightSDK", mIsLightSDK);
		Basic_JSONUtil.put(object, "mIsMobileLogin", mIsMobileLogin);
		Basic_JSONUtil.put(object, "mShowBoard", mShowBoard);
		return object.toString();
	}

	@Override
	public boolean deserialize(String json) {
		try {
			JSONObject object = new JSONObject(json);
			mAutoLogin = Basic_JSONUtil.getBoolean(object, "mAutoLogin", true);
			mRemenberPsw = Basic_JSONUtil.getBoolean(object, "mRemenberPsw",
					true);
			mEnableQuickReg = Basic_JSONUtil.getBoolean(object,
					"mEnableQuickReg", false);
			mEnableViewPsw = Basic_JSONUtil.getBoolean(object,
					"mEnableViewPsw", false);
			mEnableVisitorMode = Basic_JSONUtil.getBoolean(object,
					"mEnableVisitorMode", true);
			mEnableHttps = Basic_JSONUtil.getBoolean(object, "mEnableHttps",
					false);
			mEnableFloatMemu = Basic_JSONUtil.getBoolean(object,
					"mEnableFloatMemu", true);
			mEnableOtherLogin = Basic_JSONUtil.getBoolean(object,
					"mEnableOtherLogin", false);
			mIsShowGift = Basic_JSONUtil.getBoolean(object,
					"mIsShowGift", true);
			mIsShowMsg = Basic_JSONUtil.getBoolean(object,
					"mIsShowMsg", true);
			mIsShowBbs = Basic_JSONUtil.getBoolean(object,
					"mIsShowBbs", true);
			mIsShowHelp = Basic_JSONUtil.getBoolean(object,
					"mIsShowHelp", true);
			mIsShowAccount = Basic_JSONUtil.getBoolean(object,
					"mIsShowAccount", true);
			mEnableBbsRedPoint = Basic_JSONUtil.getBoolean(object,
					"mEnableBbsRedPoint", true);
			mEnableFloatMemuDialog = Basic_JSONUtil.getBoolean(object,
					"mEnableFloatMemuDialog", true);
			mIsInitEpaySDK = Basic_JSONUtil.getBoolean(object, "mIsInitEpaySDK", false);
			mEnableErrorReport = Basic_JSONUtil.getBoolean(object, "mEnableErrorReport", false);
			mLastInitEpaySDKIMSI = Basic_JSONUtil.getString(object, "mLastInitEpaySDKIMSI", null);
			mEpayIdentify = Basic_JSONUtil.getString(object, "mEpayIdentify", null);
			mRedpointTime = Basic_JSONUtil.getInt(object, "mRedpointTime", 0);
			mOuwanPackageName = Basic_JSONUtil.getString(object, "mOuwanPackageName", null);
			mOuwanCommunityUrl = Basic_JSONUtil.getString(object, "mOuwanCommunityUrl", null);
			mOuwanDownloadUrl = Basic_JSONUtil.getString(object, "mOuwanDownloadUrl", null);
			mExitDialogDownloadBtnText = Basic_JSONUtil.getString(object, "mExitDialogDownloadBtnText", null);
			mExitDialogCommunityBtnText = Basic_JSONUtil.getString(object, "mExitDialogCommunityBtnText", null);
			mEnableExitToCommunity = Basic_JSONUtil.getBoolean(object, "mEnableExitToCommunity", false);
			mIsLightSDK = Basic_JSONUtil.getBoolean(object, "mIsLightSDK", false);
			mIsMobileLogin = Basic_JSONUtil.getBoolean(object, "mIsMobileLogin", true);
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
		return mCacheManager.generateMd5CacheKey(FILE_NAME_APP_PREFERENCE);
	}

	public boolean save() {
		try {
			mCacheManager.saveCache(this);
			mHasLocalCache = true;
			return true;
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return false;
	}

	public boolean isAutoLogin() {
		return mAutoLogin;
	}

	public void setAutoLogin(boolean autoLogin) {
		mAutoLogin = autoLogin;
	}

	public boolean isRemenberPsw() {
		return mRemenberPsw;
	}

	public void setRemenberPsw(boolean remenberPsw) {
		mRemenberPsw = remenberPsw;
	}

	public boolean isEnableQuickReg() {
		return mEnableQuickReg;
	}

	public void setEnableQuickReg(boolean enableQuickReg) {
		mEnableQuickReg = enableQuickReg;
	}

	public boolean isEnableViewPsw() {
		return mEnableViewPsw;
	}

	public void setEnableViewPsw(boolean enableViewPsw) {
		mEnableViewPsw = enableViewPsw;
	}

	public boolean isEnableVisitorMode() {
		return mEnableVisitorMode;
	}

	public void setEnableVisitorMode(boolean enableVisitorMode) {
		mEnableVisitorMode = enableVisitorMode;
	}

	public boolean isEnableHttps() {
		return mEnableHttps;
	}

	public void setEnableHttps(boolean enableHttps) {
		mEnableHttps = enableHttps;
	}

	public boolean isEnableOtherLogin() {
		return mEnableOtherLogin;
	}

	public void setEnableOtherLogin(boolean enableOtherLogin) {
		mEnableOtherLogin = enableOtherLogin;
	}

	public boolean isEnableFloatMemu() {
		return mEnableFloatMemu;
	}

	public void setEnableFloatMemu(boolean enableFloatMemu) {
		mEnableFloatMemu = enableFloatMemu;
	}

	public boolean isShowGift() {
		return mIsShowGift;
	}

	public void setShowGift(boolean isShowGift) {
		mIsShowGift = isShowGift;
	}

	public boolean isShowMsg() {
		return mIsShowMsg;
	}

	public void setShowMsg(boolean isShowMsg) {
		mIsShowMsg = isShowMsg;
	}

	public boolean isShowBbs() {
		return mIsShowBbs;
	}

	public void setShowBbs(boolean isShowBbs) {
		mIsShowBbs = isShowBbs;
	}

	public boolean isShowHelp() {
		return mIsShowHelp;
	}

	public void setShowHelp(boolean isShowHelp) {
		mIsShowHelp = isShowHelp;
	}

	public boolean isShowAccount() {
		return mIsShowAccount;
	}

	public void setShowAccount(boolean isShowAccount) {
		mIsShowAccount = isShowAccount;
	}

	public boolean isEnableBbsRedPoint() {
		return mEnableBbsRedPoint;
	}

	public void setEnableBbsRedPoint(boolean EnableBbsRedPoint) {
		mEnableBbsRedPoint = EnableBbsRedPoint;
	}
	public boolean isEnableFloatMemuDialog() {
		return mEnableFloatMemuDialog;
	}

	public void setEnableFloatMemuDialog(boolean enableFloatMemuDialog) {
		mEnableFloatMemuDialog = enableFloatMemuDialog;
	}

	public boolean isInitEpaySDK() {
		return mIsInitEpaySDK;
	}

	public void setInitEpaySDK(boolean isInitEpaySDK) {
		mIsInitEpaySDK = isInitEpaySDK;
	}

	public boolean isEnableErrorReport() {
		return mEnableErrorReport;
	}

	public void setEnableErrorReport(boolean enableErrorReport) {
		mEnableErrorReport = enableErrorReport;
	}

	public String getLastInitEpaySDKIMSI() {
		return mLastInitEpaySDKIMSI;
	}

	public void setLastInitEpaySDKIMSI(String lastInitEpaySDKIMSI) {
		mLastInitEpaySDKIMSI = lastInitEpaySDKIMSI;
	}

	public String getEpayIdentify() {
		return mEpayIdentify;
	}

	public void setEpayIdentify(String epayIdentify) {
		mEpayIdentify = epayIdentify;
	}

	public int getRedpointTime() {
		return mRedpointTime;
	}

	public void setRedpointTime(int redpointTime) {
		mRedpointTime = redpointTime;
	}

	public String getOuwanPackageName() {
		return mOuwanPackageName;
	}

	public void setOuwanPackageName(String ouwanPackageName) {
		mOuwanPackageName = ouwanPackageName;
	}

	public String getOuwanCommunityUrl() {
		return mOuwanCommunityUrl;
	}

	public void setOuwanCommunityUrl(String ouwanCommunityUrl) {
		mOuwanCommunityUrl = ouwanCommunityUrl;
	}

	public String getOuwanDownloadUrl() {
		return mOuwanDownloadUrl;
	}

	public void setOuwanDownloadUrl(String ouwanDownloadUrl) {
		mOuwanDownloadUrl = ouwanDownloadUrl;
	}

	public String getExitDialogDownloadBtnText() {
		return mExitDialogDownloadBtnText;
	}

	public void setExitDialogDownloadBtnText(String exitDialogDownloadBtnText) {
		mExitDialogDownloadBtnText = exitDialogDownloadBtnText;
	}

	public String getExitDialogCommunityBtnText() {
		return mExitDialogCommunityBtnText;
	}

	public void setExitDialogCommunityBtnText(String exitDialogCommunityBtnText) {
		mExitDialogCommunityBtnText = exitDialogCommunityBtnText;
	}

	public boolean isEnableExitToCommunity(){
		return mEnableExitToCommunity;
	}

	public void setEnableExitToCommunity(boolean isEnableExitWithJump){
		mEnableExitToCommunity = isEnableExitWithJump;
	}

	public boolean isLightSDK(){
		return mIsLightSDK;
	}
	public void setIsLightSDK(boolean isLightSDK){
		mIsLightSDK = isLightSDK;
	}

	public boolean isMobileLogin(){
		return mIsMobileLogin;
	}
	public void setMobileLogin(boolean isMobileLogin){
		mIsMobileLogin = isMobileLogin;
	}

	public boolean isShowBoard(){
		return mShowBoard;
	}
	public void setShowBoard(boolean showBoard){
		mShowBoard = showBoard;
	}

	public boolean isHasLocalCache() {
		return mHasLocalCache;
	}
}
