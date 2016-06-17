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
import com.oplay.giftcool.model.data.resp.message.SystemMessage;
import com.oplay.giftcool.util.DateUtil;
import com.oplay.giftcool.util.IntentUtil;

import java.util.ArrayList;

/**
 * 系统消息列表适配器
 * <p/>
 * Created by zsigui on 16-4-18.
 */
public class MessageSystemAdapter extends BaseRVAdapter<SystemMessage> implements View.OnClickListener {


    public MessageSystemAdapter(Context context) {
        this(context, null);
    }

    public MessageSystemAdapter(Context context, ArrayList<SystemMessage> data) {
        super(context, data);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MessageHolder(LayoutInflater.from(mContext).inflate(R.layout.item_list_msg_system, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder h, int position) {
        MessageHolder holder = (MessageHolder) h;
        SystemMessage item = getItem(position);
        holder.tvTitle.setText(item.title);
        holder.tvContent.setText(item.content);
        holder.tvTime.setText(DateUtil.optDateLong(item.time, item.timestamp));
        if (item.isRead == 1) {
            holder.ivNew.setVisibility(View.GONE);
        } else {
            holder.ivNew.setVisibility(View.VISIBLE);
        }
        holder.itemView.setOnClickListener(this);
        holder.itemView.setTag(TAG_POSITION, position);
    }

    @Override
    public void onClick(View v) {
        if (v.getTag(TAG_POSITION) == null) {
            return;
        }
        final Integer pos = (Integer) v.getTag(TAG_POSITION);
        final SystemMessage item = getItem(pos);
        item.isRead = 1;
        notifyItemChanged(pos);
        if (item.giftId != 0) {
            IntentUtil.jumpGiftDetail(mContext, item.giftId);
        }
    }

    private class MessageHolder extends BaseRVHolder {

        private TextView tvTitle;
        private ImageView ivNew;
        private TextView tvContent;
        private TextView tvTime;

        public MessageHolder(View itemView) {
            super(itemView);
            tvTitle = getViewById(R.id.tv_title);
            ivNew = getViewById(R.id.iv_new_notify);
            tvContent = getViewById(R.id.tv_content);
            tvTime = getViewById(R.id.tv_time);
        }
    }
}
