package com.oplay.giftassistant.ui.fragment.gift;

import android.os.Bundle;
import android.widget.TextView;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.config.WebViewUrl;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment_WebView;

/**
 * Created by zsigui on 16-1-15.
 */
public class GiftWebDetailFragment extends BaseFragment_WebView {

	private static final String KEY_ID = "key_data_id";
	private int mId;
	private TextView btnDownload;

	public static GiftWebDetailFragment newInstance(int id) {
		GiftWebDetailFragment fragment = new GiftWebDetailFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(KEY_ID, id);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		initViewManger(R.layout.fragment_game_detail);
		btnDownload = getViewById(R.id.btn_download);
	}

	@Override
	protected void setListener() {
		btnDownload.setOnClickListener(this);
	}

	@Override
	protected void processLogic(Bundle savedInstanceState) {
		if (getArguments() == null) {
			mViewManager.showEmpty();
			return;
		}
		mId = getArguments().getInt(KEY_ID);
		String url = WebViewUrl.GIFT_DETAIL + "?id=" + mId;
		loadUrl(url);
		mIsLoading = true;
	}

	@Override
	protected void lazyLoad() {
		mIsLoading = true;
		reloadPage();
	}
}
