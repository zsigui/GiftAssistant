package com.oplay.giftcool.config;

/**
 * 定义Intent传输的时候的通用键名
 * <p/>
 * Created by zsigui on 16-1-6.
 */
public class KeyConfig {

	public static final String KEY_TYPE = "key_type";
	public static final String KEY_DATA = "key_data";
	public static final String KEY_DATA_O = "key_data2";
	public static final String KEY_TITLE = "key_title";
	public static final String KEY_URL = "key_url";
	public static final String KEY_SEARCH = "key_search";
	public static final String KEY_NAME = "key_name";
	public static final String KEY_STATUS = "key_status";

	/* result code */
	public static final int SUCCESS = 0;
	public static final int FAIL = -1;
	public static final int TYPE_ID_DEFAULT = 0x0;

	/* main activity */
	public static final int TYPE_ID_INDEX_GIFT = 0x1;
	public static final int TYPE_ID_INDEX_GAME = 0x2;
	public static final int TYPE_ID_INDEX_POST = 0x3;

	/* setting activity  */
	public static final int TYPE_ID_MY_GIFT_CODE = 0x101;
	public static final int TYPE_ID_MY_ATTENTION = 0x102;
	public static final int TYPE_ID_WALLET = 0x103;
	public static final int TYPE_ID_TASK = 0x104;
	public static final int TYPE_ID_MSG = 0x105;
	public static final int TYPE_ID_DOWNLOAD = 0x106;
	public static final int TYPE_ID_SETTING = 0x107;

	public static final int TYPE_ID_DETAIL_BEAN = 0x113;
	public static final int TYPE_ID_DETAIL_SCORE = 0x114;
	public static final int TYPE_ID_PROFILE = 0x115;
	public static final int TYPE_ID_FEEDBACK = 0x116;
	public static final int TYPE_ID_USERINFO = 0x117;
	public static final int TYPE_ID_USER_SET_NICK = 0x118;
	public static final int TYPE_ID_USER_SET_AVATAR = 0x119;

	/* login activity */
	public static final int TYPE_ID_LOGIN_MAIN = 0x151;
	public static final int TYPE_ID_OUWAN_LOGIN = 0;
	public static final int TYPE_ID_PHONE_LOGIN = 1;

	/* gift list activity */
	public static final int TYPE_ID_GIFT_LIMIT = 0x170;
	public static final int TYPE_ID_GIFT_NEW = 0x171;
	public static final int TYPE_ID_GIFT_LIKE = 0x172;

	/* web activity */
	public static final int TYPE_ID_LOTTERY = 0x173;
	public static final int TYPE_SIGN_IN_EVERY_DAY = 0x174;

	/* game list activity */
	// 新游推荐
	public static final int TYPE_ID_GAME_NEW = 0x201;
	// 特定类型游戏
	public static final int TYPE_ID_GAME_TYPE = 0x202;
	// 热门游戏
	public static final int TYPE_ID_GAME_HOT = 0x203;
	// 游戏搜索
	public static final int TYPE_ID_GAME_SEARCH = 0x204;

	/* detail activity */
	public static final int TYPE_ID_GIFT_DETAIL = 0x301;
	public static final int TYPE_ID_GAME_DETAIL = 0x302;

	/* post list activity */
	public static final int TYPE_ID_POST_OFFICIAL = 0x401;

	/* message central activity */
	public static final int TYPE_ID_MSG_NEW_GIFT_NOTIFY = 0x501;
	public static final int TYPE_ID_MSG_ADMIRE = 0x502;
	public static final int TYPE_ID_MSG_SYSTEM = 0x503;
	public static final int TYPE_ID_MSG_COMMENT = 0x504;

	public static final String CODE_MSG_NEW_GIFT_NOTIFY = "msg_notify";
	public static final String CODE_MSG_ADMIRE = "msg_admire";
	public static final String CODE_MSG_SYSTEM = "msg_system";
	public static final String CODE_MSG_COMMENT = "msg_comment";

	/* task fragment */
	public static final int REQUEST_UPDATE_AVATAR = 0xA00;
	public static final int REQUEST_SET_NICK = 0xA01;

	/* my gift list fragment */
	// 已抢
	public static final int TYPE_KEY_SEIZED = 1;
	// 已淘
	public static final int TYPE_KEY_SEARCH = 2;
	// 已过期
	public static final int TYPE_KEY_OVERTIME = 3;
}
