package com.oplay.giftcool.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by zsigui on 16-11-3.
 */

public class CommentDetailInfo implements Serializable{

    /**
     * 评论ID
     */
    @SerializedName("comment_id")
    public int id;

    /**
     * 评论人的头像
     */
    @SerializedName("avatar")
    public String avatar;

    /**
     * 该评论底下其他评论数量
     */
    @SerializedName("comment_count")
    public int commentCount;

    /**
     * 评论内容
     */
    @SerializedName("content")
    public String content;

    /**
     * 评论时间
     */
    @SerializedName("create_time")
    public String pubTime;

    /**
     * 所在楼层
     */
    @SerializedName("floor")
    public int floor;

    /**
     * 评论图片，缩略图
     */
    @SerializedName("imgs")
    public ArrayList<String> thumbImgs;

    /**
     * 评论图片，原图
     */
    @SerializedName("full_imgs")
    public ArrayList<String> Imgs;

    /**
     * 该评论被点赞数
     */
    @SerializedName("like_count")
    public int likeCount;

    /**
     * 评论用户的昵称
     */
    @SerializedName("nick")
    public String nick;

    /**
     * 评论用户的id
     */
    @SerializedName("user_id")
    public int userId;

    /**
     * 是否点赞了
     */
    @SerializedName("is_like")
    public boolean isLike;
}
