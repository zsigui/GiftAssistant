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

	@SerializedName("keyword")
	public String keyword;

	@SerializedName("prompts")
	public ArrayList<String> promptList;
}
