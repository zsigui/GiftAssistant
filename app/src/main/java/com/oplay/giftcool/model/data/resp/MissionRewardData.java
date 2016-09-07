package com.oplay.giftcool.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Locale;

/**
 * Created by zsigui on 16-4-12.
 */
public class MissionRewardData implements Serializable {

    @SerializedName("code")
    public String code;

    @SerializedName("name")
    public String displayName;

    @SerializedName("finish_time")
    public String finishTime;

    @SerializedName("reward_points")
    public int rewardPoint;

    @Override
    public String toString() {
        return String.format(Locale.CHINA, "奖励名称: %s ; 数量: %d", displayName, rewardPoint);
    }
}
