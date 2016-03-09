package com.oplay.giftcool.ui.fragment.game;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.GameNoticeAdapter;
import com.oplay.giftcool.adapter.other.DividerItemDecoration;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.NetUrl;
import com.oplay.giftcool.config.NetStatusCode;
import com.oplay.giftcool.model.data.req.ReqPageData;
import com.oplay.giftcool.model.data.resp.IndexGameNew;
import com.oplay.giftcool.model.data.resp.OneTypeDataList;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Refresh;
import com.oplay.giftcool.util.NetworkUtil;
import com.socks.library.KLog;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by zsigui on 15-12-30.
 */
public class GameNoticeFragment extends BaseFragment_Refresh<IndexGameNew> {

	private final static String PAGE_NAME = "游戏排行";
	private final static String KEY_DATA = "key_data";
	private final static long MAINTAIN_DATA_TIME = 5 * 1000;
	private JsonReqBase<ReqPageData> mReqPageObj;

	private RecyclerView mDataView;
	private GameNoticeAdapter mAdapter;

	private boolean mInPage = false;
	private boolean mIsRunning = false;
	private Handler mHandler = new Handler();
	/**
	 * 每隔5秒调起一次清除数据
	 */
	private Runnable mClearDataTask = new Runnable() {
		@Override
		public void run() {
			if (!mCanShowUI) {
				return;
			}
			if (mInPage || mData == null || mData.size() < 10) {
				mHandler.postDelayed(this, MAINTAIN_DATA_TIME);
				return;
			}
			ArrayList<IndexGameNew> remainData = new ArrayList<>(10);
			for (int i = 0; i < 10; i++) {
				remainData.add(mData.get(i));
			}
			if (!mInPage || mIsRunning) {
				mNoMoreLoad = false;
				updateData(remainData);
			}
		}
	};

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
		mDataView = getViewById(R.id.lv_content);
	}

	@Override
	protected void setListener() {
		mDataView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
				if (ImageLoader.getInstance().isInited()) {
					if (newState == RecyclerView.SCROLL_STATE_IDLE) {
						ImageLoader.getInstance().resume();
					} else if (newState == RecyclerView.SCROLL_STATE_SETTLING
							|| newState == RecyclerView.SCROLL_STATE_DRAGGING) {
						ImageLoader.getInstance().pause();
					}
				}
			}
		});
	}

	@Override
	protected void processLogic(Bundle savedInstanceState) {
		ReqPageData data = new ReqPageData();
		mReqPageObj = new JsonReqBase<ReqPageData>(data);

		LinearLayoutManager llm = new LinearLayoutManager(getContext());
		llm.setOrientation(LinearLayoutManager.VERTICAL);
		DividerItemDecoration decoration = new DividerItemDecoration(getContext(), llm.getOrientation());
		mDataView.setLayoutManager(llm);
		mDataView.addItemDecoration(decoration);
		mAdapter = new GameNoticeAdapter(getActivity());
		mDataView.setAdapter(mAdapter);
		mViewManager.showContent();
	}

	@Override
	protected void lazyLoad() {
		refreshInitConfig();

		Global.THREAD_POOL.execute(new Runnable() {
			@Override
			public void run() {
				if (NetworkUtil.isConnected(getContext())) {
					mReqPageObj.data.page = 1;
					Global.getNetEngine().obtainGameList(NetUrl.GAME_GET_INDEX_NOTICE, mReqPageObj)
							.enqueue(new Callback<JsonRespBase<OneTypeDataList<IndexGameNew>>>() {
								@Override
								public void onResponse(Response<JsonRespBase<OneTypeDataList<IndexGameNew>>> response,
								                       Retrofit retrofit) {
									if (!mCanShowUI) {
										return;
									}
									if (response != null && response.isSuccess()) {
										if (response.body() != null &&
												response.body().getCode() == NetStatusCode.SUCCESS) {
											refreshSuccessEnd();
											OneTypeDataList<IndexGameNew> backObj = response.body().getData();
											refreshLoadState(backObj.data, backObj.isEndPage);
											updateData(backObj.data);
											return;
										}
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
						Global.getNetEngine().obtainGameList(NetUrl.GAME_GET_INDEX_NOTICE, mReqPageObj)
								.enqueue(new Callback<JsonRespBase<OneTypeDataList<IndexGameNew>>>() {
									@Override
									public void onResponse(Response<JsonRespBase<OneTypeDataList<IndexGameNew>>>
											                       response, Retrofit
											retrofit) {
										if (!mCanShowUI) {
											return;
										}
										if (response != null && response.isSuccess()) {
											if (response.body() != null && response.body().isSuccess()) {
												moreLoadSuccessEnd();
												OneTypeDataList<IndexGameNew> backObj = response.body().getData();
												setLoadState(backObj.data, backObj.isEndPage);
												addMoreData(backObj.data);
												return;
											}
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
		if (data == null || data.size() == 0) {
			mViewManager.showEmpty();
			return;
		}
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

	/*@Override
	public void onPause() {
		super.onPause();
		if (!mIsRunning) {
			mHandler.postDelayed(mClearDataTask, MAINTAIN_DATA_TIME);
			mIsRunning = true;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mInPage && mIsRunning) {
			mHandler.removeCallbacks(mClearDataTask);
			mIsRunning = false;
		}
	}*/

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		mInPage = isVisibleToUser;
		if (isVisibleToUser) {
			mHandler.removeCallbacks(mClearDataTask);
			mIsRunning = false;
		} else {
			mHandler.postDelayed(mClearDataTask, MAINTAIN_DATA_TIME);
			mIsRunning = true;
		}
	}

	@Override
	public String getPageName() {
		return PAGE_NAME;
	}
}
