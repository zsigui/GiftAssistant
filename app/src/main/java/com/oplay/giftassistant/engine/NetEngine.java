package com.oplay.giftassistant.engine;

import com.oplay.giftassistant.config.NetUrl;
import com.oplay.giftassistant.model.UserModel;
import com.oplay.giftassistant.model.data.req.ReqGiftDetail;
import com.oplay.giftassistant.model.data.req.ReqIndexGift;
import com.oplay.giftassistant.model.data.req.ReqLogin;
import com.oplay.giftassistant.model.data.req.ReqModifyAvater;
import com.oplay.giftassistant.model.data.req.ReqModifyNick;
import com.oplay.giftassistant.model.data.req.ReqModifyPhone;
import com.oplay.giftassistant.model.data.req.ReqPageData;
import com.oplay.giftassistant.model.data.resp.IndexGameNew;
import com.oplay.giftassistant.model.data.resp.IndexGameSuper;
import com.oplay.giftassistant.model.data.resp.IndexGift;
import com.oplay.giftassistant.model.data.resp.IndexGiftLike;
import com.oplay.giftassistant.model.data.resp.IndexGiftNew;
import com.oplay.giftassistant.model.data.resp.ModifyPhone;
import com.oplay.giftassistant.model.data.resp.OneTypeDataList;
import com.oplay.giftassistant.model.data.resp.ScoreMissionList;
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


	/* ---------------- 用户接口  ---------------- */

	/**
	 * 注销登录
	 */
	@POST(NetUrl.USER_LOGOUT)
	Call<Void> logout(@Body JsonReqBase<Object> reqData);

	/**
	 * 更新session，保持登录态情况下，每次启动APP进行验证
	 */
	@POST(NetUrl.USER_UPDATE_SESSION)
	Call<JsonRespBase<UserModel>> updateSession(@Body JsonReqBase<Object> reqData);

	/**
	 * 修改手机号码，需要进行多歩
	 */
	@POST
	Call<JsonRespBase<ModifyPhone>> modifyPhone(@Url String url, @Body JsonReqBase<ReqModifyPhone> reqData);

	/**
	 * 登录，分为 手机号码登录 和 偶玩账号登录
	 */
	@POST
	Call<JsonRespBase<UserModel>> login(@Url String url, @Body JsonReqBase<ReqLogin> reqData);

	/**
	 * 修改用户昵称
	 */
	@POST(NetUrl.USER_MODIFY_NICK)
	Call<JsonRespBase<UserModel>> modifyUserNick(@Body JsonReqBase<ReqModifyNick> reqData);

	/**
	 * 修改用户头像
	 */
	@POST(NetUrl.USER_MODIFY_AVATAR)
	Call<JsonRespBase<UserModel>> modifyUserAvater(@Body JsonReqBase<ReqModifyAvater> reqData);


	/* ---------------- 积分接口  ---------------- */

	/**
	 * 获取积分任务
	 */
	@POST(NetUrl.SCORE_GET_TASK)
	Call<JsonRespBase<ScoreMissionList>> obtainScoreTask(@Body JsonReqBase<Void> reqData);
}
