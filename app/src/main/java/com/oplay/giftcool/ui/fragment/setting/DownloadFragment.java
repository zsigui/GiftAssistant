package com.oplay.giftcool.ui.fragment.setting;

import android.os.Bundle;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.AppDownloadAdapter;
import com.oplay.giftcool.download.ApkDownloadManager;
import com.oplay.giftcool.model.data.resp.GameDownloadInfo;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.oplay.giftcool.ui.widget.StickyListHeadersListViewExpandable;

import java.util.List;

/**
 * Created by zsigui on 16-1-6.
 */
public class DownloadFragment extends BaseFragment {

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

	}

	@Override
	protected void processLogic(Bundle savedInstanceState) {
		int count = mAdapter.getCount();
		if (count == 0) {
			showEmpty();
		} else {
			showContent();
		}
	}

	@Override
	protected void lazyLoad() {

	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mAdapter != null) {
			mAdapter.release();
		}
	}

//	@Override
//	public void onResume() {
//		super.onResume();
//		mData = ApkDownloadManager.getInstance(getContext()).getDownloadList();
//		mAdapter.notifyDataSetUpdated();
//	}

	@Override
	public String getPageName() {
		return PAGE_NAME;
	}

	public void showContent() {
		if (mViewManager != null) {
			mViewManager.showContent();
		}
	}

	public void showEmpty() {
		if (mViewManager != null) {
			mViewManager.showEmpty();
		}
	}
}
