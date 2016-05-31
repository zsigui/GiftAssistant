package com.oplay.giftcool.model.data.resp;

import com.google.gson.annotations.SerializedName;

/**
 * Created by zsigui on 15-12-28.
 */
public class IndexGameNew extends GameDownloadInfo {

    // 新增礼包数量
    @SerializedName("new_add_count")
    public int newCount;

    // 拥有礼包总数
    @SerializedName("has_gift_count")
    public int totalCount;

    // 在玩人数
    @SerializedName("plays")
    public int playCount;

    // 主推游戏Banner地址
    @SerializedName("stroll_img_url")
    public String banner;

    // 最新礼包名
    @SerializedName("gift_name")
    public String giftName;


}
