package com.oplay.giftcool.adapter;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
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
 * Created by zsigui on 15-12-31.
 */
public class IndexGameHotWithTitleAdapter extends BaseRVAdapter_Download implements View.OnClickListener {

	public IndexGameHotWithTitleAdapter(Context context) {
		super(context);
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		switch (viewType) {
			case IndexTypeUtil.ITEM_HEADER:
				return new HeaderVH(LayoutInflater.from(mContext).inflate(R.layout.item_header_index, parent, false));
			case IndexTypeUtil.ITEM_NORMAL:
				return new NormalVH(LayoutInflater.from(mContext).inflate(R.layout.item_grid_game_super, parent, false));
		}
		return null;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		switch (getItemViewType(position)) {
			case IndexTypeUtil.ITEM_HEADER:
				HeaderVH headerVH = (HeaderVH) holder;
				headerVH.tvTitle.setText("热门游戏");
				headerVH.setIsRecyclable(false);
				headerVH.itemView.setTag(IndexTypeUtil.TAG_POSITION, position);
				headerVH.itemView.setOnClickListener(this);
				break;
			case IndexTypeUtil.ITEM_NORMAL:
				NormalVH normalVH = (NormalVH) holder;
				final IndexGameNew o = mData.get(position - 1);
				o.initAppInfoStatus(mContext);
				normalVH.tvName.setText(o.name);
				if (o.totalCount > 0) {
					normalVH.ivGift.setVisibility(View.VISIBLE);
				} else {
					normalVH.ivGift.setVisibility(View.GONE);
				}
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
			// 头部被点击,跳转热门游戏界面
			IntentUtil.jumpGameHotList(mContext);
			return true;
		}
		return false;
	}
	@Override
	protected int getItemHeaderCount() {
		return 1;
	}

	@Override
	public void onAttachedToRecyclerView(RecyclerView recyclerView) {
		super.onAttachedToRecyclerView(recyclerView);
		RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();
		if (lm instanceof GridLayoutManager) {
			final GridLayoutManager glm = (GridLayoutManager) lm;
			glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
				@Override
				public int getSpanSize(int position) {
					return getItemViewType(position) == IndexTypeUtil.ITEM_HEADER ? glm.getSpanCount(): 1;
				}
			});
		}
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
		TextView btnDownload;

		public NormalVH(View itemView) {
			super(itemView);
			tvName = getViewById(R.id.tv_name);
			ivGift = getViewById(R.id.iv_gift);
			ivIcon = getViewById(R.id.iv_icon);
			btnDownload = getViewById(R.id.tv_download);
		}
	}
}
