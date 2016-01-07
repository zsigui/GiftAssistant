package com.oplay.giftassistant.ui.widget.button;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.config.GiftTypeUtil;

/**
 * 礼包按钮
 *
 * Created by zsigui on 16-1-5.
 */
public class GiftButton extends TextView{

	public GiftButton(Context context) {
		super(context);
	}

	public GiftButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public GiftButton(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public void setState(int state) {
		setEnabled(true);
		switch (state) {
			case GiftTypeUtil.TYPE_NORMAL_FINISHED :
				setText(R.string.st_gift_finished);
			case GiftTypeUtil.TYPE_NORMAL_WAIT_SEARCH :
				setText(R.string.st_gift_wait_search);
			case GiftTypeUtil.TYPE_NORMAL_WAIT_SEIZE :
				setText(R.string.st_gift_wait_seize);
				setEnabled(false);
			case GiftTypeUtil.TYPE_NORMAL_SEARCH :
				setText(R.string.st_gift_search);
			case GiftTypeUtil.TYPE_NORMAL_SEIZE :
				setText(R.string.st_gift_seize);
				setBackgroundResource(R.drawable.selector_btn_orange);
				break;
			case GiftTypeUtil.TYPE_LIMIT_FINISHED :
				setText(R.string.st_gift_finished);
			case GiftTypeUtil.TYPE_LIMIT_WAIT_SEIZE :
				setText(R.string.st_gift_wait_seize);
				setEnabled(false);
			case GiftTypeUtil.TYPE_LIMIT_SEIZE :
				setText(R.string.st_gift_seize);
				setBackgroundResource(R.drawable.selector_btn_red);
				break;
		}
	}
}
