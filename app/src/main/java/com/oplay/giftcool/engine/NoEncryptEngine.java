package com.oplay.giftcool.engine;

import com.oplay.giftcool.config.NetUrl;
import com.oplay.giftcool.model.data.resp.GameDetail;
import com.oplay.giftcool.model.json.base.JsonRespBase;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * 设置不需要加密的连接请求(不适用json方式)
 *
 * Created by zsigui on 16-4-13.
 */
public interface NoEncryptEngine {

	/**
	 * 上传回复，带图片
	 */
//	@Multipart
//	@POST(NetUrl.POST_REPLY)
//	Call<JsonRespBase<Void>> postReply(@PartMap HashMap<String, RequestBody> pics);
//
//	@POST(NetUrl.POST_REPLY)
//	Call<JsonRespBase<Void>> commitReply(@Body JsonReqBase<ReqCommitReply> reqData);

	@POST(NetUrl.POST_REPLY)
	Call<JsonRespBase<Void>> commitReply(@Body String reqData);

	@GET("app_detail_web/")
	Call<JsonRespBase<GameDetail>> obtainGameDetail(@Query("app_id") int appId);
}
