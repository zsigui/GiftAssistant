package com.oplay.giftcool.model.data.resp;

import com.google.gson.annotations.SerializedName;

/**
 * 广告闪屏信息类
 *
 * 1309添加 <br />
 * Created by zsigui on 16-8-3. <br />
 */
public class AdInfo {

    /**
     * 操作跳转的uri
     */
    @SerializedName("uri")
    public String uri;

    /**
     * 广告播放时间，0时表示不显示，该时间值默认>=0，单位:s
     */
    @SerializedName("display_time")
    public int displayTime;

    /**
     * 闪屏图片地址
     */
    @SerializedName("img")
    public String img;

    /**
     * 是否显示跳过按钮，默认true
     */
    @SerializedName("show_pass")
    public boolean showPass = true;

}
