package com.oplay.giftcool.adapter.holder;

import android.view.View;
import android.widget.TextView;

import com.oplay.giftcool.R;

/**
 * Created by zsigui on 16-8-8.
 */
public class StylePreciousHolder extends StyleBaseHolder {

    public TextView tvPrice;

    public StylePreciousHolder(View itemView) {
        super(itemView);
        tvPrice = getViewById(R.id.tv_price);
    }
}
