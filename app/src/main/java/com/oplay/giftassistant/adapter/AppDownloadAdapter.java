package com.oplay.giftassistant.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oplay.giftassistant.R;
import com.oplay.giftassistant.adapter.base.BaseListAdapter;
import com.oplay.giftassistant.config.AppDebugConfig;
import com.oplay.giftassistant.config.GameTypeUtil;
import com.oplay.giftassistant.download.ApkDownloadManager;
import com.oplay.giftassistant.model.AppStatus;
import com.oplay.giftassistant.model.DownloadStatus;
import com.oplay.giftassistant.model.data.resp.IndexGameNew;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment;
import com.oplay.giftassistant.ui.fragment.dialog.ConfirmDialog;
import com.oplay.giftassistant.ui.widget.StickyListHeadersListViewExpandable;
import com.oplay.giftassistant.ui.widget.stickylistheaders.StickyListHeadersAdapter;
import com.oplay.giftassistant.util.IntentUtil;
import com.oplay.giftassistant.util.SystemUtil;
import com.socks.library.KLog;

import java.util.HashMap;
import java.util.List;

/**
 * AppDownloadAdapter
 *
 * @author zacklpx
 *         date 16-1-12
 *         description
 */
public class AppDownloadAdapter extends BaseListAdapter<IndexGameNew> implements View.OnClickListener,
		StickyListHeadersAdapter {

	private BaseFragment mFragment;
	private ImageLoader mImageLoader;
	private ApkDownloadManager mDownloadManagerInstance;

	private HashMap<String, ViewHolder> mMap_Url_ViewHolder;

	private StickyListHeadersListViewExpandable mListView;

	private String mStrDownloadingAndPaused;
	private String mStrDownloaded;
	private int mEndIndexOfDownloading;
	private int mEndIndexOfPaused;
	private int mEndIndexOfDownloaded;

	public AppDownloadAdapter(List<IndexGameNew> listData, BaseFragment fragment) {
		super(fragment.getActivity(), listData);
		mFragment = fragment;
		mImageLoader = ImageLoader.getInstance();
		mMap_Url_ViewHolder = new HashMap<>();
		mDownloadManagerInstance = ApkDownloadManager.getInstance(fragment.getActivity());
		initIndex();
	}

	public void setExpandableListView(StickyListHeadersListViewExpandable listView) {
		mListView = listView;
	}

	public void notifyDataSetUpdated() {
		initIndex();
		notifyDataSetInvalidated();
	}

	private void initIndex() {
		final int endIndexOfDownloading = mDownloadManagerInstance.getEndOfDownloading();
		final int endIndexOfPaused = mDownloadManagerInstance.getEndOfPaused();
		final int endIndexOfDownloaded = mDownloadManagerInstance.getEndOfFinished();
		if (endIndexOfDownloading > mListData.size()) return;
		mEndIndexOfDownloading = endIndexOfDownloading >= 0 ? endIndexOfDownloading : 0;
		mEndIndexOfPaused = endIndexOfPaused;
		mEndIndexOfDownloaded = endIndexOfDownloaded;
		mStrDownloadingAndPaused = String.format("当前下载(%d)", endIndexOfPaused);
		mStrDownloaded = String.format("下载完成(%d)", endIndexOfDownloaded - endIndexOfPaused);
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.item_list_downloading, null);
			holder = bindConvertViewWithHolder(convertView);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		final IndexGameNew appInfo = mListData.get(position);
		final String rawUrl = appInfo.downloadUrl;
		mMap_Url_ViewHolder.put(rawUrl, holder);
		holder.mIvIcon.setTag(rawUrl);

		bindImageViewWithUrl(holder.mIvIcon, appInfo.img, R.drawable.ic_img_default);
		mImageLoader.displayImage(appInfo.img, holder.mIvIcon);
		initViewHolderByStatus(rawUrl, appInfo.downloadStatus);
		holder.mTvAction.setTag(TAG_POSITION, position);
		holder.mTvAction.setOnClickListener(this);
		holder.mTvInfo.setTag(TAG_POSITION, position);
		holder.mTvInfo.setOnClickListener(this);
		holder.mTvDelete.setTag(TAG_POSITION, position);
		holder.mTvDelete.setOnClickListener(this);
		convertView.setTag(TAG_POSITION, position);
		convertView.setOnClickListener(this);
		return convertView;
	}

	@Override
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		final HeaderViewHolder holder;
		if (convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.list_section_download, parent, false);
			holder = new HeaderViewHolder();
			holder.mTvHeaderText = (TextView) convertView.findViewById(R.id.tv_download_section_header);
			convertView.setTag(holder);
		} else {
			holder = (HeaderViewHolder) convertView.getTag();
		}

		if (position < mEndIndexOfPaused) {
			holder.mTvHeaderText.setText(mStrDownloadingAndPaused);
		} else {
			holder.mTvHeaderText.setText(mStrDownloaded);
		}

		return convertView;
	}

	@Override
	public long getHeaderId(int position) {
		if (position < mEndIndexOfPaused) {
			return 0;
		} else {
			return 1;
		}
	}

	@Override
	public void onDestroy() {
		if (mMap_Url_ViewHolder != null) {
			mMap_Url_ViewHolder.clear();
		}
	}

	@Override
	public void onClick(View v) {
		try {
			if (mListView != null) {
				mListView.collapse();
			}
			final Object tag = v.getTag(TAG_POSITION);
			if (!(tag instanceof Integer)) {
				return;
			}
			final int position = (Integer) tag;
			if (position >= mListData.size()) {
				return;
			}

			final IndexGameNew appInfo = mListData.get(position);
			switch (v.getId()) {
				// 下载按钮响应
				case R.id.tv_downloading_action:
					String str = ((TextView) v).getText().toString();
					if ("暂停".equals(str)) {
						appInfo.stopDownload();
					}
					if ("继续".equals(str) || "重试".equals(str)) {
						appInfo.restartDownload();
					}
					if ("安装".equals(str)) {
						appInfo.startInstall();
					}
					if ("打开".equals(str)) {
						appInfo.startApp();
					}
					break;
				// 详细按钮响应
				case R.id.tv_downloading_detail:
					IntentUtil.jumpGameDetail(mContext, appInfo.id, GameTypeUtil.JUMP_STATUS_DETAIL);
					break;
				// 删除按钮响应
				case R.id.tv_downloading_delete:
					if (position < mEndIndexOfPaused) {
						showDelDownloadingConfirmDialog(mFragment, appInfo);
					} else {
						showDelDownloadedConfirmDialog(mFragment, appInfo);
					}
					break;
				default:
					break;
			}
		} catch (Throwable e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(e);
			}
		}
	}

	private void showDelDownloadingConfirmDialog(BaseFragment activity, final IndexGameNew appInfo) {
		final ConfirmDialog confirmDialog = ConfirmDialog.newInstance();
		confirmDialog.setTitle("提示");
		confirmDialog.setContent("游戏还没下载完，确定删除吗？");
		confirmDialog.setListener(new ConfirmDialog.OnDialogClickListener() {
			@Override
			public void onCancel() {
				confirmDialog.dismiss();
			}

			@Override
			public void onConfirm() {
				mDownloadManagerInstance.removeDownloadTask(appInfo.downloadUrl);
				confirmDialog.dismiss();
			}
		});
		confirmDialog.show(activity.getChildFragmentManager(), ConfirmDialog.class.getSimpleName());
	}

	private void showDelDownloadedConfirmDialog(BaseFragment activity, final IndexGameNew appInfo) {
		final ConfirmDialog confirmDialog = ConfirmDialog.newInstance();
		confirmDialog.setTitle("提示");
		confirmDialog.setContent("确定删除安装包吗？");
		confirmDialog.setListener(new ConfirmDialog.OnDialogClickListener() {
			@Override
			public void onCancel() {
				confirmDialog.dismiss();
			}

			@Override
			public void onConfirm() {
				mDownloadManagerInstance.removeDownloadTask(appInfo.downloadUrl);
				SystemUtil.deletePackage(appInfo.getDestFilePath());
				confirmDialog.dismiss();
			}
		});
		confirmDialog.show(activity.getChildFragmentManager(), ConfirmDialog.class.getSimpleName());
	}

	private ViewHolder bindConvertViewWithHolder(View convertView) {
		final ViewHolder holder = new ViewHolder();
		holder.mIvIcon = (ImageView) convertView.findViewById(R.id.iv_downloading_icon);
		holder.mTvAction = (TextView) convertView.findViewById(R.id.tv_downloading_action);
		holder.mPBar = (ProgressBar) convertView.findViewById(R.id.pbar_downloading);
		holder.mTvAppName = (TextView) convertView.findViewById(R.id.tv_downloading_name);
		holder.mTvPercent = (TextView) convertView.findViewById(R.id.tv_downloading_degree);
		holder.mTvSpeed = (TextView) convertView.findViewById(R.id.tv_downloading_rate);
		holder.mTvDelete = convertView.findViewById(R.id.tv_downloading_delete);
		holder.mTvInfo = convertView.findViewById(R.id.tv_downloading_detail);
		convertView.setTag(holder);
		return holder;
	}

	private void initDownloadingStatus(ViewHolder holder, IndexGameNew appInfo) {
		holder.mPBar.setVisibility(View.VISIBLE);
		holder.mPBar.setEnabled(true);
		holder.mTvAction.setBackgroundResource(R.drawable.selector_btn_grey);
		holder.mTvAction.setText("暂停");
		updateProgressText(holder.mTvPercent, appInfo.getCompleteSizeStr(), appInfo.getApkFileSizeStr());
		holder.mTvSpeed.setVisibility(View.VISIBLE);
		updateDownloadRate(holder.mTvSpeed, 0);
	}

	private void initFailedStatus(ViewHolder holder, IndexGameNew appInfo) {
		holder.mPBar.setVisibility(View.VISIBLE);
		holder.mPBar.setEnabled(false);
		holder.mTvAction.setBackgroundResource(R.drawable.selector_btn_green);
		holder.mTvAction.setText("重试");
		updateProgressText(holder.mTvPercent, appInfo.getCompleteSizeStr(), appInfo.getApkFileSizeStr());
		holder.mTvSpeed.setVisibility(View.VISIBLE);
		holder.mTvSpeed.setText("下载暂停中");
	}

	private void initPauseStatus(ViewHolder holder, IndexGameNew appInfo) {
		holder.mPBar.setVisibility(View.VISIBLE);
		holder.mPBar.setEnabled(false);
		holder.mTvAction.setBackgroundResource(R.drawable.selector_btn_green);
		holder.mTvAction.setText("继续");
		updateProgressText(holder.mTvPercent, appInfo.getCompleteSizeStr(), appInfo.getApkFileSizeStr());
		holder.mTvSpeed.setVisibility(View.VISIBLE);
		holder.mTvSpeed.setText("下载暂停中");
	}

	private void initDownloadedStatus(ViewHolder holder, IndexGameNew appInfo) {
		holder.mPBar.setVisibility(View.GONE);
		holder.mTvPercent.setText(String.format("版本号：%s | %s", appInfo.versionName, appInfo.getApkFileSizeStr()));
		holder.mTvSpeed.setVisibility(View.GONE);
		AppStatus status = appInfo.appStatus;
		holder.mTvAction.setBackgroundResource(R.drawable.selector_btn_blue);
		if (status == AppStatus.INSTALLABLE || status == AppStatus.UPDATABLE) {
			holder.mTvAction.setText("安装");
		} else {
			holder.mTvAction.setText("打开");
		}
	}

	private void updateProgressText(TextView textView, String completeSizeStr, String apkFileSizeStr) {
		textView.setText(String.format("%s/%s", completeSizeStr, apkFileSizeStr));
	}

	private void updateDownloadRate(TextView textView, long speedBytesPers) {
		textView.setText(String.format("%dKb/s", speedBytesPers >> 10));
	}

	private ViewHolder findVisibleViewHolderByUrl(String url) {
		final ViewHolder holder = mMap_Url_ViewHolder.get(url);
		if (holder == null) {
			return null;
		}
		if (holder.mIvIcon.getTag() == null || !holder.mIvIcon.getTag().equals(url)) {
			return null;
		}
		return holder;
	}

	private void initViewHolderByStatus(String rawUrl, DownloadStatus status) {
		final IndexGameNew appInfo = mDownloadManagerInstance.getAppInfoByUrl(rawUrl);
		if (appInfo == null) return;
		final ViewHolder holder = findVisibleViewHolderByUrl(rawUrl);
		if (holder == null) {
			return;
		}

		if (status == DownloadStatus.DOWNLOADING || status == DownloadStatus.PENDING) {
			initDownloadingStatus(holder, appInfo);
		} else if (status == DownloadStatus.FAILED) {
			initFailedStatus(holder, appInfo);
		} else if (status == DownloadStatus.PAUSED) {
			initPauseStatus(holder, appInfo);
		} else {
			initDownloadedStatus(holder, appInfo);
		}

		int percent = (int) (appInfo.completeSize * 100 / appInfo.apkFileSize);

		holder.mPBar.setProgress(percent);

		final Object tag = holder.mIvIcon.getTag(TAG_URL);
		final String iconUrl = appInfo.img;
		if (tag != null && !tag.equals(iconUrl)) {
			holder.mIvIcon.setImageResource(R.drawable.ic_img_default);
		}

		holder.mIvIcon.setTag(TAG_URL, iconUrl);
		mImageLoader.displayImage(iconUrl, holder.mIvIcon);
		holder.mTvAppName.setText(appInfo.name);
	}

	public void notifyStatusChanged() {
		notifyDataSetUpdated();
	}

	public void updateDownloadingView(String url, int percent, long speedBytesPers) {
		try {
			final IndexGameNew appInfo = mDownloadManagerInstance.getAppInfoByUrl(url);
			if (appInfo == null) {
				return;
			}
			if (percent == 0) {
				return;
			}

			final ViewHolder holder = findVisibleViewHolderByUrl(url);
			if (holder == null) {
				return;
			}

			updateProgressText(holder.mTvPercent, appInfo.getCompleteSizeStr(), appInfo.getApkFileSizeStr());
			updateDownloadRate(holder.mTvSpeed, speedBytesPers);
			holder.mPBar.setProgress(percent);
		} catch (Throwable e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(e);
			}
		}
	}

	static class ViewHolder {
		ImageView mIvIcon;
		ProgressBar mPBar;
		TextView mTvPercent;
		TextView mTvSpeed;
		TextView mTvAppName;
		TextView mTvAction;
		View mTvInfo;
		View mTvDelete;
	}

	static class HeaderViewHolder {
		TextView mTvHeaderText;
	}
}
