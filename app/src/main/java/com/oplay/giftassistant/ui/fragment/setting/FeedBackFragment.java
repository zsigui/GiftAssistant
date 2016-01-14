package com.oplay.giftassistant.ui.fragment.setting;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment_WithName;

/**
 * Created by zsigui on 16-1-6.
 */
public class FeedBackFragment extends BaseFragment_WithName implements TextWatcher {

	private RadioButton rbFunction;
	private RadioButton rbPay;
	private RadioButton rbOther;
	private RadioGroup mTypeGroup;
	private EditText etContent;
	private TextView tvContentCount;
	private EditText etPhone;
	private TextView btnSend;

	public static FeedBackFragment newInstance() {
		return new FeedBackFragment();
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		setContentView(R.layout.fragment_feedback);
		rbFunction = getViewById(R.id.rb_function);
		rbPay = getViewById(R.id.rb_pay);
		rbOther = getViewById(R.id.rb_other);
		mTypeGroup = getViewById(R.id.rg_type);
		etContent = getViewById(R.id.et_content);
		tvContentCount = getViewById(R.id.tv_content_count);
		etPhone = getViewById(R.id.et_phone);
		btnSend = getViewById(R.id.btn_send);
	}

	@Override
	protected void setListener() {
		btnSend.setOnClickListener(this);
		etContent.addTextChangedListener(this);
	}

	@Override
	protected void processLogic(Bundle savedInstanceState) {
	}

	@Override
	protected void lazyLoad() {

	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}

	@Override
	public void afterTextChanged(Editable s) {
		if (s.toString().length() > 500) {
			s.subSequence(0, 500);
		}
		tvContentCount.setText(String.format("%s/500", s.toString().length()));
	}
}
