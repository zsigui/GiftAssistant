package com.oplay.giftassistant.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oplay.giftassistant.R;
import com.oplay.giftassistant.config.AppDebugConfig;
import com.oplay.giftassistant.config.Global;
import com.oplay.giftassistant.listener.OnItemClickListener;
import com.oplay.giftassistant.model.DownloadStatus;
import com.oplay.giftassistant.model.data.resp.IndexGameNew;
import com.oplay.giftassistant.util.ViewUtil;

import net.youmi.android.libs.common.debug.Debug_SDK;

import java.util.ArrayList;
import java.util.HashMap;

import cn.bingoogolapple.androidcommon.adapter.BGARecyclerViewAdapter;
import cn.bingoogolapple.androidcommon.adapter.BGAViewHolderHelper;

/**
 * Created by zsigui on 15-12-28.
 */
public class IndexGameNewWithTitleAdapter extends BGARecyclerViewAdapter<IndexGameNew> implements View.OnClickListener{

	private static final int TAG_POSITION = 0xFFF11133;
	private static final int TAG_URL = 0xffff1111;

	private OnItemClickListener<IndexGameNew> mListener;
	private HashMap<String, IndexGameNew> mPackageNameMap;
	private HashMap<String, TextView> mUrlDownloadBtn;

	public IndexGameNewWithTitleAdapter(RecyclerView recyclerView, OnItemClickListener<IndexGameNew> listener) {
		super(recyclerView, R.layout.item_index_game_new);
		mListener = listener;
		mPackageNameMap = new HashMap<>();
		mUrlDownloadBtn = new HashMap<>();
	}


	@Override
	protected void fillData(BGAViewHolderHelper bgaViewHolderHelper, int i, final IndexGameNew o) {
		o.initAppInfoStatus(mContext);
		bgaViewHolderHelper.setText(R.id.tv_name, o.name);
		if (o.playCount < 10000) {
			bgaViewHolderHelper.setText(R.id.tv_play,
					Html.fromHtml(String.format("<font color='#ffaa17'>%d人</font>在玩", o.playCount)));
		} else {
			bgaViewHolderHelper.setText(R.id.tv_play,
					Html.fromHtml(String.format("<font color='#ffaa17'>%.1f万人</font>在玩",
							(float) o.playCount / 10000)));
		}
		if (o.newCount > 0) {
			bgaViewHolderHelper.setVisibility(R.id.iv_gift, View.VISIBLE);
		}else {
			bgaViewHolderHelper.setVisibility(R.id.iv_gift, View.GONE);
		}
		bgaViewHolderHelper.setText(R.id.tv_size, o.size);
		if (o.totalCount > 0) {
			bgaViewHolderHelper.setText(R.id.tv_gift,
					Html.fromHtml(String.format("<font color='#ffaa17'>%s</font> 等共<font color='#ffaa17'>%d</font>款礼包",
							o.giftName, o.totalCount)));
		} else {
			bgaViewHolderHelper.setText(R.id.tv_gift, "暂时还木有礼包");
		}
		// n款礼包
		ImageLoader.getInstance().displayImage(o.img, bgaViewHolderHelper.<ImageView>getView(R.id.iv_icon),
				Global.IMAGE_OPTIONS);
		View convertView = bgaViewHolderHelper.getConvertView();
		convertView.setTag(TAG_POSITION, i);
		convertView.setOnClickListener(this);
		TextView downloadBtn = bgaViewHolderHelper.getView(R.id.tv_download);

		downloadBtn.setOnClickListener(this);
		downloadBtn.setTag(TAG_POSITION, i);
		downloadBtn.setTag(TAG_URL, o.downloadUrl);
		ViewUtil.initDownloadBtnStatus(downloadBtn, o.appStatus);
		mPackageNameMap.put(o.packageName, o);
		mUrlDownloadBtn.put(o.downloadUrl, downloadBtn);
	}

	@Override
	public void onClick(View v) {
		try {
			final Object tag = v.getTag(TAG_POSITION);
			if (tag instanceof Integer) {
				final int position = (Integer) tag;
				if (position < mDatas.size()) {
					final IndexGameNew appInfo = mDatas.get(position);
					if (mListener != null) {
						mListener.onItemClick(appInfo, v, position);
					}
				}
			}
		}catch (Throwable e) {
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


	public void updateData(ArrayList<IndexGameNew> games) {
		this.mDatas = games;
		notifyDataSetChanged();
	}
}
