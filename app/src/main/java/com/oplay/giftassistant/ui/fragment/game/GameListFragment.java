package com.oplay.giftassistant.ui.fragment.game;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.adapter.NestedGameListAdapter;
import com.oplay.giftassistant.config.GameTypeUtil;
import com.oplay.giftassistant.config.Global;
import com.oplay.giftassistant.listener.OnItemClickListener;
import com.oplay.giftassistant.model.data.req.ReqPageData;
import com.oplay.giftassistant.model.data.resp.IndexGameNew;
import com.oplay.giftassistant.model.data.resp.OneTypeDataList;
import com.oplay.giftassistant.model.json.base.JsonReqBase;
import com.oplay.giftassistant.model.json.base.JsonRespBase;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment_Refresh;
import com.oplay.giftassistant.util.IntentUtil;
import com.oplay.giftassistant.util.NetworkUtil;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by zsigui on 16-1-4.
 */
public class GameListFragment extends BaseFragment_Refresh<IndexGameNew> implements OnItemClickListener<IndexGameNew> {

	private static final String KEY_URL = "key_data_url";
	private static final String KEY_SEARCH = "key_data_search";
	private static final String KEY_TAG_ID = "key_tag_id";

	private String mUrl;
	private String mSearchKey;
	private int mTagId = -1;

	private JsonReqBase<ReqPageData> mReqPageObj;
	private NestedGameListAdapter mAdapter;
	private ListView mDataView;

	public static GameListFragment newInstance(String url, String searchKey) {
		GameListFragment fragment = new GameListFragment();
		Bundle bundle = new Bundle();
		bundle.putString(KEY_URL, url);
		bundle.putString(KEY_SEARCH, searchKey);
		fragment.setArguments(bundle);
		return fragment;
	}

	public static GameListFragment newInstance(String url, int tagId) {
		GameListFragment fragment = new GameListFragment();
		Bundle bundle = new Bundle();
		bundle.putString(KEY_URL, url);
		bundle.putInt(KEY_TAG_ID, tagId);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		initViewManger(R.layout.fragment_refresh_lv_container);

		mDataView = getViewById(R.id.rv_content);
	}

	@Override
	protected void setListener() {
	}

	@Override
	protected void processLogic(Bundle savedInstanceState) {
		ReqPageData data = new ReqPageData();
		mReqPageObj = new JsonReqBase<ReqPageData>(data);


		if (getArguments() != null) {
			mUrl = getArguments().getString(KEY_URL);
			mSearchKey = getArguments().getString(KEY_SEARCH);
			mTagId = getArguments().getInt(KEY_TAG_ID, -1);
		}

		if (mTagId == -1) {
			mReqPageObj.data.searchKey = mSearchKey;
		} else {
			mReqPageObj.data.labelType = mTagId;
		}

		mAdapter = new NestedGameListAdapter(getContext(), this);
		mDataView.setAdapter(mAdapter);
	}

	@Override
	protected void lazyLoad() {
		refreshInitConfig();

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
										refreshSuccessEnd();
										OneTypeDataList<IndexGameNew> backObj = response.body().getData();
										setLoadState(backObj.data, backObj.isEndPage);
										updateData(backObj.data);
										return;
									}
									refreshFailEnd();
								}

								@Override
								public void onFailure(Throwable t) {
									refreshFailEnd();
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

	@Override
	public void onItemClick(IndexGameNew item, View view, int position) {
		IntentUtil.jumpGameDetail(getContext(), item.id, GameTypeUtil.JUMP_STATUS_DETAIL);
	}
}
