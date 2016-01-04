package com.oplay.giftassistant.engine;

import com.oplay.giftassistant.config.NetUrl;
import com.oplay.giftassistant.model.data.req.ReqGiftDetail;
import com.oplay.giftassistant.model.data.req.ReqIndexGift;
import com.oplay.giftassistant.model.data.req.ReqPageData;
import com.oplay.giftassistant.model.data.resp.IndexGameNew;
import com.oplay.giftassistant.model.data.resp.IndexGameSuper;
import com.oplay.giftassistant.model.data.resp.IndexGift;
import com.oplay.giftassistant.model.data.resp.IndexGiftLike;
import com.oplay.giftassistant.model.data.resp.IndexGiftNew;
import com.oplay.giftassistant.model.data.resp.OneTypeDataList;
import com.oplay.giftassistant.model.json.JsonRespGiftList;
import com.oplay.giftassistant.model.json.base.JsonReqBase;
import com.oplay.giftassistant.model.json.base.JsonRespBase;

import java.util.ArrayList;

import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.POST;
import retrofit.http.Url;

/**
 * Created by zsigui on 15-12-16.
 */
public interface NetEngine {

	/**
	 * 获取 首页-礼包 页面的数据
	 */
	@POST(NetUrl.GIFT_GET_INDEX)
	Call<JsonRespBase<IndexGift>> obtainIndexGift(@Body JsonReqBase<ReqIndexGift> reqData);

	/**
	 * 获取 首页-礼包-猜你喜欢 页面的数据
	 */
	@POST(NetUrl.GIFT_GET_ALL_LIKE)
	Call<JsonRespBase<ArrayList<IndexGiftLike>>> obtainGiftLike(@Body JsonReqBase<ReqIndexGift> reqData);

	/**
	 * 获取 首页-礼包-每日限量礼包 页面的数据
	 */
	@POST(NetUrl.GIFT_GET_ALL_LIMIT)
	Call<JsonRespGiftList> obtainGiftLimit(@Body JsonReqBase<String> reqData);

	/**
	 * 获取 首页-礼包-新鲜出炉礼包 页面的数据
	 */
	@POST(NetUrl.GIFT_GET_ALL_NEW)
	Call<JsonRespGiftList> obtainGiftNew(@Body JsonReqBase<String> reqData);

    /**
     * 获取 礼包详情 页面的数据
     */
    @POST(NetUrl.GIFT_GET_DETAIL)
    Call<JsonRespBase<IndexGiftNew>> obtainGiftDetail(@Body JsonReqBase<ReqGiftDetail> reqData);

	/**
	 * 获取 首页-游戏-精品 页面的数据
	 */
	@POST(NetUrl.GAME_GET_INDEX_SUPER)
	Call<JsonRespBase<IndexGameSuper>> obtainIndexGameSuper(@Body JsonReqBase<String> reqData);

	/**
	 * 获取 礼包 单列表数据
	 */
    @POST
    Call<JsonRespBase<OneTypeDataList<IndexGiftNew>>> obtainGiftList(@Url String url, @Body
    JsonReqBase<ReqPageData> reqData);

	/**
	 * 获取 游戏 单列表数据
	 */
	@POST
	Call<JsonRespBase<OneTypeDataList<IndexGameNew>>> obtainGameList(@Url String url, @Body
	JsonReqBase<ReqPageData> reqData);

}
