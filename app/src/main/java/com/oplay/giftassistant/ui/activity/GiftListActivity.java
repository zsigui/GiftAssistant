package com.oplay.giftassistant.ui.activity;

import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;

import com.litesuits.common.utils.PackageUtil;
import com.oplay.giftassistant.R;
import com.oplay.giftassistant.config.AppDebugConfig;
import com.oplay.giftassistant.config.GiftTypeUtil;
import com.oplay.giftassistant.config.Global;
import com.oplay.giftassistant.config.NetUrl;
import com.oplay.giftassistant.config.StatusCode;
import com.oplay.giftassistant.engine.NetEngine;
import com.oplay.giftassistant.model.data.req.ReqIndexGift;
import com.oplay.giftassistant.model.data.resp.IndexGiftLike;
import com.oplay.giftassistant.model.data.resp.IndexGiftNew;
import com.oplay.giftassistant.model.data.resp.OneTypeDataList;
import com.oplay.giftassistant.model.data.resp.TimeDataList;
import com.oplay.giftassistant.model.json.JsonRespGiftList;
import com.oplay.giftassistant.model.json.base.JsonReqBase;
import com.oplay.giftassistant.model.json.base.JsonRespBase;
import com.oplay.giftassistant.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftassistant.ui.fragment.NetErrorFragment;
import com.oplay.giftassistant.ui.fragment.gift.GiftLikeListFragment;
import com.oplay.giftassistant.ui.fragment.gift.GiftMutilDayFragment;
import com.oplay.giftassistant.util.DateUtil;
import com.oplay.giftassistant.util.NetworkUtil;
import com.socks.library.KLog;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by zsigui on 15-12-29.
 */
public class GiftListActivity extends BaseAppCompatActivity {

	public static final String KEY_TYPE = "key_data_type";

	public static final int TYPE_LIMIT = 1;
	public static final int TYPE_LIKE = 2;
	public static final int TYPE_NEW = 3;


	private NetEngine mEngine;
	private NetErrorFragment mNetErrorFragment;
	private int type = 0;


	@Override
	@SuppressWarnings("unchecked")
	protected void initView() {
		setContentView(R.layout.activity_common_with_back);

		if (getIntent() != null) {
			type = getIntent().getIntExtra(KEY_TYPE, TYPE_LIKE);
		}
	}

	@Override
	protected void initMenu(@NonNull Toolbar toolbar) {
		super.initMenu(toolbar);
		switch (type) {
			case TYPE_LIMIT:
				setBarTitle("今日限量礼包");
				break;
			case TYPE_NEW:
				setBarTitle("新鲜出炉礼包");
				break;
			case TYPE_LIKE:
				setBarTitle("猜你喜欢");
				break;
		}

	}

	@Override
	protected void processLogic() {
		mEngine = Global.getNetEngine();
		mNeedWorkCallback = true;
		loadData();
	}

	public void loadData() {
		if (mIsLoading) {
			return;
		}
		mIsLoading = true;
		displayLoadingUI(R.id.fl_container);
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (type == TYPE_LIMIT) {
					loadLimitGiftData();
				} else if (type == TYPE_NEW) {
					loadNewGiftData();
				} else if (type == TYPE_LIKE) {
					loadLikeGiftData();
				}
			}
		}).start();
	}

	/**
	 * 加载猜你喜欢数据
	 */
	private void loadLikeGiftData() {
		if (!NetworkUtil.isConnected(this)) {
			displayNetworkErrUI();
			return;
		}
		ReqIndexGift data = new ReqIndexGift();
		data.appNames = PackageUtil.getInstalledAppName(getApplicationContext());
		JsonReqBase<ReqIndexGift> reqData = new JsonReqBase<ReqIndexGift>(data);
		mEngine.obtainGiftLike(reqData).enqueue(new Callback<JsonRespBase<OneTypeDataList<IndexGiftLike>>>() {
			@Override
			public void onResponse(Response<JsonRespBase<OneTypeDataList<IndexGiftLike>>> response,
			                       Retrofit retrofit) {
				if (!mNeedWorkCallback) {
					return;
				}
				mIsLoading = false;
				if (response != null && response.isSuccess()) {
					if (response.body() != null && response.body().getCode() == StatusCode.SUCCESS) {
						displayGiftLikeUI(response.body().getData().data);
						return;
					}
					if (AppDebugConfig.IS_DEBUG) {
						KLog.d(AppDebugConfig.TAG_APP, (response.body() == null ? "解析失败" : response.body().error()));
					}
				}
				displayNetworkErrUI();
			}

			@Override
			public void onFailure(Throwable t) {
				if (!mNeedWorkCallback) {
					return;
				}
				mIsLoading = false;
				if (AppDebugConfig.IS_DEBUG) {
					KLog.e(t);
				}
				//displayNetworkErrUI();
				ArrayList<IndexGiftLike> data = initStashLikeData();
				displayGiftLikeUI(data);
			}
		});

	}

	private void loadNewGiftData() {
		if (!NetworkUtil.isConnected(this)) {
			displayNetworkErrUI();
			return;
		}
		mEngine.obtainGiftNew(new JsonReqBase<String>()).enqueue(new Callback<JsonRespGiftList>() {
			@Override
			public void onResponse(Response<JsonRespGiftList> response, Retrofit retrofit) {
				if (!mNeedWorkCallback) {
					return;
				}
				mIsLoading = false;
				if (response != null && response.isSuccess()) {
					if (response.body() != null && response.body().getCode() == StatusCode.SUCCESS) {
						displayGiftNewUI(response.body().getData());
						return;
					}
					if (AppDebugConfig.IS_DEBUG) {
						KLog.d(AppDebugConfig.TAG_APP, (response.body() == null ? "解析失败" : response.body().error()));
					}
				}
				// 加载错误页面也行
				displayNetworkErrUI();
			}

			@Override
			public void onFailure(Throwable t) {
				if (!mNeedWorkCallback) {
					return;
				}
				mIsLoading = false;
				if (AppDebugConfig.IS_DEBUG) {
					KLog.e(t);
				}
				//displayNetworkErrUI();
				ArrayList<TimeDataList<IndexGiftNew>> data = initStashNewData();
				displayGiftNewUI(data);
			}
		});
	}

	private void loadLimitGiftData() {
		if (!NetworkUtil.isConnected(this)) {
			displayNetworkErrUI();
			return;
		}
		mEngine.obtainGiftLimit(new JsonReqBase<String>()).enqueue(new Callback<JsonRespGiftList>() {
			@Override
			public void onResponse(Response<JsonRespGiftList> response, Retrofit retrofit) {
				if (!mNeedWorkCallback) {
					return;
				}
				mIsLoading = false;
				if (response != null && response.isSuccess()) {
					if (response.body() != null && response.body().getCode() == StatusCode.SUCCESS) {
						displayGiftLimitUI(response.body().getData());
						return;
					}
					if (AppDebugConfig.IS_DEBUG) {
						KLog.d(AppDebugConfig.TAG_APP, (response.body() == null ? "解析失败" : response.body().error()));
					}
				}
				// 加载错误页面也行
				displayNetworkErrUI();
			}

			@Override
			public void onFailure(Throwable t) {
				if (!mNeedWorkCallback) {
					return;
				}
				mIsLoading = false;
				if (AppDebugConfig.IS_DEBUG) {
					KLog.e(t);
				}
				//displayNetworkErrUI();
				ArrayList<TimeDataList<IndexGiftNew>> data = initStashLimitData();
				displayGiftLimitUI(data);
			}
		});
	}

	private void displayGiftLikeUI(ArrayList<IndexGiftLike> data) {
		replaceFrag(R.id.fl_container, GiftLikeListFragment.newInstance(data),
				GiftLikeListFragment.class.getSimpleName(), false);
	}

	private void displayGiftLimitUI(ArrayList<TimeDataList<IndexGiftNew>> data) {
		replaceFrag(R.id.fl_container, GiftMutilDayFragment.newInstance(data, NetUrl.GIFT_GET_ALL_LIMIT_BY_PAGE),
				GiftMutilDayFragment.class.getSimpleName(), false);
	}

	private void displayGiftNewUI(ArrayList<TimeDataList<IndexGiftNew>> data) {
		replaceFrag(R.id.fl_container, GiftMutilDayFragment.newInstance(data, NetUrl.GIFT_GET_ALL_NEW_BY_PAGE),
				GiftMutilDayFragment.class.getSimpleName(), false);
	}

	/**
	 * 显示网络错误提示
	 */
	private void displayNetworkErrUI() {
		if (mNetErrorFragment == null) {
			mNetErrorFragment = NetErrorFragment.newInstance();
			mNetErrorFragment.setOnRetryListener(new NetErrorFragment.OnRetryListener() {
				@Override
				public void onRetry() {
					loadData();
				}
			});
		}
		reattachFrag(R.id.fl_container, mNetErrorFragment, mNetErrorFragment.getClass().getSimpleName());
	}

	/*
			以下部分全为生成的测试数据，后期需要删除
		 */
	private ArrayList<TimeDataList<IndexGiftNew>> initStashLimitData() {
		// 先暂时使用缓存数据假定
		ArrayList<TimeDataList<IndexGiftNew>> result = new ArrayList<>();
		for (int k = 0; k < 5; k++) {
			TimeDataList<IndexGiftNew> timeData = new TimeDataList<>();
			timeData.data = new ArrayList<IndexGiftNew>();

			IndexGiftNew ngift = new IndexGiftNew();
			ngift.gameName = "全民神将-攻城战";
			ngift.id = 335;
			ngift.status = GiftTypeUtil.STATUS_SEIZE;
			ngift.priceType = GiftTypeUtil.PAY_TYPE_BOTH;
			ngift.img = "http://owan-avatar.ymapp.com/app/10986/icon/icon_1449227350.png_140_140_100.png";
			ngift.name = "至尊礼包";
			ngift.isLimit = true;
			ngift.bean = 30;
			ngift.score = 3000;
			ngift.searchTime = DateUtil.getDate("yyyy-MM-dd HH:mm", 5);
			ngift.seizeTime = DateUtil.getDate("yyyy-MM-dd HH:mm", 3);
			ngift.searchCount = 0;
			ngift.remainCount = 10;
			ngift.totalCount = 10;
			ngift.content = "30钻石，5000金币，武器经验卡x6，100块神魂石，10000颗迷魂珠";
			timeData.data.add(ngift);
			IndexGiftNew ng2 = new IndexGiftNew();
			ng2.gameName = "鬼吹灯之挖挖乐";
			ng2.id = 336;
			ng2.status = GiftTypeUtil.STATUS_WAIT_SEARCH;
			ng2.priceType = GiftTypeUtil.PAY_TYPE_BOTH;
			ng2.img = "http://owan-avatar.ymapp.com/app/11061/icon/icon_1450325761.png_140_140_100.png";
			ng2.name = "高级礼包";
			ng2.isLimit = false;
			ng2.bean = 30;
			ng2.score = 3000;
			ng2.searchTime = DateUtil.getDate("yyyy-MM-dd HH:mm", 1);
			ng2.seizeTime = DateUtil.getDate("yyyy-MM-dd HH:mm", -1);
			ng2.searchCount = 0;
			ng2.remainCount = 159;
			ng2.totalCount = 350;
			ng2.content = "30钻石，5000金币，武器经验卡x6，100块神魂石，10000颗迷魂珠";
			timeData.data.add(ng2);
			IndexGiftNew ng3 = new IndexGiftNew();
			ng3.gameName = "兽人战争";
			ng3.id = 337;
			ng3.status = GiftTypeUtil.STATUS_SEARCH;
			ng3.priceType = GiftTypeUtil.PAY_TYPE_SCORE;
			ng3.img = "http://owan-avatar.ymapp.com/app/11058/icon/icon_1450059064.png_140_140_100.png";
			ng3.name = "高级礼包";
			ng3.isLimit = false;
			ng3.score = 1500;
			ng3.searchTime = DateUtil.getDate("yyyy-MM-dd HH:mm", 6);
			;
			ng3.seizeTime = DateUtil.getDate("yyyy-MM-dd HH:mm", 3);
			;
			ng3.searchCount = 355;
			ng3.remainCount = 0;
			ng3.totalCount = 350;
			ng3.content = "30钻石，5000金币，武器经验卡x6，100块神魂石，10000颗迷魂珠";
			timeData.data.add(ng3);
			for (int i = 0; i < 7; i++) {
				IndexGiftNew ng = new IndexGiftNew();
				ng.gameName = "逍遥西游";
				ng.id = i;
				ng.status = GiftTypeUtil.STATUS_SEIZE;
				ng.priceType = GiftTypeUtil.PAY_TYPE_SCORE;
				ng.img = "http://owan-avatar.ymapp.com/app/10657/icon/icon_1450246643.png_140_140_100.png";
				ng.name = "普通礼包";
				ng.isLimit = false;
				ng.score = (int) (Math.random() * 100) * 10;
				ng.searchTime = DateUtil.getDate("yyyy-MM-dd HH:mm", 3);
				;
				ng.seizeTime = DateUtil.getDate("yyyy-MM-dd HH:mm", 3);
				;
				ng.searchCount = 0;
				ng.remainCount = 100;
				ng.totalCount = 100;
				ng.content = "30钻石，5000金币，武器经验卡x6，100块神魂石，10000颗迷魂珠";
				timeData.data.add(ng);
			}
			timeData.date = "2016-01-0" + (k + 1);
			result.add(timeData);
		}

		return result;
	}

	private ArrayList<TimeDataList<IndexGiftNew>> initStashNewData() {
		// 先暂时使用缓存数据假定
		ArrayList<TimeDataList<IndexGiftNew>> result = new ArrayList<>();
		for (int k = 0; k < 3; k++) {
			TimeDataList<IndexGiftNew> timeData = new TimeDataList<>();
			timeData.data = new ArrayList<IndexGiftNew>();

			IndexGiftNew ngift = new IndexGiftNew();
			ngift.gameName = "全民神将-攻城战";
			ngift.id = 335;
			ngift.status = GiftTypeUtil.STATUS_SEIZE;
			ngift.priceType = GiftTypeUtil.PAY_TYPE_BOTH;
			ngift.img = "http://owan-avatar.ymapp.com/app/10986/icon/icon_1449227350.png_140_140_100.png";
			ngift.name = "至尊礼包";
			ngift.isLimit = true;
			ngift.bean = 30;
			ngift.score = 3000;
			ngift.searchTime = DateUtil.getDate("yyyy-MM-dd HH:mm", 8);
			;
			ngift.seizeTime = DateUtil.getDate("yyyy-MM-dd HH:mm", -4);
			;
			ngift.searchCount = 0;
			ngift.remainCount = 10;
			ngift.totalCount = 10;
			ngift.content = "30钻石，5000金币，武器经验卡x6，100块神魂石，10000颗迷魂珠";
			timeData.data.add(ngift);
			IndexGiftNew ng2 = new IndexGiftNew();
			ng2.gameName = "鬼吹灯之挖挖乐";
			ng2.id = 336;
			ng2.status = GiftTypeUtil.STATUS_WAIT_SEARCH;
			ng2.priceType = GiftTypeUtil.PAY_TYPE_BOTH;
			ng2.img = "http://owan-avatar.ymapp.com/app/11061/icon/icon_1450325761.png_140_140_100.png";
			ng2.name = "高级礼包";
			ng2.isLimit = false;
			ng2.bean = 30;
			ng2.score = 3000;
			ng2.searchTime = DateUtil.getDate("yyyy-MM-dd HH:mm", -3);
			;
			ng2.seizeTime = DateUtil.getDate("yyyy-MM-dd HH:mm", -7);
			;
			ng2.searchCount = 0;
			ng2.remainCount = 159;
			ng2.totalCount = 350;
			ng2.content = "30钻石，5000金币，武器经验卡x6，100块神魂石，10000颗迷魂珠";
			timeData.data.add(ng2);
			IndexGiftNew ng3 = new IndexGiftNew();
			ng3.gameName = "兽人战争";
			ng3.id = 337;
			ng3.status = GiftTypeUtil.STATUS_WAIT_SEARCH;
			ng3.priceType = GiftTypeUtil.PAY_TYPE_SCORE;
			ng3.img = "http://owan-avatar.ymapp.com/app/11058/icon/icon_1450059064.png_140_140_100.png";
			ng3.name = "高级礼包";
			ng3.isLimit = false;
			ng3.score = 1500;
			ng3.searchTime = DateUtil.getDate("yyyy-MM-dd HH:mm", 3);
			;
			ng3.seizeTime = DateUtil.getDate("yyyy-MM-dd HH:mm", 3);
			;
			ng3.searchCount = 355;
			ng3.remainCount = 0;
			ng3.totalCount = 350;
			ng3.content = "30钻石，5000金币，武器经验卡x6，100块神魂石，10000颗迷魂珠";
			timeData.data.add(ng3);
			for (int i = 0; i < 7; i++) {
				IndexGiftNew ng = new IndexGiftNew();
				ng.id = i;
				ng.status = GiftTypeUtil.STATUS_WAIT_SEIZE;
				ng.priceType = GiftTypeUtil.PAY_TYPE_SCORE;
				ng.gameName = "逍遥西游";
				ng.img = "http://owan-avatar.ymapp.com/app/10657/icon/icon_1450246643.png_140_140_100.png";
				ng.name = "普通礼包";
				ng.isLimit = false;
				ng.score = (int) (Math.random() * 100) * 10;
				ng.searchTime = DateUtil.getDate("yyyy-MM-dd HH:mm", 3);
				;
				ng.seizeTime = DateUtil.getDate("yyyy-MM-dd HH:mm", 0);
				;
				ng.searchCount = 0;
				ng.remainCount = 100;
				ng.totalCount = 100;
				ng.content = "30钻石，5000金币，武器经验卡x6，100块神魂石，10000颗迷魂珠";
				timeData.data.add(ng);
			}
			timeData.date = "2016-01-0" + (k + 1);
			result.add(timeData);
		}

		return result;
	}

	private ArrayList<IndexGiftLike> initStashLikeData() {
		ArrayList<IndexGiftLike> like = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			IndexGiftLike game = new IndexGiftLike();
			game.name = "口袋妖怪复刻";
			game.totalCount = 10 - i;
			game.newCount = i;
			game.img = "http://owan-avatar.ymapp.com/app/10946/icon/icon_1439432439.png_140_140_100.png";
			game.playCount = (int) (Math.random() * 20000);
			game.size = 0.5f * (int) (Math.random() * 100) + "M";
			like.add(game);
		}
		return like;
	}

}
