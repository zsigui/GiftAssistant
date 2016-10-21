package com.oplay.giftcool.model.data.req;

import com.google.gson.annotations.SerializedName;

/**
 * Created by zsigui on 16-1-18.
 */
public class ReqTaskReward {

    /**
     * 任务代号
     */
    @SerializedName("code")
    public String code;

    /**
     * 是否直接HTTP返回通知不走SocketIO
     */
    @SerializedName("reply_notify")
    public int replyNotify;

    /**
     * 试玩游戏需要传特定ID
     */
    @SerializedName("app_id")
    public String appId;

    /**
     * 分享特定活动ID
     */
    @SerializedName("activity_id")
    public String activityId;

    /**
     * 当任务为签到时写入，空则取今天
     */
    @SerializedName("date")
    public String date;

}
