package com.oplay.giftcool.model.data.resp.message;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zsigui on 16-8-22.
 */
public class AwardNotify implements Serializable {

    /**
     * 奖励描述
     */
    @SerializedName("content")
    public String description;

    /**
     * 奖励类型，礼包类型需要再次调取接口获取礼包码
     *
     * 0 礼包 1 金币 2 偶玩豆 3 其他
     */
    @SerializedName("type")
    public int type;

    /**
     * 显示奖励的图标
     */
    @SerializedName("icon")
    public String icon;

    /**
     * 奖励ID
     */
    @SerializedName("plan_id")
    public int id;
}
