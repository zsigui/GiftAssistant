package com.oplay.giftcool.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.util.HashSet;

/**
 * Created by zsigui on 16-9-5.
 */
public class GiftLikeList extends OneTypeDataList<IndexGiftLike> {

    @SerializedName("names")
    public HashSet<String> appNames;

    @SerializedName("packages")
    public HashSet<String> packageNames;
}
