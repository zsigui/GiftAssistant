package com.oplay.giftassistant.ui.fragment.search;

import android.os.Bundle;
import android.widget.ListView;

import com.nolanlawson.supersaiyan.widget.SuperSaiyanScrollView;
import com.oplay.giftassistant.R;
import com.oplay.giftassistant.model.data.resp.SearchDataResult;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment;

/**
 * Created by zsigui on 15-12-22.
 */
public class ResultFragment extends BaseFragment {

	private SuperSaiyanScrollView mContainerView;
	private ListView mDataView;

	public static ResultFragment newInstance() {
		return new ResultFragment();
	}


	@Override
	protected void initView(Bundle savedInstanceState) {
		setContentView(R.layout.fragment_search_data);
		mContainerView = getViewById(R.id.sssl_search_data_container);
		mDataView = getViewById(R.id.lv_search_data_content);
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

	public void updateData(SearchDataResult data) {

	}
}
