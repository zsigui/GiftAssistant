package com.oplay.giftassistant.ui.fragment;

import android.os.Bundle;
import android.widget.ListView;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.adapter.IndexGiftNewAdapter;
import com.oplay.giftassistant.adapter.util.GiftTypeUtil;
import com.oplay.giftassistant.config.Global;
import com.oplay.giftassistant.model.data.req.ReqPageData;
import com.oplay.giftassistant.model.data.resp.IndexGiftNew;
import com.oplay.giftassistant.model.data.resp.OneTypeDataList;
import com.oplay.giftassistant.model.json.base.JsonReqBase;
import com.oplay.giftassistant.model.json.base.JsonRespBase;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment_Refresh;
import com.oplay.giftassistant.util.NetworkUtil;
import com.oplay.giftassistant.util.ViewUtil;
import com.socks.library.KLog;

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
	private IndexGiftNewAdapter mAdapter;

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
		mRefreshLayout = getViewById(R.id.srl_layout);
		mDataView = getViewById(R.id.lv_container);
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
		mAdapter = new IndexGiftNewAdapter(getContext());
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
        mRefreshLayout.setPullDownRefreshEnable(false);
	}

    @Override
    protected void lazyLoad() {

    }

    private void setLoadState(OneTypeDataList<IndexGiftNew> backObj) {
        if (backObj.isEndPage == 1 || backObj.data == null) {
            // 无更多不再请求加载
            mNoMoreLoad = true;
            mRefreshLayout.setIsShowLoadingMoreView(false);
        } else {
            mNoMoreLoad = false;
            mRefreshLayout.setIsShowLoadingMoreView(true);
        }
    }

    /**
     * 加载更多数据
     */
    @Override
    protected void loadMoreData() {
        if (!mNoMoreLoad && !mIsLoadMore) {
            mIsLoadMore = true;
            mReqPageObj.data.date = mDate;
            KLog.e(mDate);
            mReqPageObj.data.page = mLastPage + 1;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (NetworkUtil.isConnected(getContext())) {
                        Global.getNetEngine().obtainIndexGiftList(mUrl, mReqPageObj)
                                .enqueue(new Callback<JsonRespBase<OneTypeDataList<IndexGiftNew>>>() {
                                    @Override
                                    public void onResponse(Response<JsonRespBase<OneTypeDataList<IndexGiftNew>>> response, Retrofit
                                            retrofit) {
                                        mIsLoadMore = false;
                                        mRefreshLayout.endLoadingMore();
                                        if (response != null && response.isSuccess()) {
                                            OneTypeDataList<IndexGiftNew> backObj = response.body().getData();
                                            setLoadState(backObj);
                                            addMoreData(backObj.data);
                                            return;
                                        }
                                        showToast("异常，加载失败");
                                    }

                                    @Override
                                    public void onFailure(Throwable t) {
                                        mIsLoadMore = false;
                                        mRefreshLayout.endLoadingMore();
                                        showToast("网络异常，加载失败");

                                        OneTypeDataList<IndexGiftNew> backObj = initStashMoreRefreshData();
                                        setLoadState(backObj);
                                        addMoreData(backObj.data);
                                    }
                                });
                    } else {
                        mViewManager.showErrorRetry();
                    }
                }
            }).start();
        }
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
            ng.isLimit = 0;
            ng.score = (int) (Math.random() * 100) * 10;
            ng.searchTime = System.currentTimeMillis() + 1000 * 60 * 60;
            ng.seizeTime = System.currentTimeMillis() + 1000 * 30 * 30;
            ng.searchCount = 0;
            ng.remainCount = 100;
            ng.totalCount = 100;
            ng.content = "30钻石，5000金币，武器经验卡x6，100块神魂石，10000颗迷魂珠";
            obj.data.add(ng);
        }
        return obj;
    }
}
