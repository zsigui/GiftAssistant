package com.oplay.giftassistant.config;

/**
 * 定义Intent传输的时候的通用键名
 *
 * Created by zsigui on 16-1-6.
 */
public class KeyConfig {

	public static final String KEY_TYPE = "key_type";
	public static final String KEY_DATA = "key_data";
	public static final String KEY_URL = "key_url";
	public static final String KEY_SEARCH = "key_search";
	public static final String KEY_NAME = "key_name";

	/* result code */
	public static final int SUCCESS = 0;
	public static final int FAIL = -1;

	public static final int TYPE_ID_DEFAULT = 0x100;
	/* setting activity  */
	public static final int TYPE_ID_SETTING = 0x101;
	public static final int TYPE_ID_WALLET = 0x102;
	public static final int TYPE_ID_DOWNLOAD = 0x103;
	public static final int TYPE_ID_DETAIL_BEAN = 0x104;
	public static final int TYPE_ID_DETAIL_SCORE = 0x106;
	public static final int TYPE_ID_MY_GIFT_CODE = 0x107;
	public static final int TYPE_ID_SCORE_TASK = 0x108;
	public static final int TYPE_ID_MSG = 0x109;
	public static final int TYPE_ID_PROFILE = 0x110;

	/* login activity */
	public static final int TYPE_ID_LOGIN_MAIN = 0x111;
	public static final int TYPE_ID_OUWAN_LOGIN = 0x112;
	public static final int TYPE_ID_PHONE_LOGIN_ONE = 0x113;
	public static final int TYPE_ID_PHONE_LOGIN_TWO = 0x114;


	/* game list activity */
	// 新游推荐
	public static final int TYPE_ID_GAME_NEW = 0x201;
	// 特定类型游戏
	public static final int TYPE_ID_GAME_TYPE = 0x202;
	// 热门游戏
	public static final int TYPE_ID_GAME_HOT = 0x203;
	// 游戏搜索
	public static final int TYPE_ID_GAME_SEARCH = 0x204;


	/* task fragment */
	public static final int REQUEST_UPDATE_AVATAR = 0xA00;
	public static final int REQUEST_SET_NICK = 0xA01;

	/* my gift list fragment */
	public static final int TYPE_KEY_SEIZED = 0x210;
	public static final int TYPE_KEY_OVERTIME = 0x211;
	public static final int TYPE_KEY_SEARCH = 0x212;

}
