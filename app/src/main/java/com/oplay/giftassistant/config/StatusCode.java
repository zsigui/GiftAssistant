package com.oplay.giftassistant.config;

/**
 *
 * 错误请求码定义 <br />
 * API (10 ~ 100) , 网关 (100 ~ 200) , 逻辑 (200 ~ ) <br />
 * <br />
 * Created by zsigui on 15-12-22.
 */
public class StatusCode {

	/**
	 * 成功
	 */
	public static int SUCCESS = 0;

	/**
	 * 打包错误
	 */
	public static int ERR_PACK = 11;

	/**
	 * 加密错误
	 */
	public static int ERR_ENCODE = 12;

	/**
	 * 解包错误
	 */
	public static int ERR_UNPACK = 14;

	/**
	 * 网络错误
	 */
	public static int ERR_NETWORK = 15;

	/**
	 * 包错误
	 */
	public static int ERR_WRONG_PKG = 16;

	/**
	 * 命令字错误
	 */
	public static int ERR_CMD = 17;

	/**
	 * APPKEY不合法
	 */
	public static int ERR_APPKEY = 110;

	/**
	 * 平台错误
	 */
	public static int ERR_PLATFORM = 111;

	/**
	 * 包头错误
	 */
	public static int ERR_HASH = 112;
}
