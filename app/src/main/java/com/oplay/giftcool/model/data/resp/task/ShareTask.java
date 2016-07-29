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
     * 分享的URL <br />
     * 对于纯图片类型，此值为要分享的图片http(s)地址; <br />
     * 对于其他类型，此值为要跳转的网页链接地址 <br />
     */
    @SerializedName("share_url")
    public String url;

    /**
     * 分享的内容类型 <br />
     * 对于微信/朋友处理 : 0/x-网页(图文) 1-文字 2-图片 3-音乐 4-视频 <br />
     * 对于QQ处理 : 0/1/x-图文 2-图片 3/4-音频 <br />
     * 对于QQ空间处理 : 都为图文 <br />
     */
    @SerializedName("content_type")
    public int contentType;
}
