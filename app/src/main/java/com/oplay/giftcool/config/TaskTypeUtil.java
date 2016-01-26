package com.oplay.giftcool.config;

/**
 * Created by zsigui on 16-1-7.
 */
public class TaskTypeUtil {


	/* List两种类型 */
	public static final int TYPE_COUNT = 2;
	public static final int TYPE_HEADER = 0;
	public static final int TYPE_CONTENT = 1;

	public static int getItemViewType(int missionType) {
		switch (missionType) {
			case MISSION_TYPE_TIRO:
			case MISSION_TYPE_DAILY:
			case MISSION_TYPE_CONTINUOUS:
				return TYPE_CONTENT;
			default:
				return TYPE_HEADER;
		}
	}

	/**
	 * 新手任务类型
	 */
	public static final int MISSION_TYPE_TIRO = 1;
	/**
	 * 每日任务类型
	 */
	public static final int MISSION_TYPE_DAILY = 2;
	/**
	 * 持续任务类型
	 */
	public static final int MISSION_TYPE_CONTINUOUS = 3;

	/**
	 * 上传头像
	 */
	public static final String ID_UPLOAD_AVATOR = "first_set_avatar";
	/**
	 * 设置昵称
	 */
	public static final String ID_SET_NICK = "first_set_nick";
	/**
	 * 绑定手机账号
	 */
	public static final String ID_BIND_PHONE = "account_band_mobile";
	/**
	 * 绑定偶玩账号
	 */
	public static final String ID_BIND_OUWAN = "account_band_ouwan";
	/**
	 * 新版本意见反馈
	 */
	public static final String ID_FEEDBACK = "version_v0.1_feedback";
	/**
	 * 搜索礼包/游戏
	 */
	public static final String ID_SEARCH = "search_gift_or_game";
	/**
	 * 评论一款游戏
	 */
	public static final String ID_JUDGE_GAME = "";
	/**
	 * 为一条评论点赞
	 */
	public static final String ID_STAR_COMMENT = "";
	/**
	 * 第一次登录
	 */
	public static final String ID_FIRST_LOGIN = "first_login_reward";
	/**
	 * 登录
	 */
	public static final String ID_LOGIN = "every_day_login";
	/**
	 * 下载游戏
	 */
	public static final String ID_DOWNLOAD = "download_game_apk";
	/**
	 * 分享普通礼包
	 */
	public static final String ID_SHARE_NORMAL_GIFT = "share_common_gift";
	/**
	 * 分享限量礼包
	 */
	public static final String ID_SHARE_LIMIT_GIFT = "share_every_day_precious_gift";
	/**
	 * 使用偶玩豆领取一款每日限量礼包
	 */
	public static final String ID_GET_LIMIT_WITH_BEAN = "get_every_day_precious_gift_by_ouwan_mili";
	/**
	 * 下载指定款游戏
	 */
	public static final String ID_DOWNLOAD_SPECIFIED = "";
	/**
	 * 连续登录7天
	 */
	public static final String ID_CONTINUOUS_LOGIN = "seven_every_day_login";
}
