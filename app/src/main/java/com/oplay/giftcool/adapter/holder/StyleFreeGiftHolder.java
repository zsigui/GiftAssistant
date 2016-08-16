package com.oplay.giftcool.adapter.holder;

import android.view.View;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.ui.widget.DeletedTextView;

/**
 * Created by zsigui on 16-8-9.
 */
public class StyleFreeGiftHolder extends StyleFreeBaseHolder {

    public DeletedTextView tvPrice;
    public TextView tvContent;

    public StyleFreeGiftHolder(View itemView) {
        super(itemView);
        tvPrice = getViewById(R.id.tv_price);
        tvContent = getViewById(R.id.tv_content);
    }
}
