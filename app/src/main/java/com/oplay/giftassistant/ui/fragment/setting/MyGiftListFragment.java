package com.oplay.giftassistant.ui.fragment.setting;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.adapter.MyGiftListAdapter;
import com.oplay.giftassistant.adapter.other.DividerItemDecoration;
import com.oplay.giftassistant.config.AppDebugConfig;
import com.oplay.giftassistant.config.GiftTypeUtil;
import com.oplay.giftassistant.config.Global;
import com.oplay.giftassistant.config.KeyConfig;
import com.oplay.giftassistant.config.NetUrl;
import com.oplay.giftassistant.config.StatusCode;
import com.oplay.giftassistant.model.data.req.ReqPageData;
import com.oplay.giftassistant.model.data.resp.IndexGiftNew;
import com.oplay.giftassistant.model.data.resp.OneTypeDataList;
import com.oplay.giftassistant.model.json.base.JsonReqBase;
import com.oplay.giftassistant.model.json.base.JsonRespBase;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment_Refresh;
import com.oplay.giftassistant.util.DateUtil;
import com.oplay.giftassistant.util.NetworkUtil;
import com.oplay.giftassistant.util.ViewUtil;
import com.socks.library.KLog;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by zsigui on 16-1-7.
 */
public class MyGiftListFragment extends BaseFragment_Refresh<IndexGiftNew> {

	private RecyclerView mDataView;
	private MyGiftListAdapter mAdapter;
	private JsonReqBase<ReqPageData> mReqPageObj;
	private String mUrl;

	public static MyGiftListFragment newInstance(String url) {
		MyGiftListFragment fragment = new MyGiftListFragment();
		Bundle bundle = new Bundle();
		bundle.putString(KeyConfig.KEY_URL, url);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		initViewManger(R.layout.fragment_refresh_rv_container);
		mRefreshLayout = getViewById(R.id.srl_layout);
		mDataView = getViewById(R.id.rv_container);
	}

	@Override
	protected void setListener() {
		mRefreshLayout.setDelegate(this);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void processLogic(Bundle savedInstanceState) {
		ReqPageData data = new ReqPageData();
		mReqPageObj = new JsonReqBase<ReqPageData>(data);

		ViewUtil.initRefreshLayout(getContext(), mRefreshLayout);
		mAdapter = new MyGiftListAdapter(mDataView);
		if (getArguments() != null) {
			mUrl = getArguments().getString(KeyConfig.KEY_URL);
		}
		mDataView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
		mDataView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
		mDataView.setAdapter(mAdapter);
		mRefreshLayout.setPullDownRefreshEnable(false);
	}

	@Override
	protected void lazyLoad() {
		lazyLoadInitConfig();

		Global.THREAD_POOL.execute(new Runnable() {
			@Override
			public void run() {
				if (NetworkUtil.isConnected(getContext())) {
					mReqPageObj.data.page = 1;
					Global.getNetEngine().obtainGiftList(NetUrl.GAME_GET_INDEX_NOTICE, mReqPageObj)
							.enqueue(new Callback<JsonRespBase<OneTypeDataList<IndexGiftNew>>>() {
								@Override
								public void onResponse(Response<JsonRespBase<OneTypeDataList<IndexGiftNew>>> response,
								                       Retrofit
										retrofit) {
									if (response != null && response.isSuccess() &&
											response.body().getCode() == StatusCode.SUCCESS) {
										lazyLoadSuccessEnd();
										OneTypeDataList<IndexGiftNew> backObj = response.body().getData();
										setLoadState(backObj.data, backObj.isEndPage);
										updateData(backObj.data);
										return;
									}
									lazyLoadFailEnd();
								}

								@Override
								public void onFailure(Throwable t) {
									if (AppDebugConfig.IS_DEBUG) {
										KLog.e(AppDebugConfig.TAG_FRAG, t);
									}
									// lazyLoadFailEnd();
									OneTypeDataList<IndexGiftNew> backObj = initStashMoreRefreshData();
									setLoadState(backObj.data, backObj.isEndPage);
									updateData(backObj.data);
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

								if (response != null && response.isSuccess() &&
										response.body().getCode() == StatusCode.SUCCESS) {
									moreLoadSuccessEnd();
									OneTypeDataList<IndexGiftNew> backObj = response.body().getData();
									setLoadState(backObj.data, backObj.isEndPage);
									return;
								}
								moreLoadFailEnd();
							}

							@Override
							public void onFailure(Throwable t) {
								moreLoadFailEnd();

								OneTypeDataList<IndexGiftNew> backObj = initStashMoreRefreshData();
								setLoadState(backObj.data, backObj.isEndPage);
								addMoreData(backObj.data);
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

	public OneTypeDataList<IndexGiftNew> initStashMoreRefreshData() {
		OneTypeDataList<IndexGiftNew> obj = new OneTypeDataList<>();
		obj.data = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			IndexGiftNew ng = new IndexGiftNew();
			ng.gameName = "逍遥西游";
			ng.id = i;
			ng.status = GiftTypeUtil.STATUS_SEIZE;
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
			ng.useStartTime = "2015-09-22 9:30";
			ng.useEndTime = "2016-09-22 9:30";
			ng.content = "30钻石，5000金币，武器经验卡x6，100块神魂石，10000颗迷魂珠";
			ng.code = "12341k23j4k1j23k";
			obj.data.add(ng);
		}
		return obj;
	}
}
