package com.oplay.giftcool.ui.widget;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.util.DensityUtil;

/**
 * 金币任务,添加金币的状态
 *
 * Created by zsigui on 16-1-6.
 */
public class ScoreText extends TextView{
	public ScoreText(Context context) {
		this(context, null);
	}

	public ScoreText(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ScoreText(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setStateEnable(true);
		setCompoundDrawablePadding(DensityUtil.dip2px(getContext(), 1));
		setCompoundDrawablesWithIntrinsicBounds(R.drawable.selector_score_mid, 0, 0, 0);
	}



	public void setStateEnable(boolean enable) {
		setEnabled(enable);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (enable) {
				setTextColor(getResources().getColor(R.color.co_btn_green, null));
			} else {
				setTextColor(getResources().getColor(R.color.co_btn_grey, null));
			}
		} else {
			if (enable) {
				setTextColor(getResources().getColor(R.color.co_btn_green));
			} else {
				setTextColor(getResources().getColor(R.color.co_btn_grey));
			}
		}
	}
}
