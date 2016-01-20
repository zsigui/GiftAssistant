package com.oplay.giftassistant.ui.fragment.gift;

import android.os.Bundle;
import android.widget.ListView;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.adapter.NestedGiftListAdapter;
import com.oplay.giftassistant.config.GiftTypeUtil;
import com.oplay.giftassistant.config.Global;
import com.oplay.giftassistant.model.data.req.ReqPageData;
import com.oplay.giftassistant.model.data.resp.IndexGiftNew;
import com.oplay.giftassistant.model.data.resp.OneTypeDataList;
import com.oplay.giftassistant.model.json.base.JsonReqBase;
import com.oplay.giftassistant.model.json.base.JsonRespBase;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment_Refresh;
import com.oplay.giftassistant.util.DateUtil;
import com.oplay.giftassistant.util.NetworkUtil;

import java.io.Serializable;
import java.util.ArrayList;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * 显示限量礼包数据的Fragment<br/>
 * 具备下拉刷新(由于数据已最新，不具备实际意义),上拉加载(显示更多同日期数据)<br/>
 * <br/>
 * Created by zsigui on 15-12-29.
 */
public class GiftListDataFragment extends BaseFragment_Refresh {

	private static final String KEY_DATA = "key_news_data";
	private static final String KEY_URL = "key_url";
	private static final String KEY_DATE = "key_date";

	private ListView mDataView;
	private ArrayList<IndexGiftNew> mData;
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
		setContentView(R.layout.fragment_gift_list_data);
		mDataView = getViewById(R.id.lv_container);
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
		mRefreshLayout.setCanShowLoad(false);
	}

	@Override
	protected void lazyLoad() {

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
			ng.img = "http://owan-avatar.ymapp.com/app/10657/icon/icon_1450246643.png_140_140_100.png";
			ng.name = "普通礼包";
			ng.isLimit = false;
			ng.score = (int) (Math.random() * 100) * 10;
			ng.searchTime = DateUtil.getDate("yyyy-MM-dd HH:mm", 3);
			ng.seizeTime = DateUtil.getDate("yyyy-MM-dd HH:mm", 5);
			ng.searchCount = 0;
			ng.remainCount = 100;
			ng.totalCount = 100;
			ng.content = "30钻石，5000金币，武器经验卡x6，100块神魂石，10000颗迷魂珠";
			obj.data.add(ng);
		}
		return obj;
	}
}
