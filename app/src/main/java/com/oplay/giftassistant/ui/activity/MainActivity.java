package com.oplay.giftassistant.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckedTextView;

import com.litesuits.common.utils.PackageUtil;
import com.oplay.giftassistant.R;
import com.oplay.giftassistant.adapter.util.GiftTypeUtil;
import com.oplay.giftassistant.config.AppDebugConfig;
import com.oplay.giftassistant.config.Global;
import com.oplay.giftassistant.engine.NetEngine;
import com.oplay.giftassistant.model.data.req.ReqIndexGift;
import com.oplay.giftassistant.model.data.resp.IndexGift;
import com.oplay.giftassistant.model.data.resp.IndexGiftBanner;
import com.oplay.giftassistant.model.data.resp.IndexGiftLike;
import com.oplay.giftassistant.model.data.resp.IndexGiftLimit;
import com.oplay.giftassistant.model.data.resp.IndexGiftNew;
import com.oplay.giftassistant.model.json.base.JsonReqBase;
import com.oplay.giftassistant.model.json.base.JsonRespBase;
import com.oplay.giftassistant.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftassistant.ui.fragment.GiftFragment;
import com.oplay.giftassistant.ui.fragment.MoocRecyclerViewFragment;
import com.oplay.giftassistant.ui.fragment.NetErrorFragment;
import com.oplay.giftassistant.ui.widget.search.SearchLayout;
import com.oplay.giftassistant.util.ToastUtil;
import com.socks.library.KLog;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/13
 */
public class MainActivity extends BaseAppCompatActivity {

	private long mLastClickTime = 0;
	// 底部Tabs
	private CheckedTextView[] mCtvs;
	// 礼物Fragment
	private GiftFragment mGiftFragment;
	private Fragment mMoocRecyclerViewFragment;
	// 当前选项卡下标
	private int mCurrentIndex = 0;

	// 网络加载接口
	private NetEngine mEngine;


	// 判断礼物界面是否初始化
	private boolean mHasGiftData = false;
	// 判断礼物界面数据是否在加载中，以防止重复调用
	private boolean mIsGiftDataLoading = false;
	// 礼物界面数据
	private IndexGift mGiftData;

	private static MainActivity sInstance;
	// handler存在泄露可能，此处待优化，或者修改更新方式
	private static Handler mHandler = new Handler() {
		@Override
		@SuppressWarnings("unchecked")
		public void handleMessage(Message msg) {
			if (sInstance == null) {
				return;
			}
			if (msg.what == 1 && sInstance.mGiftData != null) {
				if (sInstance.mCurrentIndex == 0)
					sInstance.updateGift();
			}
		}
	};


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sInstance = this;
	}

	protected void initView() {
		setContentView(R.layout.activity_main);
		CheckedTextView ctvGame = getViewById(R.id.ctv_game);
		CheckedTextView ctvGift = getViewById(R.id.ctv_gift);
		mCtvs = new CheckedTextView[2];
		mCtvs[0] = ctvGift;
		mCtvs[1] = ctvGame;
	}

	@Override
	protected void initMenu(@NonNull Toolbar toolbar) {
		super.initMenu(toolbar);
		SearchLayout searchLayout = getViewById(toolbar, R.id.sl_search);
		if (searchLayout != null) {
			searchLayout.setCanGetFocus(false);
			searchLayout.setOnClickListener(this);
		}
	}

	@Override
	protected void processLogic() {
		for (CheckedTextView ctv : mCtvs) {
			ctv.setOnClickListener(this);
		}

		mEngine = Global.getNetEngine();

		// 加载数据在父类进行，初始先显示加载页面，同时起到占位作用
		setCurSelected(mCurrentIndex);
	}

	private void setCurSelected(int position) {
		for (CheckedTextView ctv : mCtvs) {
			ctv.setChecked(false);
			ctv.setTextColor(getResources().getColor(R.color.co_tab_index_text_normal));
		}
		mCurrentIndex = position;
		mCtvs[position].setChecked(true);
		mCtvs[position].setTextColor(getResources().getColor(R.color.co_tab_index_text_selected));
		if (position == 0) {
			if (mHasGiftData) {
				displayGiftUI();
			} else {
				displayLoadingUI(R.id.fl_container);
				initGiftData();
			}
		} else {
			displayGameUI();
		}
	}

	/**
	 * 更新GiftFragment数据
	 */
	private void updateGift() {
		if (mGiftData == null) {
			return;
		}
		if (mGiftFragment == null) {
			mGiftFragment = GiftFragment.newInstance(mGiftData.banner, mGiftData.like, mGiftData.limit,
					mGiftData.news);
		} else {
			mGiftFragment.updateBanners(mGiftData.banner);
			mGiftFragment.updateLikeData(mGiftData.like);
			mGiftFragment.updateLimitData(mGiftData.limit);
			mGiftFragment.updateNewData(mGiftData.news);
		}
		reattachFrag(R.id.fl_container, mGiftFragment, mGiftFragment.getClass().getSimpleName());
	}


	private void displayGameUI() {
		if (mMoocRecyclerViewFragment == null) {
			mMoocRecyclerViewFragment = MoocRecyclerViewFragment.newInstance();
		}
		reattachFrag(R.id.fl_container, mMoocRecyclerViewFragment,
				mMoocRecyclerViewFragment.getClass().getSimpleName());
	}

	private void displayGiftUI() {
		if (mGiftFragment == null) {
			mGiftFragment = GiftFragment.newInstance();
		}
		reattachFrag(R.id.fl_container, mGiftFragment, mGiftFragment.getClass().getSimpleName());
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		for (int i = 0; i < mCtvs.length; i++) {
			if (v.getId() == mCtvs[i].getId()) {
				setCurSelected(i);
				return;
			}
		}
		switch (v.getId()) {
			case R.id.ctv_gift:
				setCurSelected(0);
				break;
			case R.id.ctv_game:
				setCurSelected(1);
				break;
			case R.id.sl_search:
				Intent intent = new Intent(MainActivity.this, SearchActivity.class);
				startActivity(intent);
				break;
		}
	}

	/**
	 * 启动的时候初始化礼包界面数据
	 */
	public void initGiftData() {
		if (mHasGiftData || mIsGiftDataLoading) {
			return;
		}

		new Thread(new Runnable() {
			@Override
			public void run() {
				mIsGiftDataLoading = true;
				try {
					Thread.sleep((int) (Math.random() * 2000));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				ReqIndexGift data = new ReqIndexGift();
				data.appNames = PackageUtil.getInstalledAppName(getApplicationContext());
				JsonReqBase<ReqIndexGift> reqData = new JsonReqBase<ReqIndexGift>(data);
				mEngine.obtainIndexGift(reqData).enqueue(new Callback<JsonRespBase<IndexGift>>() {
					@Override
					public void onResponse(Response<JsonRespBase<IndexGift>> response, Retrofit retrofit) {
						mIsGiftDataLoading = false;
						if (response.code() == 200 && response.body() != null) {
							// 获取数据成功
							mHasGiftData = true;
							mGiftData = response.body().getData();
							// 通知更新界面
							mHandler.sendEmptyMessage(1);
							return;
						}
						displayNetworkErrUI();
					}

					@Override
					public void onFailure(Throwable t) {
						if (AppDebugConfig.IS_DEBUG) {
							KLog.e(t);
						}
						mIsGiftDataLoading = false;
						// displayNetworkErrUI();
						mGiftData = initStashGiftData();
						mHandler.sendEmptyMessage(1);
					}
				});
			}
		}).start();
	}

	/**
	 * 此处自定义假数据显示，实际可以将数据每次获取数据写入文件以待无网使用，看具体情况
	 */
	private IndexGift initStashGiftData() {
		mHasGiftData = true;
		// 先暂时使用缓存数据假定
		ArrayList<IndexGiftBanner> bannerData = new ArrayList<IndexGiftBanner>();
		ArrayList<IndexGiftLimit> limitData = new ArrayList<IndexGiftLimit>();
		ArrayList<IndexGiftLike> likeData = new ArrayList<IndexGiftLike>();
		ArrayList<IndexGiftNew> newData = new ArrayList<IndexGiftNew>();

		IndexGiftBanner banner1 = new IndexGiftBanner();
		banner1.url = "http://owan-img.ymapp.com/img/upload/www/2015/12/28/1451266522_48a10badcdbd.jpg";
		bannerData.add(banner1);
		IndexGiftBanner banner2 = new IndexGiftBanner();
		banner2.url = "http://owan-img.ymapp.com/img/upload/www/2015/12/22/1450752589_814869b92f05.jpg";
		bannerData.add(banner2);
		IndexGiftBanner banner3 = new IndexGiftBanner();
		banner3.url = "http://owan-img.ymapp.com/img/upload/www/2015/12/23/1450833623_8e099a40a742.jpg";
		bannerData.add(banner3);

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
		ng2.status = GiftTypeUtil.STATUS_FINISHED;
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
			IndexGiftLike game = new IndexGiftLike();
			game.name = "口袋妖怪复刻";
			game.totalCount = 10 - i;
			game.newCount = i;
			game.img = "http://owan-img.ymapp.com/app/10946/icon/icon_1439432439.png_140_140_100.png";
			likeData.add(game);
			IndexGiftLimit gift = new IndexGiftLimit();
			gift.gameName = "少年三国志";
			gift.name = "传奇礼包";
			gift.img = "http://owan-img.ymapp.com/app/b6/41/8113/icon/icon_1431085220.png_140_140_100.png";
			gift.remainCount = i * 10 + i;
			limitData.add(gift);
			IndexGiftNew ng = new IndexGiftNew();
			ng.gameName = "逍遥西游";
			ng.id = i;
			ng.status = GiftTypeUtil.STATUS_WAIT_SEARCH;
			ng.priceType = GiftTypeUtil.PAY_TYPE_SCORE;
			ng.img = "http://owan-img.ymapp.com/app/10657/icon/icon_1450246643.png_140_140_100.png";
			ng.name = "普通礼包";
			ng.isLimit = 0;
			ng.score = (int)(Math.random() * 100) * 10;
			ng.searchTime = System.currentTimeMillis() + 1000 * 60 * 60;
			ng.seizeTime = System.currentTimeMillis() + 1000 * 30 * 30;
			ng.searchCount = 0;
			ng.remainCount = 100;
			ng.totalCount = 100;
			ng.content = "30钻石，5000金币，武器经验卡x6，100块神魂石，10000颗迷魂珠";
			newData.add(ng);
		}

		IndexGift indexGift = new IndexGift();
		indexGift.banner = bannerData;
		indexGift.like = likeData;
		indexGift.limit = limitData;
		indexGift.news = newData;
		return indexGift;
	}

	private void displayNetworkErrUI() {
		// 先暂时显示，后面需要修改
		reattachFrag(R.id.fl_container, NetErrorFragment.newInstance(), NetErrorFragment.class.getSimpleName());
	}

	@Override
	public void onBackPressed() {
		if (System.currentTimeMillis() - mLastClickTime <= 1000) {
			mApp.exit();
			finish();
			System.exit(0);
		} else {
			mLastClickTime = System.currentTimeMillis();
			ToastUtil.showShort("再次点击退出应用");
		}
	}

}
