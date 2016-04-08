package com.oplay.giftcool.model.data.resp;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 首页活动返回数据实体
 * Created by zsigui on 16-4-8.
 */
public class IndexPost implements Serializable {

	/**
	 * 官方活动
	 */
	public ArrayList<IndexPostNew> officialData;

	/**
	 * 游戏快讯
	 */
	public ArrayList<IndexPostNew> notifyData;
}
