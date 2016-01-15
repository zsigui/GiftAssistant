package com.oplay.giftassistant.config;

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
	public static final String ID_UPLOAD_AVATOR = "";
	/**
	 * 设置昵称
	 */
	public static final String ID_SET_NICK = "";
	/**
	 * 绑定手机账号
	 */
	public static final String ID_BIND_PHONE = "";
	/**
	 * 绑定偶玩账号
	 */
	public static final String ID_BIND_OUWAN = "";
	/**
	 * 新版本意见反馈
	 */
	public static final String ID_FEEDBACK = "";
	/**
	 * 搜索礼包/游戏
	 */
	public static final String ID_SEARCH = "";
	/**
	 * 评论一款游戏
	 */
	public static final String ID_JUDGE_GAME = "";
	/**
	 * 为一条评论点赞
	 */
	public static final String ID_STAR_COMMENT = "";
	/**
	 * 登录
	 */
	public static final String ID_LOGIN = "";
	/**
	 * 下载游戏
	 */
	public static final String ID_DOWNLOAD = "";
	/**
	 * 分享普通礼包
	 */
	public static final String ID_SHARE_NORMAL_GIFT = "";
	/**
	 * 分享限量礼包
	 */
	public static final String ID_SHARE_LIMIT_GIFT = "";
	/**
	 * 使用偶玩豆领取一款每日限量礼包
	 */
	public static final String ID_GET_LIMIT_WITH_BEAN = "";
	/**
	 * 下载指定款游戏
	 */
	public static final String ID_DOWNLOAD_SPECIFIED = "";
	/**
	 * 连续登录7天
	 */
	public static final String ID_CONTINUOUS_LOGIN = "";
}
