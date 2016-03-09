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
public class InitAppConfig implements Serializable{
	@SerializedName("qq_v2")
	public ArrayList<InitQQ> qqInfo;

	@SerializedName("start_img")
	public String startImgUrl;

	@SerializedName("is_show_download")
	public boolean isShowDownload;

	/**
	 * 是否具备抽奖功能
	 */
	@SerializedName("activity")
	public IndexBanner broadcastBanner;
}
