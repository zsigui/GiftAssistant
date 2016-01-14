package net.ouwan.umipay.android.config;

import net.youmi.android.libs.platform.coder.Coder_SDKPswCoder;

/**
 * ConstantString
 * <p/>
 * 全部静态字符串都放这类，需要是可修改，防关键字查找
 *
 * @author zacklpx
 *         date 15-1-28
 *         description
 */
public class ConstantString {

	//对外的tag：umipay
	public final static String LOGTAG = "425e5b16584a";

	public final static String LOGTAG_KEY = "o82bH3H6";

	//.umipaycache
	public final static String CACHE_DIR = "4d44090845054c0553535106";

	public final static String CACHE_DIR_KEY = "I176py7D";

	//服务器地址
	public final static String SERVER_URL_KEY = "P8TOBUjW";
	//http://gw2.umipay.com/mp/gw/pay/
	public final static String SERVER_URL = "0b1217410c4b4e0243024c45095912574e18055f0e4d0f401c504e4946581d1a";
	//http://test.gw2.umipay.com/mp/gw/pay/
	public final static String TEST_SERVER_URL =
			"0b1217410c4b4e115143161e03475018425b0f40021b4c535c5a160b461603421b1359414a";

	//  https://pay.umipay.com
	public final static String HOST_HTTPS = "0b121741455e4e4a44511b1e115d0b46564f48530c0f";
	//  https://test.pay.umipay.com
	public final static String HOST4TEST_HTTPS = "0b121741455e4e4a405511444a40034f19430b5913031b1e505854";
	//  http://pay.umipay.com
	public final static String HOST_HTTP = "0b1217410c4b4e1555494c45095912574e18055f0e";
	//  http://test.pay.umipay.com
	public final static String HOST4TEST_HTTP = "0b1217410c4b4e115143161e14511b18425b0f40021b4c535c5a";
	//  /page/term.html
	public final static String UMIPAY_AGREEMENT_URL = "4c160256534b1500465d4c58105d0e";
	//  /account/
	public static final String UMIPAY_ACCOUNT_URL = "4c07005259110f111b";
	//  /account/forget
	public static final String UMIPAY_FORGET_URL = "4c07005259110f111b560d42035516";
	//  /account/bind
	public static final String UMIPAY_BIND_URL = "4c07005259110f111b520b5e00";
	//  /pay/jump
	public static final String UMIPAY_PAY_URL = "4c160248190e140844";
	//  /jump/jump
	public static final String UMIPAY_JUMP_URL = "4c0c165c464b0b105940";
	//  /pay/cache
	public static final String UMIPAY_CACHE_URL = "4c160248190700065c55";

	public static final String SO_LIB_NAME_KEY = "ONRuxMnz";
	// libymfx.so
	public static final String SO_LIB_NAME_YMFX = "540b524054564f4b1559";
	// libentryexstd.so
	public static final String SO_LIB_NAME_ENTRYEXSTD = "540b525c5744451c034e124c001a4a5b";

	public final static String MSG_KEY = "EuRe8ZDV";

	public final static String PARAMETER_ERR_MSG = "87b7e784f185dba6a8dd9698";

	public static String getParameterErrMsg() {
		return Coder_SDKPswCoder.decode(PARAMETER_ERR_MSG, MSG_KEY);
	}
}
