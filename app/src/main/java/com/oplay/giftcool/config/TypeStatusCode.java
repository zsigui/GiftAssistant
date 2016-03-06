package com.oplay.giftcool.config;

/**
 * 各种类型状态，常见的是否状态
 *
 * Created by zsigui on 16-3-8.
 */
public abstract class TypeStatusCode {

	// 关注状态
	public static final int FOCUS_OFF = 0;
	public static final int FOCUS_ON = 1;

	// 推送消息状态
	public static final int PUSH_UNREAD = 0;
	public static final int PUSH_READED = 1;
}
