package com.oplay.giftcool.adapter.holder;

import android.view.View;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.ui.widget.DeletedTextView;

/**
 * Created by zsigui on 16-8-8.
 */
public class StyleCouponHolder extends StyleLabelBaseHolder {

    public DeletedTextView tvPrice;
    public TextView tvPlatform;

    public StyleCouponHolder(View itemView) {
        super(itemView);
        tvPrice = getViewById(R.id.tv_price);
        tvPlatform = getViewById(R.id.tv_platform);
    }
}
