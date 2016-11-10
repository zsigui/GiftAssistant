package com.oplay.giftcool.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zsigui on 16-11-3.
 */

public class PostVoteItem implements Serializable {

    /**
     * 投票项ID
     */
    @SerializedName("item_id")
    public int id;

    /**
     * 获得的投票数
     */
    @SerializedName("item_amount")
    public int amount;

    /**
     * 是否投了该选项
     */
    @SerializedName("item_vote")
    public boolean isVote;

    /**
     * 投票项内容
     */
    @SerializedName("name")
    public String name;
}
