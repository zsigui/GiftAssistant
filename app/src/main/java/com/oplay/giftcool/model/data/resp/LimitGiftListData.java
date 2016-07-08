package com.oplay.giftcool.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author mink
 * @date 2016/3/7
 */
public class LimitGiftListData<T> implements Serializable {

    @SerializedName("data")
    public ArrayList<T> data;

    @SerializedName("page_id")
    public int page;

    @SerializedName("page_size")
    public int pageSize;

    @SerializedName("is_end_page")
    public boolean isEndPage;
}
