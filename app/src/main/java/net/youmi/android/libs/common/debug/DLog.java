package net.youmi.android.libs.common.debug;

/**
 * 测试用log输出，全局开关在父类
 * <p/>
 * 支持两套方案：
 * <p/>
 * 1、di,de,dd,dv,dw 等的使用： <br>
 * 和常规的Log使用差不多，输入标签，输入信息，输出Throwable就可以了
 * <p/>
 * 2、ti,te,td,tv,tw 等的使用：<br>
 * 和常规的Log区别在于标签的创建：<br>
 * 需要传入preTag(模块名称)， 以及Object（模块中的某个类）来创建一个标签
 * <p/>
 * <hr>
 * 其他注意注意事项：
 * <ol>
 * <li>比较多，后面在说吧</li>
 * </ol>
 * <hr>
 *
 * @author zhitaocai
 */
public class DLog extends DebugSdkLog {

	public final static boolean isDownloadLog = true;

	public final static String mDownloadTag = "download_";

	public final static boolean isCacheLog = true;

	public final static String mCacheTag = "cache_";

	public final static boolean isNetLog = true;

	public final static String mNetTag = "net_";

	public final static boolean isGlobalLog = true;

	public final static String mGlobalTag = "global_";

	public final static boolean isCoderLog = true;

	public final static String mCoderTag = "coder_";

	public final static boolean isBasicLog = true;

	public final static String mBasicTag = "basic_";

	public final static boolean isUtilLog = true;

	public final static String mUtilTag = "util_";

	public final static boolean isUiLog = true;

	public final static String mUiTag = "ui_";

	public final static boolean isInitLog = true;

	public final static String mInitTag = "init_";

	public final static boolean isJsLog = true;

	public final static String mJsTag = "js_";

	public final static boolean isLocationLog = true;

	public final static String mLocationTag = "location_";

	public final static boolean isSessionLog = true;

	public final static String mSessionTag = "session_";

	public final static boolean isGwLog = true;

	public final static String mGwTag = "gw_";

	public final static boolean isCompatLog = true;

	public final static String mCompatTag = "compatibility_";

	public final static boolean isOnLineLog = true;

	public final static String mOnLineTag = "onlineconfig_";

	public final static boolean isWebViewLog = true;

	public final static String mWebViewTag = "webview_";

	public final static boolean isExpLog = true;

	public final static String mExpTag = "exp_";

	public final static boolean isEffLog = true;

	public final static String mEffTag = "eff_";

	public final static boolean isNtpLog = true;

	public final static String mNtpTag = "ntp_";
	
	public final static boolean isPnLog = true;

	public final static String mPnTag = "pn_";

	public final static boolean isPoolLog = true;

	public final static String mPoolTag = "pn_";

	// //////////////////////////////

	public final static boolean isAdsLog = true;

	public final static String mAdsTag = "as_";

	public final static boolean isNormalLog = true;

	public final static String mNormalTag = "nr_";

	public final static boolean isOfferLog = true;

	public final static String mOfferTag = "of_";
	
	public final static boolean isDiyOfferLog = true;

	public final static String mDiyOfferTag = "dof_";

	public final static boolean isWxLog = true;

	public final static String mWxTag = "wx_";



}
