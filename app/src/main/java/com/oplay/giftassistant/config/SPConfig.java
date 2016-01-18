package com.oplay.giftassistant.config;

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
	public static final String SALT_USER_INFO = "abcdef33575471";
	public static final String KEY_USER_INFO = "kljizckuiqew";
	public static final String KEY_USER_ID = "uid";
	public static final String KEY_USERNAME = "un";
	public static final String KEY_SESSION = "ss";
	public static final String KEY_PHONE = "mp";
	public static final String KEY_PROFILE_URL = "pu";


    /* 获取搜索记录 */
    public static final String SP_SEARCH_FILE = "12341bbedf";
    public static final String KEY_SEARCH_INDEX = "index_search_history";

	/* 全局设置记录 */
	public static final String SP_APP_CONFIG_FILE = "aaaa";
	public static final String KEY_AUTO_CHECK_UPDATE = "auto_check_update";
	public static final String KEY_AUTO_DELETE_APK = "auto_delete_apk";
	public static final String KEY_ACCEPT_PUSH = "auto_accept_push";
	public static final String KEY_AUTO_INSTALL = "auto_install_apk";
	public static final String KEY_IS_ALLOW_DOWNLOAD = "allow_download";
	public static final String KEY_IS_SAVE_FLOW = "save_flow";
	public static final String KEY_IS_PLAY_DOWNLOAD_COMPLETE = "play_sound_on_download_complete";
}
