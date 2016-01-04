package com.oplay.giftassistant.ui.fragment.game;

import android.os.Bundle;
import android.widget.ListView;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.adapter.IndexGameNewAdapter;
import com.oplay.giftassistant.config.Global;
import com.oplay.giftassistant.model.data.req.ReqPageData;
import com.oplay.giftassistant.model.data.resp.IndexGameNew;
import com.oplay.giftassistant.model.data.resp.OneTypeDataList;
import com.oplay.giftassistant.model.json.base.JsonReqBase;
import com.oplay.giftassistant.model.json.base.JsonRespBase;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment_Refresh;
import com.oplay.giftassistant.util.NetworkUtil;
import com.oplay.giftassistant.util.ViewUtil;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by zsigui on 16-1-4.
 */
public class GameListFragment extends BaseFragment_Refresh<IndexGameNew> {

	private static final String KEY_URL = "key_data_url";
	private static final String KEY_SEARCH = "key_data_search";

	private String mUrl;
	private String mSearchKey;

	private JsonReqBase<ReqPageData> mReqPageObj;
	private IndexGameNewAdapter mAdapter;
	private ListView mDataView;

	public static GameListFragment newInstance(String url, String searchKey) {
		GameListFragment fragment = new GameListFragment();
		Bundle bundle = new Bundle();
		bundle.putString(KEY_URL, url);
		bundle.putString(KEY_SEARCH, searchKey);
		return fragment;
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		initViewManger(R.layout.fragment_refresh_lv_container);

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


		if (getArguments() != null) {
			mUrl = getArguments().getString(KEY_URL);
			mSearchKey = getArguments().getString(KEY_SEARCH);
		}
		mReqPageObj.data.searchKey = mSearchKey;

		ViewUtil.initRefreshLayout(getContext(), mRefreshLayout);
		mAdapter = new IndexGameNewAdapter(getContext());
		mDataView.setAdapter(mAdapter);
	}

	@Override
	protected void lazyLoad() {
		lazyLoadInitConfig();

		new Thread(new Runnable() {
			@Override
			public void run() {
				if (NetworkUtil.isConnected(getContext())) {
					mReqPageObj.data.page = 1;
					Global.getNetEngine().obtainGameList(mUrl, mReqPageObj)
							.enqueue(new Callback<JsonRespBase<OneTypeDataList<IndexGameNew>>>() {
								@Override
								public void onResponse(Response<JsonRespBase<OneTypeDataList<IndexGameNew>>> response, Retrofit
										retrofit) {
									if (response != null && response.isSuccess()) {
										lazyLoadSuccessEnd();
										OneTypeDataList<IndexGameNew> backObj = response.body().getData();
										setLoadState(backObj.data, backObj.isEndPage);
										updateData(backObj.data);
										return;
									}
									lazyLoadFailEnd();
								}

								@Override
								public void onFailure(Throwable t) {
									lazyLoadFailEnd();
									OneTypeDataList<IndexGameNew> backObj = initStashRefreshData();
									setLoadState(backObj.data, backObj.isEndPage);
									updateData(backObj.data);
								}
							});
				} else {
					mViewManager.showErrorRetry();
				}
			}
		}).start();
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
						Global.getNetEngine().obtainGameList(mUrl, mReqPageObj)
								.enqueue(new Callback<JsonRespBase<OneTypeDataList<IndexGameNew>>>() {
									@Override
									public void onResponse(Response<JsonRespBase<OneTypeDataList<IndexGameNew>>> response, Retrofit
											retrofit) {
										if (response != null && response.isSuccess()) {
											moreLoadSuccessEnd();
											OneTypeDataList<IndexGameNew> backObj = response.body().getData();
											setLoadState(backObj.data, backObj.isEndPage);
											addMoreData(backObj.data);
											return;
										}
										moreLoadFailEnd();
									}

									@Override
									public void onFailure(Throwable t) {
										moreLoadFailEnd();

										OneTypeDataList<IndexGameNew> backObj = initStashMoreRefreshData();
										setLoadState(backObj.data, backObj.isEndPage);
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
