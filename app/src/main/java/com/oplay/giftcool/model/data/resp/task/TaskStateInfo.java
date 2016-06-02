package com.oplay.giftcool.model.data.resp.task;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 返回的各种任务状态实体
 * <p/>
 * Created by zsigui on 16-4-20.
 */
public class TaskStateInfo implements Serializable {

    /**
     * 签到状态
     */
    @SerializedName("daily_signin")
    public TaskSignFinTask signInState;

    /**
     * 每日抽奖状态
     */
    @SerializedName("daily_lottery")
    public TaskLottery lotteryState;

}
