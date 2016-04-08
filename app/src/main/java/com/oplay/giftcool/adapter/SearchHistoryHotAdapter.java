package com.oplay.giftcool.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.base.BaseRVAdapter;
import com.oplay.giftcool.adapter.base.BaseRVHolder;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.util.GameTypeUtil;
import com.oplay.giftcool.model.data.resp.IndexGameNew;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.ViewUtil;

/**
 * Created by zsigui on 16-3-4.
 */
public class SearchHistoryHotAdapter extends BaseRVAdapter<IndexGameNew> implements View.OnClickListener {

    public SearchHistoryHotAdapter(Context context) {
        super(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemHolder(LayoutInflater.from(mContext).inflate(R.layout.item_search_history_hot, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        IndexGameNew item = getItem(position);
        ItemHolder itemHolder = (ItemHolder) holder;
        ViewUtil.showImage(itemHolder.ivIcon, item.img);
        itemHolder.tvName.setText(item.name);
        itemHolder.tvGift.setText(Html.fromHtml(String.format(ConstString.TEXT_GIFT_TOTAL, item.totalCount)));
        itemHolder.itemView.setTag(TAG_POSITION, position);
        itemHolder.itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getTag(TAG_POSITION) == null) {
            return;
        }
        IndexGameNew game = getItem((Integer) v.getTag(TAG_POSITION));
        IntentUtil.jumpGameDetail(mContext, game.id, GameTypeUtil.JUMP_STATUS_GIFT);
    }

    static class ItemHolder extends BaseRVHolder {

        private ImageView ivIcon;
        private TextView tvName;
        private TextView tvGift;

        public ItemHolder(View itemView) {
            super(itemView);
            ivIcon = getViewById(R.id.iv_icon);
            tvName = getViewById(R.id.tv_name);
            tvGift = getViewById(R.id.tv_gift);
        }
    }
}
