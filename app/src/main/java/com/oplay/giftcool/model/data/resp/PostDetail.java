package com.oplay.giftcool.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by zsigui on 16-11-3.
 */

public class PostDetail {

    /**
     * 帖子的信息
     */
    @SerializedName("activity_data")
    public PostDetailInfo postInfo;

    /**
     * 游戏信息
     */
    @SerializedName("app_data")
    public GameDownloadInfo gameInfo;

    /**
     * 一级评论信息
     */
    @SerializedName("comment_data")
    public ArrayList<CommentDetailInfo> commentInfos;

    /**
     * 投票信息
     */
    @SerializedName("vote_data")
    public PostVoteInfo voteInfo;
}
