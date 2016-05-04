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
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.model.DrawerModel;

/**
 * Created by zsigui on 16-1-21.
 */
public class DrawerAdapter extends BaseRVAdapter<DrawerModel> {

	final String TEXT_ATTEND;
	final String TEXT_HAS_ATTENDED;
	final int COLOR_ATTEND;
	final int COLOR_HAS_ATTENDED;

	public DrawerAdapter(Context context) {
		super(context);
		TEXT_ATTEND = context.getResources().getString(R.string.st_drawer_tag_sign_in);
		TEXT_HAS_ATTENDED = context.getResources().getString(R.string.st_drawer_tag_has_signed);
		COLOR_ATTEND = context.getResources().getColor(R.color.co_btn_red);
		COLOR_HAS_ATTENDED = context.getResources().getColor(R.color.co_btn_grey);
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
		switch (item.count) {
			case 0:
				drawerItemHolder.tvCount.setVisibility(View.GONE);
				if(item.id == KeyConfig.TYPE_SIGN_IN_EVERY_DAY) {
					drawerItemHolder.tvAttend.setText(TEXT_HAS_ATTENDED);
					drawerItemHolder.tvAttend.setBackgroundResource(R.drawable.shape_rect_btn_grey_border_small);
					drawerItemHolder.tvAttend.setTextColor(COLOR_HAS_ATTENDED);
					drawerItemHolder.tvAttend.setVisibility(View.VISIBLE);
				} else {
					drawerItemHolder.tvAttend.setVisibility(View.GONE);
				}
				break;
			default:
				if (item.id == KeyConfig.TYPE_SIGN_IN_EVERY_DAY) {
					drawerItemHolder.tvAttend.setText(TEXT_ATTEND);
					drawerItemHolder.tvAttend.setBackgroundResource(R.drawable.shape_rect_btn_red_border);
					drawerItemHolder.tvAttend.setTextColor(COLOR_ATTEND);
					drawerItemHolder.tvAttend.setVisibility(View.VISIBLE);
					drawerItemHolder.tvCount.setVisibility(View.GONE);
				} else {
					drawerItemHolder.tvAttend.setVisibility(View.GONE);
					drawerItemHolder.tvCount.setVisibility(View.VISIBLE);
					drawerItemHolder.tvCount.setText(String.valueOf(item.count));
				}
				break;
		}
		drawerItemHolder.itemView.setId(item.id);
		drawerItemHolder.itemView.setOnClickListener(item.listener);
	}

	public class DrawerItemHolder extends BaseRVHolder {

		private TextView tvName;
		private ImageView ivIcon;
		private TextView tvCount;
		private TextView tvAttend;

		public DrawerItemHolder(View itemView) {
			super(itemView);
			tvName = getViewById(R.id.nav_item_text);
			ivIcon = getViewById(R.id.nav_item_icon);
			tvCount = getViewById(R.id.nav_item_count);
			tvAttend = getViewById(R.id.nav_item_attend);
		}
	}
}
