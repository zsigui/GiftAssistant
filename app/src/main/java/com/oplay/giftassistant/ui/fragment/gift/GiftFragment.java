package com.oplay.giftassistant.ui.fragment.gift;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.litesuits.common.utils.PackageUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.oplay.giftassistant.R;
import com.oplay.giftassistant.adapter.IndexGiftLikeAdapter;
import com.oplay.giftassistant.adapter.IndexGiftLimitAdapter;
import com.oplay.giftassistant.adapter.IndexGiftNewAdapter;
import com.oplay.giftassistant.config.AppDebugConfig;
import com.oplay.giftassistant.config.GiftTypeUtil;
import com.oplay.giftassistant.config.Global;
import com.oplay.giftassistant.config.StatusCode;
import com.oplay.giftassistant.manager.ObserverManager;
import com.oplay.giftassistant.model.data.req.ReqIndexGift;
import com.oplay.giftassistant.model.data.resp.IndexGift;
import com.oplay.giftassistant.model.data.resp.IndexGiftBanner;
import com.oplay.giftassistant.model.data.resp.IndexGiftLike;
import com.oplay.giftassistant.model.data.resp.IndexGiftLimit;
import com.oplay.giftassistant.model.data.resp.IndexGiftNew;
import com.oplay.giftassistant.model.json.base.JsonReqBase;
import com.oplay.giftassistant.model.json.base.JsonRespBase;
import com.oplay.giftassistant.ui.activity.GiftListActivity;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment_Refresh;
import com.oplay.giftassistant.ui.widget.NestedListView;
import com.oplay.giftassistant.util.DateUtil;
import com.oplay.giftassistant.util.NetworkUtil;
import com.socks.library.KLog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cn.bingoogolapple.bgabanner.BGABanner;
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
public class GiftFragment extends BaseFragment_Refresh implements View.OnClickListener {

	private static final String KEY_BANNER = "key_banner";
	private static final String KEY_LIKE = "key_like";
	private static final String KEY_LIMIT = "key_limit";
	private static final String KEY_NEW = "key_new";

	private List<View> views;

	private ScrollView mScrollView;
	// 活动视图, 3张
	private BGABanner mBanner;
	// 猜你喜欢
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
	private IndexGiftNewAdapter mNewAdapter;

	// 礼物界面数据
	private IndexGift mGiftData;


	public static GiftFragment newInstance() {
		return new GiftFragment();
	}

	public static GiftFragment newInstance(ArrayList<IndexGiftBanner> banners,
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

		mRefreshLayout = getViewById(R.id.srl_layout);
		mScrollView = getViewById(R.id.sv_container);
		mBanner = getViewById(R.id.banner);
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
		mRefreshLayout.setDelegate(this);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void processLogic(Bundle savedInstanceState) {

		ObserverManager.getInstance().addGiftUpdateListener(this);

		// 设置Banner
		views = new ArrayList<>(3);
		for (int i = 0; i < 3; i++) {
			View v = View.inflate(mActivity, R.layout.view_banner_img, null);
			views.add(v);
		}
		mBanner.setViews(views);

		// 设置RecyclerView的LayoutManager
		LinearLayoutManager llmLike = new LinearLayoutManager(getContext());
		llmLike.setOrientation(LinearLayoutManager.HORIZONTAL);
		LinearLayoutManager llmLimit = new LinearLayoutManager(getContext());
		llmLimit.setOrientation(LinearLayoutManager.HORIZONTAL);

		mLikeView.setLayoutManager(llmLike);
		mLimitView.setLayoutManager(llmLimit);
		mLikeAdapter = new IndexGiftLikeAdapter(mLikeView);
		mLimitAdapter = new IndexGiftLimitAdapter(mLimitView);
		mNewAdapter = new IndexGiftNewAdapter(getActivity());
		mRefreshLayout.setIsShowLoadingMoreView(false);

		// 加载数据

		if (getArguments() != null) {

			Serializable s;
			s = getArguments().getSerializable(KEY_BANNER);
			if (s != null) {
				updateBanners((ArrayList<IndexGiftBanner>) s);
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

	private void loadBanner(ArrayList<IndexGiftBanner> banners) {
		if (banners == null || banners.size() != views.size()) {
			if (AppDebugConfig.IS_FRAG_DEBUG) {
				KLog.d(AppDebugConfig.TAG_FRAG, "bannerUrls is not to be null and the size need to be 3 : " +
						banners);
			}
			return;
		}
		for (int i = 0; i < views.size(); i++) {
			ImageLoader.getInstance().displayImage(banners.get(i).url, (ImageView) getViewById(views.get(i), R.id
					.iv_image_view));
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
						mIsLoading = false;
						if (response != null && response.isSuccess()) {
							if (response.body() != null && response.body().getCode() == StatusCode.SUCCESS) {
								// 获取数据成功
								refreshSuccessEnd();
								updateData(response.body().getData());
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
						mIsLoading = false;
						//refreshFailEnd();
						updateData(initStashGiftData());
					}
				});
			}
		}).start();
	}


	/* 更新控件数据 start */

	public void updateData(IndexGift data) {
		KLog.e(data);
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

	public void updateBanners(ArrayList<IndexGiftBanner> banners) {
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

	/* 更新控件数据 end */

	@Override
	public void onClick(View v) {
		Intent intent;
		switch (v.getId()) {
			case R.id.rl_hot_all:
	            intent = new Intent(getActivity(), GiftListActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt(GiftListActivity.KEY_TYPE, GiftListActivity.TYPE_LIKE);
                intent.putExtras(bundle);
                startActivity(intent);
				break;
			case R.id.rl_limit_all:
				intent = new Intent(getContext(), GiftListActivity.class);
				intent.putExtra(GiftListActivity.KEY_TYPE, GiftListActivity.TYPE_LIMIT);
				getContext().startActivity(intent);
				break;
			case R.id.rl_new_all:
				intent = new Intent(getContext(), GiftListActivity.class);
				intent.putExtra(GiftListActivity.KEY_TYPE, GiftListActivity.TYPE_NEW);
				getContext().startActivity(intent);
				break;
		}
	}




	/**
	 * 此处自定义假数据显示，实际可以将数据每次获取数据写入文件以待无网使用，看具体情况
	 */
	private IndexGift initStashGiftData() {
		mHasData = true;
		// 先暂时使用缓存数据假定
		ArrayList<IndexGiftBanner> bannerData = new ArrayList<IndexGiftBanner>();
		ArrayList<IndexGiftNew> limitData = new ArrayList<IndexGiftNew>();
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
		ngift.priceType = GiftTypeUtil.PAY_TYPE_BOTH;
		ngift.img = "http://owan-img.ymapp.com/app/10986/icon/icon_1449227350.png_140_140_100.png";
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
		ng2.img = "http://owan-img.ymapp.com/app/11061/icon/icon_1450325761.png_140_140_100.png";
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
		ng3.img = "http://owan-img.ymapp.com/app/11058/icon/icon_1450059064.png_140_140_100.png";
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
			game.img = "http://owan-img.ymapp.com/app/10946/icon/icon_1439432439.png_140_140_100.png";
			likeData.add(game);
			IndexGiftNew gift = new IndexGiftNew();
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
