package net.youmi.android.libs.common.debug;


/**
 * 测试用log输出，全局开关在父类
 * 
 * 支持两套方案：
 * <p>
 * 1、di,de,dd,dv,dw 等的使用： <br>
 * 和常规的Log使用差不多，输入标签，输入信息，输出Throwable就可以了
 * <p>
 * 2、ti,te,td,tv,tw 等的使用：<br>
 * 和常规的Log区别在于标签的创建：<br>
 * 需要传入preTag(模块名称)， 以及Object（模块中的某个类）来创建一个标签
 * 
 * <hr>
 * 其他注意注意事项：
 * <ol>
 * <li>比较多，后面在说吧</li>
 * </ol>
 * <hr>
 * 
 * @author zhitaocai
 * 
 */
public class Debug_SDK extends Debug_SDK_Log_Temp {

	public final static boolean isDownloadLog = false;
	public final static String mDownloadTag = "download_";

	public final static boolean isCacheLog = false;
	public final static String mCacheTag = "cache_";

	public final static boolean isNetLog = false;
	public final static String mNetTag = "net_";

	public final static boolean isGlobalLog = false;
	public final static String mGlobalTag = "global_";

	public final static boolean isCoderLog = false;
	public final static String mCoderTag = "coder_";

	public final static boolean isBasicLog = false;
	public final static String mBasicTag = "basic_";

	public final static boolean isUtilLog = false;
	public final static String mUtilTag = "util_";

	public final static boolean isUiLog = false;
	public final static String mUiTag = "ui_";

	public final static boolean isInitLog = false;
	public final static String mInitTag = "init_";

	public final static boolean isJsLog = false;
	public final static String mJsTag = "js_";

	public final static boolean isLocationLog = false;
	public final static String mLocationTag = "location_";

	public final static boolean isSessionLog = false;
	public final static String mSessionTag = "session_";

	public final static boolean isGwLog = false;
	public final static String mGwTag = "gw_";

	public final static boolean isCompatLog = false;
	public final static String mCompatTag = "compatibility_";

	public final static boolean isOnLineLog = false;
	public final static String mOnLineTag = "onlineconfig_";

	public final static boolean isWebViewLog = false;
	public final static String mWebViewTag = "webview_";

	public final static boolean isExpLog = false;
	public final static String mExpTag = "exp_";

	public final static boolean isEffLog = false;
	public final static String mEffTag = "eff_";

	public final static boolean isNtpLog = false;
	public final static String mNtpTag = "ntp_";
	
	public final static boolean isPnLog = false;
	public final static String mPnTag = "pn_";

	public final static boolean isPoolLog = false;
	public final static String mPoolTag = "pn_";


	// //////////////////////////////

	public final static boolean isAdsLog = false;
	public final static String mAdsTag = "as_";

	public final static boolean isOfferLog = false;
	public final static String mOfferTag = "of_";
	
	public final static boolean isDiyOfferLog = false;
	public final static String mDiyOfferTag = "dof_";
	
	public final static boolean isWxLog = false;
	public final static String mWxTag = "wx_";
	
}
