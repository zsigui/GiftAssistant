package com.oplay.giftcool.ui.fragment.gift;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.other.LimmitGiftListAdapter;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.GiftTypeUtil;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.StatusCode;
import com.oplay.giftcool.listener.OnItemClickListener;
import com.oplay.giftcool.manager.ObserverManager;
import com.oplay.giftcool.manager.PayManager;
import com.oplay.giftcool.model.data.req.ReqPageData;
import com.oplay.giftcool.model.data.req.ReqRefreshGift;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;
import com.oplay.giftcool.model.data.resp.TimeDataList;
import com.oplay.giftcool.model.json.JsonRespGiftList;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.service.ClockService;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Refresh;
import com.oplay.giftcool.ui.widget.button.GiftButton;
import com.oplay.giftcool.ui.widget.stickylistheaders.StickyListHeadersListView;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.NetworkUtil;
import com.oplay.giftcool.util.ToastUtil;
import com.socks.library.KLog;

import net.ouwan.umipay.android.debug.Debug_Log;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * 显示限量礼包数据的Fragment<br/>
 * 具备下拉刷新,上拉加载(合并之后不再加载)<br/>
 * <br/>
 * Created by mink on 16-03-04.
 */
public class GiftLimmitListDataFragment extends BaseFragment_Refresh<IndexGiftNew> implements
		OnItemClickListener<IndexGiftNew> {

	private static final String KEY_DATA = "key_limmit_data";
	private static final String KEY_URL = "key_url";

	private StickyListHeadersListView mDataView;
	private LimmitGiftListAdapter mAdapter;

	private List<String> mDate;

	public static GiftLimmitListDataFragment newInstance() {
		return new GiftLimmitListDataFragment();
	}

	public static GiftLimmitListDataFragment newInstance(ArrayList<TimeDataList<IndexGiftNew>> data, String url) {
		GiftLimmitListDataFragment fragment = new GiftLimmitListDataFragment();
		Bundle bundle = new Bundle();
		bundle.putSerializable(KEY_DATA, data);
		bundle.putString(KEY_URL, url);
		fragment.setArguments(bundle);
		return fragment;
	}

	private void getDateFromTimeDataList(ArrayList<TimeDataList<IndexGiftNew>> data) {
		if(data == null){
			return;
		}
		mData = new ArrayList<IndexGiftNew>();
		mDate = new ArrayList<String>();
		try {
			Collections.sort(data, new Comparator<TimeDataList<IndexGiftNew>>() {
				@Override
				public int compare(TimeDataList<IndexGiftNew> lhs, TimeDataList<IndexGiftNew> rhs) {

					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
					//将字符串形式的时间转化为Date类型的时间
					try {
						Date a = sdf.parse(lhs.date);
						Date b = sdf.parse(rhs.date);

						//Date类的一个方法，如果a早于b返回true，否则返回false
						if (a.before(b))
							return 1;
						else
							return -1;
					} catch (Throwable e) {
						if (AppDebugConfig.IS_DEBUG) {
							KLog.e(AppDebugConfig.TAG_FRAG, e);
						}
					}
					return -1;
				}
			});
			for (int i = 0;i < data.size();i++) {
				TimeDataList<IndexGiftNew> d = data.get(i);
				StringBuffer date = new StringBuffer();
				switch(i){
					case 0:
						date.append("今天");
						break;
					case 1:
						date.append("昨天");
						break;
					default:
						date.append("以前");
						break;
				}
				for (int t = 0; t < d.data.size(); t++) {
					mData.add(d.data.get(t));
					mDate.add(date.toString());
				}
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		initViewManger(R.layout.fragment_gift_limmit_lv_container);
		mDataView = getViewById(R.id.lv_content);
	}

	@Override
	protected void setListener() {
		ObserverManager.getInstance().addGiftUpdateListener(this);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void processLogic(Bundle savedInstanceState) {
		ReqPageData data = new ReqPageData();

		mAdapter = new LimmitGiftListAdapter(getContext());
		if (getArguments() != null) {
			Serializable s = getArguments().getSerializable(KEY_DATA);
			if (s != null) {
				getDateFromTimeDataList((ArrayList<TimeDataList<IndexGiftNew>>) s);
				mAdapter.setData(mData, mDate);
				mHasData = true;
				mLastPage = 1;
			}
		}
		mAdapter.setListener(this);
		mDataView.setAdapter(mAdapter);
		if (mData != null) {
			mNoMoreLoad = true;
			mRefreshLayout.setCanShowLoad(mData.size() >= 6);
			updateData(mData);
		}
		startClockService();
	}

	@Override
	public void onLoad() {
		//不加载更多
		mRefreshLayout.setLoading(false);
		return;
	}
	@Override
	protected void lazyLoad() {
		refreshInitConfig();
		Global.THREAD_POOL.execute(new Runnable() {
			@Override
			public void run() {
				if (NetworkUtil.isConnected(getContext())) {
					Global.getNetEngine().obtainGiftLimit(new JsonReqBase<String>()).enqueue(
							new Callback<JsonRespGiftList>() {
								@Override
								public void onResponse(Response<JsonRespGiftList> response, Retrofit retrofit) {
									if(!mCanShowUI) {
										return;
									}
									if(response != null && response.isSuccess() && response.body() != null && response.body().getCode() == StatusCode.SUCCESS) {
										refreshSuccessEnd();
										getDateFromTimeDataList(response.body().getData());
										refreshLoadState(mData, true);
										updateData(mData);
										return;
									}
									refreshFailEnd();
								}
								@Override
								public void onFailure(Throwable t) {
									if(!mCanShowUI) {
										return;
									}
									if(AppDebugConfig.IS_DEBUG) {
									KLog.e(AppDebugConfig.TAG_FRAG, t);
									}
									refreshFailEnd();
								}
							}
					);
				} else {
					mViewManager.showErrorRetry();
				}
			}
		});
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		stopClockService();
	}

	@Override
	public void onGiftUpdate(int action) {
		if (action != ObserverManager.STATUS.GIFT_UPDATE_PART
				&& action != ObserverManager.STATUS.GIFT_UPDATE_ALL) {
			return;
		}
		if (mIsSwipeRefresh || mIsNotifyRefresh || mData == null) {
			return;
		}
		mIsNotifyRefresh = true;
		Global.THREAD_POOL.execute(new Runnable() {
			@Override
			public void run() {
				HashSet<Integer> ids = new HashSet<Integer>();
				for (IndexGiftNew gift : mData) {
					ids.add(gift.id);
				}
				ReqRefreshGift reqData = new ReqRefreshGift();
				reqData.ids = ids;
				Global.getNetEngine().refreshGift(new JsonReqBase<ReqRefreshGift>(reqData))
						.enqueue(new Callback<JsonRespBase<HashMap<String, IndexGiftNew>>>() {

							@Override
							public void onResponse(Response<JsonRespBase<HashMap<String, IndexGiftNew>>> response,
							                       Retrofit retrofit) {
								if (response != null && response.isSuccess()) {
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
							public void onFailure(Throwable t) {
								mIsNotifyRefresh = false;
							}
						});
			}
		});
	}

	@Override
	public void release() {

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
		mLastPage = 1;
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

	private void startClockService() {
		Intent intent = new Intent(getContext(), ClockService.class);
		getContext().startService(intent);
	}

	private void stopClockService() {
		Intent intent = new Intent(getContext(), ClockService.class);
		getContext().stopService(intent);
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
