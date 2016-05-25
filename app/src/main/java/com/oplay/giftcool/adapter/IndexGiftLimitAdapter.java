package com.oplay.giftcool.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.base.BaseRVAdapter;
import com.oplay.giftcool.adapter.base.BaseRVHolder;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.manager.StatisticsManager;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;
import com.oplay.giftcool.ui.widget.DeletedTextView;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.ViewUtil;
import com.socks.library.KLog;

import java.util.ArrayList;

/**
 * Created by zsigui on 15-12-24.
 */
public class IndexGiftLimitAdapter extends BaseRVAdapter<IndexGiftNew> implements View.OnClickListener {

	private static final int TAG_POS = 0x2234fff1;

	public IndexGiftLimitAdapter(Context context) {
		super(context);
	}

	public void updateData(ArrayList<IndexGiftNew> data) {
		mData = data;
		notifyDataSetChanged();
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ItemHolder(LayoutInflater.from(mContext).inflate(R.layout.item_index_gift_limit, parent, false));
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		IndexGiftNew item = getItem(position);
		ItemHolder itemHolder = (ItemHolder) holder;
		ViewUtil.showImage(itemHolder.ivIcon, item.img);
		itemHolder.tvGameName.setText(item.gameName);
		itemHolder.tvName.setText(item.name);
		itemHolder.itemView.setTag(TAG_POS, item.id);
		itemHolder.itemView.setOnClickListener(this);
		ViewUtil.siteValueUI(itemHolder.tvPrice, item.originPrice, false);
	}

	@Override
	public void onClick(View v) {
		try {
			Integer i = v.getTag(TAG_POS) == null ? 0 : (Integer) v.getTag(TAG_POS);
			IntentUtil.jumpGiftDetail(mContext, i);
			if (AppDebugConfig.IS_STATISTICS_SHOW) {
				IndexGiftNew data = mData.get(i);
				if (data != null) {
					StatisticsManager.getInstance().trace(mContext,
							StatisticsManager.ID.GIFT_LIMIT_ITEM,
							StatisticsManager.ID.STR_GIFT_LIMIT_ITEM,
							String.format("第%s项点击:[%s]%s", i, data.gameName, data.name));
				}
			}
		} catch (Throwable t) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.d(AppDebugConfig.TAG_ADAPTER, t);
			}
		}
	}

	static class ItemHolder extends BaseRVHolder {

		ImageView ivIcon;
		TextView tvGameName;
		TextView tvName;
		DeletedTextView tvPrice;

		public ItemHolder(View itemView) {
			super(itemView);
			tvName = getViewById(R.id.tv_name);
			tvGameName = getViewById(R.id.tv_game_name);
			tvPrice = getViewById(R.id.tv_price);
			ivIcon = getViewById(R.id.iv_icon);
		}
	}
}
