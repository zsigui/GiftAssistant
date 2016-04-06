package com.oplay.giftcool.model.data.resp;

import java.io.Serializable;

/**
 * 活动首页内容实体
 *
 * Created by zsigui on 16-4-6.
 */
public class IndexPostNew implements Serializable {

	/**
	 * 活动ID
	 */
	public int id;

	/**
	 * 活动首页图
	 */
	public String img;

	/**
	 * 是否显示新活动标志
	 */
	public boolean isNew;

	/**
	 * 活动标题
	 */
	public String title;

	/**
	 * 活动状态，暂时初定 0 已结束 1 进行中
	 */
	public int state;
}
