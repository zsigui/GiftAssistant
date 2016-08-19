package com.oplay.giftcool.adapter.holder;

import android.view.View;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.ui.widget.ArrowAnimView;
import com.oplay.giftcool.ui.widget.ClockAnimView;

/**
 * Created by zsigui on 16-8-9.
 */
public class StyleLabelBaseHolder extends StyleBaseHolder {

    public TextView tvSeize;
    public ArrowAnimView aavView;
    public ClockAnimView cavView;

    public StyleLabelBaseHolder(View itemView) {
        super(itemView);
        tvSeize = getViewById(R.id.tv_seize);
        aavView = getViewById(R.id.aav_view);
        cavView = getViewById(R.id.cav_view);
    }
}
