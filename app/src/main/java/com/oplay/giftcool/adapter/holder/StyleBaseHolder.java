package com.oplay.giftcool.adapter.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.base.BaseRVHolder;
import com.oplay.giftcool.ui.widget.button.GiftButton;

/**
 * Created by zsigui on 16-8-9.
 */
public class StyleBaseHolder extends BaseRVHolder{

    public ImageView ivIcon;
    public TextView tvName;
    public GiftButton btnSend;
    public TextView tvSpend;
    public TextView tvCount;
    public TextView tvPercent;
    public ProgressBar pbPercent;
    public TextView tvSeizeHint;

    public StyleBaseHolder(View itemView) {
        super(itemView);
        ivIcon = getViewById(R.id.iv_icon);
        tvName = getViewById(R.id.tv_name);
        tvCount = getViewById(R.id.tv_count);
        btnSend = getViewById(R.id.btn_send);
        tvPercent = getViewById(R.id.tv_percent);
        pbPercent = getViewById(R.id.pb_percent);
        tvSpend = getViewById(R.id.tv_spend);
        tvSeizeHint = getViewById(R.id.tv_seize_hint);
    }
}
