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
import com.oplay.giftcool.listener.OnItemClickListener;
import com.oplay.giftcool.sharesdk.base.IShare;

import java.util.ArrayList;

/**
 * Created by zsigui on 16-1-22.
 */
public class ShareAdapter extends BaseRVAdapter<IShare> implements View.OnClickListener {

	private static final int TAG_POSITION = 0xFF100087;

	public ShareAdapter(Context context) {
		super(context);
	}

	public ShareAdapter(Context context, ArrayList<IShare> data) {
		super(context, data);
	}

	public ShareAdapter(Context context, ArrayList<IShare> data, OnItemClickListener<IShare> listener) {
		super(context, data, listener);
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grid_share, parent, false);
		return new ViewHolder(itemView);
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		if (holder instanceof ViewHolder) {
			ViewHolder myHolder = (ViewHolder) holder;
			IShare item = getItem(position);
			myHolder.description.setText(item.getDescription());
			myHolder.icon.setImageResource(item.getIconId());
			myHolder.itemView.setTag(TAG_POSITION, position);
			myHolder.itemView.setOnClickListener(this);
		}
	}

	@Override
	public void onClick(View v) {
		final Object obj = v.getTag(TAG_POSITION);
		if (obj instanceof Integer) {
			final int position = (Integer) obj;
			IShare item = getItem(position);
			if (mListener != null) {
				mListener.onItemClick(item, v, position);
			}
		}
	}

	protected static class ViewHolder extends RecyclerView.ViewHolder {
		ImageView icon;
		TextView description;

		public ViewHolder(View itemView) {
			super(itemView);
			icon = (ImageView) itemView.findViewById(R.id.iv_icon);
			description = (TextView) itemView.findViewById(R.id.tv_name);
		}
	}
}
