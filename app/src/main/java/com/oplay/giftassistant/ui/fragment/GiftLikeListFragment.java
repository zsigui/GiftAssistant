package com.oplay.giftassistant.ui.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.adapter.GiftLikeListAdapter;
import com.oplay.giftassistant.adapter.other.DividerItemDecoration;
import com.oplay.giftassistant.model.data.resp.IndexGiftLike;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by zsigui on 15-12-30.
 */
public class GiftLikeListFragment extends BaseFragment {


	private static final String KEY_DATA = "key_like_data";

	private RecyclerView rvData;
	private GiftLikeListAdapter mAdapter;

	public static GiftLikeListFragment newInstance() {
		return new GiftLikeListFragment();
	}

	public static GiftLikeListFragment newInstance(ArrayList<IndexGiftLike> data) {
		GiftLikeListFragment fragment = new GiftLikeListFragment();
		Bundle bundle = new Bundle();
		bundle.putSerializable(KEY_DATA, data);
		fragment.setArguments(bundle);
		return fragment;
	}


	@Override
	protected void initView(Bundle savedInstanceState) {
		setContentView(R.layout.fragment_gift_list_like_container);
		rvData = getViewById(R.id.rv_container);
	}

	@Override
	protected void setListener() {

	}

	@Override
	@SuppressWarnings("unchecked")
	protected void processLogic(Bundle savedInstanceState) {

		LinearLayoutManager llm = new LinearLayoutManager(getContext());
		llm.setOrientation(LinearLayoutManager.VERTICAL);
		DividerItemDecoration decoration = new DividerItemDecoration(getContext(),
				llm.getOrientation(),
				getContext().getResources().getColor(R.color.co_divider_bg),
				1);
		rvData.setLayoutManager(llm);
		rvData.addItemDecoration(decoration);
		mAdapter = new GiftLikeListAdapter(rvData);

		if (getArguments() != null) {
			Serializable s = getArguments().getSerializable(KEY_DATA);
			if (s != null) {
				mAdapter.setDatas((ArrayList<IndexGiftLike>) s);
			}
		}

		rvData.setAdapter(mAdapter);

	}

	public void updateData(ArrayList<IndexGiftLike> data) {
		if (data == null) {
			return;
		}
		mAdapter.setDatas(data);
		mAdapter.notifyDataSetChanged();
	}

	@Override
	protected void lazyLoad() {

	}


}
