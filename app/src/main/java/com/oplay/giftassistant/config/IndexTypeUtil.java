package com.oplay.giftassistant.config;

/**
 * Created by zsigui on 16-1-16.
 */
public class IndexTypeUtil {

	/*
	 * 下载位置使用参数
	 */
	public static final int TAG_POSITION = 0xFFF11133;
	public static final int TAG_URL = 0xffff1111;

	/*
	 * 单纯头部内容的module
	 */
	public static final int ITEM_HEADER = 0;
	public static final int ITEM_NORMAL = 1;
	// 暂未使用
	public static final int ITEM_FOOTER = 2;


	/*
	 * 游戏精品界面
	 */
	// 首页固定4项
	public static  final int ITEM_INDEX_COUNT = 4;

	public static  final int ITEM_BANNER = 0;
	public static final int ITEM_HOT = 1;
	public static final int ITEM_RECOMMEND = 2;
	public static final int ITEM_NEW = 3;
}
