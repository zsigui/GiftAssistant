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
    public static final String KEY_SPLASH_URL = "img";

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
}
