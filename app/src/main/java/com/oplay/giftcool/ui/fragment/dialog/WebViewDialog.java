package com.oplay.giftcool.ui.fragment.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Dialog_NoButton;

/**
 * Created by zsigui on 16-1-29.
 */
public class WebViewDialog extends BaseFragment_Dialog_NoButton {

	private final static String KEY_TITLE = "title";
	private final static String KEY_URL = "url";
	String mTitle;
	String mUrl;


	public static WebViewDialog newInstance(String title, String url) {
		WebViewDialog dialog = new WebViewDialog();
		Bundle bundle = new Bundle();
		bundle.putString(KEY_TITLE, title);
		bundle.putString(KEY_URL, url);
		dialog.setArguments(bundle);
		return dialog;
	}

	@Override
	protected void bindViewWithData(View contentView, @Nullable Bundle savedInstanceState) {
		WebView wv = (WebView) contentView.findViewById(R.id.wv_container);
		wv.loadUrl(mUrl);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Bundle args = getArguments();
		if (args != null) {
			mTitle = args.getString(KEY_TITLE);
			mUrl = args.getString(KEY_URL);
		}
		super.onCreate(savedInstanceState);
	}

	@Override
	protected String getTitle() {
		return mTitle;
	}

	@Override
	protected View getContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return LayoutInflater.from(getActivity()).inflate(R.layout.fragment_webview, container, false);
	}
}
