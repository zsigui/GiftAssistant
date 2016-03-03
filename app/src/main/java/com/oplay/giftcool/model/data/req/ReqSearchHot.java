package com.oplay.giftcool.model.data.req;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * @author JackieZhuang
 * @email zsigui@foxmail.com
 * @date 2016/3/5
 */
public class ReqSearchHot implements Serializable {

    @SerializedName("history_data")
    public List<String> data;

    @SerializedName("page_id")
    public int pageId;

    @SerializedName("page_size")
    public int pageSize;

}
