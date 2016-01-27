package com.oplay.giftcool.ui.fragment.gift;

import android.os.Bundle;
import android.os.Handler;
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
import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.IndexGiftLikeAdapter;
import com.oplay.giftcool.adapter.IndexGiftLimitAdapter;
import com.oplay.giftcool.adapter.NestedGiftListAdapter;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.BannerTypeUtil;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.StatusCode;
import com.oplay.giftcool.manager.ObserverManager;
import com.oplay.giftcool.model.NetworkImageHolderView;
import com.oplay.giftcool.model.data.req.ReqIndexGift;
import com.oplay.giftcool.model.data.req.ReqRefreshGift;
import com.oplay.giftcool.model.data.resp.IndexBanner;
import com.oplay.giftcool.model.data.resp.IndexGift;
import com.oplay.giftcool.model.data.resp.IndexGiftLike;
import com.oplay.giftcool.model.data.resp.IndexGiftLimit;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.service.ClockService;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Refresh;
import com.oplay.giftcool.ui.widget.NestedListView;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.NetworkUtil;
import com.oplay.giftcool.util.SystemUtil;
import com.socks.library.KLog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

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

	private final static String PAGE_NAME = "礼包首页";
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
	// 每隔5分钟刷新一次
	private Handler mHandler = new Handler();
	private Runnable mRefreshRunnable = new Runnable() {
		@Override
		public void run() {
			mIsNotifyRefresh = true;
			lazyLoad();
			mHandler.postDelayed(this, 5 * 60 * 1000);
		}
	};

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
		mHandler.postDelayed(mRefreshRunnable, 5 * 60 * 1000);
	}

	private void loadBanner(ArrayList<IndexBanner> banners) {
		if (banners == null) {
			return;
		}
		if (banners.size() == 0) {
			IndexBanner banner = new IndexBanner();
			banner.url = "drawable://" + R.drawable.ic_banner_empty_default;
			banner.type = BannerTypeUtil.ACTION_SCORE_TASK;
			banners.add(banner);
		}
		if (mGiftData != null) {
			mGiftData.banner = banners;
		}
		ArrayList<String> data = new ArrayList<>();
		for (IndexBanner banner : banners) {
			data.add(banner.url);
		}
		mBanner.setPages(new CBViewHolderCreator<NetworkImageHolderView>() {

			@Override
			public NetworkImageHolderView createHolder() {
				return new NetworkImageHolderView();
			}
		}, data)
				.setPageIndicator(new int[]{0, 0})
				.setPageIndicatorAlign(ConvenientBanner.PageIndicatorAlign.CENTER_HORIZONTAL)
				.setOnItemClickListener(this);
		if (data.size() == 1) {
			mBanner.setCanLoop(false);
		} else {
			mBanner.setCanLoop(true);
			mBanner.setScrollDuration(500);
			//mBanner.getViewPager().setPageTransformer(true, new ZoomStackPageTransformer());
			mBanner.startTurning(3000);
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
				data.appNames = SystemUtil.getInstalledAppName(getContext());
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
					}
				});
			}
		}).start();
	}


	/* 更新控件数据 start */

	public void updateData(IndexGift data) {
		if (data == null) {
			if (!mHasData) {
				mViewManager.showErrorRetry();
			}
			return;
		}
		if (data.limit != null && data.limit.size() == 0
				&& data.news != null && data.news.size() == 0
				&& data.banner != null && data.banner.size() == 0
				&& data.like != null && data.like.size() == 0) {
			// 数据为空
			mViewManager.showEmpty();
			return;
		}
		int y = mScrollView.getScrollY();
		mViewManager.showContent();
		mHasData = true;
		mGiftData = data;
		updateBanners(data.banner);
		updateLikeData(data.like);
		updateLimitData(data.limit);
		updateNewData(data.news);
		mScrollView.smoothScrollTo(0, y);
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

	@Override
	public void onResume() {
		super.onResume();
		ClockService.startService(getContext());
		if (mBanner != null) {
			mBanner.startTurning(3000);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		ClockService.stopService(getContext());
		if (mBanner != null) {
			mBanner.stopTurning();
		}
	}

	@Override
	public void onGiftUpdate() {
		if (mIsSwipeRefresh || mIsNotifyRefresh || mGiftData == null) {
			return;
		}
		mIsNotifyRefresh = true;
		Global.THREAD_POOL.execute(new Runnable() {
			@Override
			public void run() {
				HashSet<Integer> ids = new HashSet<Integer>();
				for (IndexGiftNew gift : mGiftData.limit) {
					ids.add(gift.id);
				}
				for (IndexGiftNew gift : mGiftData.news) {
					ids.add(gift.id);
				}
				ReqRefreshGift reqData = new ReqRefreshGift();
				reqData.ids = ids;
				Global.getNetEngine().refreshGift(new JsonReqBase<ReqRefreshGift>(reqData))
						.enqueue(new Callback<JsonRespBase<HashMap<String, IndexGiftNew>>>() {

							@Override
							public void onResponse(Response<JsonRespBase<HashMap<String, IndexGiftNew>>> response,
							                       Retrofit retrofit) {
								if (!mCanShowUI) {
									return;
								}
								if (response != null && response.isSuccess()) {
									if (response.body() != null && response.body().isSuccess()) {
										// 数据刷新成功，进行更新
										HashMap<String, IndexGiftNew> respData = response.body().getData();
										ArrayList<Integer> waitDelIndexs = new ArrayList<Integer>();
										updateCircle(respData, waitDelIndexs, mGiftData.limit);
										delIndex(mGiftData.limit, waitDelIndexs);
										waitDelIndexs.clear();
										updateCircle(respData, waitDelIndexs, mGiftData.news);
										delIndex(mGiftData.news, waitDelIndexs);
										int y = mScrollView.getScrollY();
										updateLimitData(mGiftData.limit);
										updateNewData(mGiftData.news);
										mScrollView.smoothScrollTo(0, y);
									}
								}
								mIsNotifyRefresh = false;
							}

							@Override
							public void onFailure(Throwable t) {
								mIsNotifyRefresh = false;
							}
						});
			}
		});
	}

	private void delIndex(ArrayList<IndexGiftNew> data, ArrayList<Integer> waitDelIndexs) {
		for (int i = waitDelIndexs.size() - 1; i >= 0; i--) {
			data.remove(waitDelIndexs.get(i).intValue());
		}
	}

	private void updateCircle(HashMap<String, IndexGiftNew> respData, ArrayList<Integer> waitDelIndexs,
	                          ArrayList<IndexGiftNew> gifts) {
		int i = 0;
		for (IndexGiftNew gift : gifts) {
			if (respData.get(gift.id + "") != null) {
				IndexGiftNew item = respData.get(gift.id + "");
				setGiftUpdateInfo(gift, item);
			} else {
				// 找不到，需要被移除
				waitDelIndexs.add(i);
			}
			i++;
		}
	}

	private void setGiftUpdateInfo(IndexGiftNew toBeSet, IndexGiftNew data) {
		toBeSet.status = data.status;
		toBeSet.seizeStatus = data.seizeStatus;
		toBeSet.searchCount = data.searchCount;
		toBeSet.searchTime = data.searchTime;
		toBeSet.totalCount = data.totalCount;
		toBeSet.remainCount = data.remainCount;
		toBeSet.code = data.code;
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (hidden) {
			ClockService.stopService(getContext());
		} else {
			ClockService.startService(getContext());
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mHandler.removeCallbacks(mRefreshRunnable);
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

	@Override
	public String getPageName() {
		return PAGE_NAME;
	}
}
