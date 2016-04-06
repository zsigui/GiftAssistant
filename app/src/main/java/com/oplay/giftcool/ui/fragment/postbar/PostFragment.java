package com.oplay.giftcool.ui.fragment.postbar;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.PostAdapter;
import com.oplay.giftcool.adapter.itemdecoration.RoundDividerItemDecoration;
import com.oplay.giftcool.model.data.resp.IndexPostNew;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Refresh;

import java.util.ArrayList;

/**
 * 活动Fragment
 *
 * Created by zsigui on 16-4-5.
 */
public class PostFragment extends BaseFragment_Refresh {

	private final String TAG_NAME = "首页活动";
	// 页面控件
	private RecyclerView rvData;
	private PostAdapter mAdapter;

	public static PostFragment newInstance() {
		return new PostFragment();
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		initViewManger(R.layout.fragment_refresh_rv_container);
		rvData = getViewById(R.id.lv_content);
	}

	@Override
	protected void setListener() {

	}

	@Override
	protected void processLogic(Bundle savedInstanceState) {
		LinearLayoutManager llp = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
		RoundDividerItemDecoration itemDecoration = new RoundDividerItemDecoration(getContext(),
				llp.getOrientation(),
				getResources().getColor(R.color.co_divider_bg),
				getResources().getDimensionPixelSize(R.dimen.di_index_post_gap_vertical));
		rvData.setLayoutManager(llp);
		rvData.addItemDecoration(itemDecoration);
		mAdapter = new PostAdapter(getContext());
		rvData.setAdapter(mAdapter);
	}

	@Override
	protected void lazyLoad() {
		updateDate(new ArrayList<IndexPostNew>());
	}

	public void updateDate(ArrayList<IndexPostNew> data) {
		if (data == null) {
			return;
		}
		mViewManager.showContent();
		mAdapter.updateData(data);
	}


	@Override
	public String getPageName() {
		return TAG_NAME;
	}
}
