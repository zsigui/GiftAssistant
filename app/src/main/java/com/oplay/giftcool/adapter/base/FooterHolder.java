package com.oplay.giftcool.adapter.base;

import android.graphics.drawable.AnimationDrawable;
import android.view.View;

import com.oplay.giftcool.R;

/**
 * Created by zsigui on 16-2-29.
 */
public class FooterHolder extends BaseRVHolder {

	AnimationDrawable animDrawable;

	public FooterHolder(View itemView) {
		super(itemView);
		animDrawable = (AnimationDrawable) getViewById(R.id.iv_anim).getBackground();
	}
}