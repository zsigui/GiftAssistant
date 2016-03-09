package com.oplay.giftcool.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.base.BaseRVAdapter;
import com.oplay.giftcool.adapter.base.BaseRVHolder;
import com.oplay.giftcool.config.TypeStatusCode;
import com.oplay.giftcool.model.data.resp.message.PushMessage;
import com.oplay.giftcool.util.DateUtil;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.ViewUtil;

/**
 * 推送消息的适配器
 * <p/>
 * Created by zsigui on 16-3-7.
 */
public class PushMessageAdapter extends BaseRVAdapter<PushMessage> implements View.OnClickListener {

	private final String TITLE_MODULE;
	private final String CONTENT_MODULE;

	public PushMessageAdapter(Context context) {
		super(context);
		TITLE_MODULE = context.getResources().getString(R.string.st_msg_push_list_title);
		CONTENT_MODULE = context.getResources().getString(R.string.st_msg_push_list_content);
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ItemHolder(LayoutInflater.from(mContext).inflate(R.layout.item_list_push_message, parent, false));
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		PushMessage data = getItem(position);
		ItemHolder itemHolder = (ItemHolder) holder;
		itemHolder.tvTime.setText(DateUtil.optDate(data.time));
		itemHolder.ivHint.setVisibility(data.readState == TypeStatusCode.PUSH_UNREAD ? View.VISIBLE : View.GONE);
		itemHolder.tvName.setText(String.format(TITLE_MODULE, data.gameName));
		itemHolder.tvContent.setText(String.format(CONTENT_MODULE, data.giftContent));
		ViewUtil.showImage(itemHolder.ivIcon, data.img);
		itemHolder.rlMsg.setOnClickListener(this);
		itemHolder.rlMsg.setTag(TAG_POSITION, position);
		itemHolder.llToGet.setOnClickListener(this);
		itemHolder.llToGet.setTag(TAG_POSITION, position);
	}

	@Override
	public void onClick(View v) {
		if (v.getTag(TAG_POSITION) == null) {
			return;
		}
		PushMessage data = getItem((Integer) v.getTag(TAG_POSITION));
		switch (v.getId()) {
			case R.id.ll_to_get:
				IntentUtil.jumpGiftDetail(mContext, data.giftId);
				break;
		}
	}

	@Override
	public void release() {
		super.release();

	}

	static class ItemHolder extends BaseRVHolder {

		RelativeLayout rlMsg;
		ImageView ivIcon;
		TextView tvName;
		ImageView ivHint;
		TextView tvContent;
		LinearLayout llToGet;
		TextView tvTime;

		public ItemHolder(View itemView) {
			super(itemView);
			rlMsg = getViewById(R.id.rl_msg);
			ivIcon = getViewById(R.id.iv_icon);
			tvName = getViewById(R.id.tv_name);
			ivHint = getViewById(R.id.iv_hint);
			tvContent = getViewById(R.id.tv_content);
			llToGet = getViewById(R.id.ll_to_get);
			tvTime = getViewById(R.id.tv_time);
		}
	}
}
