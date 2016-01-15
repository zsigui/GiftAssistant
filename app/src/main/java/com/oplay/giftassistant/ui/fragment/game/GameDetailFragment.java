package com.oplay.giftassistant.ui.fragment.game;

import android.os.Bundle;
import android.widget.TextView;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.config.WebViewUrl;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment_WebView;

/**
 * Created by zsigui on 16-1-15.
 */
public class GameDetailFragment extends BaseFragment_WebView {

	private static final String KEY_ID = "key_data_id";
	private static final String KEY_COLOR = "key_data_color";

	private int mId;
	private int mStatusBarColor;
	private TextView btnDownload;

	public static GameDetailFragment newInstance(int id, int color) {
		GameDetailFragment fragment = new GameDetailFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(KEY_ID, id);
		bundle.putInt(KEY_COLOR, color);
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
		mStatusBarColor = getArguments().getInt(KEY_COLOR);
		String url = WebViewUrl.GAME_DETAIL + "?id=" + mId + "&theme=" + getResources().getColor(mStatusBarColor);
		loadUrl(url);
		mIsLoading = true;
	}

	@Override
	protected void lazyLoad() {
		mIsLoading = true;
		reloadPage();
	}
}
