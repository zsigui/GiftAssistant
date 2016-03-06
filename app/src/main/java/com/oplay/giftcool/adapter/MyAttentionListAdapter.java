package com.oplay.giftcool.adapter;

import android.content.Context;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.base.BaseListAdapter;
import com.oplay.giftcool.listener.OnItemClickListener;
import com.oplay.giftcool.model.data.resp.IndexGiftLike;
import com.oplay.giftcool.model.data.resp.MyAttention;
import com.oplay.giftcool.util.ViewUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zsigui on 16-3-8.
 */
public class MyAttentionListAdapter extends BaseListAdapter<MyAttention> implements View.OnClickListener {

	private OnItemClickListener<MyAttention> mItemClickListener;

	public MyAttentionListAdapter(Context context, List<MyAttention> objects) {
		super(context, objects);
	}

	public void updateData(ArrayList<MyAttention> data) {
		if (data == null) {
			return;
		}
		mData = data;
		notifyDataSetChanged();
	}

	public void setItemClickListener(OnItemClickListener<MyAttention> itemClickListener) {
		mItemClickListener = itemClickListener;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (getCount() == 0) {
			return null;
		}

		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = createView(parent, holder);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		bindData(position, convertView, holder);
		return convertView;
	}

	private View createView(ViewGroup parent, ViewHolder holder) {
		View convertView = mLayoutInflater.inflate(R.layout.item_list_attention, parent, false);
		holder.ivIcon = ViewUtil.getViewById(convertView, R.id.iv_icon);
		holder.tvName = ViewUtil.getViewById(convertView, R.id.tv_name);
		holder.tvContent = ViewUtil.getViewById(convertView, R.id.tv_content);
		holder.tvSize = ViewUtil.getViewById(convertView, R.id.tv_size);
		holder.tvCount = ViewUtil.getViewById(convertView, R.id.tv_count);
		holder.tvRemain = ViewUtil.getViewById(convertView, R.id.tv_remain);
		holder.btnQuickFocus = ViewUtil.getViewById(convertView, R.id.btn_quick_focus);
		return convertView;
	}

	/**
	 * 进行数据绑定
	 */
	private void bindData(int position, View convertView, ViewHolder holder) {
		IndexGiftLike o = getItem(position);
		holder.tvName.setText(o.name);
		if (o.playCount > 10000) {
			holder.tvContent.setText(Html.fromHtml(
					String.format("<font color='#ffaa17'>%.1f万人</font>在玩",
							(float) o.playCount / 10000)));
		} else {
			holder.tvContent.setText(Html.fromHtml(
					String.format("<font color='#ffaa17'>%d人</font>在玩",
							o.playCount)));
		}
		holder.tvSize.setText(o.size);
		holder.tvCount.setText(Html.fromHtml(String.format("共<font color='#ffaa17'>%d</font>款礼包", o.totalCount)));
		holder.tvRemain.setText(Html.fromHtml(String.format("今日新增<font color='#ffaa17'>%d</font>款", o.newCount)));
		ViewUtil.showImage(holder.ivIcon, o.img);
		convertView.setOnClickListener(this);
		convertView.setTag(TAG_POSITION, position);
		holder.btnQuickFocus.setOnClickListener(this);
		holder.btnQuickFocus.setTag(TAG_POSITION, position);
	}

	@Override
	public void onClick(View v) {
		if (mData == null || v.getTag(TAG_POSITION) == null) {
			return;
		}
		Integer pos = (Integer) v.getTag(TAG_POSITION);
		if (mItemClickListener != null) {
			mItemClickListener.onItemClick(getItem(pos), v, pos);
		}
	}

	static class ViewHolder {
		TextView tvName;
		TextView tvContent;
		TextView tvSize;
		TextView tvCount;
		TextView tvRemain;
		ImageView ivIcon;
		TextView btnQuickFocus;
	}
}