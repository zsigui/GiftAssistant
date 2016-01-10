package com.oplay.giftassistant.ui.widget.button;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.oplay.giftassistant.R;

/**
 * Created by zsigui on 16-1-10.
 */
public class TagButton extends TextView {

	public static final int STATE_NONE = 0;
	public static final int STATE_RED = 1;
	public static final int STATE_ORANGE = 2;
	public static final int STATE_BLUE = 3;
	public static final int STATE_PURPLE = 4;
	public static final int STATE_LIGHT_GREEN = 5;


	public TagButton(Context context) {
		this(context, null);
	}

	public TagButton(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TagButton(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setState(0);
	}

	public void setState(int state) {
		setTextColor(getResources().getColor(R.color.co_white));
		switch (state) {
			case STATE_NONE:
				setTextColor(getResources().getColor(R.color.co_common_text_main));
				setBackgroundResource(0);
				break;
			case STATE_RED:
				setBackgroundResource(R.drawable.shape_rect_tag_red);
				break;
			case STATE_ORANGE:
				setBackgroundResource(R.drawable.shape_rect_tag_orange);
				break;
			case STATE_BLUE:
				setBackgroundResource(R.drawable.shape_rect_tag_blue);
				break;
			case STATE_PURPLE:
				setBackgroundResource(R.drawable.shape_rect_tag_purple);
				break;
			case STATE_LIGHT_GREEN:
				setBackgroundResource(R.drawable.shape_rect_tag_light_green);
				break;
		}
	}
}
