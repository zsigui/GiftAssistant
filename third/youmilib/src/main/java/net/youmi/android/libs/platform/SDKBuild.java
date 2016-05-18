package net.youmi.android.libs.platform;

import net.youmi.android.libs.common.YmLibBuild;

public class SDKBuild extends YmLibBuild {


	public final static boolean IS_WEIXIN_SDK = false;

	public final static boolean IS_WEIXIN_SDK_TEST_MODEL = false;

	// /**
	// * 协议版本号
	// */
	public final static String PROTOCOL_CORE_VERSION = "2";

	/**
	 * Android平台编号
	 */
	public final static int PLATFORM_CODE = 3;

	/**
	 * 积分墙sdk版本号 e.g 500
	 */
	public final static int OFFER_SDK_VERSION_CODE = 551;

	/**
	 * 积分墙sdk版本名 e.g "5.0.0"
	 */
	public final static String OFFER_SDK_VERSION_NAME = "5.5.1";

	/**
	 * 无积分sdk版本号 e.g 411
	 */
	public final static int NORMAL_SDK_VERSION_CODE = 531;

	/**
	 * 无积分sdk版本名 e.g "411"
	 */
	public final static String NORMAL_SDK_VERSION_NAME = "531";

	/**
	 * 无积分sdk的版本名，用于在init的时候输出log用 e.g "4.1.1"
	 */
	public final static String NORMAL_SDK_VERSION_NAME_FOR_LOG = "5.3.1";

	/**
	 * 获取较大的sdk版本号
	 *
	 * @return
	 */
	public final static int getLargeVersionCode() {
		return OFFER_SDK_VERSION_CODE >= NORMAL_SDK_VERSION_CODE ? OFFER_SDK_VERSION_CODE : NORMAL_SDK_VERSION_CODE;
	}
}
