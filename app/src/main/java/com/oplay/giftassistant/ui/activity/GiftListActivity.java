package com.oplay.giftassistant.ui.activity;

import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;

import com.litesuits.common.utils.PackageUtil;
import com.oplay.giftassistant.R;
import com.oplay.giftassistant.adapter.util.GiftTypeUtil;
import com.oplay.giftassistant.config.AppDebugConfig;
import com.oplay.giftassistant.config.Global;
import com.oplay.giftassistant.engine.NetEngine;
import com.oplay.giftassistant.model.data.req.ReqGiftDetail;
import com.oplay.giftassistant.model.data.req.ReqIndexGift;
import com.oplay.giftassistant.model.data.resp.IndexGiftLike;
import com.oplay.giftassistant.model.data.resp.IndexGiftNew;
import com.oplay.giftassistant.model.json.JsonRespLimitGift;
import com.oplay.giftassistant.model.json.base.JsonReqBase;
import com.oplay.giftassistant.model.json.base.JsonRespBase;
import com.oplay.giftassistant.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftassistant.ui.fragment.GiftDetailFragment;
import com.oplay.giftassistant.ui.fragment.GiftLikeListFragment;
import com.oplay.giftassistant.ui.fragment.GiftListContainerFragment;
import com.oplay.giftassistant.ui.fragment.NetErrorFragment;
import com.socks.library.KLog;

import java.io.Serializable;
import java.util.ArrayList;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by zsigui on 15-12-29.
 */
public class GiftListActivity extends BaseAppCompatActivity {

	public static final String KEY_TYPE = "key_data_type";
	public static final String KEY_DETAIL_ID = "key_detail_id";
	public static final String KEY_DETAIL_NAME = "key_detail_name";
	public static final String KEY_GIFT_LIKE_DATA = "key_gift_like_data";

	private NetEngine mEngine;
	private NetErrorFragment mNetErrorFragment;
	// 0 礼包详情
	// 1 今日限量礼包
	// 2 新鲜出炉礼包
	// 3 猜你喜欢
	private int type = 0;
	private int detailId;
	private String detailName;
	private ArrayList<IndexGiftLike> giftData;


	@Override
	@SuppressWarnings("unchecked")
	protected void initView() {
		setContentView(R.layout.activity_common_with_back);

		if (getIntent() != null) {
			type = getIntent().getIntExtra(KEY_TYPE, 0);
			if (type == 0) {
				detailId = getIntent().getIntExtra(KEY_DETAIL_ID, 0);
				detailName = getIntent().getStringExtra(KEY_DETAIL_NAME);
			} else if (type == 3) {
				Serializable s = getIntent().getSerializableExtra(KEY_GIFT_LIKE_DATA);
				if (s != null) {
					giftData = (ArrayList<IndexGiftLike>) s;
				}
			}
		}
	}

	@Override
	protected void initMenu(@NonNull Toolbar toolbar) {
		super.initMenu(toolbar);
		switch (type) {
			case 0:
				setBarTitle(detailName);
				break;
			case 1:
				setBarTitle("今日限量礼包");
				break;
			case 2:
				setBarTitle("新鲜出炉礼包");
				break;
			case 3:
				setBarTitle("猜你喜欢");
				break;
		}

	}

	@Override
	protected void processLogic() {
		displayLoadingUI(R.id.fl_container);

		mEngine = Global.getNetEngine();
		loadData();
	}

	public void loadData() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (type == 1) {
					loadLimitGiftData();
				} else if (type == 2) {
					loadNewGiftData();
				} else if (type == 3) {
					loadLikeGiftData();
				} else {
					loadGiftDetailData();
				}
			}
		}).start();
	}

	/**
	 * 加载猜你喜欢数据
	 */
	private void loadLikeGiftData() {
		ReqIndexGift data = new ReqIndexGift();
		data.appNames = PackageUtil.getInstalledAppName(getApplicationContext());
		JsonReqBase<ReqIndexGift> reqData = new JsonReqBase<ReqIndexGift>(data);
		mEngine.obtainGiftLike(reqData)
				.enqueue(new Callback<JsonRespBase<ArrayList<IndexGiftLike>>>() {
					@Override
					public void onResponse(Response<JsonRespBase<ArrayList<IndexGiftLike>>> response,
					                       Retrofit retrofit) {
						if (response != null && response.code() == 200) {
							displayGiftLikeUI(response.body().getData());
							return;
						}
						displayNetworkErrUI();
					}

					@Override
					public void onFailure(Throwable t) {
						if (AppDebugConfig.IS_DEBUG) {
							KLog.e(t);
						}
						//displayNetworkErrUI();
						ArrayList<IndexGiftLike> data = initStashLikeData();
						displayGiftLikeUI(data);
					}
				});
	}

	/**
	 * 加载详情数据
	 */
	private void loadGiftDetailData() {
		// 获取某个礼包详情
		ReqGiftDetail data = new ReqGiftDetail();
		data.id = detailId;
		mEngine.obtainGiftDetail(new JsonReqBase<ReqGiftDetail>(data))
				.enqueue(new Callback<JsonRespBase<IndexGiftNew>>() {
					@Override
					public void onResponse(Response<JsonRespBase<IndexGiftNew>> response, Retrofit retrofit) {
						if (response != null && response.code() == 200) {
							displayGiftDetailUI(response.body().getData());
							return;
						}
						// 加载错误页面也行
						displayNetworkErrUI();
					}

					@Override
					public void onFailure(Throwable t) {
						if (AppDebugConfig.IS_DEBUG) {
							KLog.e(t);
						}
						//displayNetworkErrUI();
						IndexGiftNew data = initStashGiftDetail();
						displayGiftDetailUI(data);
					}
				});
	}

	private void loadNewGiftData() {
		mEngine.obtainGiftNew(new JsonReqBase<String>()).enqueue(new Callback<JsonRespLimitGift>() {
			@Override
			public void onResponse(Response<JsonRespLimitGift> response, Retrofit retrofit) {
				if (response != null && response.code() == 200) {
					displayGiftNewUI(response.body().getData());
					return;
				}
				// 加载错误页面也行
				displayNetworkErrUI();
			}

			@Override
			public void onFailure(Throwable t) {
				if (AppDebugConfig.IS_DEBUG) {
					KLog.e(t);
				}
				//displayNetworkErrUI();
				ArrayList<ArrayList<IndexGiftNew>> data = initStashNewData();
				displayGiftNewUI(data);
			}
		});
	}

	private void loadLimitGiftData() {
		mEngine.obtainGiftLimit(new JsonReqBase<String>()).enqueue(new Callback<JsonRespLimitGift>() {
			@Override
			public void onResponse(Response<JsonRespLimitGift> response, Retrofit retrofit) {
				if (response != null && response.code() == 200) {
					displayGiftNewUI(response.body().getData());
					return;
				}
				// 加载错误页面也行
				displayNetworkErrUI();
			}

			@Override
			public void onFailure(Throwable t) {
				if (AppDebugConfig.IS_DEBUG) {
					KLog.e(t);
				}
				//displayNetworkErrUI();
				ArrayList<ArrayList<IndexGiftNew>> data = initStashLimitData();
				displayGiftNewUI(data);
			}
		});
	}

	private void displayGiftLikeUI(ArrayList<IndexGiftLike> data) {
		replaceFrag(R.id.fl_container, GiftLikeListFragment.newInstance(data),
				GiftLikeListFragment.class.getSimpleName());
	}

	private void displayGiftDetailUI(IndexGiftNew data) {
		replaceFrag(R.id.fl_container, GiftDetailFragment.newInstance(data),
				GiftDetailFragment.class.getSimpleName());
	}

	private void displayGiftNewUI(ArrayList<ArrayList<IndexGiftNew>> data) {
		replaceFrag(R.id.fl_container, GiftListContainerFragment.newInstance(data),
				GiftListContainerFragment.class.getSimpleName());
	}

	/**
	 * 显示网络错误提示
	 */
	private void displayNetworkErrUI() {
		if (mNetErrorFragment == null) {
			mNetErrorFragment = NetErrorFragment.newInstance();
		}
		replaceFrag(R.id.fl_container, mNetErrorFragment, mNetErrorFragment.getClass().getSimpleName());
	}


	/*
		以下部分全为生成的测试数据，后期需要删除
	 */
	private ArrayList<ArrayList<IndexGiftNew>> initStashLimitData() {
		// 先暂时使用缓存数据假定
		ArrayList<ArrayList<IndexGiftNew>> result = new ArrayList<>();
		for (int k = 0; k < 5; k++) {
			ArrayList<IndexGiftNew> newData = new ArrayList<IndexGiftNew>();

			IndexGiftNew ngift = new IndexGiftNew();
			ngift.gameName = "全民神将-攻城战";
			ngift.id = 335;
			ngift.status = GiftTypeUtil.STATUS_SEIZE;
			ngift.priceType = GiftTypeUtil.PAY_TYPE_BOTN;
			ngift.img = "http://owan-img.ymapp.com/app/10986/icon/icon_1449227350.png_140_140_100.png";
			ngift.name = "至尊礼包";
			ngift.isLimit = 1;
			ngift.bean = 30;
			ngift.score = 3000;
			ngift.searchTime = System.currentTimeMillis() + 1000 * 60 * 60;
			ngift.seizeTime = System.currentTimeMillis() - 1000 * 60 * 10;
			ngift.searchCount = 0;
			ngift.remainCount = 10;
			ngift.totalCount = 10;
			ngift.content = "30钻石，5000金币，武器经验卡x6，100块神魂石，10000颗迷魂珠";
			newData.add(ngift);
			IndexGiftNew ng2 = new IndexGiftNew();
			ng2.gameName = "鬼吹灯之挖挖乐";
			ng2.id = 336;
			ng2.status = GiftTypeUtil.STATUS_WAIT_SEARCH;
			ng2.priceType = GiftTypeUtil.PAY_TYPE_BOTN;
			ng2.img = "http://owan-img.ymapp.com/app/11061/icon/icon_1450325761.png_140_140_100.png";
			ng2.name = "高级礼包";
			ng2.isLimit = 0;
			ng2.bean = 30;
			ng2.score = 3000;
			ng2.searchTime = System.currentTimeMillis() + 1000 * 60 * 60;
			ng2.seizeTime = System.currentTimeMillis() - 1000 * 60 * 10;
			ng2.searchCount = 0;
			ng2.remainCount = 159;
			ng2.totalCount = 350;
			ng2.content = "30钻石，5000金币，武器经验卡x6，100块神魂石，10000颗迷魂珠";
			newData.add(ng2);
			IndexGiftNew ng3 = new IndexGiftNew();
			ng3.gameName = "兽人战争";
			ng3.id = 337;
			ng3.status = GiftTypeUtil.STATUS_SEARCH;
			ng3.priceType = GiftTypeUtil.PAY_TYPE_SCORE;
			ng3.img = "http://owan-img.ymapp.com/app/11058/icon/icon_1450059064.png_140_140_100.png";
			ng3.name = "高级礼包";
			ng3.isLimit = 0;
			ng3.score = 1500;
			ng3.searchTime = System.currentTimeMillis() - 1000 * 60 * 30;
			ng3.seizeTime = System.currentTimeMillis() - 1000 * 60 * 60;
			ng3.searchCount = 355;
			ng3.remainCount = 0;
			ng3.totalCount = 350;
			ng3.content = "30钻石，5000金币，武器经验卡x6，100块神魂石，10000颗迷魂珠";
			newData.add(ng3);
			for (int i = 0; i < 10; i++) {
				IndexGiftNew ng = new IndexGiftNew();
				ng.gameName = "逍遥西游";
				ng.id = i;
				ng.status = GiftTypeUtil.STATUS_SEIZE;
				ng.priceType = GiftTypeUtil.PAY_TYPE_SCORE;
				ng.img = "http://owan-img.ymapp.com/app/10657/icon/icon_1450246643.png_140_140_100.png";
				ng.name = "普通礼包";
				ng.isLimit = 0;
				ng.score = (int) (Math.random() * 100) * 10;
				ng.searchTime = System.currentTimeMillis() + 1000 * 60 * 60;
				ng.seizeTime = System.currentTimeMillis() + 1000 * 30 * 30;
				ng.searchCount = 0;
				ng.remainCount = 100;
				ng.totalCount = 100;
				ng.content = "30钻石，5000金币，武器经验卡x6，100块神魂石，10000颗迷魂珠";
				newData.add(ng);
			}
			result.add(newData);
		}

		return result;
	}

	private ArrayList<ArrayList<IndexGiftNew>> initStashNewData() {
		// 先暂时使用缓存数据假定
		ArrayList<ArrayList<IndexGiftNew>> result = new ArrayList<>();
		for (int k = 0; k < 3; k++) {
			ArrayList<IndexGiftNew> newData = new ArrayList<IndexGiftNew>();

			IndexGiftNew ngift = new IndexGiftNew();
			ngift.gameName = "全民神将-攻城战";
			ngift.id = 335;
			ngift.status = GiftTypeUtil.STATUS_SEIZE;
			ngift.priceType = GiftTypeUtil.PAY_TYPE_BOTN;
			ngift.img = "http://owan-img.ymapp.com/app/10986/icon/icon_1449227350.png_140_140_100.png";
			ngift.name = "至尊礼包";
			ngift.isLimit = 1;
			ngift.bean = 30;
			ngift.score = 3000;
			ngift.searchTime = System.currentTimeMillis() + 1000 * 60 * 60;
			ngift.seizeTime = System.currentTimeMillis() - 1000 * 60 * 10;
			ngift.searchCount = 0;
			ngift.remainCount = 10;
			ngift.totalCount = 10;
			ngift.content = "30钻石，5000金币，武器经验卡x6，100块神魂石，10000颗迷魂珠";
			newData.add(ngift);
			IndexGiftNew ng2 = new IndexGiftNew();
			ng2.gameName = "鬼吹灯之挖挖乐";
			ng2.id = 336;
			ng2.status = GiftTypeUtil.STATUS_WAIT_SEARCH;
			ng2.priceType = GiftTypeUtil.PAY_TYPE_BOTN;
			ng2.img = "http://owan-img.ymapp.com/app/11061/icon/icon_1450325761.png_140_140_100.png";
			ng2.name = "高级礼包";
			ng2.isLimit = 0;
			ng2.bean = 30;
			ng2.score = 3000;
			ng2.searchTime = System.currentTimeMillis() + 1000 * 60 * 60;
			ng2.seizeTime = System.currentTimeMillis() - 1000 * 60 * 10;
			ng2.searchCount = 0;
			ng2.remainCount = 159;
			ng2.totalCount = 350;
			ng2.content = "30钻石，5000金币，武器经验卡x6，100块神魂石，10000颗迷魂珠";
			newData.add(ng2);
			IndexGiftNew ng3 = new IndexGiftNew();
			ng3.gameName = "兽人战争";
			ng3.id = 337;
			ng3.status = GiftTypeUtil.STATUS_WAIT_SEARCH;
			ng3.priceType = GiftTypeUtil.PAY_TYPE_SCORE;
			ng3.img = "http://owan-img.ymapp.com/app/11058/icon/icon_1450059064.png_140_140_100.png";
			ng3.name = "高级礼包";
			ng3.isLimit = 0;
			ng3.score = 1500;
			ng3.searchTime = System.currentTimeMillis() - 1000 * 60 * 30;
			ng3.seizeTime = System.currentTimeMillis() - 1000 * 60 * 60;
			ng3.searchCount = 355;
			ng3.remainCount = 0;
			ng3.totalCount = 350;
			ng3.content = "30钻石，5000金币，武器经验卡x6，100块神魂石，10000颗迷魂珠";
			newData.add(ng3);
			for (int i = 0; i < 10; i++) {
				IndexGiftNew ng = new IndexGiftNew();
				ng.id = i;
				ng.status = GiftTypeUtil.STATUS_WAIT_SEIZE;
				ng.priceType = GiftTypeUtil.PAY_TYPE_SCORE;
				ng.gameName = "逍遥西游";
				ng.img = "http://owan-img.ymapp.com/app/10657/icon/icon_1450246643.png_140_140_100.png";
				ng.name = "普通礼包";
				ng.isLimit = 0;
				ng.score = (int) (Math.random() * 100) * 10;
				ng.searchTime = System.currentTimeMillis() + 1000 * 60 * 60;
				ng.seizeTime = System.currentTimeMillis() + 1000 * 30 * 30;
				ng.searchCount = 0;
				ng.remainCount = 100;
				ng.totalCount = 100;
				ng.content = "30钻石，5000金币，武器经验卡x6，100块神魂石，10000颗迷魂珠";
				newData.add(ng);
			}
			result.add(newData);
		}

		return result;
	}

	private IndexGiftNew initStashGiftDetail() {
		// 先暂时使用缓存数据假定
		ArrayList<IndexGiftNew> newData = new ArrayList<>();
		IndexGiftNew ng1 = new IndexGiftNew();
		ng1.gameName = "全民神将-攻城战";
		ng1.id = 335;
		ng1.status = GiftTypeUtil.STATUS_WAIT_SEARCH;
		ng1.priceType = GiftTypeUtil.PAY_TYPE_BOTN;
		ng1.img = "http://owan-img.ymapp.com/app/10986/icon/icon_1449227350.png_140_140_100.png";
		ng1.name = "至尊礼包";
		ng1.isLimit = 1;
		ng1.bean = 30;
		ng1.score = 3000;
		ng1.searchTime = System.currentTimeMillis() + 1000 * 60 * 60;
		ng1.seizeTime = System.currentTimeMillis() - 1000 * 60 * 10;
		ng1.useDeadline = "2015.12.10 12:00 ~ 2016.12.10 12:00";
		ng1.searchCount = 0;
		ng1.remainCount = 10;
		ng1.totalCount = 10;
		ng1.content = "30钻石，5000金币，武器经验卡x6，100块神魂石，10000颗迷魂珠";
		ng1.note = "[1] 点击主界面右下角“设置”按钮\n[2] 点击“兑换礼包”";
		newData.add(ng1);
		IndexGiftNew ng2 = new IndexGiftNew();
		ng2.gameName = "鬼吹灯之挖挖乐";
		ng2.id = 336;
		ng2.status = GiftTypeUtil.STATUS_SEIZE;
		ng2.priceType = GiftTypeUtil.PAY_TYPE_BEAN;
		ng2.img = "http://owan-img.ymapp.com/app/11061/icon/icon_1450325761.png_140_140_100.png";
		ng2.name = "高级礼包";
		ng2.isLimit = 1;
		ng2.bean = 30;
		ng2.searchTime = System.currentTimeMillis() + 1000 * 60 * 60;
		ng2.seizeTime = System.currentTimeMillis() - 1000 * 60 * 10;
		ng2.useDeadline = "2015.12.09 9:30 ~ 2016.12.09 9:30";
		ng2.searchCount = 0;
		ng2.remainCount = 159;
		ng2.totalCount = 350;
		ng2.content = "30钻石，5000金币，武器经验卡x6，100块神魂石，10000颗迷魂珠";
		ng2.note = "[1] 点击主界面右下角“设置”按钮\n[2] 点击“兑换礼包”";
		newData.add(ng2);
		IndexGiftNew ng3 = new IndexGiftNew();
		ng3.gameName = "兽人战争";
		ng3.id = 337;
		ng3.status = GiftTypeUtil.STATUS_SEIZE;
		ng3.priceType = GiftTypeUtil.PAY_TYPE_SCORE;
		ng3.img = "http://owan-img.ymapp.com/app/11058/icon/icon_1450059064.png_140_140_100.png";
		ng3.name = "高级礼包";
		ng3.useDeadline = "2015.12.09 9:30 ~ 2016.12.09 9:30";
		ng3.isLimit = 0;
		ng3.score = 1500;
		ng3.searchTime = System.currentTimeMillis() - 1000 * 60 * 30;
		ng3.seizeTime = System.currentTimeMillis() - 1000 * 60 * 60;
		ng3.searchCount = 355;
		ng3.remainCount = 0;
		ng3.totalCount = 350;
		ng3.content = "30钻石，5000金币，武器经验卡x6，100块神魂石，10000颗迷魂珠";
		ng3.note = "[1] 点击主界面右下角“设置”按钮\n[2] 点击“兑换礼包”";
		newData.add(ng3);

		for (IndexGiftNew gift : newData) {
			if (gift.id == detailId) {
				return gift;
			}
		}
		return newData.get((int) (Math.random() * 3));
	}

	private ArrayList<IndexGiftLike> initStashLikeData() {
		ArrayList<IndexGiftLike> like = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			IndexGiftLike game = new IndexGiftLike();
			game.name = "口袋妖怪复刻";
			game.totalCount = 10 - i;
			game.newCount = i;
			game.img = "http://owan-img.ymapp.com/app/10946/icon/icon_1439432439.png_140_140_100.png";
			game.playCount = (int) (Math.random() * 20000);
			game.size = 0.5f * (int) (Math.random() * 100) + "M";
			like.add(game);
		}
		return like;
	}

}
