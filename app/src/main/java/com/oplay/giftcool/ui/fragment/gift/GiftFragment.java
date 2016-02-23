package com.oplay.giftcool.ui.fragment.gift;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bigkoo.convenientbanner.ConvenientBanner;
import com.bigkoo.convenientbanner.holder.CBViewHolderCreator;
import com.bigkoo.convenientbanner.listener.OnItemClickListener;
import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.IndexGiftLikeAdapter;
import com.oplay.giftcool.adapter.IndexGiftLimitAdapter;
import com.oplay.giftcool.adapter.IndexGiftZeroAdapter;
import com.oplay.giftcool.adapter.NestedGiftListAdapter;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.BannerTypeUtil;
import com.oplay.giftcool.config.GiftTypeUtil;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.StatusCode;
import com.oplay.giftcool.manager.ObserverManager;
import com.oplay.giftcool.manager.PayManager;
import com.oplay.giftcool.model.NetworkImageHolderView;
import com.oplay.giftcool.model.data.req.ReqIndexGift;
import com.oplay.giftcool.model.data.req.ReqRefreshGift;
import com.oplay.giftcool.model.data.resp.IndexBanner;
import com.oplay.giftcool.model.data.resp.IndexGift;
import com.oplay.giftcool.model.data.resp.IndexGiftLike;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.service.ClockService;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Refresh;
import com.oplay.giftcool.ui.widget.NestedListView;
import com.oplay.giftcool.ui.widget.button.GiftButton;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.NetworkUtil;
import com.socks.library.KLog;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import retrofit.Call;
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
public class GiftFragment extends BaseFragment_Refresh implements View.OnClickListener, OnItemClickListener,
		com.oplay.giftcool.listener.OnItemClickListener<IndexGiftNew> {

	private final static String PAGE_NAME = "礼包首页";
	private static final int ID_BANNER = 1;
	private static final int ID_ZERO = 2;
	private static final int ID_LIKE = 3;
	private static final int ID_LIMIT = 4;
	private static final int ID_NEWS = 5;
	private static final int ID_ALL = 6;
	private static final int ID_CIRCLE = 7;

	private ScrollView mScrollView;
	// 活动视图, 3张
	private ConvenientBanner mBanner;
	// 0元疯抢
	private RecyclerView mZeroView;
	// 猜你喜欢
	private RelativeLayout mLikeBar;
	private RecyclerView mLikeView;
	// 今日限量
	private RelativeLayout mLimitBar;
	private RecyclerView mLimitView;
	// 今日出炉
	private RelativeLayout mNewBar;
	private NestedListView mNewView;


	private IndexGiftZeroAdapter mZeroAdapter;
	private IndexGiftLikeAdapter mLikeAdapter;
	private IndexGiftLimitAdapter mLimitAdapter;
	private NestedGiftListAdapter mNewAdapter;

	// 礼物界面数据
	private IndexGift mGiftData;
	// 请求后游戏键值的MD5串
	private String mGameKey;
	// 每隔5分钟刷新一次
	private Handler mHandler;
	private Runnable mRefreshRunnable = new Runnable() {
		@Override
		public void run() {
			mIsNotifyRefresh = true;
			lazyLoad();
			mHandler.postDelayed(this, 5 * 60 * 1000);
		}
	};

	public static GiftFragment newInstance() {
		GiftFragment fragment = new GiftFragment();
		fragment.mHandler = new UpdateHandler(fragment);
		return fragment;
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		initViewManger(R.layout.fragment_gifts);
		mScrollView = getViewById(R.id.sv_container);
		mBanner = getViewById(R.id.banner);
		mLikeBar = getViewById(R.id.rl_hot_all);
		mLikeView = getViewById(R.id.rv_like_content);
		mLimitBar = getViewById(R.id.rl_limit_all);
		mLimitView = getViewById(R.id.rv_limit_content);
		mNewBar = getViewById(R.id.rl_new_all);
		mNewView = getViewById(R.id.rv_new_content);
		((TextView) getViewById(R.id.tv_limit_hint)).setText(Html.fromHtml("(每天<font " +
				"color='#f85454'>20:00</font>开抢10款)"));
		mZeroView = getViewById(R.id.rv_zero);
		((TextView) getViewById(R.id.tv_zero_limit)).setText(Html.fromHtml("(每天<font " +
				"color='#f85454'>20:00</font>开抢3款)"));
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
		LinearLayoutManager llmZero = new LinearLayoutManager(getContext());
		llmZero.setOrientation(LinearLayoutManager.HORIZONTAL);
		LinearLayoutManager llmLike = new LinearLayoutManager(getContext());
		llmLike.setOrientation(LinearLayoutManager.HORIZONTAL);
		LinearLayoutManager llmLimit = new LinearLayoutManager(getContext());
		llmLimit.setOrientation(LinearLayoutManager.HORIZONTAL);

		mZeroView.setLayoutManager(llmZero);
		mLikeView.setLayoutManager(llmLike);
		mLimitView.setLayoutManager(llmLimit);
		mZeroAdapter = new IndexGiftZeroAdapter(getContext());
		mLikeAdapter = new IndexGiftLikeAdapter(mLikeView);
		mLimitAdapter = new IndexGiftLimitAdapter(mLimitView);
		mNewAdapter = new NestedGiftListAdapter(getActivity());

		// 加载数据
		mZeroView.setAdapter(mZeroAdapter);
		mLikeView.setAdapter(mLikeAdapter);
		mLimitView.setAdapter(mLimitAdapter);
		mNewView.setAdapter(mNewAdapter);

		mIsPrepared = true;
		mScrollView.smoothScrollTo(0, 0);
		mHandler.postDelayed(mRefreshRunnable, 5 * 60 * 1000);
		mViewManager.showContent();
		mNewAdapter.setListener(this);
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
		mBanner.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (mRefreshLayout != null) {
					switch (event.getAction()) {
						case MotionEvent.ACTION_MOVE:
							mRefreshLayout.setEnabled(false);
							break;
						case MotionEvent.ACTION_UP:
						case MotionEvent.ACTION_CANCEL:
							mRefreshLayout.setEnabled(true);
							break;

					}
				}
				return false;
			}
		});
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Global
				.getBannerHeight(getContext()));
		mBanner.setLayoutParams(lp);
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
			//mBanner.getViewPager().setPageTransformer(true, new ZoomStackPageTransformer());
			mBanner.startTurning(5000);
		}

	}


	@Override
	protected void lazyLoad() {

		refreshInitConfig();

		Global.THREAD_POOL.execute(new Runnable() {
			@Override
			public void run() {
				if (AppDebugConfig.IS_DEBUG) {
					KLog.d(AppDebugConfig.TAG_FRAG, "lazyLoad.Thread start() ");
				}
				ReqIndexGift data = new ReqIndexGift();
				data.appNames = Global.getInstalledAppNames();
				JsonReqBase<ReqIndexGift> reqData = new JsonReqBase<ReqIndexGift>(data);
				if (AppDebugConfig.IS_DEBUG) {
					KLog.d(AppDebugConfig.TAG_FRAG, "lazyLoad.Thread obtain index gift ");
				}
				Global.getNetEngine().obtainIndexGift(reqData).enqueue(new Callback<JsonRespBase<IndexGift>>() {
					@Override
					public void onResponse(Response<JsonRespBase<IndexGift>> response, Retrofit retrofit) {
						// 获取数据成功
						if (!mCanShowUI) {
							return;
						}
						if (AppDebugConfig.IS_DEBUG) {
							KLog.d(AppDebugConfig.TAG_FRAG, "onResponse start() ");
						}
						if (response != null && response.isSuccess()) {
							Global.sServerTimeDiffLocal = System.currentTimeMillis() - response.headers().getDate
									("Date").getTime();
							if (response.body() != null && response.body().getCode() == StatusCode.SUCCESS) {
								// 获取数据成功
								if (AppDebugConfig.IS_DEBUG) {
									KLog.d(AppDebugConfig.TAG_FRAG, "response = " + response.body().getData());
								}
								refreshSuccessEnd();
								updateData(response.body().getData());
								return;
							}
						}
						refreshFailEnd();
					}

					@Override
					public void onFailure(Throwable t) {
						if (!mCanShowUI) {
							return;
						}
						if (AppDebugConfig.IS_DEBUG) {
							KLog.e(t);
						}
						refreshFailEnd();
					}
				});
			}
		});
	}


	/* 更新控件数据 start */

	public void updateData(IndexGift data) {
		if (data == null) {
			if (!mHasData) {
				mViewManager.showErrorRetry();
			}
			return;
		}
		if ((data.limit == null || data.limit.size() == 0)
				&& (data.zero == null || data.zero.size() == 0)
				&& (data.news == null || data.news.size() == 0)
				&& (data.banner == null || data.banner.size() == 0)
				&& (data.like == null || data.like.size() == 0)) {
			// 数据为空
			mViewManager.showEmpty();
			return;
		}
		int y = mScrollView.getScrollY();
		mViewManager.showContent();
		mHasData = true;
		mGiftData = data;
		updateZeroData(data.zero);
		updateBanners(data.banner);
		updateLikeData(data.like);
		updateLimitData(data.limit);
		updateNewData(data.news);
		mScrollView.smoothScrollTo(0, y);
	}

	public void updateBanners(ArrayList<IndexBanner> banners) {
		loadBanner(banners);
	}

	public void updateZeroData(ArrayList<IndexGiftNew> zeroData) {
		if (zeroData == null) {
			return;
		}
		mZeroAdapter.updateData(zeroData);
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
		ClockService.startService(getContext().getApplicationContext());
		if (mBanner != null) {
			mBanner.startTurning(3000);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		ClockService.stopService(getContext().getApplicationContext());
		if (mBanner != null) {
			mBanner.stopTurning();
		}
	}

	private Call<JsonRespBase<HashMap<String, IndexGiftNew>>> mRefreshCall = null;

	@Override
	public void onGiftUpdate(int action) {
		if (action == ObserverManager.STATUS.GIFT_UPDATE_ALL) {
			mRefreshLayout.setRefreshing(true);
			onRefresh();
			return;
		}
		if (action == ObserverManager.STATUS.GIFT_UPDATE_PART) {
			if (mIsSwipeRefresh || mIsNotifyRefresh || mGiftData == null) {
				return;
			}
			mIsNotifyRefresh = true;
			Global.THREAD_POOL.execute(new Runnable() {
				@Override
				public void run() {
					if (!NetworkUtil.isConnected(getContext())) {
						refreshFailEnd();
						return;
					}
					HashSet<Integer> ids = new HashSet<Integer>();
					for (IndexGiftNew gift : mGiftData.limit) {
						ids.add(gift.id);
					}
					for (IndexGiftNew gift : mGiftData.news) {
						ids.add(gift.id);
					}
					ReqRefreshGift reqData = new ReqRefreshGift();
					reqData.ids = ids;
					mRefreshCall = Global.getNetEngine().refreshGift(new JsonReqBase<ReqRefreshGift>(reqData));
					try {
						Response<JsonRespBase<HashMap<String, IndexGiftNew>>> response = mRefreshCall.execute();
						if (response != null && response.isSuccess() && mCanShowUI) {
							if (response.body() != null && response.body().isSuccess()) {
								// 数据刷新成功，进行更新
								HashMap<String, IndexGiftNew> respData = response.body().getData();
								ArrayList<Integer> waitDelIndexs = new ArrayList<Integer>();
								updateCircle(respData, waitDelIndexs, mGiftData.zero);
								delIndex(mGiftData.zero, waitDelIndexs);
								waitDelIndexs.clear();
								updateCircle(respData, waitDelIndexs, mGiftData.limit);
								delIndex(mGiftData.limit, waitDelIndexs);
								waitDelIndexs.clear();
								updateCircle(respData, waitDelIndexs, mGiftData.news);
								delIndex(mGiftData.news, waitDelIndexs);
								Message msg = Message.obtain();
								msg.what = ID_CIRCLE;
								msg.obj = mGiftData;
								mHandler.sendMessage(msg);
								response = null;
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						mIsNotifyRefresh = false;
					}
				}
			});
		}
	}

	public void scrollToPos(final int type) {
		if (mScrollView == null) {
			return;
		}
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				int height = mBanner.getMeasuredHeight();
				switch (type) {
					case 1:
						if (mZeroView != null) {
							mScrollView.smoothScrollTo(0, height);
						}
						break;
					case 2:
						if (mLikeView != null) {
							height += mZeroView.getMeasuredHeight()
									+ mApp.getResources().getDimensionPixelSize(R.dimen.di_index_module_gap);
							mScrollView.smoothScrollTo(0, height);
						}
						break;
					case 3:
						if (mLimitView != null) {
							height += mLikeBar.getMeasuredHeight() * 2
									+ mZeroView.getMeasuredHeight()
									+ mLikeView.getMeasuredHeight()
									+ mApp.getResources().getDimensionPixelSize(R.dimen.di_index_module_gap) * 2;
							mScrollView.smoothScrollTo(0, height);
						}
						break;
					case 4:
						if (mNewView != null) {
							height += mLikeBar.getMeasuredHeight() * 3
									+ mLikeView.getMeasuredHeight() + mZeroView.getMeasuredHeight()
									+ mLimitView.getMeasuredHeight()
									+ mApp.getResources().getDimensionPixelSize(R.dimen.di_index_module_gap) * 3;
							mScrollView.scrollTo(0, height);
						}
						break;
				}
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
		if (mHandler != null) {
			mHandler.removeCallbacks(mRefreshRunnable);
		}
	}

	@Override
	public void release() {
		super.release();
//		AssistantApp.getRefWatcher(getActivity()).watch(mZeroAdapter);
//		AssistantApp.getRefWatcher(getActivity()).watch(mLikeAdapter);
//		AssistantApp.getRefWatcher(getActivity()).watch(mLimitAdapter);
//		AssistantApp.getRefWatcher(getActivity()).watch(mBanner);
		if (mZeroAdapter != null) {
			mZeroAdapter = null;
		}
		if (mHandler != null) {
			mHandler.removeCallbacksAndMessages(null);
			mHandler = null;
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
		IndexBanner banner = mGiftData.banner.get(position);
		AppDebugConfig.trace(getContext(), "礼包首页推荐位", String.format("第%d推广位，标题：%s", position, banner.title));
		BannerTypeUtil.handleBanner(getContext(), banner);
	}

	@Override
	public String getPageName() {
		return PAGE_NAME;
	}

	@Override
	public void onItemClick(IndexGiftNew gift, View v, int position) {
		switch (v.getId()) {
			case R.id.rl_recommend:
				IntentUtil.jumpGiftDetail(getContext(), gift.id);
				break;
			case R.id.btn_send:
				if (gift.giftType == GiftTypeUtil.GIFT_TYPE_ZERO_SEIZE) {
					// 对于0元抢，先跳转到游戏详情
					IntentUtil.jumpGiftDetail(getContext(), gift.id);
				} else {
					PayManager.getInstance().seizeGift(getContext(), gift, (GiftButton) v);
				}
				break;
		}
	}

	private static class UpdateHandler extends Handler {

		private WeakReference<GiftFragment> mFragWeakLink;

		public UpdateHandler(GiftFragment frag) {
			mFragWeakLink = new WeakReference<GiftFragment>(frag);
		}

		@Override
		@SuppressWarnings("unchecked")
		public void handleMessage(Message msg) {
			if (mFragWeakLink == null || mFragWeakLink.get() == null) {
				return;
			}
			GiftFragment fragment = mFragWeakLink.get();
			int y = fragment.mScrollView.getScrollY();
			switch (msg.what) {
				case ID_BANNER:
					fragment.updateBanners((ArrayList<IndexBanner>) msg.obj);
					break;
				case ID_ZERO:
					fragment.updateZeroData((ArrayList<IndexGiftNew>) msg.obj);
					break;
				case ID_LIKE:
					fragment.updateLikeData((ArrayList<IndexGiftLike>) msg.obj);
					break;
				case ID_LIMIT:
					fragment.updateLimitData((ArrayList<IndexGiftNew>) msg.obj);
					break;
				case ID_NEWS:
					fragment.updateNewData((ArrayList<IndexGiftNew>) msg.obj);
					break;
				case ID_ALL:
					fragment.updateData((IndexGift) msg.obj);
					break;
				case ID_CIRCLE:
					IndexGift g = (IndexGift) msg.obj;
					fragment.updateZeroData(g.zero);
					fragment.updateLimitData(g.limit);
					fragment.updateNewData(g.news);
					break;
				default:
			}
			fragment.mScrollView.smoothScrollTo(0, y);
		}
	}
}
