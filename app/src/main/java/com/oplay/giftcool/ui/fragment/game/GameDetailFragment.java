package com.oplay.giftcool.ui.fragment.game;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.WebViewUrl;
import com.oplay.giftcool.config.util.GameTypeUtil;
import com.oplay.giftcool.download.ApkDownloadManager;
import com.oplay.giftcool.download.listener.OnDownloadStatusChangeListener;
import com.oplay.giftcool.download.listener.OnProgressUpdateListener;
import com.oplay.giftcool.listener.ShowBottomBarListener;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.model.data.resp.GameDownloadInfo;
import com.oplay.giftcool.model.data.resp.IndexGameNew;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_WebView;
import com.oplay.giftcool.ui.widget.button.DownloadButtonView;
import com.oplay.giftcool.util.ThreadUtil;

import java.util.Locale;

/**
 * Created by zsigui on 16-1-15.
 */
public class GameDetailFragment extends BaseFragment_WebView implements OnDownloadStatusChangeListener,
        OnProgressUpdateListener, ShowBottomBarListener {

    private static final String PAGE_NAME = "游戏详情页";
    private static final String KEY_ID = "key_data_id";
    private static final String KEY_COLOR = "key_data_color";
    private static final String KEY_STATUS = "key_data_status";


    private LinearLayout downloadLayout;
    private DownloadButtonView btnDownload;
    private IndexGameNew mAppInfo;

    public static GameDetailFragment newInstance(int id, int status, String color) {
        GameDetailFragment fragment = new GameDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_ID, id);
        bundle.putString(KEY_COLOR, color);
        bundle.putInt(KEY_STATUS, status);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.fragment_game_detail);
        btnDownload = getViewById(R.id.btn_download);
        downloadLayout = getViewById(R.id.ll_download);
    }

    @Override
    protected void setListener() {
        btnDownload.setOnClickListener(this);
        ApkDownloadManager.getInstance(getContext()).addDownloadStatusListener(this);
        ApkDownloadManager.getInstance(getContext()).addProgressUpdateListener(this);
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        if (getArguments() == null) {
            mViewManager.showEmpty();
            return;
        }
        int id = getArguments().getInt(KEY_ID);
        int status = getArguments().getInt(KEY_STATUS, GameTypeUtil.JUMP_STATUS_DETAIL);
        String statusBarColor = getArguments().getString(KEY_COLOR, "f85454");
        AccountManager.getInstance().syncCookie();
        updateUrl(id, status, statusBarColor);
    }

    public void updateUrl(int id, int status, String statusBarColor) {
        String url = String.format(Locale.CHINA, "%s?id=%d&theme=%s&status=%d&download=%s",
                WebViewUrl.getWebUrl(WebViewUrl.GAME_DETAIL), id, statusBarColor, status, mApp.isAllowDownload());
        loadUrl(url);
    }

    @Override
    protected void lazyLoad() {
        reloadPage();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.btn_download:
                if (mAppInfo != null) {
                    mAppInfo.handleOnClick(getChildFragmentManager());
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ApkDownloadManager.getInstance(getContext().getApplicationContext()).removeDownloadStatusListener(this);
        ApkDownloadManager.getInstance(getContext().getApplicationContext()).removeProgressUpdateListener(this);
    }

    @Override
    public void showBar(final boolean isShow, final Object param) {
        if (param == null) {
            return;
        }
        if (AssistantApp.getInstance().isAllowDownload()) {
            ThreadUtil.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int visible = isShow ? View.VISIBLE : View.GONE;
                    mAppInfo = (IndexGameNew) param;
                    if (downloadLayout != null) {
                        downloadLayout.setVisibility(visible);
                    }
                    if (isShow && btnDownload != null) {
                        if (mAppInfo.downloadState == 1
                                && !TextUtils.isEmpty(mAppInfo.downloadUrl)) {
                            mAppInfo.initAppInfoStatus(getActivity());
                            btnDownload.setEnabled(true);
                            int progress = ApkDownloadManager.getInstance(getContext()).getProgressByUrl(mAppInfo
                                    .downloadUrl);
                            btnDownload.setStatus(mAppInfo.appStatus, "");
                            btnDownload.setProgress(progress);
                        } else {
                            btnDownload.setEnabled(false);
                        }
                    }
                }
            });
        }
    }

//	public void setDownloadBtn(final boolean isShow, final FragmentActivity hostActivity, final IndexGameNew appInfo) {
//		ThreadUtil.runOnUiThread(new Runnable() {
//			@Override
//			public void run() {
//				int visible = isShow ? View.VISIBLE : View.GONE;
//				mAppInfo = appInfo;
//				if (downloadLayout != null) {
//					downloadLayout.setVisibility(visible);
//				}
//				if (isShow && btnDownload != null) {
//					mAppInfo.initAppInfoStatus(getContext());
//					int progress = ApkDownloadManager.getInstance(getContext()).getProgressByUrl(mAppInfo
//							.downloadUrl);
//					btnDownload.setStatus(mAppInfo.appStatus, "");
//					btnDownload.setProgress(progress);
//				}
//			}
//		});
//	}

    @Override
    public void onDownloadStatusChanged(final GameDownloadInfo appInfo) {
        ThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (downloadLayout != null && downloadLayout.getVisibility() == View.VISIBLE) {
                    mAppInfo.downloadStatus = appInfo.downloadStatus;
                    mAppInfo.initAppInfoStatus(getContext());
                    btnDownload.setStatus(mAppInfo.appStatus, "");
                }
            }
        });

    }

    @Override
    public void onProgressUpdate(String url, final int percent, long speedBytesPers) {
        ThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (downloadLayout != null && downloadLayout.getVisibility() == View.VISIBLE) {
                    btnDownload.setProgress(percent);
                }
            }
        });
    }

    @Override
    public String getPageName() {
        return PAGE_NAME;
    }
}
