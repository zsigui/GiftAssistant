package com.oplay.giftcool.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zsigui on 16-8-26.
 */
public class BindAccount implements Serializable {

    /**
     * 用户ID
     */
    @SerializedName("cuid")
    public int uid;

    /**
     * 用户昵称
     */
    @SerializedName("nick")
    public String nickname;

    /**
     * 用户名
     */
    @SerializedName("username")
    public String username;

    /**
     * 注册账号时的游戏名
     */
    @SerializedName("reg_app_name")
    public String regAppName;
}
