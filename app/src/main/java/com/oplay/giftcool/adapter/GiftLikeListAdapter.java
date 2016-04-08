package com.oplay.giftcool.adapter;

import android.content.Context;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.base.BaseListAdapter;
import com.oplay.giftcool.config.util.GameTypeUtil;
import com.oplay.giftcool.model.data.resp.IndexGiftLike;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.ViewUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 猜你喜欢礼包列表适配器
 *
 * Created by zsigui on 15-12-30.
 */
public class GiftLikeListAdapter extends BaseListAdapter<IndexGiftLike> implements View.OnClickListener {


	public GiftLikeListAdapter(Context context, List<IndexGiftLike> objects) {
		super(context, objects);
	}

	public void updateData(ArrayList<IndexGiftLike> data) {
		if (data == null) {
			return;
		}
		mData = data;
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (getCount() == 0) {
			return null;
		}

		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mLayoutInflater.inflate(R.layout.item_list_gift_like, parent, false);
			holder.ivIcon = ViewUtil.getViewById(convertView, R.id.iv_icon);
			holder.tvName = ViewUtil.getViewById(convertView, R.id.tv_name);
			holder.tvContent = ViewUtil.getViewById(convertView, R.id.tv_content);
			holder.tvSize = ViewUtil.getViewById(convertView, R.id.tv_size);
			holder.tvCount = ViewUtil.getViewById(convertView, R.id.tv_count);
			holder.tvRemain = ViewUtil.getViewById(convertView, R.id.tv_remain);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		IndexGiftLike o = getItem(position);
		holder.tvName.setText(o.name);
		if (o.playCount > 10000) {
			holder.tvContent.setText(Html.fromHtml(
					String.format("<font color='#ffaa17'>%.1f万人</font>在玩", (float) o.playCount / 10000)));
		} else {
			holder.tvContent.setText(Html.fromHtml(
					String.format("<font color='#ffaa17'>%d人</font>在玩", o.playCount)));
		}
		holder.tvSize.setText(o.size);
		holder.tvCount.setText(Html.fromHtml(String.format("共<font color='#ffaa17'>%d</font>款礼包", o.totalCount)));
		holder.tvRemain.setText(Html.fromHtml(String.format("<font color='#ffaa17'>%s</font>", o.giftName)));
		ViewUtil.showImage(holder.ivIcon, o.img);
		convertView.setOnClickListener(this);
		convertView.setTag(TAG_POSITION, position);
		return convertView;
	}

	@Override
	public void onClick(View v) {
		if (mData == null || v.getTag(TAG_POSITION) == null) {
			return;
		}
		Integer pos = (Integer) v.getTag(TAG_POSITION);
		IntentUtil.jumpGameDetail(mContext, getItem(pos).id, GameTypeUtil.JUMP_STATUS_GIFT);
	}

	static class ViewHolder {
		TextView tvName;
		TextView tvContent;
		TextView tvSize;
		TextView tvCount;
		TextView tvRemain;
		ImageView ivIcon;
	}
}
