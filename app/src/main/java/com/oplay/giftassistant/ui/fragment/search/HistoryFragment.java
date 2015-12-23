package com.oplay.giftassistant.ui.fragment.search;

import android.os.Bundle;
import android.widget.ListView;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.adapter.SearchHistoryAdapter;
import com.oplay.giftassistant.config.AppDebugConfig;
import com.oplay.giftassistant.ui.activity.SearchActivity;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment;
import com.socks.library.KLog;

import java.util.List;

/**
 * Created by zsigui on 15-12-22.
 */
public class HistoryFragment extends BaseFragment {

	private ListView mListView;
	private SearchHistoryAdapter mAdapter;

	public static HistoryFragment newInstance() {
		return new HistoryFragment();
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		setContentView(R.layout.fragment_search_history);
		mListView = getViewById(R.id.lv_history);
		mAdapter = new SearchHistoryAdapter(null, true);

	}

	@Override
	protected void setListener() {
		mAdapter.setClearListener(new SearchHistoryAdapter.OnClearListener() {
			@Override
			public void onClearPerform() {
				((SearchActivity)getActivity()).clearHistory();
			}

			@Override
			public void onSearchPerform(String keyword) {
				((SearchActivity)getActivity()).sendSearchRequest(keyword);
			}
		});
	}

	@Override
	protected void processLogic(Bundle savedInstanceState) {
		mListView.setAdapter(mAdapter);
	}

	@Override
	protected void lazyLoad() {

	}

	public void updateData(List<String> data, boolean isHistory) {
		if (mAdapter != null) {
			if (AppDebugConfig.IS_FRAG_DEBUG) {
				KLog.v(data);
			}
			mAdapter.updateData(data, isHistory);
		}
	}
}
