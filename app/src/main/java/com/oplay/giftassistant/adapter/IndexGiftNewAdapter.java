package com.oplay.giftassistant.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.model.data.resp.IndexNewGift;
import com.oplay.giftassistant.util.ViewUtil;

import java.util.List;

/**
 * Created by zsigui on 15-12-24.
 */
public class IndexGiftNewAdapter extends BaseAdapter {

    // 限量礼包类型，可抢，limit
    private static final int TYPE_LIMIT_SEIZE = 0;
    // 限量礼包类型，等待抢，disabled - text
    private static final int TYPE_LIMIT_WAIT_SEIZE = 1;
    // 限量礼包类型，已结束, disabled
    private static final int TYPE_LIMIT_FINISHED = 2;
    // 正常礼包类型，可抢，normal
    private static final int TYPE_NORMAL_SEIZE = 10;
    // 正常礼包类型，可淘号，disabled - text
    private static final int TYPE_NORMAL_SEARCH = 11;
    // 正常礼包类型，等待抢号，disabled - text
    private static final int TYPE_NORMAL_WAIT_SEIZE = 12;
    // 正常礼包类型，等待淘号，disabled - text
    private static final int TYPE_NORMAL_WAIT_SEARCH = 13;

    private List<IndexNewGift> mDatas;
    private LayoutInflater mInflater;

    public IndexGiftNewAdapter(Context context) {
        this(context, null);
    }

    public IndexGiftNewAdapter(Context context, List<IndexNewGift> datas) {
        mInflater = LayoutInflater.from(context);
        this.mDatas = datas;
    }

    public void updateData(List<IndexNewGift> data) {
        this.mDatas = data;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return getCount() == 0 ? null : mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }

    @Override
    public int getViewTypeCount() {
        return 7;
    }

    @Override
    public int getItemViewType(int position) {
        IndexNewGift gift = mDatas.get(position);
        long currentTime = System.currentTimeMillis();
        if (gift.isLimit == 1) {
            if (currentTime > gift.seizeTime) {
                // 已经开抢
                if (gift.remainCount == 0) {
                    // 已经结束
                    return TYPE_LIMIT_FINISHED;
                } else {
                    // 抢号中
                    return TYPE_LIMIT_SEIZE;
                }
            } else {
                // 等待抢号
                // time 代表抢号时间
                return TYPE_LIMIT_WAIT_SEIZE;
            }
        } // if finished
        else {
            if (currentTime > gift.seizeTime) {
                // 已经开抢
                if (gift.remainCount == 0) {
                    // 已经结束
                    // 淘号逻辑
                    if (currentTime > gift.searchTime) {
                        // 处于淘号状态
                        return TYPE_NORMAL_SEARCH;
                    } else {
                        // 等待淘号
                        return TYPE_NORMAL_WAIT_SEARCH;
                    }
                } else {
                    // 抢号中
                    return TYPE_NORMAL_SEIZE;
                }
            } else {
                // 等待抢号
                // time 代表抢号时间
                return TYPE_NORMAL_WAIT_SEIZE;
            }
        } // else finished
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        int type = getItemViewType(position);
        if (convertView == null) {
            viewHolder = new ViewHolder();
            if (type == TYPE_NORMAL_SEIZE) {
                convertView = mInflater.inflate(R.layout.item_index_gift_new_normal, null);
                ImageView ivIcon = ViewUtil.getViewById(convertView, R.id.iv_icon);
                TextView tvTitle = ViewUtil.getViewById(convertView, R.id.tv_title);
                TextView tvContent = ViewUtil.getViewById(convertView, R.id.tv_content);
                TextView btnSend = ViewUtil.getViewById(convertView, R.id.btn_send);
                TextView tvScore = ViewUtil.getViewById(convertView, R.id.tv_score);
                TextView tvPercent = ViewUtil.getViewById(convertView, R.id.tv_percent);
                ProgressBar pbPercent = ViewUtil.getViewById(convertView, R.id.pb_percent);
            } else if (type == TYPE_LIMIT_SEIZE) {
                convertView = mInflater.inflate(R.layout.item_index_gift_new_limit, null);
                ImageView ivIcon = ViewUtil.getViewById(convertView, R.id.iv_icon);
                TextView tvTitle = ViewUtil.getViewById(convertView, R.id.tv_title);
                TextView tvContent = ViewUtil.getViewById(convertView, R.id.tv_content);
                TextView btnSend = ViewUtil.getViewById(convertView, R.id.btn_send);
                TextView tvScore = ViewUtil.getViewById(convertView, R.id.tv_score);
                TextView tvOr = ViewUtil.getViewById(convertView, R.id.tv_or);
                TextView tvBean = ViewUtil.getViewById(convertView, R.id.tv_bean);
                TextView tvRemain = ViewUtil.getViewById(convertView, R.id.tv_remain_text);
            } else {
                convertView = mInflater.inflate(R.layout.item_index_gift_new_disabled, null);
                switch (type) {
                    case TYPE_LIMIT_WAIT_SEIZE:
                    case TYPE_NORMAL_WAIT_SEIZE:
                    case TYPE_NORMAL_WAIT_SEARCH:
                    case TYPE_NORMAL_SEARCH:
                        TextView tvCount = ViewUtil.getViewById(convertView, R.id.tv_count);
                    case TYPE_LIMIT_FINISHED:
                        ImageView ivIcon = ViewUtil.getViewById(convertView, R.id.iv_icon);
                        TextView tvTitle = ViewUtil.getViewById(convertView, R.id.tv_title);
                        TextView tvContent = ViewUtil.getViewById(convertView, R.id.tv_content);
                        TextView btnSend = ViewUtil.getViewById(convertView, R.id.btn_send);
                        break;
                    default:
                        throw new IllegalStateException("type is not support! " + type);
                }
            }
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        return convertView;
    }

    /**
     * ViewHolder缓存，由于大体结构相似，统一一个ViewHolder类型
     */
    static class ViewHolder {

    }
}
