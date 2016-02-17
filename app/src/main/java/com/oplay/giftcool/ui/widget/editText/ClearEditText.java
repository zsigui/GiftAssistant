package com.oplay.giftcool.ui.widget.editText;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.util.ViewUtil;

/**
 * Created by zsigui on 16-1-11.
 */
public class ClearEditText extends LinearLayout implements View.OnClickListener {

	private AutoCompleteTextView actvInput;
	private TextView tvClear;
	private boolean mIsPhoneType = false;

	public ClearEditText(Context context) {
		this(context, null);
	}

	public ClearEditText(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ClearEditText(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		actvInput = ViewUtil.getViewById(this, R.id.et_input);
		tvClear = ViewUtil.getViewById(this, R.id.tv_user_clear);
		if (tvClear != null) {
			tvClear.setOnClickListener(this);
			tvClear.setVisibility(View.GONE);
		}
		if (mIsPhoneType) {
			actvInput.setInputType(InputType.TYPE_CLASS_PHONE);
		} else {
			actvInput.setInputType(InputType.TYPE_CLASS_TEXT);
		}

	}

	public boolean isPhoneType() {
		return mIsPhoneType;
	}

	public void setPhoneType(boolean isPhone) {
		mIsPhoneType = isPhone;
	}

	public String getText() {
		return actvInput.getText().toString();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.tv_user_clear:
				actvInput.setText("");
				break;
		}
	}
}
