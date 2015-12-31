package com.oplay.giftassistant.engine;

import com.oplay.giftassistant.model.data.req.ReqGiftDetail;
import com.oplay.giftassistant.model.data.req.ReqIndexGift;
import com.oplay.giftassistant.model.data.req.ReqPageData;
import com.oplay.giftassistant.model.data.resp.IndexGameNew;
import com.oplay.giftassistant.model.data.resp.IndexGameSuper;
import com.oplay.giftassistant.model.data.resp.IndexGift;
import com.oplay.giftassistant.model.data.resp.IndexGiftLike;
import com.oplay.giftassistant.model.data.resp.IndexGiftNew;
import com.oplay.giftassistant.model.data.resp.OneTypeGameList;
import com.oplay.giftassistant.model.json.JsonRespLimitGift;
import com.oplay.giftassistant.model.json.base.JsonReqBase;
import com.oplay.giftassistant.model.json.base.JsonRespBase;

import java.util.ArrayList;

import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.POST;

/**
 * Created by zsigui on 15-12-16.
 */
public interface NetEngine {

	/**
	 * 获取 首页-礼包 页面的数据
	 */
	@POST("gift_index")
	Call<JsonRespBase<IndexGift>> obtainIndexGift(@Body JsonReqBase<ReqIndexGift> reqData);

	/**
	 * 获取 首页-礼包-猜你喜欢 页面的数据
	 */
	@POST("guess_you_like_list")
	Call<JsonRespBase<ArrayList<IndexGiftLike>>> obtainGiftLike(@Body JsonReqBase<ReqIndexGift> reqData);

	/**
	 * 获取 首页-礼包-每日限量礼包 页面的数据
	 */
	@POST("api/index/gift/limit/all")
	Call<JsonRespLimitGift> obtainGiftLimit(@Body JsonReqBase<String> reqData);

	/**
	 * 获取 首页-礼包-新鲜出炉礼包 页面的数据
	 */
	@POST("api/index/gift/new/all")
	Call<JsonRespLimitGift> obtainGiftNew(@Body JsonReqBase<String> reqData);

    /**
     * 获取 礼包详情 页面的数据
     */
    @POST("api/index/gift/detail")
    Call<JsonRespBase<IndexGiftNew>> obtainGiftDetail(@Body JsonReqBase<ReqGiftDetail> reqData);

	/**
	 * 获取 首页-游戏-精品 页面的数据
	 */
	@POST("api/index/game/super")
	Call<JsonRespBase<IndexGameSuper>> obtainIndexGameSuper(@Body JsonReqBase<String> reqData);

	/**
	 * 获取 首页-游戏-榜单 页面的数据
	 */
	@POST("api/index/game/notice")
	Call<JsonRespBase<OneTypeGameList<IndexGameNew>>> obtainIndexGameNotice(@Body JsonReqBase<ReqPageData> reqData);
}
