package com.oplay.giftcool.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.base.BaseRVAdapter;
import com.oplay.giftcool.adapter.base.BaseRVHolder;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.util.GameTypeUtil;
import com.oplay.giftcool.model.data.resp.IndexGiftLike;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.ViewUtil;

import java.util.Locale;

/**
 * Created by zsigui on 15-12-24.
 */
public class IndexGiftLikeAdapter extends BaseRVAdapter<IndexGiftLike> implements View.OnClickListener {


    private static final int TAG_POSITION = 0x1234FFFF;

    public IndexGiftLikeAdapter(Context context) {
        super(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_index_gift_like, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewHolder itemHolder = (ViewHolder) holder;
        IndexGiftLike o = getItem(position);
        itemHolder.tvName.setText(o.name);
        if (o.newestCreateTime > Global.getLikeNewTimeArray().get(o.id) &&
                o.newestCreateTime * 1000 > AssistantApp.getInstance().getLastLaunchTime()) {
            itemHolder.ivHint.setVisibility(View.VISIBLE);
        } else {
            itemHolder.ivHint.setVisibility(View.GONE);
        }
        itemHolder.tvTotal.setText(Html.fromHtml(String.format(Locale.CHINA,
                "<font color='#ffaa17'>%d</font>款礼包", o.totalCount)));
        ViewUtil.showImage(itemHolder.ivIcon, o.img);
        itemHolder.itemView.setOnClickListener(this);
        itemHolder.itemView.setTag(TAG_POSITION, position);
    }

    @Override
    public void onClick(View v) {
        if (mData == null || v.getTag(TAG_POSITION) == null) {
            return;
        }
        Integer pos = (Integer) v.getTag(TAG_POSITION);
        IndexGiftLike o = getItem(pos);
        Global.getLikeNewTimeArray().put(o.id, (int) o.newestCreateTime);
        notifyItemChanged(pos);
        IntentUtil.jumpGameDetail(mContext, o.id, GameTypeUtil.JUMP_STATUS_GIFT);
    }

    static class ViewHolder extends BaseRVHolder {
        TextView tvName;
        TextView tvTotal;
        ImageView ivIcon;
        ImageView ivHint;

        public ViewHolder(View itemView) {
            super(itemView);
            ivIcon = getViewById(R.id.iv_icon);
            tvName = getViewById(R.id.tv_game_name);
            tvTotal = getViewById(R.id.tv_total);
            ivHint = getViewById(R.id.iv_hint);
        }
    }

}
