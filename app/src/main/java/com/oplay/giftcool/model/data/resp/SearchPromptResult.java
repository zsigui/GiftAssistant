package com.oplay.giftcool.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/20
 */
public class SearchPromptResult implements Serializable {

	@SerializedName("search_word")
	public String keyword;

	@SerializedName("app_dict_list")
	public ArrayList<PromptData> promptList;
}
