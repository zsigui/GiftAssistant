package com.oplay.giftassistant.ui.fragment.search;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zsigui on 15-12-22.
 */
public class PromptFragment extends BaseFragment {

	public static String KEY_WORD_ARG = "search_keyword";
	public static String PROMPT_KEYS_ARG = "prompt_keywords";
	private String mKeyword;
	private List<String> mPromptKeywords;

	public static Fragment newInstance() {
		return new PromptFragment();
	}

	public static Fragment newInstance(String keyword) {
		Fragment instant = new Fragment();
		Bundle bundle = new Bundle();
		bundle.putString(KEY_WORD_ARG, keyword);
		return instant;
	}

	public static Fragment newInstance(String keyword, ArrayList<String> promtKeywords) {
		Fragment instant = new Fragment();
		Bundle bundle = new Bundle();
		bundle.putString(KEY_WORD_ARG, keyword);
		bundle.putStringArrayList(PROMPT_KEYS_ARG, promtKeywords);
		return instant;
	}


	@Override
	protected void initView(Bundle savedInstanceState) {
		setContentView(R.layout.fragment_search_history);
	}

	@Override
	protected void setListener() {

	}

	@Override
	protected void processLogic(Bundle savedInstanceState) {
		if (getArguments() != null) {
			mKeyword = getArguments().getString(KEY_WORD_ARG);
			mPromptKeywords = getArguments().getStringArrayList(PROMPT_KEYS_ARG);
		}
		if (mKeyword != null) {

		}
	}

	@Override
	protected void lazyLoad() {

	}
}
