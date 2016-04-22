package com.oplay.giftcool.config;

/**
 * Created by zsigui on 15-12-24.
 */
public class NetUrl {

	//public static final String TEST_URL_BASE = "http://172.16.3.239:8888/api/";
	//public static final String TEST_URL_BASE = "http://172.16.3.68:7000/";
	/**
	 * 测试地址
	 */
	public static final String TEST_URL_BASE = "http://test.lbapi.ouwan.com/api/";
	/**
	 * 正式地址
	 */
	public static final String URL_BASE = "http://lbapi.ouwan.com/api/";

	public static String REAL_URL = TEST_URL_BASE;

	public static String getBaseUrl() {
		return AppConfig.TEST_MODE ? REAL_URL : URL_BASE;
	}


	/**
	 * 礼包/游戏搜索
	 */
	public static final String GET_SEARCH = "gift_search";

	/**
	 * 礼包/游戏搜索-关键词提示
	 */
	public static final String GET_SEARCH_KEY = "tip_for_search";

	/**
	 * 礼包/游戏搜索-热门搜索推荐
	 */
	public static final String GET_SEARCH_HOT_DATA = "gift_hot_search";

	/**
	 * 提交求礼包申请
	 */
	public static final String COMMIT_HOPE_GIFT = "ask_gift/";

	/**
	 * 礼包首页-新鲜出炉列表
	 */
	public static final String GIFT_GET_INDEX_NEW = "new_gift_list";

	/**
	 * 礼包-刷新数据
	 */
	public static final String GIFT_REFRESH = "gift_update";

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
	 * 每日限量-某页
	 */
	public static final String GIFT_GET_LIMIT_BY_PAGE = "day_limit_list_by_page_v2/";

	/**
	 * 每日新增-全部
	 */
	public static final String GIFT_GET_ALL_NEW = "day_new_list";

	/**
	 * 每日新增-某日某页
	 */
	public static final String GIFT_GET_ALL_NEW_BY_PAGE = "day_new_list_by_page";

	/**
	 * 抢礼包
	 */
	public static final String GIFT_SEIZE_CODE = "gift_buy";

	/**
	 * 请求特定礼包的礼包码
	 */
	public static final String GIFT_GET_SPECIFIC_CODE = "get_pay_result";

	/**
	 * 通知偶玩豆订单支付失败
	 */
	public static final String GIFT_FAIL_PAY = "failed_pay";

	/**
	 * 礼包详情
	 */
	public static final String GIFT_GET_DETAIL = "gift_plan_detail";

	/**
	 * 游戏首页-精品
	 */
	public static final String GAME_GET_INDEX_SUPER = "app_index";

	/**
	 * 游戏首页-类别(标签)
	 */
	public static final String GAME_GET_INDEX_TYPE = "recommend_labels";

	/**
	 * 游戏首页-榜单
	 */
	public static final String GAME_GET_INDEX_NOTICE = "app_board";

	/**
	 * 新游推荐
	 */
	public static final String GAME_GET_NEW = "new_app";

	/**
	 * 热门手游
	 */
	public static final String GAME_GET_HOT = "hot_app";

	/**
	 * 游戏搜索
	 */
	public static final String GAME_GET_SEARCH = "default";

	/**
	 * 游戏详情
	 */
	public static final String GAME_GET_DETAIL = "app_detail";

	/**
	 * 获取某类游戏
	 */
	public static final String GAME_GET_TYPE = "label_apps";

	/**
	 * 获取首页活动数据
	 */
	public static final String POST_GET_INDEX = "activity/index";

	/**
	 * 获取活动列表数据
	 */
	public static final String POST_GET_LIST = "activity/list";

	/**
	 * 获取活动回复提交的token
	 */
	public static final String POST_REPLY_GET_TOKEN = "activity/comment_token";

	/**
	 * 进行活动回复
	 */
	public static final String POST_REPLY = "activity/add_comment";


	/* 用户接口 */

	/**
	 * 用户退出
	 */
	public static final String USER_LOGOUT = "account/logout_account";

	/**
	 * 更新Session
	 */
	public static final String USER_UPDATE_SESSION = "account/change_login_session";

	/**
	 * 手机登录-第一步
	 */
	public static final String USER_PHONE_LOGIN_FIRST = "account/get_phone_verification_code_for_login";

	/**
	 * 手机登录-第二步
	 */
	public static final String USER_PHONE_LOGIN_SECOND = "v2/account/login_account_by_phone_verification_code";

	/**
	 * 偶玩账号登录
	 */
	public static final String USER_OUWAN_LOGIN = "v2/account/login_account_by_ouwan";

	/**
	 * 获取用户个人信息
	 */
	public static final String USER_GET_INFO = "account/get_account_info";

	/**
	 * 获取用户部分信息
	 */
	public static final String USER_GET_PART_INFO = "account/get_account_gift_points_info";

	/**
	 * 修改用户昵称
	 */
	public static final String USER_MODIFY_NICK = "v2/account/modify_account_nick";

	/**
	 * 修改用户头像
	 */
	public static final String USER_MODIFY_AVATAR = "v2/account/change_account_avatar";

	/**
	 * 修改绑定手机-第一步
	 */
	public static final String USER_MODIFY_PHONE_FIRST = "account/modify_account_bind_mobile_step_1";

	/**
	 * 修改绑定手机-第二步
	 */
	public static final String USER_MODIFY_PHONE_SECOND = "account/modify_account_bind_mobile_step_2";

	/**
	 * 修改绑定手机-第三歩
	 */
	public static final String USER_MODIFY_PHONE_THIRD = "account/modify_account_bind_mobile_step_3";

	/**
	 * 修改绑定手机-第四步
	 */
	public static final String USER_MODIFY_PHONE_FOUR = "account/modify_account_bind_mobile_step_4";

	/**
	 * 获取我的礼包
	 */
	public static final String USER_GIFT_SEIZED = "my_gift";



	/* 任务接口 */

	/**
	 * 获取金币任务
	 */
	public static final String SCORE_GET_TASK = "v1/mission/user_mission";

	/**
	 * 请求金币奖励
	 */
	public static final String SCORE_REWARD = "v1/mission/finish_mission";

	/**
	 * 签到初始化接口，获取签到状态
	 */
	public static final String SCORE_SIGNIN_INIT = "v1/mission/daily_signin_info";

	/* 应用接口 */

	/**
	 * 意见反馈
	 */
	public static final String APP_POST_FEEDBACK = "v2/feedback/add_feedback";

	/**
	 * 应用初始化
	 */
	public static final String APP_INIT = "init";

	/**
	 * 应用版本更新
	 */
	public static final String APP_VERSION_UPDATE = "update_version";

	/**
	 * 上报手机应用信息
	 */
	public static final String APP_INFO_REPORTED = "v1/uil";

	/* 消息接口 */

	/**
	 * 推送消息列表数据
	 */
	public static final String MESSAGE_PUSH_LIST = "jpush_message_list/";

	/**
	 * 关注/取消关注游戏
	 */
	public static final String GAME_FOCUS_CHANGE = "change_focus_game/";

	/**
	 * 关注游戏列表
	 */
	public static final String GAME_FOCUS_LIST = "focus_game_list/";

	/**
	 * 修改推送消息状态
	 */
	public static final String MESSAGE_CHANGE_STATUS = "change_jpush_message_status/";

	/**
	 * 获取消息中心未读消息数量
	 */
	public static final String MESSAGE_UNREAD_COUNT = "account/user_message";

	/**
	 * 用户收到的赞/评论
	 */
	public static final String MESSAGE_REPLY_LIST = "activity/user_notice";

	/**
	 * 系统消息
	 */
	public static final String MESSAGE_SYSTEM_LIST = "message/system_message_list";


}
