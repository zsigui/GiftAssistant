package com.oplay.giftcool.model.data.resp.message;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 消息中心未读消息数量
 * <p/>
 * Created by zsigui on 16-4-21.
 */
public class MessageCentralUnread implements Serializable {

    /**
     * 未读点赞数量
     */
    @SerializedName("uncheck_like")
    public int unreadAdmireCount;

    /**
     * 未读评论数量
     */
    @SerializedName("uncheck_comment")
    public int unreadCommentCount;

    /**
     * 未读系统消息数量
     */
    @SerializedName("uncheck_system_message")
    public int unreadSystemCount;

    /**
     * 未读礼包消息数量
     */
    @SerializedName("uncheck_jpush_message")
    public int unreadNewGiftCount;

    /**
     * 最新的点赞用户消息
     */
    @SerializedName("like_content")
    public String newestAdmire;

    /**
     * 最新的评论消息
     */
    @SerializedName("comment_content")
    public String newestComment;

    /**
     * 最新的系统消息
     */
    @SerializedName("system_message_content")
    public String newestSystem;

    /**
     * 最新的礼包通知消息
     */
    @SerializedName("jpush_message_content")
    public String newestGift;

}
