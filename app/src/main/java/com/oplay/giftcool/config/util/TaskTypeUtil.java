package com.oplay.giftcool.config.util;

import com.oplay.giftcool.model.data.resp.task.ScoreMission;

/**
 * Created by zsigui on 16-1-7.
 */
public class TaskTypeUtil {


	/* List两种类型 */
	public static final int TYPE_COUNT = 2;
	public static final int TYPE_HEADER = 0;
	public static final int TYPE_CONTENT = 1;


	public static int getItemViewType(ScoreMission mission) {
		if (mission.isHeader) {
			return TYPE_HEADER;
		} else {
			return TYPE_CONTENT;
		}
	}

	/*----------- 处理类型 ----------*/
	/**
	 * 打开应用特定页面
	 */
	public static final int MISSION_TYPE_JUMP_PAGE = 1;
	/**
	 * 执行特定代码
	 */
	public static final int MISSION_TYPE_EXECUTE_LOGIC = 2;
	/**
	 * 下载并打开应用
	 */
	public static final int MISSION_TYPE_DOWNLOAD = 3;


	/*----------- 任务类型的子类型处理代号 -----------*/
	/*------- 类型一 --------*/
	public static final int INFO_ONE_SDK_RECHARGE = 0;
	public static final int INFO_ONE_SDK_BIND_OUWAN = 1;
	public static final int INFO_ONE_SDK_BIND_PHONE = 2;
	/*------- 类型二 --------*/
	public static final int INFO_TWO_SHARE_GCOOL = 101;
	public static final int INFO_TWO_REQUEST_GIFT = 102;
	public static final int INFO_TWO_SHOW_UPGRADE = 103;

	/*----------- 任务代号 -----------*/
	/**
	 * 首次登录
	 */
	public static final String ID_FIRST_LOGIN = "FIRST_LOGIN";
	/**
	 * 首次设置头像
	 */
	public static final String ID_SET_AVATAR = "FIRST_SET_AVATAR";
	/**
	 * 首次设置昵称
	 */
	public static final String ID_SET_NICK = "FIRST_SET_NIC";
	/**
	 * 关注游戏
	 */
	public static final String ID_FOCUS_GAME = "FOCUS_GAME";
	/**
	 * 求礼包
	 */
	public static final String ID_REQUEST_GIFT = "REQUEST_GIFT";
	/**
	 * 版本反馈
	 */
	public static final String ID_FEEDBACK = "CLIENT_FEEDBACK";
	/**
	 * 版本升级
	 */
	public static final String ID_UPGRADE = "CLIENT_UPGRADE";
	/**
	 * 签到
	 */
	public static final String ID_SIGN_IN = "DAILY_SIGNIN";
	/**
	 * 分享礼包酷
	 */
	public static final String ID_GCOOL_SHARE = "SHARE_GIFTCOOL_CONTENT";
	/**
	 * 分享礼包
	 */
	public static final String ID_GIFT_SHARE = "SHARE_GIFT";
	/**
	 * 试玩游戏
	 */
	public static final String ID_PLAY_GAME = "PLAY_SPECIFIED_GAME";
	/**
	 * 使用偶玩豆购买礼包
	 */
	public static final String ID_BUG_GIFT_USE_OUWAN = "BUG_GIFT_USE_OUWAN_DOU";
}
