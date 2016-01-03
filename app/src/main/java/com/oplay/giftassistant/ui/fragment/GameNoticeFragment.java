package com.oplay.giftassistant.ui.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.adapter.GameNoticeAdapter;
import com.oplay.giftassistant.adapter.other.DividerItemDecoration;
import com.oplay.giftassistant.config.Global;
import com.oplay.giftassistant.model.data.req.ReqPageData;
import com.oplay.giftassistant.model.data.resp.IndexGameNew;
import com.oplay.giftassistant.model.data.resp.OneTypeDataList;
import com.oplay.giftassistant.model.json.base.JsonReqBase;
import com.oplay.giftassistant.model.json.base.JsonRespBase;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment_Refresh;
import com.oplay.giftassistant.util.NetworkUtil;
import com.oplay.giftassistant.util.ToastUtil;
import com.oplay.giftassistant.util.ViewUtil;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by zsigui on 15-12-30.
 */
public class GameNoticeFragment extends BaseFragment_Refresh {

	private static final String KEY_DATA = "key_data";
	private ArrayList<IndexGameNew> mData;
	private JsonReqBase<ReqPageData> mReqPageObj;

	private RecyclerView mDataView;
	private GameNoticeAdapter mAdapter;

	public static GameNoticeFragment newInstance() {
		return new GameNoticeFragment();
	}

	public static GameNoticeFragment newInstance(ArrayList<IndexGameNew> data) {
		GameNoticeFragment fragment = new GameNoticeFragment();
		Bundle bundle = new Bundle();
		bundle.putSerializable(KEY_DATA, data);
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
	protected void processLogic(Bundle savedInstanceState) {
		ReqPageData data = new ReqPageData();
		mReqPageObj = new JsonReqBase<ReqPageData>(data);

		ViewUtil.initRefreshLayout(getContext(), mRefreshLayout);
		LinearLayoutManager llm = new LinearLayoutManager(getContext());
		llm.setOrientation(LinearLayoutManager.VERTICAL);
        DividerItemDecoration decoration = new DividerItemDecoration(getContext(), llm.getOrientation());
		mDataView.setLayoutManager(llm);
        mDataView.addItemDecoration(decoration);
		mAdapter = new GameNoticeAdapter(mDataView);

		mDataView.setAdapter(mAdapter);
	}

	@Override
	protected void lazyLoad() {
        mIsLoading = true;
        if (!mIsRefresh) {
            mViewManager.showLoading();
            mHasData = false;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (NetworkUtil.isConnected(getContext())) {
                    mReqPageObj.data.page = 1;
                    Global.getNetEngine().obtainIndexGameNotice(mReqPageObj)
                            .enqueue(new Callback<JsonRespBase<OneTypeDataList<IndexGameNew>>>() {
                                @Override
                                public void onResponse(Response<JsonRespBase<OneTypeDataList<IndexGameNew>>> response, Retrofit
                                        retrofit) {
                                    if (response != null && response.isSuccess()) {
                                        mHasData = true;
                                        OneTypeDataList<IndexGameNew> backObj = response.body().getData();
                                        setLoadState(backObj);
                                        mIsLoading = mIsRefresh = false;
                                        updateData(backObj.data);
                                        return;
                                    }
                                    if (mIsRefresh) {
                                        // 放弃此次获取数据请求
                                        ToastUtil.showShort("刷新请求出错");
                                    } else {
                                        // 初次获取失败
                                        mHasData = false;
                                        mViewManager.showErrorRetry();
                                    }
                                    mRefreshLayout.endRefreshing();
                                    mIsLoading = mIsRefresh = false;
                                }

                                @Override
                                public void onFailure(Throwable t) {
                                    if (!mIsRefresh) {
                                        mHasData = false;
                                        mViewManager.showErrorRetry();
                                    }
                                    mIsLoading = mIsRefresh = false;
                                    mRefreshLayout.endRefreshing();
                                    OneTypeDataList<IndexGameNew> backObj = initStashRefreshData();
                                    setLoadState(backObj);
                                    updateData(backObj.data);
                                }
                            });
                } else {
                    mViewManager.showErrorRetry();
                }
            }
        }).start();
	}

    private void setLoadState(OneTypeDataList<IndexGameNew> backObj) {
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
			mReqPageObj.data.page = mLastPage + 1;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (NetworkUtil.isConnected(getContext())) {
                        Global.getNetEngine().obtainIndexGameNotice(mReqPageObj)
                                .enqueue(new Callback<JsonRespBase<OneTypeDataList<IndexGameNew>>>() {
                                    @Override
                                    public void onResponse(Response<JsonRespBase<OneTypeDataList<IndexGameNew>>> response, Retrofit
                                            retrofit) {
                                        mIsLoadMore = false;
                                        mRefreshLayout.endLoadingMore();
                                        if (response != null && response.isSuccess()) {
                                            OneTypeDataList<IndexGameNew> backObj = response.body().getData();
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

                                        OneTypeDataList<IndexGameNew> backObj = initStashMoreRefreshData();
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

    public void updateData(ArrayList<IndexGameNew> data) {
        mViewManager.showContent();
        mHasData = true;
        mData = data;
        mAdapter.updateData(mData);
        mLastPage = 1;
    }

    private void addMoreData(ArrayList<IndexGameNew> moreData) {
        if (moreData == null) {
            return;
        }
        mData.addAll(moreData);
        mAdapter.updateData(mData);
        mLastPage += 1;
    }

    public OneTypeDataList<IndexGameNew> initStashRefreshData() {
        OneTypeDataList<IndexGameNew> obj = new OneTypeDataList<>();
        obj.data = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            IndexGameNew game = new IndexGameNew();
            game.id = i + 1;
            game.name = "全民神将-攻城战";
            game.newCount = 2;
            game.playCount = 53143;
            game.totalCount = 12;
            game.giftName = "至尊礼包";
            game.img = "http://owan-img.ymapp.com/app/10986/icon/icon_1449227350.png_140_140_100.png";
            game.size = "" + (0.8 * i + 10 * i);
            obj.data.add(game);
        }
        obj.page = 1;
        obj.isEndPage = 0;
        return obj;
    }

    public OneTypeDataList<IndexGameNew> initStashMoreRefreshData() {
        OneTypeDataList<IndexGameNew> obj = new OneTypeDataList<>();
        obj.data = new ArrayList<>();
        for (int i = mLastPage * 10; i < 10 + mLastPage * 10; i++) {
            IndexGameNew game = new IndexGameNew();
            game.id = i + 1;
            game.name = "鬼吹灯之挖挖乐";
            game.newCount = 2;
            game.playCount = 53143;
            game.totalCount = 12;
            game.giftName = "高级礼包";
            game.img = "http://owan-img.ymapp.com/app/11061/icon/icon_1450325761.png_140_140_100.png";
            game.size = "" + (0.8 * i + 10 * i);
            obj.data.add(game);
        }
        obj.page = mLastPage + 1;
        obj.isEndPage = (int)(Math.random() * 2);
        return obj;
    }
}
