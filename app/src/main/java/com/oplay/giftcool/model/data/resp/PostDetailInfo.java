package com.oplay.giftcool.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zsigui on 16-11-3.
 */

public class PostDetailInfo implements Serializable{

    /**
     * 活动ID
     */
    @SerializedName("activity_id")
    public int id;

    /**
     * 活动标题
     */
    @SerializedName("title")
    public String title;

    /**
     * 活动内容
     */
    @SerializedName("content")
    public String content;


    /**
     * 评论数量
     */
    @SerializedName("comment_count")
    public int commentCount;

    /**
     * 浏览数
     */
    @SerializedName("page_view")
    public int skipCount;

    /**
     * 活动开始时间
     */
    @SerializedName("start_time")
    public String pubTime;


    /**
     * 活动状态，暂时初定 0 进行中 1 未开始 2 已结束
     */
    @SerializedName("status")
    public int state;

    /**
     * 资讯类型： 0 普通资讯 1 官方资讯
     */
    @SerializedName("type")
    public int type;

    /**
     * 活动大图
     */
    @SerializedName("img")
    public String img;

    /**
     * 发贴用户昵称
     */
    @SerializedName("nick")
    public String nick;

    /**
     * 发贴用户类型： 0 普通用户 1 管理员
     */
    @SerializedName("user_type")
    public int userType;

    /**
     * 发贴用户签名
     */
    @SerializedName("signature")
    public String signature;

    /**
     * 发贴用户ID
     */
    @SerializedName("user_id")
    public int userId;

    /**
     * 发帖用户头像
     */
    @SerializedName("avatar")
    public String icon;
}
