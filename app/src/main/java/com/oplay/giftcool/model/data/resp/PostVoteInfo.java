package com.oplay.giftcool.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by zsigui on 16-11-3.
 */

public class PostVoteInfo implements Serializable {

    /**
     * 投票主题ID
     */
    @SerializedName("id")
    public int id;

    /**
     * 是否完成了本次投票
     */
    @SerializedName("is_vote")
    public boolean isVote;

    /**
     * 投票列表
     */
    @SerializedName("item_list")
    public ArrayList<PostVoteItem> voteItems;

    /**
     * 投票的主题
     */
    @SerializedName("title")
    public String title;

    /**
     * 投票的类型 0 单选 1 多选
     */
    @SerializedName("type")
    public int type;

    /**
     * 总的投票数量
     */
    @SerializedName("vote_amount")
    public int voteCount;
}
