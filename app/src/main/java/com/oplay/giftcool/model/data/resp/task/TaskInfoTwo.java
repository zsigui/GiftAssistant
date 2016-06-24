package com.oplay.giftcool.model.data.resp.task;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 任务额外信息类型二
 * <p/>
 * Created by zsigui on 16-4-15.
 */
public class TaskInfoTwo implements Serializable {

    @SerializedName("type")
    public int type;

    @SerializedName("data")
    public String data;

}
