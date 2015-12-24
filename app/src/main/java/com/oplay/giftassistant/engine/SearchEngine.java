package com.oplay.giftassistant.engine;

import com.oplay.giftassistant.ext.retrofit2.http.Cmd;
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

    @GET("search")
	@Cmd(2001)
    Call<JsonRespSearchPrompt> getSearchPromt(@Query("keyword") String keyword);

	@GET("search")
	@Cmd(2002)
	Call<JsonRespSearchData> getSearchData(@Query("keyword") String keyword);
}
