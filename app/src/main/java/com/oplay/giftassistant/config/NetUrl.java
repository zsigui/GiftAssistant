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
}
