package com.oplay.giftcool.adapter;

import android.content.Context;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.base.BaseListAdapter;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.download.ApkDownloadManager;
import com.oplay.giftcool.download.listener.OnDownloadStatusChangeListener;
import com.oplay.giftcool.listener.OnItemClickListener;
import com.oplay.giftcool.model.DownloadStatus;
import com.oplay.giftcool.model.data.resp.GameDownloadInfo;
import com.oplay.giftcool.model.data.resp.IndexGameNew;
import com.oplay.giftcool.util.ViewUtil;

import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.util.Util_System_Runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by zsigui on 15-12-28.
 */
public class NestedGameListAdapter extends BaseListAdapter<IndexGameNew> implements View.OnClickListener,
		OnDownloadStatusChangeListener{

	private static final int TAG_POSITION = 0xFFF11133;
	private static final int TAG_URL = 0xffff1111;

	private OnItemClickListener<IndexGameNew> mListener;
	private HashMap<String, IndexGameNew> mPackageNameMap;
	private HashMap<String, TextView> mUrlDownloadBtn;

	public NestedGameListAdapter(Context context, List<IndexGameNew> objects,
	                             OnItemClickListener<IndexGameNew> listener) {
		super(context, objects);
		mListener = listener;
		mPackageNameMap = new HashMap<>();
		mUrlDownloadBtn = new HashMap<>();
		ApkDownloadManager.getInstance(context).addDownloadStatusListener(this);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (getCount() == 0) {
			return null;
		}

		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mLayoutInflater.inflate(R.layout.item_index_game_new, parent, false);
			holder.ivIcon = ViewUtil.getViewById(convertView, R.id.iv_icon);
			holder.tvName = ViewUtil.getViewById(convertView, R.id.tv_name);
			holder.tvContent = ViewUtil.getViewById(convertView, R.id.tv_content);
			holder.tvGift = ViewUtil.getViewById(convertView, R.id.tv_gift);
			holder.ivGift = ViewUtil.getViewById(convertView, R.id.iv_gift_hint);
			holder.tvDownload = ViewUtil.getViewById(convertView, R.id.tv_download);
            convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		IndexGameNew o = getItem(position);
		o.initAppInfoStatus(mContext);
		holder.tvName.setText(o.name);
		if (o.playCount > 10000) {
			holder.tvContent.setText(Html.fromHtml(
					String.format("<font color='#ffaa17'>%.1f万人</font>在玩",
							(float) o.playCount / 10000)));
		} else {
			holder.tvContent.setText(Html.fromHtml(
					String.format("<font color='#ffaa17'>%d人</font>在玩",
							o.playCount)));
		}
		if (o.totalCount > 0) {
			holder.ivGift.setVisibility(View.VISIBLE);
			holder.tvGift.setText(Html.fromHtml(String.format("<font color='#ffaa17'>%s</font> 等共<font color='#ffaa17'>%d</font>款礼包",
							o.giftName, o.totalCount)));
		} else {
			holder.tvGift.setText("暂时还木有礼包");
			holder.ivGift.setVisibility(View.GONE);
		}
		ViewUtil.showImage(holder.ivIcon, o.img);
		convertView.setOnClickListener(this);
		convertView.setTag(TAG_POSITION, position);
		convertView.setOnClickListener(this);
		holder.tvDownload.setOnClickListener(this);
		holder.tvDownload.setTag(TAG_POSITION, position);
		holder.tvDownload.setTag(TAG_URL, o.downloadUrl);
        ViewUtil.initDownloadBtnStatus(holder.tvDownload, o.appStatus);
		mPackageNameMap.put(o.packageName, o);
		mUrlDownloadBtn.put(o.downloadUrl, holder.tvDownload);
		return convertView;
	}

	@Override
	public void onClick(View v) {
		try {
			final Object tag = v.getTag(TAG_POSITION);
			if (tag instanceof Integer) {
				final int position = (Integer) tag;
				if (position < mData.size()) {
					final IndexGameNew appInfo = mData.get(position);
					if (mListener != null) {
						mListener.onItemClick(appInfo, v, position);
					}
				}
			}
		} catch (Throwable e) {
			if (AppDebugConfig.IS_DEBUG) {
				Debug_SDK.e(e);
			}
		}
	}

	public void updateViewByPackageName(String packageName, DownloadStatus status) {
		final IndexGameNew app = mPackageNameMap.get(packageName);
		if (app != null) {
			app.downloadStatus = status;
			app.initAppInfoStatus(mContext);
			notifyDataSetChanged();
		}
	}

	public void updateViewByPackageName(String packageName) {
		final IndexGameNew app = mPackageNameMap.get(packageName);
		if (app != null) {
			app.initAppInfoStatus(mContext);
			notifyDataSetChanged();
		}
	}

	public void setData(ArrayList<IndexGameNew> data) {
		mData = data;
	}

	public void updateData(ArrayList<IndexGameNew> data) {
		if (data == null) {
			return;
		}
		mData = data;
		notifyDataSetChanged();
	}
	@Override
	public void onDownloadStatusChanged(final GameDownloadInfo appInfo) {
		Util_System_Runtime.getInstance().runInUiThread(new Runnable() {
			@Override
			public void run() {
				updateViewByPackageName(appInfo.packageName);
			}
		});
	}

	@Override
	public void release() {
		ApkDownloadManager.getInstance(mContext).removeDownloadStatusListener(this);
		if (mPackageNameMap != null) {
			mPackageNameMap.clear();
			mPackageNameMap = null;
		}
		if (mUrlDownloadBtn != null) {
			mUrlDownloadBtn.clear();
			mUrlDownloadBtn = null;
		}
		mContext = null;
		mData = null;
		mListener = null;
	}


	static class ViewHolder {
		TextView tvName;
		TextView tvGift;
		TextView tvContent;
		TextView tvDownload;
		ImageView ivIcon;
		ImageView ivGift;
	}
}
