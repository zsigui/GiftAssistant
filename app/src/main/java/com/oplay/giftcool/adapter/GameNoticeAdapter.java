package com.oplay.giftcool.adapter;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
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
import com.oplay.giftcool.adapter.base.FooterHolder;
import com.oplay.giftcool.config.GameTypeUtil;
import com.oplay.giftcool.config.IndexTypeUtil;
import com.oplay.giftcool.listener.FooterListener;
import com.oplay.giftcool.listener.OnItemClickListener;
import com.oplay.giftcool.model.AppStatus;
import com.oplay.giftcool.model.data.resp.IndexGameNew;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.ViewUtil;

/**
 * Created by zsigui on 15-12-31.
 */
public class GameNoticeAdapter extends BaseRVAdapter_Download implements FooterListener {

	private boolean mHasFooter = false;

	public GameNoticeAdapter(Context context) {
		super(context);
		setListener(new OnItemClickListener<IndexGameNew>() {
			@Override
			public void onItemClick(IndexGameNew item, View view, int position) {
				if (view.getId() == R.id.tv_download && !AppStatus.DISABLE.equals(item.appStatus)) {
					if (mContext != null && mContext instanceof FragmentActivity) {
						item.handleOnClick(((FragmentActivity) mContext).getSupportFragmentManager());
					}
				} else {
					IntentUtil.jumpGameDetail(mContext, item.id, GameTypeUtil.JUMP_STATUS_DETAIL);
				}
			}
		});
	}

	@Override
	protected int getItemFooterCount() {
		return mHasFooter ? 1 : 0;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		if (viewType == IndexTypeUtil.ITEM_FOOTER) {
			return new FooterHolder(LayoutInflater.from(mContext).inflate(R.layout.view_item_footer, parent, false));
		}
		return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_index_game_notice, parent, false));
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		if (getItemViewType(position) == IndexTypeUtil.ITEM_FOOTER) {
			return;
		}
		ViewHolder viewHolder = (ViewHolder) holder;
		final IndexGameNew o = mData.get(position);
		o.initAppInfoStatus(mContext);
		viewHolder.tvName.setText(o.name);
		if (o.totalCount > 0) {
			viewHolder.ivGift.setVisibility(View.VISIBLE);
		} else {
			viewHolder.ivGift.setVisibility(View.GONE);
		}
		int tagId = R.drawable.ic_notice_other;
		viewHolder.tvTag.setVisibility(View.GONE);
		switch (position) {
			case 0:
				tagId = R.drawable.ic_notice_first;
				break;
			case 1:
				tagId = R.drawable.ic_notice_second;
				break;
			case 2:
				tagId = R.drawable.ic_notice_third;
				break;
			default:
				viewHolder.tvTag.setVisibility(View.VISIBLE);
		}
		viewHolder.ivTag.setImageResource(tagId);
		viewHolder.tvTag.setText(String.valueOf(position + 1));
		if (o.playCount < 10000) {
			viewHolder.tvPlay.setText(Html.fromHtml(String.format("<font color='#ffaa17'>%d人</font>在玩",
					o.playCount)));
		} else {
			viewHolder.tvPlay.setText(Html.fromHtml(String.format("<font color='#ffaa17'>%.1f万人</font>在玩",
					(float) o.playCount / 10000)));
		}
		if (o.totalCount > 0) {
			viewHolder.tvGift.setText(Html.fromHtml(
					String.format("<font color='#ffaa17'>%s</font> 等共<font color='#ffaa17'>%d</font>款礼包",
							o.giftName, o.totalCount)));
		} else {
			viewHolder.tvGift.setText("暂时还木有礼包");
		}
		viewHolder.tvSize.setText(o.size);
		ViewUtil.showImage(viewHolder.ivIcon, o.img);
		ViewUtil.initDownloadBtnStatus(viewHolder.btnDownload, o.appStatus);
		viewHolder.itemView.setOnClickListener(this);
		viewHolder.itemView.setTag(IndexTypeUtil.TAG_POSITION, position);
		viewHolder.btnDownload.setTag(IndexTypeUtil.TAG_POSITION, position);
		viewHolder.btnDownload.setTag(IndexTypeUtil.TAG_URL, o.downloadUrl);
		viewHolder.btnDownload.setOnClickListener(this);

		mPackageNameMap.put(o.packageName, o);
		mUrlDownloadBtn.put(o.downloadUrl, viewHolder.btnDownload);
	}

	@Override
	public void showFooter(boolean isShow) {
		mHasFooter = isShow;
		if (mHasFooter) {
			notifyItemInserted(getItemCount() - 1);
		} else {
			notifyItemRemoved(getItemCount());
		}
	}

	class ViewHolder extends BaseRVHolder {
		TextView tvName;
		ImageView ivTag;
		TextView tvTag;
		ImageView ivGift;
		ImageView ivIcon;
		TextView tvPlay;
		TextView tvSize;
		TextView tvGift;
		TextView btnDownload;

		public ViewHolder(View itemView) {
			super(itemView);
			tvName = getViewById(R.id.tv_name);
			ivTag = getViewById(R.id.iv_tag);
			tvTag = getViewById(R.id.tv_tag);
			ivGift = getViewById(R.id.iv_gift);
			ivIcon = getViewById(R.id.iv_icon);
			tvPlay = getViewById(R.id.tv_content);
			tvSize = getViewById(R.id.tv_size);
			tvGift = getViewById(R.id.tv_gift);
			btnDownload = getViewById(R.id.tv_download);
		}
	}
}
