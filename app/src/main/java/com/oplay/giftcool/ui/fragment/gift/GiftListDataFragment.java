package com.oplay.giftcool.ui.fragment.gift;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.NestedGiftListAdapter;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.GiftTypeUtil;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.NetStatusCode;
import com.oplay.giftcool.listener.OnItemClickListener;
import com.oplay.giftcool.manager.ObserverManager;
import com.oplay.giftcool.manager.PayManager;
import com.oplay.giftcool.model.data.req.ReqPageData;
import com.oplay.giftcool.model.data.req.ReqRefreshGift;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;
import com.oplay.giftcool.model.data.resp.OneTypeDataList;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Refresh;
import com.oplay.giftcool.ui.widget.button.GiftButton;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.NetworkUtil;
import com.socks.library.KLog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 显示限量礼包数据的Fragment<br/>
 * 具备下拉刷新,上拉加载(显示更多同日期数据)<br/>
 * <br/>
 * Created by zsigui on 15-12-29.
 */
public class GiftListDataFragment extends BaseFragment_Refresh<IndexGiftNew> implements
		OnItemClickListener<IndexGiftNew> {

	private static final String KEY_DATA = "key_news_data";
	private static final String KEY_URL = "key_url";
	private static final String KEY_DATE = "key_date";

	private ListView mDataView;
	private JsonReqBase<ReqPageData> mReqPageObj;
	private String mUrl;
	private String mDate;
	private NestedGiftListAdapter mAdapter;

	public static GiftListDataFragment newInstance() {
		return new GiftListDataFragment();
	}

	public static GiftListDataFragment newInstance(ArrayList<IndexGiftNew> data, String date, String url) {
		GiftListDataFragment fragment = new GiftListDataFragment();
		Bundle bundle = new Bundle();
		bundle.putSerializable(KEY_DATA, data);
		bundle.putString(KEY_URL, url);
		bundle.putString(KEY_DATE, date);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onResume() {
		super.onResume();
//		AlarmClockManager.getInstance().setAllowNotifyGiftUpdate(true);
	}

	@Override
	public void onPause() {
		super.onPause();
//		AlarmClockManager.getInstance().setAllowNotifyGiftUpdate(false);
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		initViewManger(R.layout.fragment_refresh_lv_container);
		mDataView = getViewById(R.id.lv_content);
	}

	@Override
	protected void setListener() {
		ObserverManager.getInstance().addGiftUpdateListener(this);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		ObserverManager.getInstance().removeGiftUpdateListener(this);
	}

	@Override
	public void release() {

	}

	@Override
	@SuppressWarnings("unchecked")
	protected void processLogic(Bundle savedInstanceState) {
		ReqPageData data = new ReqPageData();
		mReqPageObj = new JsonReqBase<ReqPageData>(data);

		mAdapter = new NestedGiftListAdapter(getContext());
		if (getArguments() != null) {
			Serializable s = getArguments().getSerializable(KEY_DATA);
			if (s != null) {
				mData = (ArrayList<IndexGiftNew>) s;
				mAdapter.setData(mData);
				mHasData = true;
				mLastPage = 1;
			}
			mUrl = getArguments().getString(KEY_URL);
			mDate = getArguments().getString(KEY_DATE);
		}
		mAdapter.setListener(this);
		mDataView.setAdapter(mAdapter);
		if (mData != null) {
			mNoMoreLoad = mData.size() < 10;
			mRefreshLayout.setCanShowLoad(mData.size() >= 6);
			updateData(mData);
		}
	}

	/**
	 * 刷新列表数据的网络请求声明
	 */
	private Call<JsonRespBase<OneTypeDataList<IndexGiftNew>>> mCallRefresh;

	@Override
	protected void lazyLoad() {
		refreshInitConfig();
		Global.THREAD_POOL.execute(new Runnable() {
			@Override
			public void run() {
				if (NetworkUtil.isConnected(getContext())) {
					if (mCallRefresh != null) {
						mCallRefresh.cancel();
					}
					mReqPageObj.data.page = 1;
					mReqPageObj.data.date = mDate;
					mCallRefresh = Global.getNetEngine().obtainGiftList(mUrl, mReqPageObj);
					mCallRefresh.enqueue(new Callback<JsonRespBase<OneTypeDataList<IndexGiftNew>>>() {
						@Override
						public void onResponse(Call<JsonRespBase<OneTypeDataList<IndexGiftNew>>> call,
						                       Response<JsonRespBase<OneTypeDataList<IndexGiftNew>>> response) {
							if (!mCanShowUI || call.isCanceled()) {
								return;
							}
							if (response != null && response.isSuccessful() && response.body() != null &&
									response.body().getCode() == NetStatusCode.SUCCESS) {
								OneTypeDataList<IndexGiftNew> backObj = response.body().getData();
								refreshLoadState(backObj.data, backObj.isEndPage);
								updateData(backObj.data);
								refreshSuccessEnd();
								return;
							}
							refreshFailEnd();
						}

						@Override
						public void onFailure(Call<JsonRespBase<OneTypeDataList<IndexGiftNew>>> call, Throwable t) {
							if (!mCanShowUI || call.isCanceled()) {
								return;
							}
							if (AppDebugConfig.IS_DEBUG) {
								KLog.e(AppDebugConfig.TAG_FRAG, t);
							}
							refreshFailEnd();
						}
					});
				} else {
					refreshFailEnd();
				}
			}
		});
	}

	/**
	 * 加载更多列表数据的网络请求声明
	 */
	private Call<JsonRespBase<OneTypeDataList<IndexGiftNew>>> mCallLoad;

	/**
	 * 加载更多数据
	 */
	@Override
	protected void loadMoreData() {
		if (mNoMoreLoad || mIsLoadMore) {
			return;
		}
		mIsLoadMore = true;
		Global.THREAD_POOL.execute(new Runnable() {
			@Override
			public void run() {
				if (!NetworkUtil.isConnected(getContext())) {
					moreLoadFailEnd();
					return;
				}
				if (mCallLoad != null) {
					mCallLoad.cancel();
				}
				mReqPageObj.data.date = mDate;
				mReqPageObj.data.page = mLastPage + 1;
				mCallLoad = Global.getNetEngine().obtainGiftList(mUrl, mReqPageObj);
				mCallLoad.enqueue(new Callback<JsonRespBase<OneTypeDataList<IndexGiftNew>>>() {
					@Override
					public void onResponse(Call<JsonRespBase<OneTypeDataList<IndexGiftNew>>> call,
					                       Response<JsonRespBase<OneTypeDataList<IndexGiftNew>>> response) {
						if (!mCanShowUI || call.isCanceled()) {
							return;
						}
						if (response != null && response.isSuccessful()) {
							moreLoadSuccessEnd();
							OneTypeDataList<IndexGiftNew> backObj = response.body().getData();
							setLoadState(backObj.data, backObj.isEndPage);
							addMoreData(backObj.data);
							return;
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
		});

	}

	public void updateData(ArrayList<IndexGiftNew> data) {
		if (data == null) {
			return;
		}
		if (data.size() == 0) {
			mViewManager.showEmpty();
		} else {
			mViewManager.showContent();
		}
		mHasData = true;
		mData = data;
		mAdapter.updateData(mData);
	}

	private void addMoreData(ArrayList<IndexGiftNew> moreData) {
		if (moreData == null) {
			return;
		}
		mData.addAll(moreData);
		mAdapter.updateData(mData);
		mLastPage += 1;
	}

	/**
	 * 刷新礼包状态的网络请求声明
	 */
	private Call<JsonRespBase<HashMap<String, IndexGiftNew>>> mCallRefreshCircle;

	@Override
	public void onGiftUpdate(int action) {
		if (action != ObserverManager.STATUS.GIFT_UPDATE_PART
				&& action != ObserverManager.STATUS.GIFT_UPDATE_ALL) {
			return;
		}
		switch (action) {
			case ObserverManager.STATUS.GIFT_UPDATE_ALL:
				if (mIsSwipeRefresh) {
					return;
				}
				mIsSwipeRefresh = true;
				lazyLoad();
				break;
//			case ObserverManager.STATUS.GIFT_UPDATE_PART:
//				if (mIsSwipeRefresh || mIsNotifyRefresh || mData == null) {
//					return;
//				}
//				mIsNotifyRefresh = true;
//				updatePartData();
//				break;
		}
	}

	private void updatePartData() {
		if (!NetworkUtil.isConnected(getContext())) {
			moreLoadFailEnd();
			return;
		}
		if (mCallRefreshCircle != null) {
			mCallRefreshCircle.cancel();
		}
		HashSet<Integer> ids = new HashSet<Integer>();
		for (IndexGiftNew gift : mData) {
			ids.add(gift.id);
		}
		ReqRefreshGift reqData = new ReqRefreshGift();
		reqData.ids = ids;
		mCallRefreshCircle = Global.getNetEngine().refreshGift(new JsonReqBase<ReqRefreshGift>(reqData));
		mCallRefreshCircle.enqueue(new Callback<JsonRespBase<HashMap<String, IndexGiftNew>>>() {

			@Override
			public void onResponse(Call<JsonRespBase<HashMap<String, IndexGiftNew>>> call,
			                       Response<JsonRespBase<HashMap<String, IndexGiftNew>>> response) {
				if (!mCanShowUI || call.isCanceled()) {
					return;
				}
				if (response != null && response.isSuccessful()) {
					if (response.body() != null && response.body().isSuccess()) {
						// 数据刷新成功，进行更新
						HashMap<String, IndexGiftNew> respData = response.body().getData();
						ArrayList<Integer> waitDelIndexs = new ArrayList<Integer>();
						updateCircle(respData, waitDelIndexs, mData);
						delIndex(mData, waitDelIndexs);
						int y = mDataView.getScrollY();
						updateData(mData);
						mDataView.smoothScrollBy(y, 0);
					}
				}
				mIsNotifyRefresh = false;
			}

			@Override
			public void onFailure(Call<JsonRespBase<HashMap<String, IndexGiftNew>>> call, Throwable t) {
				mIsNotifyRefresh = false;
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
	public String getPageName() {
		return "礼包列表";
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
}
