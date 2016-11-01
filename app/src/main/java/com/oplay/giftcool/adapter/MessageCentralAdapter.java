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
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.model.data.resp.message.CentralHintMessage;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.ViewUtil;

/**
 * 消息中心适配器
 * <p/>
 * Created by zsigui on 16-4-17.
 */
public class MessageCentralAdapter extends BaseRVAdapter<CentralHintMessage> implements View.OnClickListener {

    public MessageCentralAdapter(Context context) {
        super(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MessageHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.item_list_message_central, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder h, int position) {
        CentralHintMessage item = getItem(position);
        MessageHolder holder = (MessageHolder) h;
        ViewUtil.showImage(holder.ivIcon, item.icon);
        holder.tvTitle.setText(item.title);
        holder.tvContent.setText(item.content);
        holder.itemView.setOnClickListener(this);
        holder.itemView.setTag(TAG_POSITION, position);
        if (item.showCount && item.count > 0) {
            holder.tvCount.setVisibility(View.VISIBLE);
            holder.tvCount.setText(String.valueOf(item.count));
        } else {
            holder.tvCount.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getTag(TAG_POSITION) == null) {
            return;
        }
        int pos = (int) v.getTag(TAG_POSITION);
        CentralHintMessage item = getItem(pos);
        handleItemClick(item, pos);
    }

    private void handleItemClick(CentralHintMessage item, int pos) {
        if (mContext == null) {
            return;
        }
        item.count = 0;
        Global.updateMsgCentralData(mContext, item.code, item.count, item.content);
        notifyItemChanged(pos);
        if (KeyConfig.CODE_MSG_COMMENT.equals(item.code)) {
            IntentUtil.jumpCommentMessage(mContext);
        } else if (KeyConfig.CODE_MSG_ADMIRE.equals(item.code)) {
            IntentUtil.jumpAdmireMessage(mContext);
        } else if (KeyConfig.CODE_MSG_SYSTEM.equals(item.code)) {
            IntentUtil.jumpSystemMessage(mContext);
        } else if (KeyConfig.CODE_MSG_NEW_GIFT_NOTIFY.equals(item.code)) {
            IntentUtil.jumpNewGiftNotify(mContext);
        }
    }

    static class MessageHolder extends BaseRVHolder {

        private ImageView ivIcon;
        private TextView tvTitle;
        private TextView tvContent;
        private TextView tvCount;

        MessageHolder(View itemView) {
            super(itemView);
            ivIcon = getViewById(R.id.iv_icon);
            tvTitle = getViewById(R.id.tv_title);
            tvContent = getViewById(R.id.tv_content);
            tvCount = getViewById(R.id.tv_item_count);
        }
    }
}
