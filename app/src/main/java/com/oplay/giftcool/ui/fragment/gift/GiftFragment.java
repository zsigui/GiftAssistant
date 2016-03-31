package com.oplay.giftcool.ui.fragment.gift;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bigkoo.convenientbanner.listener.OnItemClickListener;
import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.GiftAdapter;
import com.oplay.giftcool.adapter.itemdecoration.DividerItemDecoration;
import com.oplay.giftcool.adapter.layoutmanager.SnapLinearLayoutManager;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.BannerTypeUtil;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.NetStatusCode;
import com.oplay.giftcool.listener.OnFinishListener;
import com.oplay.giftcool.manager.AlarmClockManager;
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
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Refresh;
import com.oplay.giftcool.util.ImageLoaderUtil;
import com.socks.library.KLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
	private Handler mHandler = new Handler(Looper.getMainLooper());
	private Runnable mRefreshRunnable = new Runnable() {
		@Override
		public void run() {
			mIsNotifyRefresh = true;
			lazyLoad();
			if (mHandler != null) {
				mHandler.postDelayed(this, 5 * 60 * 1000);
			}
		}
	};

	public static GiftFragment newInstance() {
		return new GiftFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
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
						startBanner();
						ImageLoaderUtil.resume();
						break;
					case RecyclerView.SCROLL_STATE_DRAGGING:
						stopBanner();
						ImageLoaderUtil.resume();
						break;
					case RecyclerView.SCROLL_STATE_SETTLING:
						stopBanner();
						ImageLoaderUtil.stop();
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

	private void stopBanner() {
		if (mAdapter != null) {
			mAdapter.stopBanner();
		}
	}

	private void startBanner() {
		if (mAdapter != null) {
			mAdapter.startBanner();
		}
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
		rvContainer.setAdapter(mAdapter);
		mIsPrepared = true;
		if (mHandler != null) {
			mHandler.postDelayed(mRefreshRunnable, 5 * 60 * 1000);
		}

		ReqIndexGift data = new ReqIndexGift();
		data.pageSize = 20;
		mReqPageObj = new JsonReqBase<ReqIndexGift>(data);
		mLastPage = 1;
	}

	/**
	 * 刷新首页数据的网络请求声明
	 */
	private Call<JsonRespBase<IndexGift>> mCallRefresh;

	@Override
	protected void lazyLoad() {

		Global.THREAD_POOL.execute(new Runnable() {
			@Override
			public void run() {
				refreshInitConfig();
				// 判断网络情况
//        if (!NetworkUtil.isConnected(getContext())) {
//            refreshFailEnd();
//            return;
//        }
				if (mCallRefresh != null) {
					mCallRefresh.cancel();
				}
				ReqIndexGift data = new ReqIndexGift();
				data.appNames = Global.getInstalledAppNames();
				JsonReqBase<ReqIndexGift> reqData = new JsonReqBase<ReqIndexGift>(data);
				mCallRefresh = Global.getNetEngine().obtainIndexGift(reqData);
				mCallRefresh.enqueue(new Callback<JsonRespBase<IndexGift>>() {
					@Override
					public void onResponse(Call<JsonRespBase<IndexGift>> call, Response<JsonRespBase<IndexGift>>
							response) {
						if (!mCanShowUI || call.isCanceled()) {
							return;
						}
						if (response != null && response.isSuccessful()) {
							Global.sServerTimeDiffLocal = System.currentTimeMillis() - response.headers().getDate
									("Date").getTime();
							if (response.body() != null && response.body().getCode() == NetStatusCode.SUCCESS) {
								// 获取数据成功
								refreshSuccessEnd();
								updateData(response.body().getData(), 0, -1);
								return;
							}
						}
						if (AppDebugConfig.IS_DEBUG) {
							KLog.d(AppDebugConfig.TAG_FRAG, (response == null ? "返回出错" : response.code() + ", " +
									response
											.message()));
						}
						refreshFailEnd();
					}

					@Override
					public void onFailure(Call<JsonRespBase<IndexGift>> call, Throwable t) {
						if (!mCanShowUI || call.isCanceled()) {
							return;
						}
						if (AppDebugConfig.IS_DEBUG) {
							KLog.d(AppDebugConfig.TAG_FRAG, t);
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
	 * 首页加载更多数据的网络请求声明
	 */
	private Call<JsonRespBase<OneTypeDataList<IndexGiftNew>>> mCallLoad;

	/**
	 * 加载更多数据
	 */
	@Override
	protected void loadMoreData() {
		if (!mNoMoreLoad && !mIsLoadMore) {
//            if (!NetworkUtil.isConnected(getContext())) {
//                moreLoadFailEnd();
//                return;
//            }
			if (mCallLoad != null) {
				mCallLoad.cancel();
			}
			mIsLoadMore = true;
			mReqPageObj.data.page = mLastPage + 1;
			mCallLoad = Global.getNetEngine().obtainIndexGiftNew(mReqPageObj);
			mCallLoad.enqueue(new Callback<JsonRespBase<OneTypeDataList<IndexGiftNew>>>() {
				@Override
				public void onResponse(Call<JsonRespBase<OneTypeDataList<IndexGiftNew>>> call,
				                       Response<JsonRespBase<OneTypeDataList<IndexGiftNew>>> response) {
					if (!mCanShowUI || call.isCanceled()) {
						return;
					}
					if (response != null && response.isSuccessful()) {
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
				public void onFailure(Call<JsonRespBase<OneTypeDataList<IndexGiftNew>>> call, Throwable t) {
					if (!mCanShowUI || call.isCanceled()) {
						return;
					}
					moreLoadFailEnd();
				}
			});
		}
	}

	/* 更新控件数据 start */

	public void updateData(IndexGift data, int start, int end) {
		if (data == null) {
			if (!mHasData) {
				mViewManager.showErrorRetry();
			} else {
				mAdapter.updateData(mGiftData, start, end);
				mViewManager.showContent();
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
		AlarmClockManager.getInstance().setAllowNotifyGiftUpdate(true);
		mIsResume = true;
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mAdapter != null) {
			mAdapter.stopBanner();
		}
		mIsResume = false;
		AlarmClockManager.getInstance().setAllowNotifyGiftUpdate(false);
	}


	@Override
	protected void onUserVisible() {
		super.onUserVisible();
		AlarmClockManager.getInstance().setAllowNotifyGiftUpdate(true);
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
		AlarmClockManager.getInstance().setAllowNotifyGiftUpdate(false);
		if (mAdapter != null) {
			mAdapter.stopBanner();
		}
		mIsVisible = false;
	}


	/**
	 * 轮询局部刷新礼包页面的网络请求声明
	 */
	private Call<JsonRespBase<HashMap<String, IndexGiftNew>>> mCallRefreshCircle = null;

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
//                    if (!NetworkUtil.isConnected(getContext())) {
//                        mIsNotifyRefresh = false;
//                        return;
//                    }
					if (mCallRefreshCircle != null) {
						mCallRefreshCircle.cancel();
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
					mCallRefreshCircle = Global.getNetEngine().refreshGift(new JsonReqBase<ReqRefreshGift>(reqData));
					try {
						Response<JsonRespBase<HashMap<String, IndexGiftNew>>> response = mCallRefreshCircle.execute();
						if (response != null && response.isSuccessful() && mCanShowUI) {
							if (response.body() != null && response.body().isSuccess()
									&& mHandler != null) {
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
								mHandler.post(new Runnable() {
									@Override
									public void run() {
										int y = rvContainer.getScrollY();
										refreshSuccessEnd();
										updateData(mGiftData, 1, -1);
										rvContainer.smoothScrollBy(0, y);
									}
								});
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
		if (mHandler == null) {
			return;
		}
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

	/**
	 * 删除已失效的索引
	 *
	 * @param data          已经有的数据列表
	 * @param waitDelIndexs 待删除的下标索引列表
	 */
	private void delIndex(ArrayList<IndexGiftNew> data, ArrayList<Integer> waitDelIndexs) {
		for (int i = waitDelIndexs.size() - 1; i >= 0; i--) {
			data.remove(waitDelIndexs.get(i).intValue());
		}
	}

	/**
	 * 遍历更新所有礼包的状态
	 */
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

	/**
	 * 更新礼包状态项内容
	 *
	 * @param toBeSet 待更新的礼包项
	 * @param data    用于更新的数据
	 */
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
		if (mCallLoad != null) {
			mCallLoad.cancel();
			mCallLoad = null;
		}
		if (mCallRefresh != null) {
			mCallRefresh.cancel();
			mCallRefresh = null;
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
}
