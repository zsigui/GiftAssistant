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
import com.oplay.giftcool.model.data.resp.GameTypeMain;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.ViewUtil;

/**
 * Created by zsigui on 16-1-10.
 */
public class GameTypeMainAdapter extends BaseRVAdapter<GameTypeMain> implements View.OnClickListener{

	private static final int TAG_POSITION = 0XFFFF1234;

	public GameTypeMainAdapter(Context context) {
		super(context);
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new TypeHolder(LayoutInflater.from(mContext).inflate(R.layout.item_grid_game_type_main, parent, false));
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		TypeHolder typeHolder =(TypeHolder) holder;
		GameTypeMain o = getItem(position);
		typeHolder.tvName.setText(o.name);
		ViewUtil.showImage(typeHolder.ivIcon, o.icon);
		typeHolder.itemView.setOnClickListener(this);
		typeHolder.itemView.setTag(TAG_POSITION, position);
	}

	@Override
	public void onClick(View v) {
		if (mData == null || v.getTag(TAG_POSITION) == null) {
			return;
		}
		GameTypeMain o = getItem((Integer)v.getTag(TAG_POSITION));
		IntentUtil.jumpGameTagList(mContext, o.id, o.name);
	}

	static class TypeHolder extends BaseRVHolder {
		TextView tvName;
		ImageView ivIcon;

		public TypeHolder(View itemView) {
			super(itemView);
			tvName = getViewById(R.id.tv_name);
			ivIcon = getViewById(R.id.iv_icon);
		}
	}

}
