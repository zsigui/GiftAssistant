package com.oplay.giftcool.engine;

import com.oplay.giftcool.config.NetUrl;
import com.oplay.giftcool.model.data.req.ReqChangeFocus;
import com.oplay.giftcool.model.data.req.ReqChangeMessageStatus;
import com.oplay.giftcool.model.data.req.ReqFeedBack;
import com.oplay.giftcool.model.data.req.ReqGetCode;
import com.oplay.giftcool.model.data.req.ReqGiftDetail;
import com.oplay.giftcool.model.data.req.ReqHopeGift;
import com.oplay.giftcool.model.data.req.ReqIndexGift;
import com.oplay.giftcool.model.data.req.ReqIndexPost;
import com.oplay.giftcool.model.data.req.ReqInitApp;
import com.oplay.giftcool.model.data.req.ReqLogin;
import com.oplay.giftcool.model.data.req.ReqModifyAvatar;
import com.oplay.giftcool.model.data.req.ReqModifyNick;
import com.oplay.giftcool.model.data.req.ReqPageData;
import com.oplay.giftcool.model.data.req.ReqPayCode;
import com.oplay.giftcool.model.data.req.ReqPostToken;
import com.oplay.giftcool.model.data.req.ReqRefreshGift;
import com.oplay.giftcool.model.data.req.ReqReportedInfo;
import com.oplay.giftcool.model.data.req.ReqSearchHot;
import com.oplay.giftcool.model.data.req.ReqSearchKey;
import com.oplay.giftcool.model.data.req.ReqTaskReward;
import com.oplay.giftcool.model.data.resp.GameTypeMain;
import com.oplay.giftcool.model.data.resp.GiftDetail;
import com.oplay.giftcool.model.data.resp.IndexGameNew;
import com.oplay.giftcool.model.data.resp.IndexGameSuper;
import com.oplay.giftcool.model.data.resp.IndexGift;
import com.oplay.giftcool.model.data.resp.IndexGiftLike;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;
import com.oplay.giftcool.model.data.resp.IndexPost;
import com.oplay.giftcool.model.data.resp.IndexPostNew;
import com.oplay.giftcool.model.data.resp.InitAppResult;
import com.oplay.giftcool.model.data.resp.MissionReward;
import com.oplay.giftcool.model.data.resp.ModifyAvatar;
import com.oplay.giftcool.model.data.resp.ModifyNick;
import com.oplay.giftcool.model.data.resp.MyAttention;
import com.oplay.giftcool.model.data.resp.OneTypeDataList;
import com.oplay.giftcool.model.data.resp.PayCode;
import com.oplay.giftcool.model.data.resp.PostToken;
import com.oplay.giftcool.model.data.resp.SearchDataResult;
import com.oplay.giftcool.model.data.resp.SearchPromptResult;
import com.oplay.giftcool.model.data.resp.UpdateInfo;
import com.oplay.giftcool.model.data.resp.UpdateSession;
import com.oplay.giftcool.model.data.resp.UserInfo;
import com.oplay.giftcool.model.data.resp.UserModel;
import com.oplay.giftcool.model.data.resp.message.MessageCount;
import com.oplay.giftcool.model.data.resp.message.PushMessage;
import com.oplay.giftcool.model.data.resp.message.ReplyMessage;
import com.oplay.giftcool.model.data.resp.message.SystemMessage;
import com.oplay.giftcool.model.data.resp.task.ScoreMissionGroup;
import com.oplay.giftcool.model.json.JsonRespGiftList;
import com.oplay.giftcool.model.json.JsonRespLimitGiftList;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Url;


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
	 * 上报手机应用信息
	 */
	@POST(NetUrl.APP_INFO_REPORTED)
	Call<JsonRespBase<Void>> reportedAppInfo(@Body JsonReqBase<ReqReportedInfo> reqData);

	/* -------------- 搜索 ---------------- */
	/**
	 * 游戏/礼包搜索
	 */
	@POST(NetUrl.GET_SEARCH)
	Call<JsonRespBase<SearchDataResult>> obtainSearchResult(@Body JsonReqBase<ReqSearchKey> reqData);

	/**
	 * 关键词提示
	 */
	@POST(NetUrl.GET_SEARCH_KEY)
	Call<JsonRespBase<SearchPromptResult>> obtainSearchPrompt(@Body JsonReqBase<ReqSearchKey> reqData);

	/**
	 * 热门搜索
	 */
	@POST(NetUrl.GET_SEARCH_HOT_DATA)
	Call<JsonRespBase<OneTypeDataList<IndexGameNew>>> obtainSearchHotData(@Body JsonReqBase<ReqSearchHot> reqData);

	/**
	 * 搜索 - 求礼包
	 */
	@POST(NetUrl.COMMIT_HOPE_GIFT)
	Call<JsonRespBase<Void>> commitHopeGift(@Body JsonReqBase<ReqHopeGift> reqData);

	/* -------------- 礼包接口 --------------- */
	/**
	 * 获取 首页-礼包 页面的数据
	 */
	@POST(NetUrl.GIFT_GET_INDEX)
	Call<JsonRespBase<IndexGift>> obtainIndexGift(@Body JsonReqBase<ReqIndexGift> reqData);

	/**
	 * 获取 首页-新鲜出炉礼包列表 页面的数据
	 */
	@POST(NetUrl.GIFT_GET_INDEX_NEW)
	Call<JsonRespBase<OneTypeDataList<IndexGiftNew>>> obtainIndexGiftNew(@Body JsonReqBase<ReqIndexGift> reqData);

	/**
	 * 获取 首页-礼包-猜你喜欢 页面的数据
	 */
	@POST(NetUrl.GIFT_GET_ALL_LIKE)
	Call<JsonRespBase<OneTypeDataList<IndexGiftLike>>> obtainGiftLike(@Body JsonReqBase<ReqIndexGift> reqData);

	/**
	 * 获取 首页-礼包-每日限量礼包 页面的数据
	 */
//	接口废弃
//	@POST(NetUrl.GIFT_GET_ALL_LIMIT)
//	Call<JsonRespGiftList> obtainGiftLimit(@Body JsonReqBase<String> reqData);
	/**
	 * 更新 首页-礼包-限量礼包 页面的数据
	 */
//	接口废弃
//	@POST(NetUrl.GIFT_GET_ALL_LIMIT)
//	Call<JsonRespBase<HashMap<String, IndexGiftNew>>> updateGiftLimit(@Body JsonReqBase<ReqRefreshGift> reqData);
	/**
	 * 获取 首页-礼包-限量礼包 页面的数据
	 */
	@POST(NetUrl.GIFT_GET_LIMIT_BY_PAGE)
	Call<JsonRespLimitGiftList> obtainGiftLimitByPage(@Body JsonReqBase<ReqPageData> reqData);

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
	 * 获取刷新指定礼包数据
	 */
	@POST(NetUrl.GIFT_REFRESH)
	Call<JsonRespBase<HashMap<String, IndexGiftNew>>> refreshGift(@Body JsonReqBase<ReqRefreshGift> reqData);

	/**
	 * 支付 抢夺礼包码
	 */
	@POST(NetUrl.GIFT_SEIZE_CODE)
	Call<JsonRespBase<PayCode>> payGiftCode(@Body JsonReqBase<ReqPayCode> reqData);

	/**
	 * 支付 获取特定礼包码(订单查询)(废弃)
	 */
	@POST(NetUrl.GIFT_GET_SPECIFIC_CODE)
	Call<JsonRespBase<PayCode>> getSpecificGiftCode(@Body JsonReqBase<ReqGetCode> reqData);

	/**
	 * 支付 通知偶玩豆订单支付失败(废弃)
	 */
	@POST(NetUrl.GIFT_FAIL_PAY)
	Call<JsonRespBase<Void>> notifyTradeFail(@Body JsonReqBase<ReqGetCode> reqData);


	/* -------------- 游戏接口 --------------- */
	/**
	 * 获取 首页-游戏-精品 页面的数据
	 */
	@POST(NetUrl.GAME_GET_INDEX_SUPER)
	Call<JsonRespBase<IndexGameSuper>> obtainIndexGameSuper(@Body JsonReqBase<Void> reqData);

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

	/* ---------------- 活动接口 ----------------- */

	/**
	 * 获取 首页-活动 页面数据
	 */
	@POST(NetUrl.POST_GET_INDEX)
	Call<JsonRespBase<IndexPost>> obtainIndexPost(@Body JsonReqBase<ReqIndexPost> reqData);

	/**
	 * 获取 活动列表 页面数据
	 */
	@POST
	Call<JsonRespBase<OneTypeDataList<IndexPostNew>>> obtainPostList(@Url String url, @Body JsonReqBase<ReqIndexPost> reqData);

	/**
	 * 获取 活动回复评论的提交TOKEN
	 */
	@POST(NetUrl.POST_REPLY_GET_TOKEN)
	Call<JsonRespBase<PostToken>> obtainReplyToken(@Body JsonReqBase<ReqPostToken> reqData);


	/* ---------------- 用户接口 ----------------- */

	/**
	 * 注销登录
	 */
	@POST(NetUrl.USER_LOGOUT)
	Call<JsonRespBase<Void>> logout(@Body JsonReqBase<Object> reqData);

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

	/* ---------------- 金币接口  ---------------- */

	/**
	 * 获取金币任务
	 */
	@POST(NetUrl.SCORE_GET_TASK)
	Call<JsonRespBase<ArrayList<ScoreMissionGroup>>> obtainScoreTask(@Body JsonReqBase<String> reqData);

	/**
	 * 获取金币任务奖励
	 */
	@POST(NetUrl.SCORE_REWARD)
	Call<JsonRespBase<MissionReward>> obtainTaskReward(@Body JsonReqBase<ReqTaskReward> reqData);

	/* ---------------- 应用接口  ---------------- */
	@POST(NetUrl.APP_POST_FEEDBACK)
	Call<JsonRespBase<Void>> postFeedBack(@Body JsonReqBase<ReqFeedBack> reqData);


	/* ------------- 消息中心 --------------- */

	/**
	 * 获取推送消息列表
	 */
	@POST(NetUrl.MESSAGE_PUSH_LIST)
	Call<JsonRespBase<OneTypeDataList<PushMessage>>> obtainPushMessage(@Body JsonReqBase<ReqPageData> reqData);

	/**
	 * 关注/取消关注游戏状态
	 */
	@POST(NetUrl.GAME_FOCUS_CHANGE)
	Call<JsonRespBase<Void>> changeGameFocus(@Body JsonReqBase<ReqChangeFocus> reqData);

	/**
	 * 获取已关注游戏列表信息
	 */
	@POST(NetUrl.GAME_FOCUS_LIST)
	Call<JsonRespBase<OneTypeDataList<MyAttention>>> obtainAttentionMessage(@Body JsonReqBase<ReqPageData> reqData);

	/**
	 * 修改推送消息状态
	 */
	@POST(NetUrl.MESSAGE_CHANGE_STATUS)
	Call<JsonRespBase<Void>> changePushMessageStatus(@Body JsonReqBase<ReqChangeMessageStatus> reqData);

	/**
	 * 获取当前未读推送消息数量
	 */
	@POST(NetUrl.MESSAGE_UNREAD_COUNT)
	Call<JsonRespBase<OneTypeDataList<MessageCount>>> obtainUnreadMessageCount(@Body JsonReqBase<Void> reqData);

	/**
	 * 获取回复消息列表，根据url决定是回复的还是点赞
	 */
	@POST
	Call<JsonRespBase<OneTypeDataList<ReplyMessage>>> obtainReplyMessage(@Url String url, @Body JsonReqBase<ReqPageData> reqData);

	/**
	 * 获取系统消息列表
	 */
	@POST("")
	Call<JsonRespBase<OneTypeDataList<SystemMessage>>> obtainSystemMessage(@Body JsonReqBase<ReqPageData> reqData);

	/*---------------- JS调用的异步方法，传参返回都用JSON字符串 -----------------*/

	/**
	 * POST网络请求
	 */
	@POST
	Call<Object> asyncPostForJsCall(@Url String url, @Body JsonReqBase<Object> reqData);

	/**
	 * GET网络请求
	 */
	@GET
	Call<Object> asyncGetForJsCall(@Url String url, @Body JsonReqBase<Object> reqData);
}
