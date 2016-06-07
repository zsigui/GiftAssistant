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
    public static final int TYPE_ID_DEFAULT = 0;

    /* main activity */
    public static final int TYPE_ID_INDEX_GIFT = 1;
    public static final int TYPE_ID_INDEX_GAME = 2;
    public static final int TYPE_ID_INDEX_POST = 3;
    public static final int TYPE_ID_INDEX_FREE = 4;
    public static final int TYPE_ID_INDEX_UPGRADE = 100;

    /* setting activity  */
    public static final int TYPE_ID_MY_GIFT_CODE = 101;
    public static final int TYPE_ID_MY_ATTENTION = 102;
    public static final int TYPE_ID_WALLET = 103;
    public static final int TYPE_ID_TASK = 104;
    public static final int TYPE_ID_MSG = 105;
    public static final int TYPE_ID_DOWNLOAD = 106;
    public static final int TYPE_ID_SETTING = 107;
    public static final int TYPE_ID_MY_COUPON = 108;

    public static final int TYPE_ID_DETAIL_BEAN = 113;
    public static final int TYPE_ID_DETAIL_SCORE = 114;
    public static final int TYPE_ID_PROFILE = 115;
    public static final int TYPE_ID_FEEDBACK = 116;
    public static final int TYPE_ID_USERINFO = 117;
    public static final int TYPE_ID_USER_SET_NICK = 118;
    public static final int TYPE_ID_USER_SET_AVATAR = 119;

    /* login activity */
    public static final int TYPE_ID_LOGIN_MAIN = 151;
    public static final int TYPE_ID_OUWAN_LOGIN = 0;
    public static final int TYPE_ID_PHONE_LOGIN = 1;

    /* gift list activity */
    public static final int TYPE_ID_GIFT_LIMIT = 170;
    public static final int TYPE_ID_GIFT_NEW = 171;
    public static final int TYPE_ID_GIFT_LIKE = 172;

    /* web activity */
    public static final int TYPE_ID_LOTTERY = 173;
    public static final int TYPE_SIGN_IN_EVERY_DAY = 174;

    /* game list activity */
    // 新游推荐
    public static final int TYPE_ID_GAME_NEW = 201;
    // 特定类型游戏
    public static final int TYPE_ID_GAME_TYPE = 202;
    // 热门游戏
    public static final int TYPE_ID_GAME_HOT = 203;
    // 游戏搜索
    public static final int TYPE_ID_GAME_SEARCH = 204;

    /* detail activity */
    public static final int TYPE_ID_GIFT_DETAIL = 301;
    public static final int TYPE_ID_GAME_DETAIL = 302;

    /* post list activity */
    public static final int TYPE_ID_POST_OFFICIAL = 401;

    /* post detail activity */
    public static final int TYPE_ID_POST_REPLY_DETAIL = 403;
    public static final int TYPE_ID_POST_COMMENT_DETAIL = 404;

    /* message central activity */
    public static final int TYPE_ID_MSG_NEW_GIFT_NOTIFY = 501;
    public static final int TYPE_ID_MSG_ADMIRE = 502;
    public static final int TYPE_ID_MSG_SYSTEM = 503;
    public static final int TYPE_ID_MSG_COMMENT = 504;

    public static final String CODE_MSG_NEW_GIFT_NOTIFY = "msg_notify";
    public static final String CODE_MSG_ADMIRE = "msg_admire";
    public static final String CODE_MSG_SYSTEM = "msg_system";
    public static final String CODE_MSG_COMMENT = "msg_comment";


    /* my gift list fragment */
    // 已抢
    public static final int TYPE_KEY_SEIZED = 1;
    // 已淘
    public static final int TYPE_KEY_SEARCH = 2;
    // 已过期
    public static final int TYPE_KEY_OVERTIME = 3;
    /* my coupon list fragment */
    // 礼包类型
    public static final int GIFT_TYPE_GIFT = 0;
    // 首充券类型
    public static final int GIFT_TYPE_COUPON = 1;
    // 已抢
    public static final int TYPE_KEY_COUPON_SEIZED = 1;
    // 已预约
    public static final int TYPE_KEY_COUPON_RESERVED = 4;
    // 已过期
    public static final int TYPE_KEY_COUPON_OVERTIME = 3;
}
