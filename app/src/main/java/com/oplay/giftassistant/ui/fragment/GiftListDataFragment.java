package com.oplay.giftassistant.ui.fragment;

import android.os.Bundle;
import android.widget.ListView;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.adapter.IndexGiftNewAdapter;
import com.oplay.giftassistant.model.data.resp.IndexGiftNew;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment;
import com.oplay.giftassistant.util.ViewUtil;

import java.io.Serializable;
import java.util.ArrayList;

import cn.bingoogolapple.refreshlayout.BGARefreshLayout;

/**
 * 显示限量礼包数据的Fragment<br/>
 * 具备下拉刷新(由于数据已最新，不具备实际意义),上拉加载(显示更多同日期数据)<br/>
 * <br/>
 * Created by zsigui on 15-12-29.
 */
public class GiftListDataFragment extends BaseFragment implements BGARefreshLayout.BGARefreshLayoutDelegate {

	private static final String KEY_DATA = "key_news_data";

	private BGARefreshLayout mRefreshLayout;
	private ListView mDataView;
	private IndexGiftNewAdapter mDataAdapter;

	public static GiftListDataFragment newInstance() {
		return new GiftListDataFragment();
	}

	public static GiftListDataFragment newInstance(ArrayList<IndexGiftNew> data) {
		GiftListDataFragment fragment = new GiftListDataFragment();
		Bundle bundle = new Bundle();
		bundle.putSerializable(KEY_DATA, data);
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
		ViewUtil.initRefreshLayout(getContext(), mRefreshLayout);
		mDataAdapter = new IndexGiftNewAdapter(getContext());
		if (getArguments() != null) {
			Serializable s = getArguments().getSerializable(KEY_DATA);
			if (s != null) {
				mDataAdapter.setData((ArrayList<IndexGiftNew>) s);
			}
		}
		mDataView.setAdapter(mDataAdapter);
	}

	public void updateData(ArrayList<IndexGiftNew> data) {
		if (data == null) {
			return;
		}
		mDataAdapter.updateData(data);
	}

	@Override
	protected void lazyLoad() {
		mHasData = true;
	}

	@Override
	public void onBGARefreshLayoutBeginRefreshing(BGARefreshLayout bgaRefreshLayout) {
		mRefreshLayout.endRefreshing();
	}

	@Override
	public boolean onBGARefreshLayoutBeginLoadingMore(BGARefreshLayout bgaRefreshLayout) {
		mRefreshLayout.endLoadingMore();
		return false;
	}
}
