package com.oplay.giftcool.util;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
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
					String str = StringUtil.stringFilter(s.toString());
					if (!s.toString().equals(str)) {
						etOne.setText(str);
						//设置新的光标所在位置
						etOne.setSelection(str.length());
						s = str;
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
							if (TextUtils.isEmpty(etOne.getText())) {
								etOne.setImeOptions(EditorInfo.IME_ACTION_DONE);
								etTwo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
							} else {
								etOne.setImeOptions(EditorInfo.IME_ACTION_NEXT);
								etTwo.setImeOptions(EditorInfo.IME_ACTION_DONE);
							}
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
					String str = StringUtil.stringFilter(s.toString());
					if (!s.toString().equals(str)) {
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
