package com.oplay.giftassistant.test;

import com.oplay.giftassistant.model.DataModel;

import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.POST;

/**
 * Created by zsigui on 15-12-21.
 */
public interface TestEngine {

	@POST("api")
	Call<DataModel<String>> testPack(@Body DataModel<String> data);
}
