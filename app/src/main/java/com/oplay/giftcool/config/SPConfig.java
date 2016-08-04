package com.oplay.giftcool.config;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/19
 */
public class SPConfig {

    /* 加密盐值,对于存储于SP的敏感数据进行加密  */
    public static final String SP_SALT = "kjaldfaier1345kjv";

    /* 获取闪屏图片地址的键值 */
    public static final String SP_CACHE_FILE = "ab2331ff";
    public static final String KEY_SPLASH_URL = "avatar";

    /* 获取用户信息,个人信息在SP中存储通过salt进行加密 */
    public static final String SP_USER_INFO_FILE = "qerkj21";
    public static final String SALT_USER_INFO = "ss";
    public static final String KEY_USER_INFO = "kljizckuiqew";
    public static final String KEY_LOGIN_LATEST_OPEN_TIME = "login_latest_open_time";
    // 上次开启应用时间
//	public static final String KEY_DOWNLOAD_LAST_TIME = "download_last_time";
//	public static final String KEY_SHARE_LIMIT_LAST_TIME = "share_limit_last_time";
//	public static final String KEY_SHARE_NORMAL_LAST_TIME = "share_normal_last_time";
//	public static final String KEY_SEARCH_LAST_TIME = "search_last_time";
//	public static final String KEY_BUY_BY_BEAN_LAST_TIME = "buy_by_bean_last_time";
    public static final String KEY_LAST_OPEN_APP_TIME = "last_open_app_time";
    public static final String KEY_LAST_PUSH_TIME = "last_push_message_time";


    /* 已输入账号资料 */
    public static final String SP_LOGIN_FILE = "loginfile1234a";
    public static final String KEY_LOGIN_OUWAN = "login_ouwan";
    public static final String KEY_LOGIN_PHONE = "login_phone";

    /* 获取搜索记录 */
    public static final String SP_SEARCH_FILE = "12341bbedf";
    public static final String KEY_SEARCH_INDEX = "index_search_history";

    /* 全局设置记录 */
    public static final String SP_APP_CONFIG_FILE = "aaaa";
    public static final String KEY_CHANNEL = "lyn3fn";
    public static final String KEY_AUTO_CHECK_UPDATE = "auto_check_update";
    public static final String KEY_AUTO_FOCUS = "auto_focus";
    public static final String KEY_REMEMBER_PWD = "remember_pwd";
    public static final String KEY_AUTO_DELETE_APK = "auto_delete_apk";
    public static final String KEY_ACCEPT_PUSH = "auto_accept_push";
    public static final String KEY_AUTO_INSTALL = "auto_install_apk";
    public static final String KEY_IS_ALLOW_DOWNLOAD = "allow_download";
    public static final String KEY_IS_SAVE_FLOW = "save_flow";
    public static final String KEY_IS_PLAY_DOWNLOAD_COMPLETE = "play_sound_on_download_complete";
    public static final String KEY_STORE_VER = "store_version";
    public static final String KEY_IS_READ_ATTENTION = "read_attention";

    /* 应用设置 */
    public static final String SP_APP_DEVICE_FILE = "deviceadf";
    public static final String KEY_TEST_REQUEST_URI = "test_request_uri";
    public static final String KEY_SOFT_INPUT_HEIGHT = "soft_input_height";

    /* 应用信息 */
    public static final String SP_APP_INFO_FILE = "infofile";
    public static final String KEY_TODAY_DOWNLOAD_TASK = "current_download_task";
    public static final String KEY_MSG_CENTRAL_LIST = "message_central_list";
    public static final String KEY_INSTALL_APP_NAMES = "app_install_names";
    public static final String KEY_AD_SPLASH_INFO = "ad_splash_info";
}
