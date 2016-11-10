package com.oplay.giftcool.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by zsigui on 16-11-3.
 */

public class CommentDetail implements Serializable {

    /**
     * 评论楼层层主的评论信息
     */
    @SerializedName("info")
    public CommentDetailInfo commentInfo;

    /**
     * 评论楼层底下评论信息
     */
    @SerializedName("list")
    public ArrayList<CommentDetailListItem> listInfo;
}
