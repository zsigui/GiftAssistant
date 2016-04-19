package com.oplay.giftcool.engine;

import com.oplay.giftcool.config.NetUrl;
import com.oplay.giftcool.model.data.req.ReqCommitReply;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;

import java.util.HashMap;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PartMap;

/**
 * 设置不需要加密的连接请求(不适用json方式)
 *
 * Created by zsigui on 16-4-13.
 */
public interface NoEncryptEngine {

	/**
	 * 上传回复，带图片
	 */
	@Multipart
	@POST(NetUrl.POST_REPLY)
	Call<JsonRespBase<Void>> postReply(@PartMap HashMap<String, RequestBody> pics);

	@POST(NetUrl.POST_REPLY)
	Call<JsonRespBase<Void>> commitReply(@Body JsonReqBase<ReqCommitReply> reqData);

	@POST(NetUrl.POST_REPLY)
	Call<JsonRespBase<Void>> commitReply(@Body String reqData);
}
