package com.oplay.giftcool.model.data.resp.task;

import com.google.gson.annotations.SerializedName;

/**
 * Created by zsigui on 16-6-2.
 */
public class TaskLottery {

    /**
     * 下次抽奖花费偶玩豆数量
     */
    @SerializedName("next_draw_cost")
    public int nextCost;

    /**
     * 剩余免费抽奖次数
     */
    @SerializedName("remain_free_draw_times")
    public int remainFreeCount;

    /**
     * 剩余付费抽奖次数
     */
    @SerializedName("remain_pay_draw_times")
    public int remianPayCount;
}
