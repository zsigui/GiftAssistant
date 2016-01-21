package com.oplay.giftcool.ui.widget.button;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.oplay.giftcool.R;

/**
 * Created by zsigui on 16-1-5.
 */
public class MissionButton extends TextView {

	public static final int STATUS_NOT_FINISH = 0;
	public static final int STATUS_GET_ENABLE = 1;
	public static final int STATUS_GET_ALREADY = 2;
	public static final int STATUS_INVALID = 3;

	public MissionButton(Context context) {
		super(context);
	}

	public MissionButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MissionButton(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}


	public void setStatus(int status) {
		switch (status) {
			case STATUS_NOT_FINISH: {
				setBackgroundResource(R.drawable.shape_rect_btn_red);
				setTextColor(getResources().getColor(R.color.co_white));
				setText("去完成");
				setEnabled(true);
				break;
			}
			case STATUS_GET_ENABLE: {
				setBackgroundResource(R.drawable.shape_rect_btn_red);
				setTextColor(getResources().getColor(R.color.co_white));
				setText("去完成");
				setEnabled(true);
				break;
			}
			case STATUS_GET_ALREADY: {
				setBackgroundResource(R.drawable.shape_rect_btn_red);
				setTextColor(getResources().getColor(R.color.co_white));
				setText("去完成");
				setEnabled(true);
				break;
			}
			case STATUS_INVALID: {
				setBackgroundResource(R.drawable.shape_rect_btn_grey);
				setTextColor(getResources().getColor(R.color.co_white));
				setText("已完成");
				setEnabled(false);
				break;
			}
		}
	}
}
