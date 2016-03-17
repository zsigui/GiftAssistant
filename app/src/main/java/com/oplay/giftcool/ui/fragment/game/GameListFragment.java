package com.oplay.giftcool.ui.fragment.game;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.NestedGameListAdapter;
import com.oplay.giftcool.config.GameTypeUtil;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.listener.OnItemClickListener;
import com.oplay.giftcool.model.AppStatus;
import com.oplay.giftcool.model.data.req.ReqPageData;
import com.oplay.giftcool.model.data.resp.IndexGameNew;
import com.oplay.giftcool.model.data.resp.OneTypeDataList;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Refresh;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.NetworkUtil;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by zsigui on 16-1-4.
 */
public class GameListFragment extends BaseFragment_Refresh<IndexGameNew> implements OnItemClickListener<IndexGameNew> {

	private final static String PAGE_NAME = "游戏列表:Key=%s,TagId=%d";
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

		mDataView = getViewById(R.id.lv_content);
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

		mAdapter = new NestedGameListAdapter(getContext(), null, this);
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
					Global.getNetEngine().obtainGameList(mUrl, mReqPageObj)
							.enqueue(new Callback<JsonRespBase<OneTypeDataList<IndexGameNew>>>() {
								@Override
								public void onResponse(Response<JsonRespBase<OneTypeDataList<IndexGameNew>>> response, Retrofit
										retrofit) {
									if (!mCanShowUI) {
										return;
									}
									if (response != null && response.isSuccess()) {
										refreshSuccessEnd();
										OneTypeDataList<IndexGameNew> backObj = response.body().getData();
										refreshLoadState(backObj.data, backObj.isEndPage);
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
	 * 加载更多数据
	 */
	@Override
	protected void loadMoreData() {
		if (!mNoMoreLoad && !mIsLoadMore) {
			mIsLoadMore = true;
			mReqPageObj.data.page = mLastPage + 1;
			Global.THREAD_POOL.execute(new Runnable() {
				@Override
				public void run() {
					if (NetworkUtil.isConnected(getContext())) {
						Global.getNetEngine().obtainGameList(mUrl, mReqPageObj)
								.enqueue(new Callback<JsonRespBase<OneTypeDataList<IndexGameNew>>>() {
									@Override
									public void onResponse(Response<JsonRespBase<OneTypeDataList<IndexGameNew>>> response, Retrofit
											retrofit) {
										if (!mCanShowUI) {
											return;
										}
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
										if (!mCanShowUI) {
											return;
										}
										moreLoadFailEnd();
									}
								});
					} else {
						moreLoadFailEnd();
					}
				}
			});
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
		if (view.getId() == R.id.tv_download) {
			if (item != null && !AppStatus.DISABLE.equals(item.appStatus)) {
				item.handleOnClick(getActivity().getSupportFragmentManager());
			}
		}else {
			IntentUtil.jumpGameDetail(getContext(), item.id, GameTypeUtil.JUMP_STATUS_DETAIL);
		}
	}

	@Override
	public String getPageName() {
		return String.format(PAGE_NAME,mSearchKey,mTagId);
	}

	@Override
	public void release() {
		super.release();
		if (mAdapter != null) {
			mAdapter.release();
			mAdapter = null;
		}
		mUrl = null;
		mSearchKey = null;
		mReqPageObj = null;
		if (mDataView != null) {
			mDataView.setAdapter(null);
			mDataView = null;
		}
	}
}
