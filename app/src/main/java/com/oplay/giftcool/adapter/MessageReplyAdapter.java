package com.oplay.giftcool.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.base.BaseRVAdapter;
import com.oplay.giftcool.adapter.base.BaseRVHolder;
import com.oplay.giftcool.model.data.resp.message.ReplyMessage;
import com.oplay.giftcool.util.DateUtil;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.ViewUtil;

import java.util.ArrayList;

/**
 * 收到的赞/回复消息列表适配器
 *
 * Created by zsigui on 16-4-18.
 */
public class MessageReplyAdapter extends BaseRVAdapter<ReplyMessage> implements View.OnClickListener {

	/**
	 * 是否回复消息
	 */
	private boolean mIsComment;
	private SpannableString ss;

	public MessageReplyAdapter(Context context) {
		this(context, null);
	}

	public MessageReplyAdapter(Context context, ArrayList<ReplyMessage> data) {
		super(context, data);
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new MessageHolder(LayoutInflater.from(mContext).inflate(R.layout.item_list_msg_reply, parent, false));
	}

	public void setIsComment(boolean isComment) {
		mIsComment = isComment;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder h, int position) {
		MessageHolder holder = (MessageHolder) h;
		ReplyMessage item = getItem(position);
		holder.tvName.setText(item.name);
		ViewUtil.showImage(holder.ivIcon, item.icon);
		if (TextUtils.isEmpty(item.hintPic)) {
			ViewUtil.showImage(holder.ivHint, item.hintPic);
		} else {
			holder.tvHint.setText(item.hintText);
		}
		holder.tvTime.setText(DateUtil.optDate(item.time));
		if (mIsComment) {
			holder.tvContent.setText(item.content);
		} else {
			if (ss == null) {
				ImageSpan span = new ImageSpan(mContext, R.drawable.ic_msg_admire);
				ss = new SpannableString("zan");
				ss.setSpan(span, 0, ss.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
			}
			holder.tvContent.setText(ss);
		}
		holder.itemView.setTag(TAG_POSITION, position);
		holder.itemView.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v.getTag(TAG_POSITION) == null) {
			return;
		}
		ReplyMessage item = getItem((Integer) v.getTag(TAG_POSITION));
		IntentUtil.jumpReplyDetail(mContext, item.postId, item.replyId);
	}

	@Override
	public void release() {
		super.release();
	}

	/**
	 * 消息Holder
	 */
	private class MessageHolder extends BaseRVHolder {

		private TextView tvName;
		private TextView tvContent;
		private ImageView ivIcon;
		private TextView tvTime;
		private TextView tvHint;
		private ImageView ivHint;

		public MessageHolder(View itemView) {
			super(itemView);
			tvName = getViewById(R.id.tv_name);
			tvContent = getViewById(R.id.tv_content);
			ivIcon = getViewById(R.id.iv_icon);
			tvTime = getViewById(R.id.tv_time);
			tvHint = getViewById(R.id.tv_hint);
			ivHint = getViewById(R.id.iv_hint);
		}
	}
}
