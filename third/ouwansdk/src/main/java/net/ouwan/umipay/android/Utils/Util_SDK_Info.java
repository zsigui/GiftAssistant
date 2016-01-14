package net.ouwan.umipay.android.Utils;

import android.content.Context;

import net.ouwan.umipay.android.config.SDKConstantConfig;

/**
 * Util_SDK_Info
 *
 * @author zacklpx
 *         date 15-3-25
 *         description
 */
public class Util_SDK_Info {
	public static final String PAYTYPE_PAYECO = "YILIAN";
	public static final String PAYTYPE_UPMP = "UPMP";
	public static final String PAYTYPE_WQPAY = "WQPAY";
	public static final String PAYTYPE_ALIPAYSDK = "ALIPAYSDK";

	public static String getSDKSupportPayType(Context context) {
		StringBuilder sb = new StringBuilder();
		if (isSDKSupportWQPAY()) {
			sb.append(PAYTYPE_WQPAY);
		}
		if (isSDKSupportPayeco()) {
			sb.append(",").append(PAYTYPE_PAYECO);
		}
		if (isSDKSupportUnionPay()) {
			sb.append(",").append(PAYTYPE_UPMP);
		}
		if (isSDKSupportAlipaySDK()) {
			sb.append(",").append(PAYTYPE_ALIPAYSDK);
		}
		return sb.toString();
	}

	public static boolean isSDKSupportWQPAY() {
		return false;
	}

	public static boolean isSDKSupportPayeco() {
		return SDKConstantConfig.SUPPORT_PAYECO;
	}

	public static boolean isSDKSupportUnionPay() {
		return SDKConstantConfig.SUPPORT_UPMP;
	}

	public static boolean isSDKSupportAlipaySDK() {
		return SDKConstantConfig.SUPPORT_ALIPAYSDK;
	}
}
