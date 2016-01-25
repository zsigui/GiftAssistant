package com.oplay.giftcool.engine;

import com.oplay.giftcool.config.NetUrl;
import com.oplay.giftcool.model.data.req.ReqFeedBack;
import com.oplay.giftcool.model.data.req.ReqGetCode;
import com.oplay.giftcool.model.data.req.ReqGiftDetail;
import com.oplay.giftcool.model.data.req.ReqIndexGift;
import com.oplay.giftcool.model.data.req.ReqInitApp;
import com.oplay.giftcool.model.data.req.ReqLogin;
import com.oplay.giftcool.model.data.req.ReqModifyAvatar;
import com.oplay.giftcool.model.data.req.ReqModifyNick;
import com.oplay.giftcool.model.data.req.ReqPageData;
import com.oplay.giftcool.model.data.req.ReqPayCode;
import com.oplay.giftcool.model.data.req.ReqSearchKey;
import com.oplay.giftcool.model.data.req.ReqTaskReward;
import com.oplay.giftcool.model.data.resp.GameTypeMain;
import com.oplay.giftcool.model.data.resp.GiftDetail;
import com.oplay.giftcool.model.data.resp.IndexGameNew;
import com.oplay.giftcool.model.data.resp.IndexGameSuper;
import com.oplay.giftcool.model.data.resp.IndexGift;
import com.oplay.giftcool.model.data.resp.IndexGiftLike;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;
import com.oplay.giftcool.model.data.resp.ModifyAvatar;
import com.oplay.giftcool.model.data.resp.ModifyNick;
import com.oplay.giftcool.model.data.resp.OneTypeDataList;
import com.oplay.giftcool.model.data.resp.PayCode;
import com.oplay.giftcool.model.data.resp.ScoreMissionList;
import com.oplay.giftcool.model.data.resp.SearchDataResult;
import com.oplay.giftcool.model.data.resp.TaskReward;
import com.oplay.giftcool.model.data.resp.UpdateInfo;
import com.oplay.giftcool.model.data.resp.UpdateSession;
import com.oplay.giftcool.model.data.resp.UserInfo;
import com.oplay.giftcool.model.data.resp.UserModel;
import com.oplay.giftcool.model.data.resp.InitAppResult;
import com.oplay.giftcool.model.json.JsonRespGiftList;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;

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
	 * 应用初始化接口
	 */
	@POST(NetUrl.APP_INIT)
	Call<JsonRespBase<InitAppResult>> initAPP(@Body JsonReqBase<ReqInitApp> reqData);

	/**
	 * 更新版本
	 */
	@POST(NetUrl.APP_VERSION_UPDATE)
	Call<JsonRespBase<UpdateInfo>> checkUpdate(@Body JsonReqBase<ReqInitApp> reqData);

	/**
	 * 游戏/礼包搜索
	 */
	@POST(NetUrl.GET_SEARCH)
	Call<JsonRespBase<SearchDataResult>> obtainSearchResult(@Body JsonReqBase<ReqSearchKey> reqData);

	/* -------------- 礼包接口 --------------- */
	/**
	 * 获取 首页-礼包 页面的数据
	 */
	@POST(NetUrl.GIFT_GET_INDEX)
	Call<JsonRespBase<IndexGift>> obtainIndexGift(@Body JsonReqBase<ReqIndexGift> reqData);

	/**
	 * 获取 首页-礼包 下端接口刷新后数据
	 */
	@POST(NetUrl.GIFT_REFRESH_INDEX)
	Call<JsonReqBase<IndexGift>> refreshIndexGift(@Body JsonReqBase<Void> reqData);

	/**
	 * 获取 首页-礼包-猜你喜欢 页面的数据
	 */
	@POST(NetUrl.GIFT_GET_ALL_LIKE)
	Call<JsonRespBase<OneTypeDataList<IndexGiftLike>>> obtainGiftLike(@Body JsonReqBase<ReqIndexGift> reqData);

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
    Call<JsonRespBase<GiftDetail>> obtainGiftDetail(@Body JsonReqBase<ReqGiftDetail> reqData);

	/**
	 * 获取 礼包 单列表数据
	 */
    @POST
    Call<JsonRespBase<OneTypeDataList<IndexGiftNew>>> obtainGiftList(@Url String url, @Body
    JsonReqBase<ReqPageData> reqData);

	/**
	 * 支付 抢夺礼包码
	 */
	@POST(NetUrl.GIFT_SEIZE_CODE)
	Call<JsonRespBase<PayCode>> payGiftCode(@Body JsonReqBase<ReqPayCode> reqData);

	/**
	 * 支付 获取特定礼包码(订单查询)
	 */
	@POST(NetUrl.GIFT_GET_SPECIFIC_CODE)
	Call<JsonRespBase<PayCode>> getSpecificGiftCode(@Body JsonReqBase<ReqGetCode> reqData);

	/**
	 * 支付 通知偶玩豆订单支付失败
	 */
	@POST(NetUrl.GIFT_FAIL_PAY)
	Call<JsonRespBase<Void>> notifyTradeFail(@Body JsonReqBase<ReqGetCode> reqData);

	/* -------------- 游戏接口 --------------- */
	/**
	 * 获取 首页-游戏-精品 页面的数据
	 */
	@POST(NetUrl.GAME_GET_INDEX_SUPER)
	Call<JsonRespBase<IndexGameSuper>> obtainIndexGameSuper(@Body JsonReqBase<String> reqData);

	/**
	 * 获取 游戏 单列表数据，用于显示游戏列表数据
	 */
	@POST
	Call<JsonRespBase<OneTypeDataList<IndexGameNew>>> obtainGameList(@Url String url, @Body
	JsonReqBase<ReqPageData> reqData);

	/**
	 * 获取 游戏 - 分类 界面
	 */
	@POST(NetUrl.GAME_GET_INDEX_TYPE)
	Call<JsonRespBase<ArrayList<GameTypeMain>>> obtainIndexGameType(@Body JsonReqBase<Void> reqData);


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
	Call<JsonRespBase<UpdateSession>> updateSession(@Body JsonReqBase<String> reqData);

	/**
	 * 登录，分为 手机号码登录 和 偶玩账号登录
	 */
	@POST
	Call<JsonRespBase<UserModel>> login(@Url String url, @Body JsonReqBase<ReqLogin> reqData);

	/**
	 * 修改用户昵称
	 */
	@POST(NetUrl.USER_MODIFY_NICK)
	Call<JsonRespBase<ModifyNick>> modifyUserNick(@Body JsonReqBase<ReqModifyNick> reqData);

	/**
	 * 修改用户头像
	 */
	@POST(NetUrl.USER_MODIFY_AVATAR)
	Call<JsonRespBase<ModifyAvatar>> modifyUserAvatar(@Body JsonReqBase<ReqModifyAvatar> reqData);

	/**
	 * 获取个人信息接口
	 */
	@POST(NetUrl.USER_GET_INFO)
	Call<JsonRespBase<UserModel>> getUserInfo(@Body JsonReqBase<Void> reqData);


	/**
	 * 获取用户部分信息接口
	 */
	@POST(NetUrl.USER_GET_PART_INFO)
	Call<JsonRespBase<UserInfo>> getUserPartInfo(@Body JsonReqBase<Void> reqData);

	/* ---------------- 积分接口  ---------------- */

	/**
	 * 获取积分任务
	 */
	@POST(NetUrl.SCORE_GET_TASK)
	Call<JsonRespBase<ScoreMissionList>> obtainScoreTask(@Body JsonReqBase<String> reqData);

	/**
	 * 获取积分任务奖励
	 */
	@POST(NetUrl.SCORE_REWARD)
	Call<JsonRespBase<TaskReward>> obtainTaskReward(@Body JsonReqBase<ReqTaskReward> reqData);

	/* ---------------- 应用接口  ---------------- */
	@POST(NetUrl.APP_POST_FEEDBACK)
	Call<JsonRespBase<TaskReward>> postFeedBack(@Body JsonReqBase<ReqFeedBack> reqData);
}
