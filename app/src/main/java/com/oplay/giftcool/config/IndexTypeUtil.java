package com.oplay.giftcool.config;

/**
 * Created by zsigui on 16-1-16.
 */
public class IndexTypeUtil {

	/*
	 * 下载位置使用参数
	 */
	public static final int TAG_POSITION = 0xFFF11133;
	public static final int TAG_URL = 0xffff1111;
	public static final int TAG_VIEW = 0xffff1144;

	/*
	 * 单纯头部内容的module
	 */
	public static final int ITEM_HEADER = 0;
	public static final int ITEM_NORMAL = 1;
	// 暂未使用
	public static final int ITEM_FOOTER = 2;


	/*
	 * 活动首页
	 */
	public static final int ITEM_POST_HEADER_COUNT = 3;
	public static final int ITEM_POST_HEADER = 0;
	public static final int ITEM_POST_OFFICIAL = 1;
	public static final int ITEM_POST_NOTIFY = 2;

	/*
	 * 游戏精品界面
	 */
	// 首页固定4项
	public static final int ITEM_GAME_SUPER_HEADER_COUNT = 4;
	public static  final int ITEM_GAME_SUPER_TOTAL_COUNT = 5;

	public static  final int ITEM_GAME_BANNER = 0;
	public static final int ITEM_GAME_HOT = 1;
	public static final int ITEM_GAME_RECOMMEND = 2;
	public static final int ITEM_GAME_TITLE = 3;
	public static final int ITEM_GAME_NEW_NORMAL = 4;

	// 游戏类型页面

	public static final int ITEM_GAME_TYPE_HEADER_COUNT = 6;
	public static final int ITEM_GAME_TYPE_GRID_COUNT = 3;
}
