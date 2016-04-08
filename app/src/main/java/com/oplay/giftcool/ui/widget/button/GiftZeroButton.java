package com.oplay.giftcool.ui.widget.button;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.util.GiftTypeUtil;

/**
 * 0元抢礼包按钮
 *
 * Created by zsigui on 16-1-5.
 */
public class GiftZeroButton extends TextView {

	private boolean mBiggerButton;

	private int mStatus;

	public GiftZeroButton(Context context) {
		this(context, null);
	}

	public GiftZeroButton(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public GiftZeroButton(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		TypedArray t = context.obtainStyledAttributes(attrs, R.styleable.GiftButton, defStyleAttr, 0);
		mBiggerButton = t.getBoolean(R.styleable.GiftButton_gb_isBigger, false);
		t.recycle();
		setRedBg();
	}

	public void setState(int state) {
		mStatus = state;
		setEnabled(false);
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
				break;
			case GiftTypeUtil.TYPE_NORMAL_SEARCHED :
			case GiftTypeUtil.TYPE_NORMAL_SEARCH :
				setText(R.string.st_gift_search);
				setEnabled(true);
				break;
			case GiftTypeUtil.TYPE_NORMAL_SEIZE :
				setText(R.string.st_gift_seize);
				setEnabled(true);
				break;
			case GiftTypeUtil.TYPE_LIMIT_SEIZE :
				setText(R.string.st_gift_seize);
				setRedBg();
				setEnabled(true);
				break;
			case GiftTypeUtil.TYPE_LIMIT_SEIZED :
			case GiftTypeUtil.TYPE_NORMAL_SEIZED:
				setText(R.string.st_gift_seized);
				break;
			case GiftTypeUtil.TYPE_LIMIT_EMPTY :
				setText(R.string.st_gift_empty);
				break;
			case GiftTypeUtil.TYPE_LIMIT_FINISHED :
				setText(R.string.st_gift_finished);
				break;
			case GiftTypeUtil.TYPE_LIMIT_WAIT_SEIZE :
				setText(R.string.st_gift_wait_seize);
				break;
		}
	}

	public int getStatus() {
		return mStatus;
	}

	private void setOrangeBg() {
		if (mBiggerButton) {
			setBackgroundResource(R.drawable.selector_btn_bigger_orange);
		} else {
			setBackgroundResource(R.drawable.selector_btn_orange);
		}
	}

	private void setRedBg() {
		if (mBiggerButton) {
			setBackgroundResource(R.drawable.selector_btn_bigger_red);
		} else {
			setBackgroundResource(R.drawable.selector_btn_red);
		}
	}
}
