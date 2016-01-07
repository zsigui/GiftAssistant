package com.oplay.giftassistant.config;

/**
 * Created by zsigui on 15-12-24.
 */
public class NetUrl {

	public static final String URL_BASE = "http://172.16.3.59/";

	/**
	 * 礼包首页
	 */
	public static final String GIFT_GET_INDEX = "gift_index";

	/**
	 * 猜你喜欢
	 */
	public static final String GIFT_GET_ALL_LIKE = "guess_you_like_list";

	/**
	 * 每日限量-全部
	 */
	public static final String GIFT_GET_ALL_LIMIT = "day_limit_list";

	/**
	 * 每日限量-某日某页
	 */
	public static final String GIFT_GET_ALL_LIMIT_BY_PAGE = "day_limit_list_by_page";

	/**
	 * 每日新增-全部
	 */
	public static final String GIFT_GET_ALL_NEW = "day_new_list";

	/**
	 * 每日新增-某日某页
	 */
	public static final String GIFT_GET_ALL_NEW_BY_PAGE = "day_new_list_by_page";

	/**
	 * 礼包详情
	 */
	public static final String GIFT_GET_DETAIL = "gift_plan_detail";

	/**
	 * 游戏首页-精品
	 */
	public static final String GAME_GET_INDEX_SUPER = "default";

	/**
	 * 游戏首页-类别(标签)
	 */
	public static final String GAME_GET_INDEX_TYPE = "default";

	/**
	 * 游戏首页-榜单
	 */
	public static final String GAME_GET_INDEX_NOTICE = "default";

	/**
	 * 新游推荐
	 */
	public static final String GAME_GET_NEW = "default";

	/**
	 * 热门手游
	 */
	public static final String GAME_GET_HOT = "default";

	/**
	 * 游戏搜索
	 */
	public static final String GAME_GET_SEARCH = "default";

	/**
	 * 游戏详情
	 */
	public static final String GAME_GET_DETAIL = "default";

	/* 用户接口 */

	/**
	 * 用户退出
	 */
	public static final String USER_LOGOUT = "api/account/logout_account";

	/**
	 * 更新Session
	 */
	public static final String USER_UPDATE_SESSION = "api/account/change_login_session";

	/**
	 * 手机登录-第一步
	 */
	public static final String USER_PHONE_LOGIN_FIRST = "api/account/get_phone_verification_code_for_login";

	/**
	 * 手机登录-第二步
	 */
	public static final String USER_PHONE_LOGIN_SECOND = "api/account/login_account_by_phone_verification_code";

	/**
	 * 偶玩账号登录
	 */
	public static final String USER_OUWAN_LOGIN = "api/account/login_account_by_ouwan";

	/**
	 * 获取用户个人信息
	 */
	public static final String USER_GET_INFO = "defaul";

	/**
	 * 修改用户昵称
	 */
	public static final String USER_MODIFY_NICK = "api/account/modify_account_nick";

	/**
	 * 修改用户头像
	 */
	public static final String USER_MODIFY_AVATAR = "defaul";

	/**
	 * 修改绑定手机-第一步
	 */
	public static final String USER_MODIFY_PHONE_FIRST = "api/account/modify_account_bind_mobile_step_1";

	/**
	 * 修改绑定手机-第二步
	 */
	public static final String USER_MODIFY_PHONE_SECOND = "api/account/modify_account_bind_mobile_step_2";

	/**
	 * 修改绑定手机-第三歩
	 */
	public static final String USER_MODIFY_PHONE_THIRD = "api/account/modify_account_bind_mobile_step_3";

	/**
	 * 修改绑定手机-第四步
	 */
	public static final String USER_MODIFY_PHONE_FOUR = "api/account/modify_account_bind_mobile_step_4";

	/**
	 * 获取已抢礼包
	 */
	public static final String USER_GIFT_SEIZED = "default";

	/**
	 * 获取已淘礼包
	 */
	public static final String USER_GIFT_SEARCHED = "default";

	/**
	 * 获取已过期礼包
	 */
	public static final String USER_GIFT_OVERTIME = "default";



	/* 积分接口 */

	/**
	 * 获取积分任务
	 */
	public static final String SCORE_GET_TASK = "api/mission/get_points_mission_list";
}
