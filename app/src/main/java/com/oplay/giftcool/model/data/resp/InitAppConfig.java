package com.oplay.giftcool.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * InitAppConfig
 *
 * @author zacklpx
 *         date 16-1-24
 *         description
 */
public class InitAppConfig implements Serializable {
    @SerializedName("qq_v2")
    public ArrayList<InitQQ> qqInfo;

    @SerializedName("start_img")
    public String startImgUrl;

    @SerializedName("is_show_download")
    public boolean isShowDownload;

    /**
     * 启动页面活动图
     */
    @SerializedName("activity")
    public IndexBanner broadcastBanner;

    /**
     * 手机登录的样式，0 旧版 1 新版
     */
    @SerializedName("phone_login_ui_type")
    public int phoneLoginType;

    /**
     * 设置推送的sdk，0 全部 1 小米推送 2 极光推送 <br />
     */
    @SerializedName("push_sdk")
    public int pushSdk;

    /**
     * 广告闪屏信息
     */
    @SerializedName("ad_splash")
    public AdInfo adInfo;

    /**
     * 对 bind_ouwan_status = 0 的用户是否需要弹出设置用户名密码页面， 0：不需要绑定，1：需要绑定，不可跳过，2：需要绑定，但可以跳过
     */
    @SerializedName("setup_ouwan_account")
    public int setupOuwanAccount;
}
