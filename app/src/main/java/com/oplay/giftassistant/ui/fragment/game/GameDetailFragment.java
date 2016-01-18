package com.oplay.giftassistant.ui.fragment.game;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.config.WebViewUrl;
import com.oplay.giftassistant.download.ApkDownloadManager;
import com.oplay.giftassistant.download.listener.OnDownloadStatusChangeListener;
import com.oplay.giftassistant.download.listener.OnProgressUpdateListener;
import com.oplay.giftassistant.manager.AccountManager;
import com.oplay.giftassistant.model.data.resp.IndexGameNew;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment_WebView;
import com.oplay.giftassistant.ui.widget.button.DownloadButtonView;

/**
 * Created by zsigui on 16-1-15.
 */
public class GameDetailFragment extends BaseFragment_WebView implements OnDownloadStatusChangeListener,
		OnProgressUpdateListener {

	private static final String KEY_ID = "key_data_id";
	private static final String KEY_COLOR = "key_data_color";

	private int mId;
	private LinearLayout downloadLayout;
	private DownloadButtonView btnDownload;
	private IndexGameNew mAppInfo;
	private String mStatusBarColor;

	public static GameDetailFragment newInstance(int id, String color) {
		GameDetailFragment fragment = new GameDetailFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(KEY_ID, id);
		bundle.putString(KEY_COLOR, color);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		initViewManger(R.layout.fragment_game_detail);
		btnDownload = getViewById(R.id.btn_download);
		downloadLayout = getViewById(R.id.ll_download);
	}

	@Override
	protected void setListener() {
		btnDownload.setOnClickListener(this);
		ApkDownloadManager.getInstance(getActivity()).addDownloadStatusListener(this);
		ApkDownloadManager.getInstance(getActivity()).addProgressUpdateListener(this);
	}

	@Override
	protected void processLogic(Bundle savedInstanceState) {
		if (getArguments() == null) {
			mViewManager.showEmpty();
			return;
		}
		mId = getArguments().getInt(KEY_ID);
		mStatusBarColor = getArguments().getString(KEY_COLOR, "f85454");
		String url = WebViewUrl.GAME_DETAIL + "?id=" + mId + "&theme=" + mStatusBarColor;
		if (AccountManager.getInstance().isLogin()) {
			AccountManager.getInstance().syncCookie();
		}
		loadUrl(url);
		mIsLoading = true;
	}

	@Override
	protected void lazyLoad() {
		mIsLoading = true;
		reloadPage();
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
			case R.id.btn_download:
				if (mAppInfo != null) {
					mAppInfo.handleOnClick(getFragmentManager());
				}
				break;
			default:
				break;
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		ApkDownloadManager.getInstance(getActivity()).removeDownloadStatusListener(this);
		ApkDownloadManager.getInstance(getActivity()).removeProgressUpdateListener(this);
	}

	public void setDownloadBtn(final boolean isShow, final IndexGameNew appInfo) {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				int visiable = isShow ? View.VISIBLE : View.GONE;
				mAppInfo = appInfo;
				if (downloadLayout != null) {
					downloadLayout.setVisibility(visiable);
				}
				if (isShow && btnDownload != null) {
					mAppInfo.initAppInfoStatus(getActivity());
					int progress = ApkDownloadManager.getInstance(getActivity()).getProgressByUrl(mAppInfo.downloadUrl);
					btnDownload.setStatus(mAppInfo.appStatus);
					btnDownload.setProgress(progress);
				}
			}
		});
	}

	@Override
	public void onDownloadStatusChanged(IndexGameNew appInfo) {
		if (downloadLayout.isShown()) {
			btnDownload.setStatus(appInfo.appStatus);
		}
	}

	@Override
	public void onProgressUpdate(String url, int percent, long speedBytesPers) {
		if (downloadLayout.isShown()) {
			btnDownload.setProgress(ApkDownloadManager.getInstance(getActivity()).getProgressByUrl(url));
		}
	}
}
