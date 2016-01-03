package com.oplay.giftassistant.model.data.req;

import java.io.Serializable;

/**
 * Created by zsigui on 15-12-31.
 */
public class ReqPageData implements Serializable {

	// 请求页码，从1开始
	public int page = 1;

	// 请求项数，默认为10
	public int pageSize = 10;

	// 请求日期，仅对于获取特定日期数据的有效，否则可忽略
    // 格式：yyyy-MM-dd
	public String date;
}
