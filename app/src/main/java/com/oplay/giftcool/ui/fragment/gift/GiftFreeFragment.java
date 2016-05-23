package com.oplay.giftcool.ui.fragment.gift;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.FreeAdapter;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;
import com.oplay.giftcool.model.data.resp.TimeData;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Refresh;
import com.oplay.giftcool.ui.widget.stickylistheaders.StickyListHeadersListView;

/**
 * Created by zsigui on 16-5-23.
 */
public class GiftFreeFragment extends BaseFragment_Refresh<TimeData<IndexGiftNew>>{

	final int PAGE_SIZE = 20;

	private StickyListHeadersListView mDataView;
	private View mLoadingView;

	private FreeAdapter mAdapter;

	public static GiftFreeFragment newInstance() {
		return new GiftFreeFragment();
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		initViewManger(R.layout.fragment_gift_limit_lv_container);
		LayoutInflater inflater = LayoutInflater.from(getContext());
		mDataView = getViewById(R.id.lv_content);

		mLoadingView = inflater.inflate(R.layout.view_item_footer, mDataView, false);
		mLoadingView.setVisibility(View.GONE);
		mDataView.addFooterView(mLoadingView);
	}

	@Override
	protected void setListener() {

	}

	@Override
	protected void processLogic(Bundle savedInstanceState) {

	}

	@Override
	protected void lazyLoad() {

	}

	@Override
	public String getPageName() {
		return "限时免费";
	}
}
