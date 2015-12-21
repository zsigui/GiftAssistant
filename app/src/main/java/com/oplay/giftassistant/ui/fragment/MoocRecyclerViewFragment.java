package com.oplay.giftassistant.ui.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.engine.NetEngine;
import com.oplay.giftassistant.model.DataModel;
import com.oplay.giftassistant.model.UserModel;
import com.oplay.giftassistant.adapter.NormalRecyclerViewAdapter;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment;
import com.oplay.giftassistant.util.ThreadUtil;
import com.socks.library.KLog;

import java.util.List;

import cn.bingoogolapple.androidcommon.adapter.BGAOnItemChildClickListener;
import cn.bingoogolapple.androidcommon.adapter.BGAOnItemChildLongClickListener;
import cn.bingoogolapple.androidcommon.adapter.BGAOnRVItemClickListener;
import cn.bingoogolapple.androidcommon.adapter.BGAOnRVItemLongClickListener;
import cn.bingoogolapple.refreshlayout.BGAMoocStyleRefreshViewHolder;
import cn.bingoogolapple.refreshlayout.BGARefreshLayout;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by zsigui on 15-12-16.
 */
public class MoocRecyclerViewFragment extends BaseFragment implements BGARefreshLayout.BGARefreshLayoutDelegate, BGAOnRVItemClickListener, BGAOnRVItemLongClickListener, BGAOnItemChildClickListener, BGAOnItemChildLongClickListener {
	private NormalRecyclerViewAdapter mAdapter;
	private BGARefreshLayout mRefreshLayout;
	private RecyclerView mDataRv;
	private int mNewPageNumber = 0;
	private int mMorePageNumber = 0;
    private NetEngine mEngine;

	@Override
	protected void initView(Bundle savedInstanceState) {
		setContentView(R.layout.fragment_first);
		mRefreshLayout = getViewById(R.id.rl_recyclerview_refresh);
		mDataRv = getViewById(R.id.rv_recyclerview_data);
	}

	@Override
	protected void setListener() {
		mRefreshLayout.setDelegate(this);

		mAdapter = new NormalRecyclerViewAdapter(mDataRv);
		mAdapter.setOnRVItemClickListener(this);
		mAdapter.setOnRVItemLongClickListener(this);
		mAdapter.setOnItemChildClickListener(this);
		mAdapter.setOnItemChildLongClickListener(this);

		// 使用addOnScrollListener，而不是setOnScrollListener();
		mDataRv.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

			}

			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

			}
		});
	}

	@Override
	protected void processLogic(Bundle savedInstanceState) {
        mEngine = mApp.getRetrofit().create(NetEngine.class);
		BGAMoocStyleRefreshViewHolder moocStyleRefreshViewHolder = new BGAMoocStyleRefreshViewHolder(mApp, true);
		moocStyleRefreshViewHolder.setUltimateColor(R.color.material_blue_grey_950);
		moocStyleRefreshViewHolder.setOriginalImage(R.mipmap.ic_launcher);
		moocStyleRefreshViewHolder.setSpringDistanceScale(0.2f);
		mRefreshLayout.setRefreshViewHolder(moocStyleRefreshViewHolder);

		mDataRv.setLayoutManager(new LinearLayoutManager(mApp, LinearLayoutManager.VERTICAL, false));

		mDataRv.setAdapter(mAdapter);
	}

	@Override
	protected void lazyLoad() {
		mNewPageNumber = 0;
		mMorePageNumber = 0;
		mEngine.obtainAllUser().enqueue(new Callback<DataModel<List<UserModel>>>() {
			@Override
			public void onResponse(Response<DataModel<List<UserModel>>> response, Retrofit retrofit) {
				KLog.d("http-status = " + response.message() + ", resp-status = " + response.body().status);
				mAdapter.setDatas(response.body().data);
			}

			@Override
			public void onFailure(Throwable t) {
				KLog.e(t);
			}
		});
	}

	private int k = 0;

	@Override
	public void onBGARefreshLayoutBeginRefreshing(BGARefreshLayout refreshLayout) {
		mNewPageNumber++;
		if (mNewPageNumber > 4) {
			mRefreshLayout.endRefreshing();
			showToast("没有最新数据了");
			return;
		}

		mEngine.obtainNewUser().enqueue(new Callback<DataModel<List<UserModel>>>() {
			@Override
			public void onResponse(final Response<DataModel<List<UserModel>>> response, Retrofit retrofit) {
				ThreadUtil.runInUIThread(new Runnable() {
					@Override
					public void run() {
						mRefreshLayout.endRefreshing();
						mAdapter.addNewDatas(response.body().data);
					}
				}, 2000);
			}

			@Override
			public void onFailure(Throwable t) {
				KLog.e(t);
				mRefreshLayout.endRefreshing();
			}
		});


	}

	@Override
	public boolean onBGARefreshLayoutBeginLoadingMore(BGARefreshLayout refreshLayout) {
		mMorePageNumber++;
		if (mMorePageNumber > 5) {
			mRefreshLayout.endLoadingMore();
			showToast("没有更多数据了");
			return false;
		}


		mEngine.obtainAllUser().enqueue(new Callback<DataModel<List<UserModel>>>() {
			@Override
			public void onResponse(final Response<DataModel<List<UserModel>>> response, Retrofit retrofit) {
				ThreadUtil.runInUIThread(new Runnable() {
					@Override
					public void run() {
						mRefreshLayout.endLoadingMore();
						mAdapter.addMoreDatas(response.body().data);
					}
				}, 2000);
			}

			@Override
			public void onFailure(Throwable t) {
				KLog.e(t);
				mRefreshLayout.endLoadingMore();
			}
		});

		return true;
	}

	@Override
	public void onItemChildClick(ViewGroup parent, View childView, int position) {
		if (childView.getId() == R.id.tv_item_normal_delete) {
			mAdapter.removeItem(position);
		}
	}

	@Override
	public boolean onItemChildLongClick(ViewGroup parent, View childView, int position) {
		if (childView.getId() == R.id.tv_item_normal_delete) {
			showToast("长按了删除 " + mAdapter.getItem(position).name);
			return true;
		}
		return false;
	}

	@Override
	public void onRVItemClick(ViewGroup parent, View itemView, int position) {
		showToast("点击了条目 " + mAdapter.getItem(position).name);
	}

	@Override
	public boolean onRVItemLongClick(ViewGroup parent, View itemView, int position) {
		showToast("长按了条目 " + mAdapter.getItem(position).name);
		return true;
	}
}
