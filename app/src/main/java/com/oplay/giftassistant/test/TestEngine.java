package com.oplay.giftassistant.test;

import com.oplay.giftassistant.ext.retrofit2.http.Cmd;
import com.oplay.giftassistant.model.json.base.JsonReqBase;
import com.oplay.giftassistant.model.json.base.JsonRespBase;

import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.POST;

/**
 * Created by zsigui on 15-12-21.
 */
public interface TestEngine {

	@POST("api")
	@Cmd(TestActivity.TEST_CMD)
	Call<JsonRespBase<String>> testPack(@Body JsonReqBase<String> data);
}
