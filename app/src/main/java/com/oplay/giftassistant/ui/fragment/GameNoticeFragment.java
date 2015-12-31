package com.oplay.giftassistant.ui.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.adapter.GameNoticeAdapter;
import com.oplay.giftassistant.config.Global;
import com.oplay.giftassistant.model.data.req.ReqPageData;
import com.oplay.giftassistant.model.data.resp.IndexGameNew;
import com.oplay.giftassistant.model.data.resp.OneTypeGameList;
import com.oplay.giftassistant.model.json.base.JsonReqBase;
import com.oplay.giftassistant.model.json.base.JsonRespBase;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment;
import com.oplay.giftassistant.util.ViewUtil;

import java.io.Serializable;
import java.util.ArrayList;

import cn.bingoogolapple.refreshlayout.BGARefreshLayout;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by zsigui on 15-12-30.
 */
public class GameNoticeFragment extends BaseFragment implements BGARefreshLayout.BGARefreshLayoutDelegate {

	private static final String KEY_DATA = "key_data";
	private ArrayList<IndexGameNew> mData;
	private int mNextPage = 0;
	private JsonReqBase<ReqPageData> mPage;

	private BGARefreshLayout mRefreshLayout;
	private RecyclerView mDataView;
	private GameNoticeAdapter mAdapter;
	private boolean mNoMoreLoad = false;

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
		setContentView(R.layout.fragment_refresh_rv_container);
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
		data.page = mNextPage;
		mPage = new JsonReqBase<ReqPageData>(data);

		ViewUtil.initRefreshLayout(getContext(), mRefreshLayout);
		LinearLayoutManager llm = new LinearLayoutManager(getContext());
		llm.setOrientation(LinearLayoutManager.VERTICAL);
		mDataView.setLayoutManager(llm);
		mAdapter = new GameNoticeAdapter(mDataView);

		if (getArguments() != null) {

			Serializable s;
			s = getArguments().getSerializable(KEY_DATA);
			if (s != null) {
				mAdapter.setDatas((ArrayList<IndexGameNew>) s);
			}
		}

		mDataView.setAdapter(mAdapter);
	}

	@Override
	protected void lazyLoad() {
		loadMoreData();
	}

	private void loadMoreData() {
		if (!mNoMoreLoad || mIsLoading) {
			mIsLoading = true;
			mPage.data.page = mNextPage;
			showLoadingDialog();
			Global.getNetEngine().obtainIndexGameNotice(mPage)
					.enqueue(new Callback<JsonRespBase<OneTypeGameList<IndexGameNew>>>() {
						@Override
						public void onResponse(Response<JsonRespBase<OneTypeGameList<IndexGameNew>>> response, Retrofit
								retrofit) {
							mIsLoading = false;
							if (response != null && response.isSuccess()) {
								mHasData = true;
								OneTypeGameList<IndexGameNew> data = response.body().getData();
								if (data.isEndPage == 1) {
									// 无更多不再请求加载
									mNoMoreLoad = true;
								}
							}
						}

						@Override
						public void onFailure(Throwable t) {
							dismissLoadingDialog();
							mIsLoading = false;
							mRefreshLayout.endLoadingMore();
						}
					});
		}
	}

	@Override
	public void onBGARefreshLayoutBeginRefreshing(BGARefreshLayout refreshLayout) {

	}

	@Override
	public boolean onBGARefreshLayoutBeginLoadingMore(BGARefreshLayout refreshLayout) {
		loadMoreData();
		return false;
	}
}
