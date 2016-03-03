package com.oplay.giftcool.ui.fragment.search;

import android.os.Bundle;
import android.widget.ListView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.SearchPromptAdapter;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.listener.OnSearchListener;
import com.oplay.giftcool.model.data.resp.PromptData;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.socks.library.KLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zsigui on 15-12-22.
 */
public class PromptFragment extends BaseFragment {

    private final static String PAGE_NAME = "搜索提示页";
    private static final String KEY_DATA = "key_prompt_data";
    private ListView mListView;
    private SearchPromptAdapter mAdapter;

    public static PromptFragment newInstance() {
        return new PromptFragment();
    }

    public static PromptFragment newInstance(ArrayList<PromptData> data) {
        PromptFragment fragment = new PromptFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_DATA, data);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.fragment_search_prompt);
        mListView = getViewById(R.id.lv_content);
        ArrayList<PromptData> data = null;
        if (getArguments() != null) {
            data = (ArrayList<PromptData>) getArguments().getSerializable(KEY_DATA);
        }
        mAdapter = new SearchPromptAdapter(getContext(), data);
    }

    @Override
    protected void setListener() {
        mAdapter.setSearchListener(new OnSearchListener() {
            @Override
            public void sendSearchRequest(String keyword, int id) {
                if (getActivity() instanceof OnSearchListener) {
                    ((OnSearchListener) getActivity()).sendSearchRequest(keyword, id);
                }
            }

            @Override
            public void clearHistory() {
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

    public void updateData(List<PromptData> data) {
        if (mAdapter != null) {
            if (AppDebugConfig.IS_FRAG_DEBUG) {
                KLog.v(data);
            }
            mAdapter.updateData(data);
        }
    }

    @Override
    public void release() {
        super.release();
        if (mAdapter != null) {
            mAdapter.release();
            mAdapter = null;
        }
        if (mListView != null) {
            mListView.setAdapter(null);
            mListView = null;
        }
    }

    @Override
    public String getPageName() {
        return PAGE_NAME;
    }
}
