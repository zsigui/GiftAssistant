package com.oplay.giftassistant.ui.widget.editText;

/**
 * Created by zsigui on 16-1-11.
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.oplay.giftassistant.R;


public class PasswordTextView extends RelativeLayout implements CompoundButton.OnCheckedChangeListener {
	private EditText mEdtMain;
	private CheckBox mBtnMain;

	private String mHint;

	public PasswordTextView(Context context) {
		super(context);
		init(context, null);
	}

	public PasswordTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public PasswordTextView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}

	public void init(Context context, AttributeSet attrs) {
		LayoutInflater.from(context).inflate(R.layout.textview_password, this, true);
		TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.PasswordTextView);
		mHint = array.getString(R.styleable.PasswordTextView_ptv_hint);
		array.recycle();
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mEdtMain = (EditText) findViewById(R.id.edt_textview_password);
		mEdtMain.setHint(mHint);
		mEdtMain.setImeOptions(EditorInfo.IME_ACTION_NONE);
		mEdtMain.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		mBtnMain = (CheckBox) findViewById(R.id.ctv_textview_password);
		mBtnMain.setOnCheckedChangeListener(this);
		//InputTextUtil.initPswFilter(mEdtMain);
	}


	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (mEdtMain != null) {
			final int cursorPosition = mEdtMain.getText().toString().length();
			if (isChecked) {
				mEdtMain.setInputType(InputType.TYPE_CLASS_TEXT);
				mEdtMain.setSelection(cursorPosition);
			} else {
				mEdtMain.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
				mEdtMain.setSelection(cursorPosition);
			}
		}
	}

	public String getText() {
		return mEdtMain.getText().toString();
	}

	public void setText(CharSequence content) {
		mEdtMain.setText(content);
		mEdtMain.setSelection(mEdtMain.getText().toString().length());
	}

	public void setForcus() {
		mEdtMain.requestFocus();
	}
}
