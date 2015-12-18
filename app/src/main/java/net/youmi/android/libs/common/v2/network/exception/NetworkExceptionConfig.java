package net.youmi.android.libs.common.v2.network.exception;

/**
 * 异常上报类，方便异常上报时候区分产品和种类，其他平台使用时请加入自己的产品标识
 *
 * @author bintou
 */
public class NetworkExceptionConfig {

	public final static String PRODUCT_WALL = "wl";

	public final static String PRODUCT_SPOT = "st";

	public final static String PRODUCT_BANNER = "br";

	public final static String PRODUCT_RECOMMEND = "rd";

	public final static String PRODUCT_VIDEO = "vd";

	public final static String PRODUCT_WEIXINWALL = "wn";

	/**
	 * 非产品，如SDK init
	 */
	public final static String PRODUCT_NONE = "none";

	/**
	 * 产品类型
	 */
	private String productType = "";

	/**
	 * 异常上报标签，例如i_user_pts
	 */
	private String tag;
	
	/*
	 * 控制是否进行错误上报，比如像广点通那种特殊的链接就不进行上报，默认进行上报。
	 */
	private boolean isPushExcepiton = true;

	/**
	 * sdk版本号
	 */
	private int sdkVersion;

	/**
	 * 发起异常上报的调用者
	 */
	private String mCaller = "SDK";

	//	/**
	//	 * 异常上报地址
	//	 */
	//	private String mExceptionReportHost;
	//
	//	/**
	//	 * 获取异常上报的地址
	//	 * 如果没有调用过set 方法，那么默认是返回exrep.youmi.net 为广告sdk的上报地址
	//	 *
	//	 * @return
	//	 */
	//	public String getExceptionReportHost() {
	//		if (Basic_StringUtil.isNullOrEmpty(mExceptionReportHost)) {
	//			mExceptionReportHost = CommonConstant.get_Url_ErrorReport();
	//		}
	//		return mExceptionReportHost;
	//	}
	//
	//	/**
	//	 * 设置默认的异常上报地址
	//	 *
	//	 * @param exceptionReportHost
	//	 */
	//	public void setExceptionReportHost(String exceptionReportHost) {
	//		mExceptionReportHost = exceptionReportHost;
	//	}

	String getCaller() {
		return mCaller;
	}

	/**
	 * 设置调用方，如"sdk" 或者　"web"前端等
	 *
	 * @param caller
	 */
	public void setCaller(String caller) {
		mCaller = caller;
	}

	int getSdkVersion() {
		if (sdkVersion == 0) {
			return 100;
		}
		return sdkVersion;
	}

	public void setSdkVersion(int sdkVersion) {
		this.sdkVersion = sdkVersion;
	}

	String getProductType() {
		if (productType == null) {
			return "not set";
		}
		return productType;
	}

	public void setProductType(String productType) {
		this.productType = productType;
	}

	public boolean isPushExcepiton() {
		return isPushExcepiton;
	}

	public void setPushExcepiton(boolean isPushExcepiton) {
		this.isPushExcepiton = isPushExcepiton;
	}

	String getTag() {
		if (tag == null) {
			return "not set";
		}
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

}
