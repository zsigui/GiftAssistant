package com.oplay.giftcool.model.data.resp;

import com.google.gson.annotations.SerializedName;

/**
 * Created by zsigui on 16-11-3.
 */

public class CommentDetailListItem extends CommentDetailInfo {

    /**
     * 被@的用户昵称
     */
    @SerializedName("at_nick")
    public String atNick;

    /**
     * 被@的用户ID
     */
    @SerializedName("at_user_id")
    public int atUserId;

    /**
     * 被@的评论的ID
     */
    @SerializedName("sub_comment_id")
    public int subCommentId;
}
