package com.oplay.giftcool.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by zsigui on 16-11-4.
 */

public class PostCommentList implements Serializable {

    @SerializedName("list")
    public ArrayList<CommentDetailInfo> commentInfos;
}
