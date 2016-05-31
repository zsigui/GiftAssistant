package com.oplay.giftcool.model.data.req;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zsigui on 15-12-31.
 */
public class ReqPageData implements Serializable {

    // 请求页码，从1开始
    @SerializedName("page_id")
    public int page = 1;

    // 请求项数，默认为10
    @SerializedName("page_size")
    public int pageSize = 20;

    @SerializedName("type")
    public int type;

    @SerializedName("gift_type")
    public int giftType;

    // 请求日期，仅对于获取特定日期数据的有效，否则可忽略
    // 格式：yyyy-MM-dd
    @SerializedName("date")
    public String date;

    @SerializedName("search_key")
    // 搜索请求
    public String searchKey;

    @SerializedName("label_id")
    // 分类ID
    public int labelType;
}
