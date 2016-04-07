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
import com.oplay.giftcool.model.data.resp.IndexPostNew;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.ViewUtil;

/**
 * 官方活动列表适配器
 *
 * Created by zsigui on 16-4-7.
 */
public class PostOfficialAdapter extends BaseRVAdapter<IndexPostNew> implements View.OnClickListener {

	private final String TEXT_STATE_DOING;
	private final String TEXT_STATE_FINISHED;

	public PostOfficialAdapter(Context context) {
		super(context);
		TEXT_STATE_DOING = context.getResources().getString(R.string.st_index_post_text_working);
		TEXT_STATE_FINISHED = context.getResources().getString(R.string.st_index_post_text_finished);
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new OfficialHolder(LayoutInflater.from(mContext)
				.inflate(R.layout.item_index_post_offical_content_old, parent, false));
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		IndexPostNew item = getItem(position);
		OfficialHolder officialHolder = (OfficialHolder) holder;
		officialHolder.tvTitle.setText(item.title);
		ViewUtil.showImage(officialHolder.ivBanner, item.img);
		if (item.isNew) {
			officialHolder.tvTitle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_task_new, 0, 0, 0);
		} else {
			officialHolder.tvTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		}
		switch (item.state) {
			case 0:
				officialHolder.tvState.setText(TEXT_STATE_FINISHED);
				officialHolder.tvState.setBackgroundResource(R.drawable.ic_post_disabled);
				break;
			case 1:
				officialHolder.tvState.setText(TEXT_STATE_DOING);
				officialHolder.tvState.setBackgroundResource(R.drawable.ic_post_enabled);
				break;
		}
		officialHolder.itemView.setOnClickListener(this);
		officialHolder.itemView.setTag(TAG_POSITION, position);
		officialHolder.tvContent.setText(item.content);
	}

	@Override
	public void onClick(View v) {
		if (v.getTag(TAG_POSITION) == null) {
			return;
		}
		// 跳转活动详情页面
		final int id = getItem((Integer) v.getTag(TAG_POSITION)).id;
		IntentUtil.jumpPostDetail(mContext, id);
	}

	private static class OfficialHolder extends BaseRVHolder {

		ImageView ivBanner;
		TextView tvState;
		TextView tvTitle;
		TextView tvContent;

		public OfficialHolder(View itemView) {
			super(itemView);
			ivBanner = getViewById(R.id.iv_icon);
			tvState = getViewById(R.id.tv_post_state);
			tvTitle = getViewById(R.id.tv_title);
			tvContent = getViewById(R.id.tv_content);
		}
	}
}
