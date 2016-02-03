package com.oplay.giftcool.ui.fragment.gift;

/**
 * 主页-礼包页面主要内容页，不承担网络请求任务
 * (如果白屏问题还存在，则采用该方法，如果不存在，则已解决)
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/13
 */
public class GiftFragment_new {}
//public class GiftFragment_new extends BaseFragment_Refresh implements View.OnClickListener, OnItemClickListener {
//
//	private final static String PAGE_NAME = "礼包首页";
//	private static final String KEY_BANNER = "key_banner";
//	private static final String KEY_LIKE = "key_like";
//	private static final String KEY_LIMIT = "key_limit";
//	private static final String KEY_NEW = "key_new";
//
//	// 顶端4个界面
//	private RelativeLayout rlErrNet;
//	private LinearLayout llLoading;
//	private ImageView ivEmpty;
//
//
//	private ScrollView mScrollView;
//	// 活动视图, 3张
//	private ConvenientBanner mBanner;
//	// 0元疯抢
//	private RecyclerView mZeroView;
//	// 猜你喜欢
//	private LinearLayout llLike;
//	private RelativeLayout mLikeBar;
//	private RecyclerView mLikeView;
//	// 今日限量
//	private RelativeLayout mLimitBar;
//	private RecyclerView mLimitView;
//	// 今日出炉
//	private RelativeLayout mNewBar;
//	private NestedListView mNewView;
//
//
//	private IndexGiftZeroAdapter mZeroAdapter;
//	private IndexGiftLikeAdapter mLikeAdapter;
//	private IndexGiftLimitAdapter mLimitAdapter;
//	private NestedGiftListAdapter mNewAdapter;
//
//	// 礼物界面数据
//	private IndexGift mGiftData;
//	// 请求后游戏键值的MD5串
//	private String mGameKey;
//	// 每隔5分钟刷新一次
//	private Handler mHandler = new Handler();
//	private Runnable mRefreshRunnable = new Runnable() {
//		@Override
//		public void run() {
//			mIsNotifyRefresh = true;
//			lazyLoad();
//			mHandler.postDelayed(this, 5 * 60 * 1000);
//		}
//	};
//
//	public static GiftFragment_new newInstance() {
//		return new GiftFragment_new();
//	}
//
//	public static GiftFragment_new newInstance(ArrayList<IndexBanner> banners,
//	                                       ArrayList<IndexGiftLike> likeGames,
//	                                       ArrayList<IndexGiftLimit> limitGifts,
//	                                       ArrayList<IndexGiftNew> newGifts) {
//		GiftFragment_new fragment = new GiftFragment_new();
//		Bundle bundle = new Bundle();
//		bundle.putSerializable(KEY_BANNER, banners);
//		bundle.putSerializable(KEY_LIKE, likeGames);
//		bundle.putSerializable(KEY_LIMIT, limitGifts);
//		bundle.putSerializable(KEY_NEW, newGifts);
//		fragment.setArguments(bundle);
//		return fragment;
//	}
//
//	@Override
//	protected void initView(Bundle savedInstanceState) {
//		setContentView(R.layout.fragment_gifts_new);
//		rlErrNet = getViewById(R.id.rl_err_404);
//		llLoading = getViewById(R.id.ll_loading);
//		ivEmpty = getViewById(R.id.iv_empty_view);
//		mScrollView = getViewById(R.id.sv_container);
//		mBanner = getViewById(R.id.banner);
//		llLike = getViewById(R.id.ll_like);
//		mLikeBar = getViewById(R.id.rl_hot_all);
//		mLikeView = getViewById(R.id.rv_like_content);
//		mLimitBar = getViewById(R.id.rl_limit_all);
//		mLimitView = getViewById(R.id.rv_limit_content);
//		mNewBar = getViewById(R.id.rl_new_all);
//		mNewView = getViewById(R.id.rv_new_content);
//		((TextView) getViewById(R.id.tv_limit_hint)).setText(Html.fromHtml("(每天<font " +
//				"color='#f85454'>20:00</font>开抢10款)"));
//		mZeroView = getViewById(R.id.rv_zero);
//		((TextView) getViewById(R.id.tv_zero_limit)).setText(Html.fromHtml("(每天<font " +
//				"color='#f85454'>20:00</font>开抢3款)"));
//	}
//
//	@Override
//	protected void setListener() {
//		rlErrNet.setOnClickListener(this);
//		mLikeBar.setOnClickListener(this);
//		mLimitBar.setOnClickListener(this);
//		mNewBar.setOnClickListener(this);
//		ObserverManager.getInstance().addGiftUpdateListener(this);
//	}
//
//	@Override
//	@SuppressWarnings("unchecked")
//	protected void processLogic(Bundle savedInstanceState) {
//
//		//
//		show(LOADING);
//
//		// 设置RecyclerView的LayoutManager
//		LinearLayoutManager llmZero = new LinearLayoutManager(getContext());
//		llmZero.setOrientation(LinearLayoutManager.HORIZONTAL);
//		LinearLayoutManager llmLike = new LinearLayoutManager(getContext());
//		llmLike.setOrientation(LinearLayoutManager.HORIZONTAL);
//		LinearLayoutManager llmLimit = new LinearLayoutManager(getContext());
//		llmLimit.setOrientation(LinearLayoutManager.HORIZONTAL);
//
//		mZeroView.setLayoutManager(llmZero);
//		mLikeView.setLayoutManager(llmLike);
//		mLimitView.setLayoutManager(llmLimit);
//		mZeroAdapter = new IndexGiftZeroAdapter(getContext());
//		mLikeAdapter = new IndexGiftLikeAdapter(mLikeView);
//		mLimitAdapter = new IndexGiftLimitAdapter(mLimitView);
//		mNewAdapter = new NestedGiftListAdapter(getActivity());
//
//		// 加载数据
//		mZeroView.setAdapter(mZeroAdapter);
//		mLikeView.setAdapter(mLikeAdapter);
//		mLimitView.setAdapter(mLimitAdapter);
//		mNewView.setAdapter(mNewAdapter);
//
//		mIsPrepared = true;
//		mScrollView.smoothScrollTo(0, 0);
//		mHandler.postDelayed(mRefreshRunnable, 5 * 60 * 1000);
//	}
//
//	private void loadBanner(ArrayList<IndexBanner> banners) {
//		if (banners == null) {
//			return;
//		}
//		if (banners.size() == 0) {
//			IndexBanner banner = new IndexBanner();
//			banner.url = "drawable://" + R.drawable.ic_banner_empty_default;
//			banner.type = BannerTypeUtil.ACTION_SCORE_TASK;
//			banners.add(banner);
//		}
//		if (mGiftData != null) {
//			mGiftData.banner = banners;
//		}
//		ArrayList<String> data = new ArrayList<>();
//		for (IndexBanner banner : banners) {
//			data.add(banner.url);
//		}
//		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Global
//				.getBannerHeight(getContext()));
//		mBanner.setLayoutParams(lp);
//		mBanner.setPages(new CBViewHolderCreator<NetworkImageHolderView>() {
//
//			@Override
//			public NetworkImageHolderView createHolder() {
//				return new NetworkImageHolderView();
//			}
//		}, data)
//				.setPageIndicator(new int[]{R.drawable.ic_banner_point_normal, R.drawable.ic_banner_point_selected})
//				.setPageIndicatorAlign(ConvenientBanner.PageIndicatorAlign.CENTER_HORIZONTAL)
//				.setOnItemClickListener(this);
//		if (data.size() == 1) {
//			mBanner.setCanLoop(false);
//		} else {
//			mBanner.setCanLoop(true);
//			mBanner.setScrollDuration(500);
//			//mBanner.getViewPager().setPageTransformer(true, new ZoomStackPageTransformer());
//			mBanner.startTurning(5000);
//		}
//
//	}
//
//
//	@Override
//	protected void lazyLoad() {
//
//		refreshInitConfig();
//
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				if (AppDebugConfig.IS_DEBUG) {
//					KLog.d(AppDebugConfig.TAG_FRAG, "lazyLoad.Thread start() ");
//				}
//				if (!NetworkUtil.isConnected(getContext())) {
//					if (AppDebugConfig.IS_DEBUG) {
//						KLog.d(AppDebugConfig.TAG_FRAG, "lazyLoad.Thread net failed ");
//					}
//					refreshFailEnd();
//					return;
//				}
//				ReqIndexGift data = new ReqIndexGift();
//				data.appNames = Global.getInstalledAppNames();
//				JsonReqBase<ReqIndexGift> reqData = new JsonReqBase<ReqIndexGift>(data);
//				if (AppDebugConfig.IS_DEBUG) {
//					KLog.d(AppDebugConfig.TAG_FRAG, "lazyLoad.Thread obtain index gift ");
//				}
//				Global.getNetEngine().obtainIndexGift(reqData).enqueue(new Callback<JsonRespBase<IndexGift>>() {
//					@Override
//					public void onResponse(Response<JsonRespBase<IndexGift>> response, Retrofit retrofit) {
//						// 获取数据成功
//						if (AppDebugConfig.IS_DEBUG) {
//							KLog.d(AppDebugConfig.TAG_FRAG, "onResponse start() ");
//						}
//						if (response != null && response.isSuccess()) {
//							if (response.body() != null && response.body().getCode() == StatusCode.SUCCESS) {
//								// 获取数据成功
//								if (AppDebugConfig.IS_DEBUG) {
//									KLog.d(AppDebugConfig.TAG_FRAG, "response = " + response.body().getData());
//								}
//								refreshSuccessEnd();
//								updateData(response.body().getData());
//								return;
//							}
//						}
//						refreshFailEnd();
//					}
//
//					@Override
//					public void onFailure(Throwable t) {
//						if (AppDebugConfig.IS_DEBUG) {
//							KLog.e(t);
//						}
//						refreshFailEnd();
//					}
//				});
//			}
//		}).start();
//	}
//
//	private static final int CONTENT = 0;
//	private static final int ERR_NET = 1;
//	private static final int LOADING = 2;
//	private static final int EMPTY = 3;
//	private int mCurShow = -1;
//
//	private void show(final int type) {
//		if (mCurShow == type) {
//			return;
//		}
//		mHandler.post(new Runnable() {
//			@Override
//			public void run() {
//
//
//				switch (type) {
//					case CONTENT:
//						mRefreshLayout.setVisibility(View.VISIBLE);
//						rlErrNet.setVisibility(View.GONE);
//						llLoading.setVisibility(View.GONE);
//						ivEmpty.setVisibility(View.GONE);
//						break;
//					case ERR_NET:
//						mRefreshLayout.setVisibility(View.GONE);
//						rlErrNet.setVisibility(View.VISIBLE);
//						llLoading.setVisibility(View.GONE);
//						ivEmpty.setVisibility(View.GONE);
//						break;
//					case LOADING:
//						mRefreshLayout.setVisibility(View.GONE);
//						rlErrNet.setVisibility(View.GONE);
//						llLoading.setVisibility(View.VISIBLE);
//						ivEmpty.setVisibility(View.GONE);
//						break;
//					case EMPTY:
//						mRefreshLayout.setVisibility(View.GONE);
//						rlErrNet.setVisibility(View.GONE);
//						llLoading.setVisibility(View.GONE);
//						ivEmpty.setVisibility(View.VISIBLE);
//						break;
//				}
//			}
//		});
//		mCurShow = type;
//	}
//
//
//	/* 更新控件数据 start */
//
//	public void updateData(IndexGift data) {
//		if (data == null) {
//			if (!mHasData) {
////				mViewManager.showErrorRetry();
//				show(ERR_NET);
//			}
//			return;
//		}
//		if ((data.limit == null || data.limit.size() == 0)
//				&& (data.zero == null || data.zero.size() == 0)
//				&& (data.news == null || data.news.size() == 0)
//				&& (data.banner == null || data.banner.size() == 0)
//				&& (data.like == null || data.like.size() == 0)) {
//			// 数据为空
////			mViewManager.showEmpty();
//			show(EMPTY);
//			return;
//		}
//		int y = mScrollView.getScrollY();
////		mViewManager.showContent();
//		show(CONTENT);
//		mHasData = true;
//		mGiftData = data;
//		updateZeroData(data.zero);
//		updateBanners(data.banner);
//		updateLikeData(data.like);
//		updateLimitData(data.limit);
//		updateNewData(data.news);
//		mScrollView.smoothScrollTo(0, y);
//	}
//
//	public void updateBanners(ArrayList<IndexBanner> banners) {
//		loadBanner(banners);
//	}
//
//	public void updateZeroData(ArrayList<IndexGiftNew> zeroData) {
//		if (zeroData == null) {
//			return;
//		}
//		mZeroAdapter.updateData(zeroData);
//	}
//
//	public void updateLikeData(ArrayList<IndexGiftLike> likeData) {
//		if (likeData == null) {
//			return;
//		}
//		mLikeAdapter.updateData(likeData);
//	}
//
//	public void updateLimitData(ArrayList<IndexGiftNew> limitData) {
//		if (limitData == null) {
//			return;
//		}
//		mLimitAdapter.updateData(limitData);
//	}
//
//	public void updateNewData(ArrayList<IndexGiftNew> newData) {
//		if (newData == null) {
//			return;
//		}
//		mNewAdapter.updateData(newData);
//	}
//
//	@Override
//	public void onResume() {
//		super.onResume();
//		ClockService.startService(getContext());
//		if (mBanner != null) {
//			mBanner.startTurning(3000);
//		}
//	}
//
//	@Override
//	public void onPause() {
//		super.onPause();
//		ClockService.stopService(getContext());
//		if (mBanner != null) {
//			mBanner.stopTurning();
//		}
//	}
//
//	@Override
//	public void onGiftUpdate() {
//		if (mIsSwipeRefresh || mIsNotifyRefresh || mGiftData == null) {
//			return;
//		}
//		mIsNotifyRefresh = true;
//		Global.THREAD_POOL.execute(new Runnable() {
//			@Override
//			public void run() {
//				if (!NetworkUtil.isConnected(getContext())) {
//					refreshFailEnd();
//					return;
//				}
//				HashSet<Integer> ids = new HashSet<Integer>();
//				for (IndexGiftNew gift : mGiftData.limit) {
//					ids.add(gift.id);
//				}
//				for (IndexGiftNew gift : mGiftData.news) {
//					ids.add(gift.id);
//				}
//				ReqRefreshGift reqData = new ReqRefreshGift();
//				reqData.ids = ids;
//				Global.getNetEngine().refreshGift(new JsonReqBase<ReqRefreshGift>(reqData))
//						.enqueue(new Callback<JsonRespBase<HashMap<String, IndexGiftNew>>>() {
//
//							@Override
//							public void onResponse(Response<JsonRespBase<HashMap<String, IndexGiftNew>>> response,
//							                       Retrofit retrofit) {
//								if (!mCanShowUI) {
//									return;
//								}
//								if (response != null && response.isSuccess()) {
//									if (response.body() != null && response.body().isSuccess()) {
//										// 数据刷新成功，进行更新
//										HashMap<String, IndexGiftNew> respData = response.body().getData();
//										ArrayList<Integer> waitDelIndexs = new ArrayList<Integer>();
//										updateCircle(respData, waitDelIndexs, mGiftData.limit);
//										delIndex(mGiftData.limit, waitDelIndexs);
//										waitDelIndexs.clear();
//										updateCircle(respData, waitDelIndexs, mGiftData.news);
//										delIndex(mGiftData.news, waitDelIndexs);
//										int y = mScrollView.getScrollY();
//										updateLimitData(mGiftData.limit);
//										updateNewData(mGiftData.news);
//										mScrollView.smoothScrollTo(0, y);
//									}
//								}
//								mIsNotifyRefresh = false;
//							}
//
//							@Override
//							public void onFailure(Throwable t) {
//								mIsNotifyRefresh = false;
//							}
//						});
//			}
//		});
//	}
//
//	public void scrollToPos(final int type) {
//		if (mScrollView == null) {
//			return;
//		}
//		mHandler.post(new Runnable() {
//			@Override
//			public void run() {
//				int height = mBanner.getMeasuredHeight();
//				switch (type) {
//					case 1:
//						if (mZeroView != null) {
//							mScrollView.smoothScrollTo(0, height);
//						}
//						break;
//					case 2:
//						if (mLikeView != null) {
//							height += mZeroView.getMeasuredHeight()
//									+ mApp.getResources().getDimensionPixelSize(R.dimen.di_index_module_gap);
//							mScrollView.smoothScrollTo(0, height);
//						}
//						break;
//					case 3:
//						if (mLimitView != null) {
//							height += mLikeBar.getMeasuredHeight() * 2
//									+ mZeroView.getMeasuredHeight()
//									+ mLikeView.getMeasuredHeight()
//									+ mApp.getResources().getDimensionPixelSize(R.dimen.di_index_module_gap) * 2;
//							mScrollView.smoothScrollTo(0, height);
//						}
//						break;
//					case 4:
//						if (mNewView != null) {
//							height += mLikeBar.getMeasuredHeight() * 3
//									+ mLikeView.getMeasuredHeight() + mZeroView.getMeasuredHeight()
//									+ mLimitView.getMeasuredHeight()
//									+ mApp.getResources().getDimensionPixelSize(R.dimen.di_index_module_gap) * 3;
//							mScrollView.scrollTo(0, height);
//						}
//						break;
//				}
//			}
//		});
//	}
//
//	private void delIndex(ArrayList<IndexGiftNew> data, ArrayList<Integer> waitDelIndexs) {
//		for (int i = waitDelIndexs.size() - 1; i >= 0; i--) {
//			data.remove(waitDelIndexs.get(i).intValue());
//		}
//	}
//
//	private void updateCircle(HashMap<String, IndexGiftNew> respData, ArrayList<Integer> waitDelIndexs,
//	                          ArrayList<IndexGiftNew> gifts) {
//		int i = 0;
//		for (IndexGiftNew gift : gifts) {
//			if (respData.get(gift.id + "") != null) {
//				IndexGiftNew item = respData.get(gift.id + "");
//				setGiftUpdateInfo(gift, item);
//			} else {
//				// 找不到，需要被移除
//				waitDelIndexs.add(i);
//			}
//			i++;
//		}
//	}
//
//	private void setGiftUpdateInfo(IndexGiftNew toBeSet, IndexGiftNew data) {
//		toBeSet.status = data.status;
//		toBeSet.seizeStatus = data.seizeStatus;
//		toBeSet.searchCount = data.searchCount;
//		toBeSet.searchTime = data.searchTime;
//		toBeSet.totalCount = data.totalCount;
//		toBeSet.remainCount = data.remainCount;
//		toBeSet.code = data.code;
//	}
//
//	@Override
//	public void onHiddenChanged(boolean hidden) {
//		super.onHiddenChanged(hidden);
//		if (hidden) {
//			ClockService.stopService(getContext());
//		} else {
//			ClockService.startService(getContext());
//		}
//	}
//
//	@Override
//	public void onDestroyView() {
//		super.onDestroyView();
//		mHandler.removeCallbacks(mRefreshRunnable);
//	}
//
//	/* 更新控件数据 end */
//
//	@Override
//	public void onClick(View v) {
//		switch (v.getId()) {
//			case R.id.rl_err_404:
//				mHasData = false;
//				lazyLoad();
//				break;
//			case R.id.rl_hot_all:
//				IntentUtil.jumpGiftHotList(getContext(), mGameKey);
//				break;
//			case R.id.rl_limit_all:
//				IntentUtil.jumpGiftLimitList(getContext());
//				break;
//			case R.id.rl_new_all:
//				IntentUtil.jumpGiftNewList(getContext());
//				break;
//		}
//	}
//
//	@Override
//	public void onItemClick(int position) {
//		if (mGiftData == null || mGiftData.banner == null || mGiftData.banner.size() <= position) {
//			return;
//		}
//		IndexBanner banner = mGiftData.banner.get(position);
//		TCAgent.onEvent(getContext(), "礼包首页推荐位", String.format("第%d推广位，标题：%s", position, banner.title));
//		BannerTypeUtil.handleBanner(getContext(), banner);
//	}
//
//	@Override
//	protected void refreshInitConfig() {
//		if (!mIsSwipeRefresh && !mIsNotifyRefresh) {
//			show(LOADING);
//		}
//		super.refreshInitConfig();
//	}
//
//	@Override
//	protected void refreshSuccessEnd() {
//		super.refreshSuccessEnd();
//		show(CONTENT);
//	}
//
//	@Override
//	protected void refreshFailEnd() {
//		if (!mIsSwipeRefresh && !mIsNotifyRefresh) {
//			show(ERR_NET);
//		}
//		super.refreshFailEnd();
//	}
//
//	@Override
//	public String getPageName() {
//		return PAGE_NAME;
//	}
//}
