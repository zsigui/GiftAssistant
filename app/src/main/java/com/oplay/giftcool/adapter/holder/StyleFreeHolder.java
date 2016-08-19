package com.oplay.giftcool.adapter.holder;

import android.view.View;
import android.widget.TextView;

import com.oplay.giftcool.R;

/**
 * Created by zsigui on 16-8-8.
 */
public class StyleFreeHolder extends StyleLabelBaseHolder {

    public TextView tvPrice;
    public TextView tvContent;

    public StyleFreeHolder(View itemView) {
        super(itemView);
        tvPrice = getViewById(R.id.tv_price);
        tvContent = getViewById(R.id.tv_content);
    }
}
