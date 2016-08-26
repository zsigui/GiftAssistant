package com.oplay.giftcool.model.data.req;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zsigui on 16-8-25.
 */
public class ReqServerInfo implements Serializable {

    @SerializedName("is_focus")
    public int isFocus = 0;

    @SerializedName("start_date")
    public String startDate;

    @SerializedName("offset")
    public int offset;

    @SerializedName("pageSize")
    public int pageSize = 20;
}
