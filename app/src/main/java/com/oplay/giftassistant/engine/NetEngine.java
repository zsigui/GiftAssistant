package com.oplay.giftassistant.engine;

import com.oplay.giftassistant.model.DataModel;
import com.oplay.giftassistant.model.UserModel;
import com.oplay.giftassistant.model.data.req.ReqIndexGift;
import com.oplay.giftassistant.model.data.resp.IndexGift;
import com.oplay.giftassistant.model.json.base.JsonReqBase;
import com.oplay.giftassistant.model.json.base.JsonRespBase;

import java.util.List;

import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;

/**
 * Created by zsigui on 15-12-16.
 */
public interface NetEngine {

	@GET("test/getusers/new")
	Call<DataModel<List<UserModel>>> obtainNewUser();

	@GET("test/getusers")
	Call<DataModel<List<UserModel>>> obtainAllUser();

	/**
	 * 获取 首页-礼包 页面的数据
	 */
	@POST("api/index/gift")
	Call<JsonRespBase<IndexGift>> obtainIndexGift(@Body JsonReqBase<ReqIndexGift> reqData);
}
