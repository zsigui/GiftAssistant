package com.oplay.giftcool.model.data.resp.message;

import java.io.Serializable;

/**
 * Created by zsigui on 16-4-17.
 */
public class CentralHintMessage implements Serializable {


	/**
	 * 消息类型代号
	 */
	public String code;

	/**
	 * 消息图标
	 */
	public int icon;

	/**
	 * 消息标题
	 */
	public String title;

	/**
	 * 显示内容
	 */
	public String content;

	/**
	 * 未读消息数量
	 */
	public int count;

	/**
	 * 是否显示未读消息数量，默认显示
	 */
	public boolean showCount = true;

	public CentralHintMessage() {
	}

	public CentralHintMessage(String code, int icon, String title, String content, int count) {
		this(code, icon, title, content, count, true);
	}

	public CentralHintMessage(String code, int icon, String title, String content, int count, boolean showCount) {
		this.code = code;
		this.icon = icon;
		this.title = title;
		this.content = content;
		this.count = count;
		this.showCount = showCount;
	}
}
