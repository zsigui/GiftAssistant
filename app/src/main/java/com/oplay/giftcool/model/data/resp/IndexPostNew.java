package com.oplay.giftcool.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 活动首页内容实体
 * <p/>
 * Created by zsigui on 16-4-6.
 */
public class IndexPostNew implements Serializable {

    /**
     * 活动ID
     */
    @SerializedName("activity_id")
    public int id;

    /**
     * 活动小图
     */
    @SerializedName("icon")
    public String img;

    /**
     * 活动大图
     */
    @SerializedName("img")
    public String banner;

    /**
     * 活动标题
     */
    @SerializedName("title")
    public String title;

    /**
     * 活动状态，暂时初定 0 进行中 1 未开始 2 已结束
     */
    @SerializedName("status")
    public int state;

    /**
     * 活动内容
     */
    @SerializedName("content")
    public String content;

    /**
     * 活动开始时间
     */
    @SerializedName("start_time")
    public String startTime;

    /**
     * 显示类型，0~100 预留服务端内容配置(暂时 0 小图类型 1 中图类型) 101 签到等固定头 102 官方活动标题 103 活动快讯标题
     */
    @SerializedName("display_type")
    public int showType;

    public IndexPostNew() {
    }

    public IndexPostNew(int showType) {
        this.showType = showType;
    }
}
