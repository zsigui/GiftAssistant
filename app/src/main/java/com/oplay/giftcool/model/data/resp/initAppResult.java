package com.oplay.giftcool.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * initAppResult
 *
 * @author zacklpx
 *         date 16-1-22
 *         description
 */
public class initAppResult implements Serializable {

	@SerializedName("is_show_download")
	boolean isShowDownload;

	@SerializedName("qq_info")
	ArrayList<String> qqInfo;

	@SerializedName("update")
	UpdateInfo updateInfo;
}
