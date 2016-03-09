package com.oplay.giftcool.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * @author JackieZhuang
 * @email zsigui@foxmail.com
 * @date 2016/3/3
 */
public class PromptData implements Serializable {

    @SerializedName("app_id")
    public int appId;

    @SerializedName("app_name")
    public String appName;
}
