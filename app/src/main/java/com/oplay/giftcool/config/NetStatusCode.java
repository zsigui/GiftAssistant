package com.oplay.giftcool.config;

/**
 * 错误请求码定义 <br />
 * API (10 ~ 100) , 网关 (100 ~ 200) , 逻辑 (200 ~ ) <br />
 * <br />
 * Created by zsigui on 15-12-22.
 */
public abstract class NetStatusCode {

	/**
	 * 成功
	 */
	public static final int SUCCESS = 0;

	/**
	 * 打包错误
	 */
	public static final int ERR_PACK = 11;

	/**
	 * 加密错误
	 */
	public static final int ERR_ENCODE = 12;

	/**
	 * 解包错误
	 */
	public static final int ERR_UNPACK = 14;

	/**
	 * 网络错误
	 */
	public static final int ERR_NETWORK = 15;


	/**
	 * 包错误
	 */
	public static final int ERR_WRONG_PKG = 16;

	/**
	 * 命令字错误
	 */
	public static final int ERR_CMD = 17;

	/**
	 * 执行请求错误，格式之类导致的捕获异常
	 */
	public static final int ERR_EXEC_FAIL = 18;

	/**
	 * 请求返回实体为空
	 */
	public static final int ERR_EMPTY_RESPONSE = 19;

	/**
	 * APPKEY不合法
	 */
	public static final int ERR_APPKEY = 110;

	/**
	 * 平台错误
	 */
	public static final int ERR_PLATFORM = 111;

	/**
	 * 包头错误
	 */
	public static final int ERR_HASH = 112;



	/* ******** 用户相关状态码 1002x ~ 1003x ********* */
	/**
	 * 用户服务出错
	 */
	public static final int ERR_BAD_SERVER = 10032;
	/**
	 * 未登录
	 */
	public static final int ERR_UN_LOGIN = 10037;

	/* **********  求礼包错误码  *********** */
	/**
	 * 该款游戏今日求礼包次数达到上限
	 */
	public static final int ERR_GAME_HOPE_GIFT_LIMIT = 11002;
	/**
	 * 每日求礼包次数达到上限
	 */
	public static final int ERR_TOTAL_HOPE_GIFT_LIMIT = 11003;
}
