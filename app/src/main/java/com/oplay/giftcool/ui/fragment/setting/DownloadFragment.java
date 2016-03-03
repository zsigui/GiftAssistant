package com.oplay.giftcool.ui.fragment.setting;

import android.os.Bundle;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.AppDownloadAdapter;
import com.oplay.giftcool.download.ApkDownloadManager;
import com.oplay.giftcool.download.listener.OnDownloadStatusChangeListener;
import com.oplay.giftcool.download.listener.OnProgressUpdateListener;
import com.oplay.giftcool.model.data.resp.GameDownloadInfo;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.oplay.giftcool.ui.widget.StickyListHeadersListViewExpandable;

import java.util.List;

/**
 * Created by zsigui on 16-1-6.
 */
public class DownloadFragment extends BaseFragment implements OnDownloadStatusChangeListener, OnProgressUpdateListener {

	private final static String PAGE_NAME = "下载管理";
	private AppDownloadAdapter mAdapter;
	private List<GameDownloadInfo> mListData;


	public static DownloadFragment newInstance() {
		return new DownloadFragment();
	}



	@Override
	protected void initView(Bundle savedInstanceState) {
		initViewManger(R.layout.fragment_download);
		mListData = ApkDownloadManager.getInstance(getActivity()).getDownloadList();
		mAdapter = new AppDownloadAdapter(mListData, this);
		StickyListHeadersListViewExpandable listView = getViewById(R.id.download_adapterView);
		listView.setAdapter(mAdapter);
		mAdapter.setExpandableListView(listView);
	}

	@Override
	protected void setListener() {
		ApkDownloadManager.getInstance(getActivity()).addDownloadStatusListener(this);
		ApkDownloadManager.getInstance(getActivity()).addProgressUpdateListener(this);
	}

	@Override
	protected void processLogic(Bundle savedInstanceState) {
		if (mListData == null || mListData.size() == 0) {
			mViewManager.showEmpty();
		} else {
			mViewManager.showContent();
		}

	}

	@Override
	protected void lazyLoad() {

	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		ApkDownloadManager.getInstance(getActivity()).removeDownloadStatusListener(this);
		ApkDownloadManager.getInstance(getActivity()).removeProgressUpdateListener(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mAdapter != null) {
			mAdapter.onDestroy();
		}
	}

//	@Override
//	public void onResume() {
//		super.onResume();
//		mData = ApkDownloadManager.getInstance(getActivity()).getDownloadList();
//		mAdapter.notifyDataSetUpdated();
//	}

	@Override
	public void onDownloadStatusChanged(GameDownloadInfo appInfo) {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (mAdapter != null) {
					mAdapter.notifyStatusChanged();
					if (mAdapter.getCount() > 0) {
						mViewManager.showContent();
					} else {
						mViewManager.showEmpty();
					}
				}
			}
		});
	}

	@Override
	public void onProgressUpdate(final String url, final int percent, final long speedBytesPers) {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (mAdapter != null) {
					mAdapter.updateDownloadingView(url, percent, speedBytesPers);
				}
			}
		});
	}

	@Override
	public String getPageName() {
		return PAGE_NAME;
	}
}
