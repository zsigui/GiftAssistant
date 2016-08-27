package com.oplay.giftcool.model.data.req;

import com.google.gson.annotations.SerializedName;

/**
 * Created by zsigui on 16-8-27.
 */
public class ReqBindMainAccount {

    @SerializedName("token")
    public String token;

    @SerializedName("phone")
    public String phone;

    @SerializedName("cuid")
    public int cuid;
}
