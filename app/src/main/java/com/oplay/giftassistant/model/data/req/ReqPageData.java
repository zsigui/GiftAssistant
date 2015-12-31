package com.oplay.giftassistant.model.data.req;

import java.io.Serializable;

/**
 * Created by zsigui on 15-12-31.
 */
public class ReqPageData implements Serializable {

	// 请求页码
	public int page;

	// 请求项数
	// public int pageSize;

	// 请求日期，仅对于获取特定日期数据的有效，否则可忽略
	public String date;
}
