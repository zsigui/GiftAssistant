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
import com.oplay.giftcool.config.TypeStatusCode;
import com.oplay.giftcool.model.data.resp.IndexPostNew;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.ToastUtil;
import com.oplay.giftcool.util.ViewUtil;

import java.util.ArrayList;

/**
 * Created by zsigui on 16-4-13.
 */
public class PostOfficialListAdapter extends BaseRVAdapter<IndexPostNew> implements View.OnClickListener {

	// 重复使用的文字类型
	private final String TEXT_STATE_DOING;
	private final String TEXT_STATE_FINISHED;
	private final String TEXT_STATE_WAIT;

	public PostOfficialListAdapter(Context context) {
		this(context, null);

	}

	public PostOfficialListAdapter(Context context, ArrayList<IndexPostNew> data) {
		super(context, data);
		TEXT_STATE_DOING = context.getResources().getString(R.string.st_index_post_text_working);
		TEXT_STATE_FINISHED = context.getResources().getString(R.string.st_index_post_text_finished);
		TEXT_STATE_WAIT = context.getResources().getString(R.string.st_index_post_text_wait);
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new OfficialHolder(LayoutInflater.from(mContext)
				.inflate(R.layout.item_index_post_content_one, parent, false));
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		final IndexPostNew item = getItem(position);
		OfficialHolder officialHolder = (OfficialHolder) holder;
		if (item.showType == 1) {
			ViewUtil.showImage(officialHolder.ivIcon, item.banner);
		} else {
			ViewUtil.showImage(officialHolder.ivIcon, item.img);
		}
		switch (item.state) {
			case TypeStatusCode.POST_FINISHED:
				officialHolder.tvState.setText(TEXT_STATE_FINISHED);
				officialHolder.tvState.setBackgroundResource(R.drawable.ic_post_disabled);
				break;
			case TypeStatusCode.POST_WAIT:
				officialHolder.tvState.setText(TEXT_STATE_WAIT);
				officialHolder.tvState.setBackgroundResource(R.drawable.ic_post_disabled);
				break;
			case TypeStatusCode.POST_BEING:
				officialHolder.tvState.setText(TEXT_STATE_DOING);
				officialHolder.tvState.setBackgroundResource(R.drawable.ic_post_enabled);
				break;
		}
		officialHolder.itemView.setOnClickListener(this);
		officialHolder.itemView.setTag(TAG_POSITION, position);
		officialHolder.tvContent.setText(item.content);
		officialHolder.tvTitle.setText(item.title);
	}

	@Override
	public void updateData(ArrayList<IndexPostNew> data) {
		if (data != null) {
			mData.clear();
			mData.addAll(data);
			notifyDataSetChanged();
		}
	}

	@Override
	public void onClick(View v) {
		if (mContext == null) {
			ToastUtil.showShort("页面失效，请重新打开应用");
			return;
		}
		final IndexPostNew item = getItem((Integer) v.getTag(TAG_POSITION));
		IntentUtil.jumpPostDetail(mContext, item.id);
	}

	private static class OfficialHolder extends BaseRVHolder {

		ImageView ivIcon;
		TextView tvState;
		TextView tvTitle;
		TextView tvContent;

		public OfficialHolder(View itemView) {
			super(itemView);
			ivIcon = getViewById(R.id.iv_icon);
			tvState = getViewById(R.id.tv_post_state);
			tvTitle = getViewById(R.id.tv_title);
			tvContent = getViewById(R.id.tv_content);
		}
	}
}
