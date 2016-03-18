package com.oplay.giftcool.ui.fragment.game;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.GameSuperAdapter;
import com.oplay.giftcool.adapter.other.DividerItemDecoration;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.NetStatusCode;
import com.oplay.giftcool.model.data.resp.IndexGameSuper;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Refresh;
import com.oplay.giftcool.util.NetworkUtil;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by zsigui on 15-12-30.
 */
public class GameSuperFragment extends BaseFragment_Refresh implements View.OnClickListener {

	private final static String PAGE_NAME = "游戏精品";
	private RecyclerView mRecyclerView;
	private GameSuperAdapter mAdapter;

	public static GameSuperFragment newInstance() {
		return new GameSuperFragment();
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		initViewManger(R.layout.fragment_refresh_custome_rv_container);
		mRecyclerView = getViewById(R.id.lv_content);
	}

	@Override
	protected void setListener() {
		mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

				if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//					if (ImageLoader.getInstance().isInited()) {
//						ImageLoader.getInstance().resume();
//					}
					if (mAdapter != null) {
						mAdapter.startBanner();
					}
				} else if (newState == RecyclerView.SCROLL_STATE_SETTLING
						|| newState == RecyclerView.SCROLL_STATE_DRAGGING) {
//					if (ImageLoader.getInstance().isInited()) {
//						ImageLoader.getInstance().pause();
//					}
					if (mAdapter != null) {
						mAdapter.stopBanner();
					}
				}
			}
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void processLogic(Bundle savedInstanceState) {

		// 设置RecyclerView的LayoutManager
        DividerItemDecoration itemDecoration = new DividerItemDecoration(getContext(),
                LinearLayoutManager.VERTICAL);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.addItemDecoration(itemDecoration);
		mAdapter = new GameSuperAdapter(getActivity());
		mRecyclerView.setAdapter(mAdapter);
		mRefreshLayout.setCanShowLoad(false);
		mIsPrepared = mNoMoreLoad = true;
		mViewManager.showContent();
	}

	public void updateData(IndexGameSuper data) {
		if (data == null || mAdapter == null) {
			if (!mHasData) {
				mViewManager.showErrorRetry();
			}
			return;
		}
		mAdapter.updateData(data);
		mViewManager.showContent();
	}

	private boolean mIsResume = false;
	private boolean mIsVisible = false;

	@Override
	public void onResume() {
		super.onResume();
		if (mAdapter != null && mIsVisible) {
			mAdapter.startBanner();
		}
		mIsResume = true;
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mAdapter != null) {
			mAdapter.stopBanner();
		}
		mIsResume = false;
	}

	@Override
	protected void onUserVisible() {
		super.onUserVisible();
		if (mAdapter != null && mIsResume) {
			mAdapter.startBanner();
		}
		mIsVisible = true;
	}

	@Override
	protected void onUserInvisible() {
		super.onUserInvisible();
		if (mAdapter != null) {
			mAdapter.stopBanner();
		}
		mIsVisible = false;
	}

	/**
	 * 刷新精品游戏界面的网络请求声明
	 */
	private Call<JsonRespBase<IndexGameSuper>> mCallRefresh;

	@Override
	protected void lazyLoad() {
		refreshInitConfig();
		Global.THREAD_POOL.execute(new Runnable() {
			@Override
			public void run() {
				if (NetworkUtil.isConnected(getContext())) {
					if (mCallRefresh != null) {
						mCallRefresh.cancel();
						mCallRefresh = mCallRefresh.clone();
					} else {
						mCallRefresh = Global.getNetEngine().obtainIndexGameSuper(new JsonReqBase<Void>());
					}
					mCallRefresh.enqueue(new Callback<JsonRespBase<IndexGameSuper>>() {

								@Override
								public void onResponse(Call<JsonRespBase<IndexGameSuper>> call,
								                       Response<JsonRespBase<IndexGameSuper>> response) {
									if (!mCanShowUI || call.isCanceled()) {
										return;
									}
									if (response != null && response.isSuccessful()) {
										if (response.body() != null && response.body().getCode() == NetStatusCode
												.SUCCESS) {
											updateData(response.body().getData());
											refreshSuccessEnd();
											return;
										}
									}
									// 出错
									refreshFailEnd();
								}

								@Override
								public void onFailure(Call<JsonRespBase<IndexGameSuper>> call, Throwable t) {
									if (!mCanShowUI || call.isCanceled()) {
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

	@Override
	public String getPageName() {
		return PAGE_NAME;
	}

	@Override
	public void release() {
		super.release();
		if (mCallRefresh != null) {
			mCallRefresh.cancel();
			mCallRefresh = null;
		}
	}
}
