package net.ouwan.umipay.android.config;

import android.content.Context;

import net.ouwan.umipay.android.api.GameParamInfo;
import net.youmi.android.libs.platform.coder.Coder_SDKPswCoder;

/**
 * SDKConstantConfig
 *
 * @author zacklpx
 *         date 15-2-3
 *         description
 */
public class SDKConstantConfig {
	public static final int UMIPAY_SDK_VERSION = 400;
	public static final int SDK_ID = 13;
	public static final int API_SDK_VERSION = 103;
	public static final int SDK_PLATFORM_ANDROID = 3;
	//0:未定，1：网游支付，2：单机支付
	public static final int UMIPAY_SDK_SERVICE = 1;
	//SDK support paytype
	public static final boolean SUPPORT_PAYECO = true;     //2.0的银行卡支付
	public static final boolean SUPPORT_UPMP = true;        //现用银联支付UPMP
	public static final boolean SUPPORT_ALIPAYSDK = true;   //native版支付宝

	//第三方登陆参数
	public static final String QQ_OAUTH_APPID = "100378813";

	public static final String QQ_SCOPE = "get_simple_userinfo";

	public static final String SINA_OAUTH_APPID = "4067843395";
	public static final String SINA_REDIRECT_URL = "https://api.weibo.com/oauth2/default.html";

	//https真实环境
	private static final String HOST_HTTPS = Coder_SDKPswCoder.decode(ConstantString.HOST_HTTPS,
			ConstantString.SERVER_URL_KEY);
	//https测试环境
	private static final String HOST4TEST_HTTPS = Coder_SDKPswCoder.decode(ConstantString.HOST4TEST_HTTPS,
			ConstantString.SERVER_URL_KEY);
	//http真实环境
	private static final String HOST_HTTP = Coder_SDKPswCoder.decode(ConstantString.HOST_HTTP,
			ConstantString.SERVER_URL_KEY);
	//http测试环境
	private static final String HOST4TEST_HTTP = Coder_SDKPswCoder.decode(ConstantString.HOST4TEST_HTTP,
			ConstantString.SERVER_URL_KEY);
	/**
	 * @Fields UMIPAY_AGREEMENT_URL : 服务条款
	 */
	private static final String UMIPAY_AGREEMENT_URL = Coder_SDKPswCoder.decode(ConstantString.UMIPAY_AGREEMENT_URL,
			ConstantString.SERVER_URL_KEY);
	/**
	 * @Fields UMIPAY_ACCOUNT_URL : 账号管理页面
	 */
	private static final String UMIPAY_ACCOUNT_URL = Coder_SDKPswCoder.decode(ConstantString.UMIPAY_ACCOUNT_URL,
			ConstantString.SERVER_URL_KEY);
	/**
	 * @Fields UMIPAY_FORGET_URL : 找回密码界面
	 */
	private static final String UMIPAY_FORGET_URL = Coder_SDKPswCoder.decode(ConstantString.UMIPAY_FORGET_URL,
			ConstantString.SERVER_URL_KEY);
	/**
	 * @Fields UMIPAY_BIND_URL : 注册成功后跳转绑定界面
	 */
	private static final String UMIPAY_BIND_URL = Coder_SDKPswCoder.decode(ConstantString.UMIPAY_BIND_URL,
			ConstantString.SERVER_URL_KEY);
	/**
	 * @Fields UMIPAY_BIND_URL : 充值界面
	 */
	private static final String UMIPAY_PAY_URL = Coder_SDKPswCoder.decode(ConstantString.UMIPAY_PAY_URL,
			ConstantString.SERVER_URL_KEY);
	/**
	 * @Fields UMIPAY_JUMP_URL : 跳转链接
	 */
	private static final String UMIPAY_JUMP_URL = Coder_SDKPswCoder.decode(ConstantString.UMIPAY_JUMP_URL,
			ConstantString.SERVER_URL_KEY);
	/**
	 * UMIPAY_CACHE_URL : 预加载
	 */
	private static final String UMIPAY_CACHE_URL = Coder_SDKPswCoder.decode(ConstantString.UMIPAY_CACHE_URL,
			ConstantString.SERVER_URL_KEY);


	public static String get_HOST_URL(Context context) {
		if (isTestMode(context)) {
			if (isEnableHttps(context)) {
				return HOST4TEST_HTTPS;
			} else {
				return HOST4TEST_HTTP;
			}
		} else {
			if (isEnableHttps(context)) {
				return HOST_HTTPS;
			} else {
				return HOST_HTTP;
			}
		}
	}

	/**
	 * 偶玩服务条款
	 *
	 * @param context
	 * @return
	 */
	public static String get_UMIPAY_AGREEMENT_URL(Context context) {
		if (isTestMode(context)) {
			if (isEnableHttps(context)) {
				return HOST4TEST_HTTPS + UMIPAY_AGREEMENT_URL;
			} else {
				return HOST4TEST_HTTP + UMIPAY_AGREEMENT_URL;
			}
		} else {
			if (isEnableHttps(context)) {
				return HOST_HTTPS + UMIPAY_AGREEMENT_URL;
			} else {
				return HOST_HTTP + UMIPAY_AGREEMENT_URL;
			}
		}
	}

	/**
	 * 账号中心URL
	 *
	 * @param context
	 * @return
	 */
	public static String get_UMIPAY_ACCOUNT_URL(Context context) {
		if (isTestMode(context)) {
			if (isEnableHttps(context)) {
				return HOST4TEST_HTTPS + UMIPAY_ACCOUNT_URL;
			} else {
				return HOST4TEST_HTTP + UMIPAY_ACCOUNT_URL;
			}
		} else {
			if (isEnableHttps(context)) {
				return HOST_HTTPS + UMIPAY_ACCOUNT_URL;
			} else {
				return HOST_HTTP + UMIPAY_ACCOUNT_URL;
			}
		}
	}

	/**
	 * 忘记密码URL
	 *
	 * @param context
	 * @return
	 */
	public static String get_UMIPAY_FORGET_URL(Context context) {
		if (isTestMode(context)) {
			if (isEnableHttps(context)) {
				return HOST4TEST_HTTPS + UMIPAY_FORGET_URL;
			} else {
				return HOST4TEST_HTTP + UMIPAY_FORGET_URL;
			}
		} else {
			if (isEnableHttps(context)) {
				return HOST_HTTPS + UMIPAY_FORGET_URL;
			} else {
				return HOST_HTTP + UMIPAY_FORGET_URL;
			}
		}
	}

	public static String get_UMIPAY_PAY_URL(Context context) {
		if (isTestMode(context)) {
			if (isEnableHttps(context)) {
				return HOST4TEST_HTTPS + UMIPAY_PAY_URL;
			} else {
				return HOST4TEST_HTTP + UMIPAY_PAY_URL;
			}
		} else {
			if (isEnableHttps(context)) {
				return HOST_HTTPS + UMIPAY_PAY_URL;
			} else {
				return HOST_HTTP + UMIPAY_PAY_URL;
			}
		}
	}

	public static String get_UMIPAY_JUMP_URL(Context context) {

		if (isTestMode(context)) {
			if (isEnableHttps(context)) {
				return HOST4TEST_HTTPS + UMIPAY_JUMP_URL;
			} else {
				return HOST4TEST_HTTP + UMIPAY_JUMP_URL;
			}
		} else {
			if (isEnableHttps(context)) {
				return HOST_HTTPS + UMIPAY_JUMP_URL;
			} else {
				return HOST_HTTP + UMIPAY_JUMP_URL;
			}
		}
	}

	public static String get_CACHE_URL(Context context) {
		StringBuilder sb = new StringBuilder();
		sb.append("?version=");
		sb.append(UMIPAY_SDK_VERSION);
		sb.append("&service=");
		sb.append(UMIPAY_SDK_SERVICE);
		if (isTestMode(context)) {
			if (isEnableHttps(context)) {
				return HOST4TEST_HTTPS + UMIPAY_CACHE_URL + sb.toString();
			} else {
				return HOST4TEST_HTTP + UMIPAY_CACHE_URL + sb.toString();
			}
		} else {
			if (isEnableHttps(context)) {
				return HOST_HTTPS + UMIPAY_CACHE_URL + sb.toString();
			} else {
				return HOST_HTTP + UMIPAY_CACHE_URL + sb.toString();
			}
		}
	}

	private static boolean isTestMode(Context context) {
		return GameParamInfo.getInstance(context).isTestMode();
	}

	private static boolean isEnableHttps(Context context) {
		return SDKCacheConfig.getInstance(context).isEnableHttps();
	}
}
