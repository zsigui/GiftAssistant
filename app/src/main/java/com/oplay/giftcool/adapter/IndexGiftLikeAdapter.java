package com.oplay.giftcool.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.base.BaseRVAdapter;
import com.oplay.giftcool.adapter.base.BaseRVHolder;
import com.oplay.giftcool.config.util.GameTypeUtil;
import com.oplay.giftcool.model.data.resp.IndexGiftLike;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.ViewUtil;

/**
 * Created by zsigui on 15-12-24.
 */
public class IndexGiftLikeAdapter extends BaseRVAdapter<IndexGiftLike> implements View.OnClickListener{


	private static final int TAG_POSITION = 0x1234FFFF;

	public IndexGiftLikeAdapter(Context context) {
		super(context);
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_index_gift_like, parent, false));
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		ViewHolder itemHolder = (ViewHolder) holder;
		IndexGiftLike o = getItem(position);
		itemHolder.tvName.setText(o.name);
		if (TextUtils.isEmpty(o.giftName)) {
			itemHolder.tvGift.setText("暂无新礼包");
		} else {
			itemHolder.tvGift.setText(o.giftName);
		}
		itemHolder.tvCount.setText(String.format("%d款礼包", o.totalCount));
		ViewUtil.showImage(itemHolder.ivIcon, o.img);
		itemHolder.itemView.setOnClickListener(this);
		itemHolder.itemView.setTag(TAG_POSITION, position);
	}

	@Override
	public void onClick(View v) {
		if (mData == null || v.getTag(TAG_POSITION) == null) {
			return;
		}
		Integer pos = (Integer)v.getTag(TAG_POSITION);
		IntentUtil.jumpGameDetail(mContext, getItem(pos).id, GameTypeUtil.JUMP_STATUS_GIFT);
	}

	static class ViewHolder extends BaseRVHolder{
		TextView tvName;
		TextView tvGift;
		TextView tvCount;
		ImageView ivIcon;

		public ViewHolder(View itemView) {
			super(itemView);
			ivIcon = getViewById(R.id.iv_icon);
			tvName = getViewById(R.id.tv_game_name);
			tvGift = getViewById(R.id.tv_gift);
			tvCount = getViewById(R.id.tv_count);
		}
	}

}
