package com.oplay.giftassistant.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.util.DensityUtil;

/**
 * 积分任务,添加积分的状态
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
	}



	public void setStateEnable(boolean enable) {
		setCompoundDrawablePadding(DensityUtil.dip2px(getContext(), 1));
		setCompoundDrawablesWithIntrinsicBounds(R.drawable.selector_score_mid, 0, 0, 0);
		if (enable) {
			setTextColor(getResources().getColor(R.color.co_btn_green));
		} else {
			setTextColor(getResources().getColor(R.color.co_btn_grey));
		}
	}
}
