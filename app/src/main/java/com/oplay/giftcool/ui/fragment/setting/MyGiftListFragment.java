package com.oplay.giftcool.ui.fragment.setting;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.MyGiftListAdapter;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.config.NetUrl;
import com.oplay.giftcool.config.StatusCode;
import com.oplay.giftcool.listener.OnItemClickListener;
import com.oplay.giftcool.model.data.req.ReqPageData;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;
import com.oplay.giftcool.model.data.resp.OneTypeDataList;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Refresh;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.NetworkUtil;
import com.socks.library.KLog;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by zsigui on 16-1-7.
 */
public class MyGiftListFragment extends BaseFragment_Refresh<IndexGiftNew> {

	private ListView mDataView;
	private MyGiftListAdapter mAdapter;
	private JsonReqBase<ReqPageData> mReqPageObj;
	private int mType;

	public static MyGiftListFragment newInstance(int type) {
		MyGiftListFragment fragment = new MyGiftListFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(KeyConfig.KEY_DATA, type);
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

		if (getArguments() != null) {
			mType = getArguments().getInt(KeyConfig.KEY_DATA);
		}
		mAdapter = new MyGiftListAdapter(getContext(), mType);
		mAdapter.setItemClickListener(new OnItemClickListener<IndexGiftNew>() {
			@Override
			public void onItemClick(IndexGiftNew item, View view, int position) {
				IntentUtil.jumpGiftDetail(getContext(), item.id);
			}
		});
		/*mDataView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
		mDataView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));*/
		mDataView.setAdapter(mAdapter);
	}

	@Override
	protected void lazyLoad() {
		refreshInitConfig();

		Global.THREAD_POOL.execute(new Runnable() {
			@Override
			public void run() {
				if (NetworkUtil.isConnected(getContext())) {
					mReqPageObj.data.page = 1;
					mReqPageObj.data.type = mType;
					Global.getNetEngine().obtainGiftList(NetUrl.USER_GIFT_SEIZED, mReqPageObj)
							.enqueue(new Callback<JsonRespBase<OneTypeDataList<IndexGiftNew>>>() {
								@Override
								public void onResponse(Response<JsonRespBase<OneTypeDataList<IndexGiftNew>>> response,
								                       Retrofit
										retrofit) {
									if (response != null && response.isSuccess() && response.body() != null &&
											response.body().getCode() == StatusCode.SUCCESS) {
										refreshSuccessEnd();
										OneTypeDataList<IndexGiftNew> backObj = response.body().getData();
										refreshLoadState(backObj.data, backObj.isEndPage);
										updateData(backObj.data);
										return;
									}
									refreshFailEnd();
								}

								@Override
								public void onFailure(Throwable t) {
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

	@Override
	protected void refreshLoadState(Object data, boolean isEndPage) {
		if (data != null && data instanceof List) {
			mRefreshLayout.setCanShowLoad(((List)data).size() > 4);
			mNoMoreLoad = isEndPage || ((List)data).size() < 10;
		} else {
			mNoMoreLoad = isEndPage || data == null;
		}
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
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (!NetworkUtil.isConnected(getContext())) {
					mViewManager.showErrorRetry();
					return;
				}
				mReqPageObj.data.page = mLastPage + 1;
				mReqPageObj.data.type = mType;
				Global.getNetEngine().obtainGiftList(NetUrl.USER_GIFT_SEIZED, mReqPageObj)
						.enqueue(new Callback<JsonRespBase<OneTypeDataList<IndexGiftNew>>>() {
							@Override
							public void onResponse(Response<JsonRespBase<OneTypeDataList<IndexGiftNew>>> response,
							                       Retrofit
									                       retrofit) {

								if (response != null && response.isSuccess() && response.body() != null &&
										response.body().getCode() == StatusCode.SUCCESS) {
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
								moreLoadFailEnd();
							}
						});
			}
		}).start();

	}

	private int mCurY;
	public void updateData(ArrayList<IndexGiftNew> data) {
		mViewManager.showContent();
		mHasData = true;
		mData = data;
		mCurY = mDataView.getScrollY();
		mAdapter.updateData(mData);
		mDataView.smoothScrollBy(mCurY, 0);
		mLastPage = 1;
	}

	private void addMoreData(ArrayList<IndexGiftNew> moreData) {
		if (moreData == null) {
			return;
		}
		mData.addAll(moreData);
		mCurY = mDataView.getScrollY();
		mAdapter.updateData(mData);
		mDataView.smoothScrollBy(mCurY, 0);
		mLastPage += 1;
	}

	@Override
	public String getPageName() {
		return "我的礼包";
	}
}
