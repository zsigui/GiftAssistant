package com.oplay.giftcool.util;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.ui.fragment.login.OuwanLoginFragment;

/**
 * Created by zsigui on 16-1-11.
 */
public class InputTextUtil {

	public static String sOrigin = null;

	/**
	 * @Title: initPswFilter
	 * @Description:只能输入非空格的ASCII码
	 */
	public static void initPswFilter(final EditText etOne, final EditText etTwo,
	                                 final TextView oneClear, final TextView twoClear,
	                                 final TextView tvSend, final TextView tvCode,
	                                 final boolean isOuwan) {
		if (etOne != null) {
			etOne.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
					sOrigin = s.toString();
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
						if (s.length() > 0 && etOne.isFocused()) {
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
					if (tvCode != null) {
						if (TextUtils.isEmpty(etOne.getText())) {
							tvCode.setEnabled(false);
							tvCode.setTextColor(AssistantApp.getInstance().getResources().getColor(R.color
									.co_btn_grey));
						} else {
							tvCode.setEnabled(true);
							tvCode.setTextColor(AssistantApp.getInstance().getResources().getColor(R.color
									.co_btn_green));
						}
					}
					if (!s.toString().equals(sOrigin)) {
						if (isOuwan) {
							OuwanLoginFragment.sNeedEncrypt = true;
						}
					}
				}

			});
		}
		if (etTwo != null) {
			etTwo.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
					sOrigin = s.toString();
				}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					String str = StringUtil.stringFilter(s.toString());
					if (!s.toString().equals(str)) {
						etTwo.setText(str);
						//设置新的光标所在位置
						etTwo.setSelection(str.length());
					}
					if (twoClear != null) {
						if (s.length() > 0 && etTwo.isFocused()) {
							twoClear.setVisibility(View.VISIBLE);
						} else {
							twoClear.setVisibility(View.GONE);
						}
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
					if (!s.toString().equals(sOrigin)) {
						if (isOuwan) {
							OuwanLoginFragment.sNeedEncrypt = true;
						}
					}
				}
			});
		}
	}
}
