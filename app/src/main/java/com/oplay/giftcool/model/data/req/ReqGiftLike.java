package com.oplay.giftcool.model.data.req;

import com.google.gson.annotations.SerializedName;

import java.util.HashSet;

/**
 * Created by zsigui on 16-9-5.
 */
public class ReqGiftLike {

    @SerializedName("games")
    public HashSet<String> appNames;

    @SerializedName("packages")
    public HashSet<String> packageName;

    @SerializedName("page_id")
    public int page = 1;

    @SerializedName("page_size")
    public int pageSize = 20;
}
