package com.oplay.giftcool.ui.fragment.gift;

import android.os.Bundle;
import android.widget.ListView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.NestedGiftListAdapter;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.StatusCode;
import com.oplay.giftcool.model.data.req.ReqPageData;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;
import com.oplay.giftcool.model.data.resp.OneTypeDataList;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Refresh;
import com.oplay.giftcool.util.NetworkUtil;
import com.socks.library.KLog;

import java.io.Serializable;
import java.util.ArrayList;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * 显示限量礼包数据的Fragment<br/>
 * 具备下拉刷新,上拉加载(显示更多同日期数据)<br/>
 * <br/>
 * Created by zsigui on 15-12-29.
 */
public class GiftListDataFragment extends BaseFragment_Refresh<IndexGiftNew> {

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
	protected void initView(Bundle savedInstanceState) {
		initViewManger(R.layout.fragment_refresh_lv_container);
		mDataView = getViewById(R.id.lv_content);
	}

	@Override
	protected void setListener() {
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void processLogic(Bundle savedInstanceState) {
		ReqPageData data = new ReqPageData();
		mReqPageObj = new JsonReqBase<ReqPageData>(data);

		mAdapter = new NestedGiftListAdapter(getActivity());
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
		mDataView.setAdapter(mAdapter);
		if (mData != null) {
			if (mData.size() < 10) {
				mRefreshLayout.setCanShowLoad(false);
				mNoMoreLoad = true;
			} else {
				mRefreshLayout.setCanShowLoad(true);
				mNoMoreLoad = false;
			}
			updateData(mData);
		}
	}

	@Override
	protected void lazyLoad() {
		refreshInitConfig();
		Global.THREAD_POOL.execute(new Runnable() {
			@Override
			public void run() {
				if (NetworkUtil.isConnected(getContext())) {
					mReqPageObj.data.page = 1;
					mReqPageObj.data.date = mDate;
					Global.getNetEngine().obtainGiftList(mUrl, mReqPageObj)
							.enqueue(new Callback<JsonRespBase<OneTypeDataList<IndexGiftNew>>>() {
								@Override
								public void onResponse(Response<JsonRespBase<OneTypeDataList<IndexGiftNew>>> response,
								                       Retrofit
										                       retrofit) {
									if (!mCanShowUI) {
										return;
									}
									if (response != null && response.isSuccess() && response.body() != null &&
											response.body().getCode() == StatusCode.SUCCESS) {
										refreshSuccessEnd();
										OneTypeDataList<IndexGiftNew> backObj = response.body().getData();
										setLoadState(backObj.data, backObj.isEndPage);
										updateData(backObj.data);
										return;
									}
									refreshFailEnd();
								}

								@Override
								public void onFailure(Throwable t) {
									if (!mCanShowUI) {
										return;
									}
									if (AppDebugConfig.IS_DEBUG) {
										KLog.e(AppDebugConfig.TAG_FRAG, t);
									}
									refreshFailEnd();
								}
							});
				} else {
					mViewManager.showErrorRetry();
				}
			}
		});
	}

	/**
	 * 加载更多数据
	 */
	@Override
	protected void loadMoreData() {
		if (mNoMoreLoad || mIsLoadMore) {
			return;
		}
		mIsLoadMore = true;
		mReqPageObj.data.date = mDate;
		mReqPageObj.data.page = mLastPage + 1;
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (!NetworkUtil.isConnected(getContext())) {
					mViewManager.showErrorRetry();
					return;
				}
				Global.getNetEngine().obtainGiftList(mUrl, mReqPageObj)
						.enqueue(new Callback<JsonRespBase<OneTypeDataList<IndexGiftNew>>>() {
							@Override
							public void onResponse(Response<JsonRespBase<OneTypeDataList<IndexGiftNew>>> response,
							                       Retrofit
									retrofit) {
								if (!mCanShowUI) {
									return;
								}
								if (response != null && response.isSuccess()) {
									moreLoadSuccessEnd();
									OneTypeDataList<IndexGiftNew> backObj = response.body().getData();
									setLoadState(backObj.data, backObj.isEndPage);
									addMoreData(backObj.data);
									return;
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
			}
		}).start();

	}

	public void updateData(ArrayList<IndexGiftNew> data) {
		mViewManager.showContent();
		mHasData = true;
		mData = data;
		mAdapter.updateData(mData);
		mLastPage = 1;
	}

	private void addMoreData(ArrayList<IndexGiftNew> moreData) {
		if (moreData == null) {
			return;
		}
		mData.addAll(moreData);
		mAdapter.updateData(mData);
		mLastPage += 1;
	}

	@Override
	public String getPageName() {
		return null;
	}
}
