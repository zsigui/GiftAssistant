package com.oplay.giftcool.ui.fragment.game;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.util.GameTypeUtil;
import com.oplay.giftcool.download.ApkDownloadManager;
import com.oplay.giftcool.download.listener.OnDownloadStatusChangeListener;
import com.oplay.giftcool.download.listener.OnProgressUpdateListener;
import com.oplay.giftcool.listener.ShowBottomBarListener;
import com.oplay.giftcool.model.data.resp.GameDownloadInfo;
import com.oplay.giftcool.model.data.resp.IndexGameNew;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.oplay.giftcool.ui.widget.button.DownloadButtonView;
import com.oplay.giftcool.util.ThreadUtil;

/**
 * Created by zsigui on 16-5-4.
 */
public class GameDetailFragment_new extends BaseFragment implements OnDownloadStatusChangeListener,
		OnProgressUpdateListener, ShowBottomBarListener {

	private static final String PAGE_NAME = "游戏详情页";
	private static final String KEY_ID = "key_data_id";
	private static final String KEY_COLOR = "key_data_color";
	private static final String KEY_STATUS = "key_data_status";

	private RelativeLayout rlHeader;
	private ImageView ivIcon;
	private TextView tvName;
	private TextView tvPlay;
	private TextView tvSize;
	private TextView tvNewAdd;
	private TextView tvTotal;
	private CheckedTextView ctvFocus;
	private SmartTabLayout stlTab;
	private ViewPager vpContent;
	private LinearLayout downloadLayout;
	private DownloadButtonView btnDownload;
	private IndexGameNew mAppInfo;



	@Override
	protected void initView(Bundle savedInstanceState) {
		initViewManger(R.layout.fragment_game_detail_new);

		rlHeader = getViewById(R.id.id_stickynavlayout_topview);
		ivIcon = getViewById(R.id.iv_icon);
		tvName = getViewById(R.id.tv_name);
		tvPlay = getViewById(R.id.tv_play);
		tvSize = getViewById(R.id.tv_size);
		tvNewAdd = getViewById(R.id.tv_new_add);
		tvTotal = getViewById(R.id.tv_total);
		ctvFocus = getViewById(R.id.tv_focus);
		stlTab = getViewById(R.id.id_stickynavlayout_indicator);
		vpContent = getViewById(R.id.id_stickynavlayout_viewpager);
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
	}

	@Override
	protected void lazyLoad() {

	}

	@Override
	public String getPageName() {
		return PAGE_NAME;
	}

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
	public void showBar(boolean isShow, Object param) {

	}
}
