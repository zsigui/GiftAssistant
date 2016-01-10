package com.oplay.giftassistant.ui.widget.button;

import android.content.Context;
import android.content.res.TypedArray;
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

	private boolean mBiggerButton;

	public GiftButton(Context context) {
		this(context, null);
	}

	public GiftButton(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public GiftButton(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		TypedArray t = context.obtainStyledAttributes(attrs, R.styleable.GiftButton, defStyleAttr, 0);
		mBiggerButton = t.getBoolean(R.styleable.GiftButton_gb_isBigger, false);
		t.recycle();
	}

	public void setState(int state) {
		setEnabled(true);
		setOrangeBg();
		switch (state) {
			case GiftTypeUtil.TYPE_NORMAL_FINISHED :
				setText(R.string.st_gift_finished);
				break;
			case GiftTypeUtil.TYPE_NORMAL_WAIT_SEARCH :
				setText(R.string.st_gift_wait_search);
				break;
			case GiftTypeUtil.TYPE_NORMAL_WAIT_SEIZE :
				setText(R.string.st_gift_wait_seize);
				setEnabled(false);
				break;
			case GiftTypeUtil.TYPE_NORMAL_SEARCHED :
			case GiftTypeUtil.TYPE_NORMAL_SEARCH :
				setText(R.string.st_gift_search);
				break;
			case GiftTypeUtil.TYPE_NORMAL_SEIZE :
				setText(R.string.st_gift_seize);
				break;
			case GiftTypeUtil.TYPE_LIMIT_SEIZED :
				setText(R.string.st_gift_seized);
				setEnabled(false);
				break;
			case GiftTypeUtil.TYPE_LIMIT_EMPTY :
				setText(R.string.st_gift_empty);
				setEnabled(false);
				break;
			case GiftTypeUtil.TYPE_LIMIT_FINISHED :
				setText(R.string.st_gift_finished);
				setEnabled(false);
				break;
			case GiftTypeUtil.TYPE_LIMIT_WAIT_SEIZE :
				setText(R.string.st_gift_wait_seize);
				setEnabled(false);
				break;
			case GiftTypeUtil.TYPE_LIMIT_SEIZE :
				setText(R.string.st_gift_seize);
				setBackgroundResource(R.drawable.selector_btn_red);
				setRedBg();
				break;
		}
		invalidate();
	}

	private void setOrangeBg() {
		if (mBiggerButton) {
			setBackgroundResource(R.drawable.selector_btn_orange);
		} else {
			setBackgroundResource(R.drawable.selector_btn_bigger_orange);
		}
	}

	private void setRedBg() {
		if (mBiggerButton) {
			setBackgroundResource(R.drawable.selector_btn_red);
		} else {
			setBackgroundResource(R.drawable.selector_btn_bigger_red);
		}
	}
}
