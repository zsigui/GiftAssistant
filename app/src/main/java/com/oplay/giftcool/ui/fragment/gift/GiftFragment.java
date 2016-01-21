package com.oplay.giftcool.ui.fragment.gift;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bigkoo.convenientbanner.ConvenientBanner;
import com.bigkoo.convenientbanner.holder.CBViewHolderCreator;
import com.bigkoo.convenientbanner.listener.OnItemClickListener;
import com.litesuits.common.utils.PackageUtil;
import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.IndexGiftLikeAdapter;
import com.oplay.giftcool.adapter.IndexGiftLimitAdapter;
import com.oplay.giftcool.adapter.NestedGiftListAdapter;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.BannerTypeUtil;
import com.oplay.giftcool.config.GiftTypeUtil;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.StatusCode;
import com.oplay.giftcool.manager.ObserverManager;
import com.oplay.giftcool.model.NetworkImageHolderView;
import com.oplay.giftcool.model.data.req.ReqIndexGift;
import com.oplay.giftcool.model.data.resp.IndexBanner;
import com.oplay.giftcool.model.data.resp.IndexGift;
import com.oplay.giftcool.model.data.resp.IndexGiftLike;
import com.oplay.giftcool.model.data.resp.IndexGiftLimit;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Refresh;
import com.oplay.giftcool.ui.widget.NestedListView;
import com.oplay.giftcool.ui.widget.transformer.ZoomStackPageTransformer;
import com.oplay.giftcool.util.DateUtil;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.NetworkUtil;
import com.socks.library.KLog;

import java.io.Serializable;
import java.util.ArrayList;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * 主页-礼包页面主要内容页，不承担网络请求任务
 *
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/13
 */
public class GiftFragment extends BaseFragment_Refresh implements View.OnClickListener, OnItemClickListener {

	private static final String KEY_BANNER = "key_banner";
	private static final String KEY_LIKE = "key_like";
	private static final String KEY_LIMIT = "key_limit";
	private static final String KEY_NEW = "key_new";

	private ScrollView mScrollView;
	// 活动视图, 3张
	private ConvenientBanner mBanner;
	// 猜你喜欢
	private LinearLayout llLike;
	private RelativeLayout mLikeBar;
	private RecyclerView mLikeView;
	// 今日限量
	private RelativeLayout mLimitBar;
	private RecyclerView mLimitView;
	// 今日出炉
	private RelativeLayout mNewBar;
	private NestedListView mNewView;


	private IndexGiftLikeAdapter mLikeAdapter;
	private IndexGiftLimitAdapter mLimitAdapter;
	private NestedGiftListAdapter mNewAdapter;

	// 礼物界面数据
	private IndexGift mGiftData;
	// 请求后游戏键值的MD5串
	private String mGameKey;


	public static GiftFragment newInstance() {
		return new GiftFragment();
	}

	public static GiftFragment newInstance(ArrayList<IndexBanner> banners,
	                                       ArrayList<IndexGiftLike> likeGames,
	                                       ArrayList<IndexGiftLimit> limitGifts,
	                                       ArrayList<IndexGiftNew> newGifts) {
		GiftFragment fragment = new GiftFragment();
		Bundle bundle = new Bundle();
		bundle.putSerializable(KEY_BANNER, banners);
		bundle.putSerializable(KEY_LIKE, likeGames);
		bundle.putSerializable(KEY_LIMIT, limitGifts);
		bundle.putSerializable(KEY_NEW, newGifts);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		initViewManger(R.layout.fragment_gifts);

		mScrollView = getViewById(R.id.sv_container);
		mBanner = getViewById(R.id.banner);
		llLike = getViewById(R.id.ll_like);
		mLikeBar = getViewById(R.id.rl_hot_all);
		mLikeView = getViewById(R.id.rv_like_content);
		mLimitBar = getViewById(R.id.rl_limit_all);
		mLimitView = getViewById(R.id.rv_limit_content);
		mNewBar = getViewById(R.id.rl_new_all);
		mNewView = getViewById(R.id.rv_new_content);
		((TextView) getViewById(R.id.tv_limit_hint)).setText(Html.fromHtml("(每天<font " +
				"color='#F86060'>20:00</font>更新10款)"));
	}

	@Override
	protected void setListener() {
		mLikeBar.setOnClickListener(this);
		mLimitBar.setOnClickListener(this);
		mNewBar.setOnClickListener(this);
		ObserverManager.getInstance().addGiftUpdateListener(this);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void processLogic(Bundle savedInstanceState) {

		// 设置RecyclerView的LayoutManager
		LinearLayoutManager llmLike = new LinearLayoutManager(getContext());
		llmLike.setOrientation(LinearLayoutManager.HORIZONTAL);
		LinearLayoutManager llmLimit = new LinearLayoutManager(getContext());
		llmLimit.setOrientation(LinearLayoutManager.HORIZONTAL);

		mLikeView.setLayoutManager(llmLike);
		mLimitView.setLayoutManager(llmLimit);
		mLikeAdapter = new IndexGiftLikeAdapter(mLikeView);
		mLimitAdapter = new IndexGiftLimitAdapter(mLimitView);
		mNewAdapter = new NestedGiftListAdapter(getActivity());
		if (AssistantApp.getInstance().isAllowDownload()) {
			llLike.setVisibility(View.VISIBLE);
		} else {
			llLike.setVisibility(View.GONE);
		}

		// 加载数据

		if (getArguments() != null) {

			Serializable s;
			s = getArguments().getSerializable(KEY_BANNER);
			if (s != null) {
				updateBanners((ArrayList<IndexBanner>) s);
			}
			s = getArguments().getSerializable(KEY_LIKE);
			if (s != null) {
				mLikeAdapter.setDatas((ArrayList<IndexGiftLike>) s);
			}
			s = getArguments().getSerializable(KEY_LIMIT);
			if (s != null) {
				mLimitAdapter.setDatas((ArrayList<IndexGiftNew>) s);
			}
			s = getArguments().getSerializable(KEY_NEW);
			if (s != null) {
				mNewAdapter.setData((ArrayList<IndexGiftNew>) s);
			}
		}

		mLikeView.setAdapter(mLikeAdapter);
		mLimitView.setAdapter(mLimitAdapter);
		mNewView.setAdapter(mNewAdapter);

		mIsPrepared = true;
		mScrollView.smoothScrollTo(0, 0);
	}

	private void loadBanner(ArrayList<IndexBanner> banners) {
		if (banners == null) {
			return;
		}
		if (mGiftData != null) {
			mGiftData.banner = banners;
		}
		ArrayList<String> data = new ArrayList<>();
		for (IndexBanner banner : banners) {
			data.add(banner.url);
		}
		if (data.size() == 0) {
			data.add("drawable://" + R.drawable.ic_banner_default);
		}
		mBanner.setPages(new CBViewHolderCreator<NetworkImageHolderView>() {

			@Override
			public NetworkImageHolderView createHolder() {
				return new NetworkImageHolderView();
			}
		}, data)
				.setPageIndicator(new int[]{R.drawable.ic_banner_point_normal, R.drawable.ic_banner_point_selected})
				.setPageIndicatorAlign(ConvenientBanner.PageIndicatorAlign.CENTER_HORIZONTAL)
				.setOnItemClickListener(this);
		if (data.size() == 1) {
			mBanner.setCanLoop(false);
		} else {
			mBanner.setCanLoop(true);
			mBanner.setScrollDuration(500);
			mBanner.getViewPager().setPageTransformer(true, new ZoomStackPageTransformer());
		}

	}


	@Override
	protected void lazyLoad() {

		refreshInitConfig();

		new Thread(new Runnable() {
			@Override
			public void run() {
				if (!NetworkUtil.isConnected(getContext())) {
					refreshFailEnd();
					return;
				}
				ReqIndexGift data = new ReqIndexGift();
				data.appNames = PackageUtil.getInstalledAppName(getContext());
				JsonReqBase<ReqIndexGift> reqData = new JsonReqBase<ReqIndexGift>(data);
				Global.getNetEngine().obtainIndexGift(reqData).enqueue(new Callback<JsonRespBase<IndexGift>>() {
					@Override
					public void onResponse(Response<JsonRespBase<IndexGift>> response, Retrofit retrofit) {
						if (response != null && response.isSuccess()) {
							if (response.body() != null && response.body().getCode() == StatusCode.SUCCESS) {
								// 获取数据成功
								updateData(response.body().getData());
								refreshSuccessEnd();
								return;
							}
						}
						refreshFailEnd();
					}

					@Override
					public void onFailure(Throwable t) {
						if (AppDebugConfig.IS_DEBUG) {
							KLog.e(t);
						}
						refreshFailEnd();
						updateData(initStashGiftData());
					}
				});
			}
		}).start();
	}


	/* 更新控件数据 start */

	public void updateData(IndexGift data) {
		if (data == null) {
			return;
		}
		mViewManager.showContent();
		mHasData = true;
		mGiftData = data;
		updateBanners(data.banner);
		updateLikeData(data.like);
		updateLimitData(data.limit);
		updateNewData(data.news);
		mScrollView.smoothScrollTo(0, 0);
	}

	public void updateBanners(ArrayList<IndexBanner> banners) {
		loadBanner(banners);
	}

	public void updateLikeData(ArrayList<IndexGiftLike> likeData) {
		if (likeData == null) {
			return;
		}
		mLikeAdapter.updateData(likeData);
	}

	public void updateLimitData(ArrayList<IndexGiftNew> limitData) {
		if (limitData == null) {
			return;
		}
		mLimitAdapter.updateData(limitData);
	}

	public void updateNewData(ArrayList<IndexGiftNew> newData) {
		if (newData == null) {
			return;
		}
		mNewAdapter.updateData(newData);
	}

	private void startClockService() {
		/*Intent intent = new Intent(getContext(), ClockService.class);
		getContext().startService(intent);*/
	}

	private void stopClockService() {
		/*Intent intent = new Intent(getContext(), ClockService.class);
		getContext().stopService(intent);*/
	}

	@Override
	public void onResume() {
		super.onResume();
		startClockService();
		if (mBanner != null){
			mBanner.startTurning(3000);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		stopClockService();
		if (mBanner != null) {
			mBanner.stopTurning();
		}
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (hidden) {
			stopClockService();
		} else {
			startClockService();
		}
	}

	/* 更新控件数据 end */

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.rl_hot_all:
				IntentUtil.jumpGiftHotList(getContext(), mGameKey);
				break;
			case R.id.rl_limit_all:
				IntentUtil.jumpGiftLimitList(getContext());
				break;
			case R.id.rl_new_all:
				IntentUtil.jumpGiftNewList(getContext());
				break;
		}
	}

	@Override
	public void onItemClick(int position) {
		if (mGiftData == null || mGiftData.banner == null || mGiftData.banner.size() <= position) {
			return;
		}
		BannerTypeUtil.handleBanner(getContext(), mGiftData.banner.get(position));
	}

	/**
	 * 此处自定义假数据显示，实际可以将数据每次获取数据写入文件以待无网使用，看具体情况
	 */
	private IndexGift initStashGiftData() {
		mHasData = true;
		// 先暂时使用缓存数据假定
		ArrayList<IndexBanner> bannerData = new ArrayList<IndexBanner>();
		ArrayList<IndexGiftNew> limitData = new ArrayList<IndexGiftNew>();
		ArrayList<IndexGiftLike> likeData = new ArrayList<IndexGiftLike>();
		ArrayList<IndexGiftNew> newData = new ArrayList<IndexGiftNew>();

		IndexBanner banner1 = new IndexBanner();
		banner1.url = "http://owan-avatar.ymapp.com/avatar/upload/www/2015/12/28/1451266522_48a10badcdbd.jpg";
		bannerData.add(banner1);
		IndexBanner banner2 = new IndexBanner();
		banner2.url = "http://owan-avatar.ymapp.com/avatar/upload/www/2015/12/22/1450752589_814869b92f05.jpg";
		bannerData.add(banner2);
		IndexBanner banner3 = new IndexBanner();
		banner3.url = "http://owan-avatar.ymapp.com/avatar/upload/www/2015/12/23/1450833623_8e099a40a742.jpg";
		bannerData.add(banner3);

		IndexGiftNew ngift = new IndexGiftNew();
		ngift.gameName = "全民神将-攻城战";
		ngift.id = 335;
		ngift.status = GiftTypeUtil.STATUS_SEIZE;
		ngift.priceType = GiftTypeUtil.PAY_TYPE_BOTH;
		ngift.img = "http://owan-avatar.ymapp.com/app/10986/icon/icon_1449227350.png_140_140_100.png";
		ngift.name = "至尊礼包";
		ngift.isLimit = true;
		ngift.bean = 5;
		ngift.score = 5;
		ngift.searchTime = DateUtil.getDate("yyyy-MM-dd HH:mm", 3);
		ngift.seizeTime = DateUtil.getDate("yyyy-MM-dd HH:mm", 1);
		ngift.searchCount = 0;
		ngift.remainCount = 10;
		ngift.totalCount = 10;
		ngift.content = "30钻石，5000金币，武器经验卡x6，100块神魂石，10000颗迷魂珠";
		newData.add(ngift);
		IndexGiftNew ng2 = new IndexGiftNew();
		ng2.gameName = "鬼吹灯之挖挖乐";
		ng2.id = 336;
		ng2.status = GiftTypeUtil.STATUS_FINISHED;
		ng2.priceType = GiftTypeUtil.PAY_TYPE_BOTH;
		ng2.img = "http://owan-avatar.ymapp.com/app/11061/icon/icon_1450325761.png_140_140_100.png";
		ng2.name = "高级礼包";
		ng2.isLimit = false;
		ng2.bean = 30;
		ng2.score = 5;
		ng2.searchTime = DateUtil.getDate("yyyy-MM-dd HH:mm", -3);
		ng2.seizeTime = DateUtil.getDate("yyyy-MM-dd HH:mm", -1);
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
		ng3.img = "http://owan-avatar.ymapp.com/app/11058/icon/icon_1450059064.png_140_140_100.png";
		ng3.name = "高级礼包";
		ng3.isLimit = false;
		ng3.score = 10;
		ng3.searchTime = DateUtil.getDate("yyyy-MM-dd HH:mm", 3);
		ng3.seizeTime = DateUtil.getDate("yyyy-MM-dd HH:mm", 3);
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
			game.img = "http://owan-avatar.ymapp.com/app/10946/icon/icon_1439432439.png_140_140_100.png";
			likeData.add(game);
			IndexGiftNew gift = new IndexGiftNew();
			gift.gameName = "少年三国志";
			gift.name = "传奇礼包";
			gift.img = "http://owan-avatar.ymapp.com/app/b6/41/8113/icon/icon_1431085220.png_140_140_100.png";
			gift.remainCount = i * 10 + i;
			limitData.add(gift);
			IndexGiftNew ng = new IndexGiftNew();
			ng.gameName = "逍遥西游";
			ng.id = i;
			ng.status = GiftTypeUtil.STATUS_WAIT_SEARCH;
			ng.priceType = GiftTypeUtil.PAY_TYPE_SCORE;
			ng.img = "http://owan-avatar.ymapp.com/app/10657/icon/icon_1450246643.png_140_140_100.png";
			ng.name = "普通礼包";
			ng.isLimit = false;
			ng.score = (int) (Math.random() * 100) * 10;
			ng.searchTime = DateUtil.getDate("yyyy-MM-dd HH:mm", 3);
			ng.seizeTime = DateUtil.getDate("yyyy-MM-dd HH:mm", 5);
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
}
