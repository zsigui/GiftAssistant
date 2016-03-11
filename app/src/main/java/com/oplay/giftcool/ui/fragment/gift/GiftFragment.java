package com.oplay.giftcool.ui.fragment.gift;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.bigkoo.convenientbanner.listener.OnItemClickListener;
import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.GiftAdapter;
import com.oplay.giftcool.adapter.other.DividerItemDecoration;
import com.oplay.giftcool.adapter.other.SnapLinearLayoutManager;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.BannerTypeUtil;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.NetStatusCode;
import com.oplay.giftcool.listener.OnFinishListener;
import com.oplay.giftcool.manager.ObserverManager;
import com.oplay.giftcool.manager.StatisticsManager;
import com.oplay.giftcool.model.data.req.ReqIndexGift;
import com.oplay.giftcool.model.data.req.ReqRefreshGift;
import com.oplay.giftcool.model.data.resp.IndexBanner;
import com.oplay.giftcool.model.data.resp.IndexGift;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;
import com.oplay.giftcool.model.data.resp.OneTypeDataList;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.service.ClockService;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Refresh;
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
public class GiftFragment extends BaseFragment_Refresh implements OnItemClickListener {

	private final static String PAGE_NAME = "礼包首页";
	private static final int ID_UPDATE = 6;

	public static final int POS_BANNER = 0;
	public static final int POS_ZERO = 1;
	public static final int POS_LIKE = 2;
	public static final int POS_LIMIT = 3;
	public static final int POS_NEW = 4;

	private RecyclerView rvContainer;
	private GiftAdapter mAdapter;

	// 礼物界面数据
	private IndexGift mGiftData;
	// 请求后游戏键值的MD5串
	private String mGameKey;
	private JsonReqBase<ReqIndexGift> mReqPageObj;
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
		initViewManger(R.layout.fragment_refresh_custome_rv_container);
		rvContainer = getViewById(R.id.lv_content);
	}

	@Override
	protected void setListener() {
		rvContainer.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
				switch (newState) {
					case RecyclerView.SCROLL_STATE_IDLE:
						if (mAdapter != null) {
							mAdapter.startBanner();
						}
//						if (ImageLoader.getInstance().isInited()) {
//							ImageLoader.getInstance().resume();
//						}
						break;
					case RecyclerView.SCROLL_STATE_DRAGGING:
					case RecyclerView.SCROLL_STATE_SETTLING:
						if (mAdapter != null) {
							mAdapter.stopBanner();
						}
//						if (ImageLoader.getInstance().isInited()) {
//							ImageLoader.getInstance().stop();
//						}
						break;
				}
			}

			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
			}
		});
		ObserverManager.getInstance().addGiftUpdateListener(this);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void processLogic(Bundle savedInstanceState) {
		SnapLinearLayoutManager llm = new SnapLinearLayoutManager(getContext(),
				LinearLayoutManager.VERTICAL, false);
		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(),
				llm.getOrientation());
		rvContainer.setLayoutManager(llm);
		rvContainer.addItemDecoration(dividerItemDecoration);
		mAdapter = new GiftAdapter(getContext());
		//mAdapter.setListener(this);
		rvContainer.setAdapter(mAdapter);
		mIsPrepared = true;
		mHandler.postDelayed(mRefreshRunnable, 5 * 60 * 1000);

		ReqIndexGift data = new ReqIndexGift();
		data.pageSize = 20;
		mReqPageObj = new JsonReqBase<ReqIndexGift>(data);
		mLastPage = 1;
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
							if (response.body() != null && response.body().getCode() == NetStatusCode.SUCCESS) {
								// 获取数据成功
								refreshSuccessEnd();
								updateData(response.body().getData(), 0, -1);
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

	private void addMoreData(ArrayList<IndexGiftNew> moreData) {
		if (moreData == null) {
			return;
		}
		int lastCount = mAdapter.getItemCount();
		mGiftData.news.addAll(moreData);
		mAdapter.updateData(mGiftData, lastCount, -1);
		mLastPage += 1;
	}

	/**
	 * 加载更多数据
	 */
	@Override
	protected void loadMoreData() {
		if (!mNoMoreLoad && !mIsLoadMore) {
			mIsLoadMore = true;
			mReqPageObj.data.page = mLastPage + 1;
			Global.THREAD_POOL.execute(new Runnable() {
				@Override
				public void run() {
					if (NetworkUtil.isConnected(getContext().getApplicationContext())) {
						Global.getNetEngine().obtainIndexGiftNew(mReqPageObj)
								.enqueue(new Callback<JsonRespBase<OneTypeDataList<IndexGiftNew>>>() {
									@Override
									public void onResponse(Response<JsonRespBase<OneTypeDataList<IndexGiftNew>>>
											                       response, Retrofit
											                       retrofit) {
										if (!mCanShowUI) {
											return;
										}
										if (response != null && response.isSuccess()) {
											if (response.body() != null && response.body().isSuccess()) {
												moreLoadSuccessEnd();
												OneTypeDataList<IndexGiftNew> backObj = response.body().getData();
												setLoadState(backObj.data, backObj.isEndPage);
												addMoreData(backObj.data);
												return;
											}
											if (AppDebugConfig.IS_DEBUG) {
												KLog.d(AppDebugConfig.TAG_FRAG, (response.body() == null ?
														"解析错误" : response.body().error()));
											}
										}
										moreLoadFailEnd();
									}

									@Override
									public void onFailure(Throwable t) {
										if (!mCanShowUI) {
											return;
										}
										moreLoadFailEnd();
									}
								});
					} else {
						mViewManager.showErrorRetry();
					}
				}
			});
		}
	}

	/* 更新控件数据 start */

	public void updateData(IndexGift data, int start, int end) {
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
		mViewManager.showContent();
		mHasData = true;
		mGiftData = data;
		mAdapter.updateData(mGiftData, start, end);
	}

	private boolean mIsResume = false;
	private boolean mIsVisible = false;

	@Override
	public void onResume() {
		super.onResume();
		if (mIsVisible) {
			if (mAdapter != null) {
				mAdapter.startBanner();
			}
		}
		ClockService.startService(mApp);
		mIsResume = true;
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mAdapter != null) {
			mAdapter.stopBanner();
		}
		mIsResume = false;
		ClockService.stopService(mApp);
	}


	@Override
	protected void onUserVisible() {
		super.onUserVisible();
		ClockService.startService(mApp);
		if (mIsResume) {
			if (mAdapter != null) {
				mAdapter.startBanner();
			}
		}
		mIsVisible = true;
	}

	@Override
	protected void onUserInvisible() {
		super.onUserInvisible();
		ClockService.stopService(mApp);
		if (mAdapter != null) {
			mAdapter.stopBanner();
		}
		mIsVisible = false;
	}


	private Call<JsonRespBase<HashMap<String, IndexGiftNew>>> mRefreshCall = null;

	@Override
	public void onGiftUpdate(int action) {
		if (!mIsPrepared) {
			mIsNotifyRefresh = mIsSwipeRefresh = false;
			return;
		}
		if (action == ObserverManager.STATUS.GIFT_UPDATE_ALL) {
			if (mIsSwipeRefresh) {
				return;
			}
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
					for (IndexGiftNew gift : mGiftData.zero) {
						ids.add(gift.id);
					}
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
								msg.what = ID_UPDATE;
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

	/**
	 * 移到到指定位置
	 *
	 * @param type
	 */
	public void scrollToPos(final int type) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				switch (type) {
					case POS_BANNER:
						if (rvContainer != null) {
							rvContainer.smoothScrollToPosition(0);
						}
						break;
					case POS_ZERO:
						if (rvContainer != null) {
							rvContainer.smoothScrollToPosition(1);
						}
						break;
					case POS_LIKE:
						if (rvContainer != null) {
							rvContainer.smoothScrollToPosition(2);
						}
						break;
					case POS_LIMIT:
						if (rvContainer != null) {
							rvContainer.smoothScrollToPosition(3);
						}
						break;
					case POS_NEW:
						if (rvContainer != null) {
							rvContainer.smoothScrollToPosition(4);
						}
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
	public void onDestroyView() {
		super.onDestroyView();
		ObserverManager.getInstance().removeGiftUpdateListener(this);
		if (mHandler != null) {
			mHandler.removeCallbacks(mRefreshRunnable);
		}
	}

	@Override
	public void release() {
		super.release();
		if (mAdapter != null && mAdapter instanceof OnFinishListener) {
			((OnFinishListener) mAdapter).release();
		}
		mAdapter = null;
		rvContainer = null;
		if (mHandler != null) {
			mHandler.removeCallbacksAndMessages(null);
			mHandler = null;
		}
	}

	@Override
	public void onItemClick(int position) {
		if (mGiftData == null || mGiftData.banner == null || mGiftData.banner.size() <= position) {
			return;
		}
		IndexBanner banner = mGiftData.banner.get(position);
		StatisticsManager.getInstance().trace(getContext(), StatisticsManager.ID.GIFT_BANNER,
				String.format("第%d推广位，标题：%s", position, banner.title));
		BannerTypeUtil.handleBanner(getContext(), banner);
	}

	@Override
	public String getPageName() {
		return PAGE_NAME;
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
			int y = fragment.rvContainer.getScrollY();
			switch (msg.what) {
				case ID_UPDATE:
					fragment.refreshSuccessEnd();
					fragment.updateData((IndexGift) msg.obj, 1, -1);
					break;
				default:
			}
			fragment.rvContainer.smoothScrollBy(0, y);
		}
	}
}
