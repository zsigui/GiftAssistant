package com.oplay.giftcool.model.data.resp.task;

import com.google.gson.annotations.SerializedName;

/**
 * Created by zsigui on 16-7-28.
 */
public class ShareTask {

    /**
     * 分享弹窗的标题
     */
    @SerializedName("dialog_title")
    public String shareDialogTitle;

    /**
     * 分享的标题
     */
    @SerializedName("title")
    public String title;

    /**
     * 分享的内容
     */
    @SerializedName("desc")
    public String desc;

    /**
     * 分享的图标
     */
    @SerializedName("icon")
    public String icon;

    /**
     * 分享的URL
     */
    @SerializedName("share_url")
    public String url;
}
