package com.oplay.giftassistant.ui.activity;

import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.config.AppDebugConfig;
import com.oplay.giftassistant.config.Global;
import com.oplay.giftassistant.engine.NetEngine;
import com.oplay.giftassistant.model.data.resp.IndexGiftNew;
import com.oplay.giftassistant.model.json.JsonRespLimitGift;
import com.oplay.giftassistant.model.json.base.JsonReqBase;
import com.oplay.giftassistant.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftassistant.ui.fragment.GiftListContainerFragment;
import com.oplay.giftassistant.ui.fragment.NetErrorFragment;
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
	public static final String KEY_GAME_NAME = "key_game_name";
	public static final String KEY_GIFT_NAME = "key_gift_name";

	private NetEngine mEngine;
	private NetErrorFragment mNetErrorFragment;
	private GiftListContainerFragment mLimitFragment;
	// 0 礼包详情
	// 1 今日限量礼包
	// 2 新鲜出炉礼包
	private int type = 0;
	private String gameName;
	private String giftName;


	@Override
	protected void initView() {
		setContentView(R.layout.activity_common_with_back);

		if(getIntent() != null) {
			type = getIntent().getIntExtra(KEY_TYPE, 0);
			if (type == 0) {
				gameName = getIntent().getStringExtra(KEY_GAME_NAME);
				giftName = getIntent().getStringExtra(KEY_GIFT_NAME);
			}
		}
	}

	@Override
	protected void initMenu(@NonNull Toolbar toolbar) {
		super.initMenu(toolbar);
		switch (type) {
			case 0:
				setBarTitle(String.format("[%s]%s", gameName, giftName));
				break;
			case 1:
				setBarTitle("今日限量礼包");
				break;
			case 2:
				setBarTitle("新鲜出炉礼包");
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
				} else if (type == 2){
					loadNewGiftData();
				} else {
					loadGiftDetailData();
				}
			}
		}).start();
	}

	private void loadGiftDetailData() {
	}

	private void loadNewGiftData() {
		mEngine.obtainGiftNew(new JsonReqBase<String>()).enqueue(new Callback<JsonRespLimitGift>() {
			@Override
			public void onResponse(Response<JsonRespLimitGift> response, Retrofit retrofit) {
				if (response != null && response.code() == 200) {
					displayDataUI(response.body().getData());
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
				displayDataUI(data);
			}
		});
	}

	private void loadLimitGiftData() {
		mEngine.obtainGiftLimit(new JsonReqBase<String>()).enqueue(new Callback<JsonRespLimitGift>() {
			@Override
			public void onResponse(Response<JsonRespLimitGift> response, Retrofit retrofit) {
				if (response != null && response.code() == 200) {
					displayDataUI(response.body().getData());
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
				displayDataUI(data);
			}
		});
	}

	private void displayDataUI(ArrayList<ArrayList<IndexGiftNew>> data) {
		if (mLimitFragment == null) {
			mLimitFragment = GiftListContainerFragment.newInstance(data);
		}
		reattachFrag(R.id.fl_container, mLimitFragment, mLimitFragment.getClass().getSimpleName());
	}

	/**
	 * 显示网络错误提示
	 */
	private void displayNetworkErrUI() {
		if (mNetErrorFragment == null) {
			mNetErrorFragment = NetErrorFragment.newInstance();
		}
		reattachFrag(R.id.fl_container, mNetErrorFragment, mNetErrorFragment.getClass().getSimpleName());
	}

	private ArrayList<ArrayList<IndexGiftNew>> initStashLimitData() {
		// 先暂时使用缓存数据假定
		ArrayList<ArrayList<IndexGiftNew>> result = new ArrayList<>();
		for (int k = 0; k< 5; k++) {
			ArrayList<IndexGiftNew> newData = new ArrayList<IndexGiftNew>();

			IndexGiftNew ngift = new IndexGiftNew();
			ngift.gameName = "全民神将-攻城战";
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
			ng3.img = "http://owan-img.ymapp.com/app/11058/icon/icon_1450059064.png_140_140_100.png";
			ng3.name = "高级礼包";
			ng3.isLimit = 0;
			ng3.bean = -1;
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
				ng.img = "http://owan-img.ymapp.com/app/10657/icon/icon_1450246643.png_140_140_100.png";
				ng.name = "普通礼包";
				ng.isLimit = 0;
				ng.bean = -1;
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
		for (int k = 0; k< 3; k++) {
			ArrayList<IndexGiftNew> newData = new ArrayList<IndexGiftNew>();

			IndexGiftNew ngift = new IndexGiftNew();
			ngift.gameName = "全民神将-攻城战";
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
			ng3.img = "http://owan-img.ymapp.com/app/11058/icon/icon_1450059064.png_140_140_100.png";
			ng3.name = "高级礼包";
			ng3.isLimit = 0;
			ng3.bean = -1;
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
				ng.img = "http://owan-img.ymapp.com/app/10657/icon/icon_1450246643.png_140_140_100.png";
				ng.name = "普通礼包";
				ng.isLimit = 0;
				ng.bean = -1;
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

}
