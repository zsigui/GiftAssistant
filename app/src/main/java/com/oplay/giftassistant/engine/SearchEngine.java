package com.oplay.giftassistant.engine;

import com.oplay.giftassistant.model.data.SearchPromptResult;
import com.oplay.giftassistant.model.json.base.JsonBaseImpl;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/20
 */
public interface SearchEngine {

    @GET("game/search")
    Call<JsonBaseImpl<SearchPromptResult>> getSearchPromt(@Query("keyword") String keyword);
}
