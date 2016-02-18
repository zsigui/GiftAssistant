package com.oplay.giftcool.ui.fragment.game;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.GameSuperAdapter;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.StatusCode;
import com.oplay.giftcool.model.data.resp.IndexGameSuper;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Refresh;
import com.oplay.giftcool.util.NetworkUtil;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

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
		initViewManger(R.layout.fragment_refresh_rv_container);
		mRecyclerView = getViewById(R.id.lv_content);
	}

	@Override
	protected void setListener() {
		mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
				if (newState == RecyclerView.SCROLL_STATE_IDLE) {
					ImageLoader.getInstance().resume();
				} else if (newState == RecyclerView.SCROLL_STATE_SETTLING
						|| newState == RecyclerView.SCROLL_STATE_DRAGGING) {
					ImageLoader.getInstance().pause();
				}
			}


		});
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void processLogic(Bundle savedInstanceState) {

		// 设置RecyclerView的LayoutManager
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
		mAdapter = new GameSuperAdapter(getActivity(), this);
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

	@Override
    protected void lazyLoad() {
	    refreshInitConfig();
        Global.THREAD_POOL.execute(new Runnable() {
            @Override
            public void run() {
                if (NetworkUtil.isConnected(getContext())) {
                    Global.getNetEngine().obtainIndexGameSuper(new JsonReqBase<String>(null))
                            .enqueue(new Callback<JsonRespBase<IndexGameSuper>>() {

                                @Override
                                public void onResponse(Response<JsonRespBase<IndexGameSuper>> response,
                                                       Retrofit retrofit) {
	                                if (!mCanShowUI) {
		                                return;
	                                }
                                    if (response != null && response.isSuccess()) {
	                                    if (response.body() != null && response.body().getCode() == StatusCode.SUCCESS) {
		                                    updateData(response.body().getData());
		                                    refreshSuccessEnd();
		                                    return;
	                                    }
                                    }
                                    // 出错
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

	@Override
	public String getPageName() {
		return PAGE_NAME;
	}
}
