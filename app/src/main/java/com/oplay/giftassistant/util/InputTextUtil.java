package com.oplay.giftassistant.util;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by zsigui on 16-1-11.
 */
public class InputTextUtil {

	/**
	 * @Title: initPswFilter
	 * @Description:只能输入非空格的ASCII码
	 */
	public static void initPswFilter(final EditText etOne, final EditText etTwo,
	                                 final TextView oneClear, final TextView tvSend) {
		if (etOne != null) {
			etOne.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					String editable = etOne.getText().toString();
					String str = StringUtil.stringFilter(editable);
					if (!editable.equals(str)) {
						etOne.setText(str);
						//设置新的光标所在位置
						etOne.setSelection(str.length());
					}
					if (oneClear != null) {
						if (s.length() > 0) {
							oneClear.setVisibility(View.VISIBLE);
						} else {
							oneClear.setVisibility(View.GONE);
						}
					}
				}

				@Override
				public void afterTextChanged(Editable s) {
					if (etTwo != null) {
						if (TextUtils.isEmpty(etOne.getText()) || TextUtils.isEmpty(etTwo.getText())) {
							tvSend.setEnabled(false);
						} else {
							tvSend.setEnabled(true);
						}
					} else {
						if (TextUtils.isEmpty(etOne.getText())) {
							tvSend.setEnabled(false);
						} else {
							tvSend.setEnabled(true);
						}
					}
				}

			});
		}
		if (etTwo != null) {
			etTwo.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {

				}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					String editable = etTwo.getText().toString();
					String str = StringUtil.stringFilter(editable);
					if (!editable.equals(str)) {
						etTwo.setText(str);
						//设置新的光标所在位置
						etTwo.setSelection(str.length());
					}
				}

				@Override
				public void afterTextChanged(Editable s) {
					if (etOne != null) {
						if (TextUtils.isEmpty(etOne.getText()) || TextUtils.isEmpty(etTwo.getText())) {
							tvSend.setEnabled(false);
						} else {
							tvSend.setEnabled(true);
						}
					} else {
						if (TextUtils.isEmpty(etTwo.getText())) {
							tvSend.setEnabled(false);
						} else {
							tvSend.setEnabled(true);
						}
					}
				}
			});
		}
	}
}
