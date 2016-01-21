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
import com.oplay.giftcool.model.DrawerModel;

/**
 * Created by zsigui on 16-1-21.
 */
public class DrawerAdapter extends BaseRVAdapter<DrawerModel>{

	public DrawerAdapter(Context context) {
		super(context);
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new DrawerItemHolder(LayoutInflater.from(mContext).inflate(R.layout.view_drawer_item, parent, false));
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		DrawerModel item = mData.get(position);
		DrawerItemHolder drawerItemHolder = (DrawerItemHolder) holder;
		drawerItemHolder.tvName.setText(item.name);
		drawerItemHolder.ivIcon.setImageResource(item.icon);
		drawerItemHolder.itemView.setOnClickListener(item.listener);
	}


	public class DrawerItemHolder extends BaseRVHolder {

		private TextView tvName;
		private ImageView ivIcon;

		public DrawerItemHolder(View itemView) {
			super(itemView);
			tvName = getViewById(R.id.nav_item_text);
			ivIcon = getViewById(R.id.nav_item_icon);
		}
	}
}
