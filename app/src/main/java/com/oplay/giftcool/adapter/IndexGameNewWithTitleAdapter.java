package com.oplay.giftcool.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.base.BaseRVAdapter_Download;
import com.oplay.giftcool.adapter.base.BaseRVHolder;
import com.oplay.giftcool.config.IndexTypeUtil;
import com.oplay.giftcool.model.data.resp.IndexGameNew;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.ViewUtil;

/**
 * Created by zsigui on 15-12-28.
 */
public class IndexGameNewWithTitleAdapter extends BaseRVAdapter_Download {

	public IndexGameNewWithTitleAdapter(Context context) {
		super(context);
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		switch (viewType) {
			case IndexTypeUtil.ITEM_HEADER:
				return new HeaderVH(LayoutInflater.from(mContext).inflate(R.layout.item_header_index, parent, false));
			case IndexTypeUtil.ITEM_NORMAL:
				return new NormalVH(LayoutInflater.from(mContext).inflate(R.layout.item_index_game_new, parent,
						false));
		}
		return null;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		if (getItemCount() == 0) {
			return;
		}
		switch (getItemViewType(position)) {
			case IndexTypeUtil.ITEM_HEADER:
				HeaderVH headerVH = (HeaderVH) holder;
				headerVH.tvTitle.setText("新游推荐");
				headerVH.setIsRecyclable(false);
				headerVH.itemView.setTag(IndexTypeUtil.TAG_POSITION, position);
				headerVH.itemView.setOnClickListener(this);
				break;
			case IndexTypeUtil.ITEM_NORMAL:
				NormalVH normalVH = (NormalVH) holder;
				normalVH.setIsRecyclable(false);
				final IndexGameNew o = mData.get(position - 1);
				o.initAppInfoStatus(mContext);
				normalVH.tvName.setText(o.name);
				if (o.playCount < 10000) {
					normalVH.ivGift.setVisibility(View.VISIBLE);
					normalVH.tvPlay.setText(Html.fromHtml(String.format("<font color='#ffaa17'>%d人</font>在玩",
							o.playCount)));
				} else {
					normalVH.ivGift.setVisibility(View.GONE);
					normalVH.tvPlay.setText(Html.fromHtml(String.format("<font color='#ffaa17'>%.1f万人</font>在玩",
							(float) o.playCount / 10000)));
				}
				if (o.totalCount > 0) {
					normalVH.tvGift.setText(Html.fromHtml(
							String.format("<font color='#ffaa17'>%s</font> 等共<font color='#ffaa17'>%d</font>款礼包",
									o.giftName, o.totalCount)));
				} else {
					normalVH.tvGift.setText("暂时还木有礼包");
				}
				normalVH.tvSize.setText(o.size);
				ViewUtil.showImage(normalVH.ivIcon, o.img);
				ViewUtil.initDownloadBtnStatus(normalVH.btnDownload, o.appStatus);
				normalVH.itemView.setOnClickListener(this);
				normalVH.itemView.setTag(IndexTypeUtil.TAG_POSITION, position);
				normalVH.btnDownload.setTag(IndexTypeUtil.TAG_POSITION, position);
				normalVH.btnDownload.setTag(IndexTypeUtil.TAG_URL, o.downloadUrl);
				normalVH.btnDownload.setOnClickListener(this);

				mPackageNameMap.put(o.packageName, o);
				mUrlDownloadBtn.put(o.downloadUrl, normalVH.btnDownload);
				break;
		}
	}

	@Override
	protected boolean handleOnClick(View v, int position) {
		if (position == 0) {
			// 头部被点击,跳转推荐新游游戏界面
			IntentUtil.jumpGameNewList(mContext);
			return true;
		}
		return false;
	}

	@Override
	protected int getItemHeaderCount() {
		return 1;
	}

	class HeaderVH extends BaseRVHolder {

		TextView tvTitle;

		public HeaderVH(View itemView) {
			super(itemView);
			tvTitle = getViewById(R.id.tv_title);
		}
	}

	class NormalVH extends BaseRVHolder {

		TextView tvName;
		ImageView ivGift;
		ImageView ivIcon;
		TextView tvPlay;
		TextView tvSize;
		TextView tvGift;
		TextView btnDownload;

		public NormalVH(View itemView) {
			super(itemView);
			tvName = getViewById(R.id.tv_name);
			ivGift = getViewById(R.id.iv_gift);
			ivIcon = getViewById(R.id.iv_icon);
			tvPlay = getViewById(R.id.tv_play);
			tvSize = getViewById(R.id.tv_size);
			tvGift = getViewById(R.id.tv_gift);
			btnDownload = getViewById(R.id.tv_download);
		}
	}

}
