package com.oplay.giftassistant.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/20
 */
public class SearchPromptResult implements Serializable {

	@SerializedName("keyword")
	public String keyword;

	@SerializedName("prompts")
	public List<String> promtList;
}
