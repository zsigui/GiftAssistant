package com.oplay.giftcool.ui.fragment.search;

import android.os.Bundle;
import android.widget.ListView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.SearchHistoryAdapter;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.ui.activity.SearchActivity;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.socks.library.KLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zsigui on 15-12-22.
 */
public class HistoryFragment extends BaseFragment {

    private static final String KEY_DATA = "key_history_data";
    private static final String KEY_IS_HISTORY = "key_is_history";
	private ListView mListView;
	private SearchHistoryAdapter mAdapter;

	public static HistoryFragment newInstance() {
		return new HistoryFragment();
	}

    public static HistoryFragment newInstance(ArrayList<String> data, boolean isHistory) {
        HistoryFragment fragment = new HistoryFragment();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(KEY_DATA, data);
        bundle.putBoolean(KEY_IS_HISTORY, isHistory);
        fragment.setArguments(bundle);
        return fragment;
    }

	@Override
	protected void initView(Bundle savedInstanceState) {
		setContentView(R.layout.fragment_search_history);
		mListView = getViewById(R.id.lv_history);
        ArrayList<String> data = null;
        boolean isHistory = true;
        if (getArguments() != null) {
            data = getArguments().getStringArrayList(KEY_DATA);
            isHistory = getArguments().getBoolean(KEY_IS_HISTORY);
        }
        mAdapter = new SearchHistoryAdapter(data, isHistory);
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
