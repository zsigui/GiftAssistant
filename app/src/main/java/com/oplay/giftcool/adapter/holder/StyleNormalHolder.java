package com.oplay.giftcool.adapter.holder;

import android.view.View;
import android.widget.TextView;

import com.oplay.giftcool.R;

/**
 * Created by zsigui on 16-8-8.
 */
public class StyleNormalHolder extends StyleBaseHolder {

    public TextView tvContent;

    public StyleNormalHolder(View itemView) {
        super(itemView);
        tvContent = getViewById(R.id.tv_content);
    }
}
