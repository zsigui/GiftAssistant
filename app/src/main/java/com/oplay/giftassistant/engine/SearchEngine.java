package com.oplay.giftassistant.engine;

import com.oplay.giftassistant.model.json.JsonRespSearchData;
import com.oplay.giftassistant.model.json.JsonRespSearchPrompt;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/20
 */
public interface SearchEngine {

    @GET("gift_search")
    Call<JsonRespSearchPrompt> getSearchPrompt(@Query("keyword") String keyword);

	@GET("gift_search")
	Call<JsonRespSearchData> getSearchData(@Query("keyword") String keyword);
}
