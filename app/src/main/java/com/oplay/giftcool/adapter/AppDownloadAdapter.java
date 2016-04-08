package com.oplay.giftcool.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.base.BaseListAdapter;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.util.GameTypeUtil;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.download.ApkDownloadManager;
import com.oplay.giftcool.download.listener.OnDownloadStatusChangeListener;
import com.oplay.giftcool.download.listener.OnProgressUpdateListener;
import com.oplay.giftcool.listener.OnFinishListener;
import com.oplay.giftcool.model.DownloadStatus;
import com.oplay.giftcool.model.data.resp.GameDownloadInfo;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.oplay.giftcool.ui.fragment.dialog.ConfirmDialog;
import com.oplay.giftcool.ui.fragment.setting.DownloadFragment;
import com.oplay.giftcool.ui.widget.StickyListHeadersListViewExpandable;
import com.oplay.giftcool.ui.widget.stickylistheaders.StickyListHeadersAdapter;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.SystemUtil;
import com.oplay.giftcool.util.ThreadUtil;
import com.oplay.giftcool.util.ViewUtil;
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
public class AppDownloadAdapter extends BaseListAdapter<GameDownloadInfo> implements View.OnClickListener,
		StickyListHeadersAdapter, OnDownloadStatusChangeListener, OnProgressUpdateListener, OnFinishListener {

	private BaseFragment mFragment;
	private Context mContext;
	private ApkDownloadManager mDownloadManagerInstance;

	private HashMap<String, ViewHolder> mMap_Url_ViewHolder;

	private StickyListHeadersListViewExpandable mListView;

	protected HashMap<String, GameDownloadInfo> mPackageNameMap;
	private String mStrDownloadingAndPaused;
	private String mStrDownloaded;
	private int mEndIndexOfDownloading;
	private int mEndIndexOfPaused;
	private int mEndIndexOfDownloaded;

	public AppDownloadAdapter(List<GameDownloadInfo> listData, @NonNull BaseFragment fragment) {
		super(fragment.getContext(), listData);
		mContext = fragment.getContext();
		mFragment = fragment;
		mMap_Url_ViewHolder = new HashMap<>();
		mDownloadManagerInstance = ApkDownloadManager.getInstance(fragment.getContext());
		mPackageNameMap = new HashMap<>();
		initIndex();
		ApkDownloadManager.getInstance(mContext).addDownloadStatusListener(this);
		ApkDownloadManager.getInstance(mContext).addProgressUpdateListener(this);
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
		if (endIndexOfDownloading > mData.size()) return;
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

		final GameDownloadInfo appInfo = mData.get(position);
		final String rawUrl = appInfo.downloadUrl;
		mMap_Url_ViewHolder.put(rawUrl, holder);
		holder.mIvIcon.setTag(rawUrl);

		bindImageViewWithUrl(holder.mIvIcon, appInfo.img, R.drawable.ic_img_default);
		if (appInfo.id == Global.GIFTCOOL_GAME_ID) {
			holder.mIvIcon.setImageResource(R.drawable.ic_launcher);
		} else {
			ViewUtil.showImage(holder.mIvIcon, appInfo.img);
		}


		initViewHolderByStatus(holder, appInfo);
		holder.mTvAction.setTag(TAG_POSITION, position);
		holder.mTvAction.setOnClickListener(this);
		holder.mTvInfo.setTag(TAG_POSITION, position);
		holder.mTvInfo.setOnClickListener(this);
		holder.mTvDelete.setTag(TAG_POSITION, position);
		holder.mTvDelete.setOnClickListener(this);
		convertView.setTag(TAG_POSITION, position);
		convertView.setOnClickListener(this);
		ViewUtil.initDownloadBtnStatus(holder.mTvAction, appInfo.appStatus);
		mPackageNameMap.put(appInfo.packageName, appInfo);
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
			if (position >= mData.size()) {
				return;
			}

			final GameDownloadInfo appInfo = mData.get(position);
			switch (v.getId()) {
				// 下载按钮响应
				case R.id.tv_downloading_action:
					if (appInfo != null) {
						appInfo.handleOnClick(mFragment == null ? null : mFragment.getChildFragmentManager());
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

	private void showDelDownloadingConfirmDialog(BaseFragment fragment, final GameDownloadInfo appInfo) {
		if (appInfo == null) {
			return;
		}
		if (fragment == null) {
			// 此时直接删除
			mDownloadManagerInstance.removeDownloadTask(appInfo.downloadUrl, true);
			return;
		}
		final ConfirmDialog confirmDialog = ConfirmDialog.newInstance();
		confirmDialog.setTitle("提示");
		confirmDialog.setContent("游戏还没下载完，确定删除吗？");
		confirmDialog.setListener(new ConfirmDialog.OnDialogClickListener() {
			@Override
			public void onCancel() {
				confirmDialog.dismissAllowingStateLoss();
			}

			@Override
			public void onConfirm() {
				mDownloadManagerInstance.removeDownloadTask(appInfo.downloadUrl, true);
				confirmDialog.dismissAllowingStateLoss();
			}
		});
		confirmDialog.show(fragment.getChildFragmentManager(), ConfirmDialog.class.getSimpleName());
	}

	private void showDelDownloadedConfirmDialog(BaseFragment activity, final GameDownloadInfo appInfo) {
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
				mDownloadManagerInstance.removeDownloadTask(appInfo.downloadUrl, true);
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

	private void initViewHolderByStatus(ViewHolder holder, GameDownloadInfo appInfo) {
		if (appInfo == null || holder == null) {
			return;
		}

		appInfo.initAppInfoStatus(mContext);
		if (appInfo.apkFileSize <= 0) {
			holder.mPBar.setProgress(0);
		} else {
			holder.mPBar.setProgress((int) (appInfo.completeSize * 100 / appInfo.apkFileSize));
		}
		holder.mTvAppName.setText(appInfo.name);
		switch (appInfo.appStatus) {
			case PAUSABLE:
				holder.mPBar.setVisibility(View.VISIBLE);
				holder.mPBar.setEnabled(true);
				updateProgressText(holder.mTvPercent, appInfo.getCompleteSizeStr(), appInfo.getApkFileSizeStr());
				holder.mTvSpeed.setVisibility(View.VISIBLE);
				updateDownloadRate(holder.mTvSpeed, 0);
				break;
			case RETRYABLE:
			case RESUMABLE:
				holder.mPBar.setVisibility(View.VISIBLE);
				holder.mPBar.setEnabled(false);
				updateProgressText(holder.mTvPercent, appInfo.getCompleteSizeStr(), appInfo.getApkFileSizeStr());
				holder.mTvSpeed.setVisibility(View.VISIBLE);
				holder.mTvSpeed.setText("下载暂停中");
				break;
			case DOWNLOADABLE:
			case OPENABLE:
			case INSTALLABLE:
			case UPDATABLE:
			default:
				holder.mPBar.setVisibility(View.GONE);
				holder.mTvPercent.setText(String.format("版本号：%s | %s", appInfo.versionName, appInfo
						.getApkFileSizeStr()));
				holder.mTvSpeed.setVisibility(View.GONE);
		}
	}

	public void notifyStatusChanged() {
		notifyDataSetUpdated();
	}

	public void updateDownloadingView(String url, int percent, long speedBytesPers) {
		try {
			final GameDownloadInfo appInfo = mDownloadManagerInstance.getAppInfoByUrl(url);
			if (appInfo == null) {
				return;
			}

			final ViewHolder holder = findVisibleViewHolderByUrl(url);
			if (holder == null) {
				return;
			}

			updateProgressText(holder.mTvPercent, appInfo.getCompleteSizeStr(), appInfo.getApkFileSizeStr());
			updateDownloadRate(holder.mTvSpeed, speedBytesPers);
			if (appInfo.apkFileSize <= 0) {
				holder.mPBar.setProgress(percent);
			} else {
				holder.mPBar.setProgress((int) (appInfo.completeSize * 100 / appInfo.apkFileSize));
			}
		} catch (Throwable e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(e);
			}
		}
	}

	public void updateViewByPackageName(String packageName, DownloadStatus status) {
		final GameDownloadInfo app = mPackageNameMap.get(packageName);
		if (app != null) {
			app.downloadStatus = status;
			app.initAppInfoStatus(mContext);
			notifyDataSetChanged();
		}
	}

	@Override
	public void onDownloadStatusChanged(final GameDownloadInfo appInfo) {
		ThreadUtil.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (mFragment != null && mFragment instanceof DownloadFragment) {
					if (getCount() > 0) {
						((DownloadFragment) mFragment).showContent();
					} else {
						((DownloadFragment) mFragment).showEmpty();
					}
				}
				if (appInfo != null) {
					updateViewByPackageName(appInfo.packageName, appInfo.downloadStatus);
					notifyStatusChanged();
				}
			}
		});
	}

	@Override
	public void onProgressUpdate(final String url, final int percent, final long speedBytesPers) {
		ThreadUtil.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				updateDownloadingView(url, percent, speedBytesPers);
			}
		});
	}

	@Override
	public void release() {
		super.release();
		if (mMap_Url_ViewHolder != null) {
			mMap_Url_ViewHolder.clear();
		}
		ApkDownloadManager.getInstance(mFragment.getContext()).removeDownloadStatusListener(this);
		ApkDownloadManager.getInstance(mFragment.getContext()).removeProgressUpdateListener(this);
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
