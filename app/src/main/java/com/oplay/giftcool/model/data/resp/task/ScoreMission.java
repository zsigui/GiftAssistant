package com.oplay.giftcool.model.data.resp.task;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 任务模型类
 * <p/>
 * Created by zsigui on 16-1-7.
 */
public class ScoreMission implements Serializable {

    /**
     * 活动代号
     */
    @SerializedName("code")
    public String code;

    /**
     * 每日每用户可以完成次数
     */
    @SerializedName("daily_limit")
    public int dailyLimit;

    /**
     * 总的限制次数
     */
    @SerializedName("limit")
    public int totalLimit;

    /**
     * 图片URL，为空采用预留
     */
    @SerializedName("icon")
    public String icon;

    /**
     * 当icon为空时，采用此备选
     */
    public int iconAlternate;

    /**
     * 任务是否完成，0未完成 1完成
     */
    @SerializedName("is_completed")
    public int isCompleted;

    /**
     * 任务名称
     */
    @SerializedName("name")
    public String name;

    /**
     * 任务描述
     */
    @SerializedName("description")
    public String description;

    /**
     * 任务完成奖励
     */
    @SerializedName("reward")
    public int reward;

    /**
     * 用户今天完成了多少次，每次可完成多次任务有效
     */
    @SerializedName("today_complete_count")
    public int todayCompleteCount = 0;

    /**
     * 用户完成该任务的次数
     */
    @SerializedName("complete_count")
    public int totalCompleteCount = 0;

    /**
     * 行为类型：1 打开应用特定位置 2 执行特定代码 3 下载并打开其他应用
     */
    @SerializedName("action_type")
    public int actionType;


    /**
     * 行为额外信息
     */
    @SerializedName("action_info")
    public String actionInfo;

    /**
     * 显示用于判断头
     */
    public boolean isHeader = false;
}
