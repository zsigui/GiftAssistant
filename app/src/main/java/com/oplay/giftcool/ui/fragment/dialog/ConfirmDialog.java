package com.oplay.giftcool.ui.fragment.dialog;

import android.support.annotation.DrawableRes;
import android.text.Spannable;
import android.text.Spanned;
import android.view.View;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Dialog;

/**
 * Created by zsigui on 16-1-6.
 */
public class ConfirmDialog extends BaseFragment_Dialog{

	private TextView tvContent;
	private String mContent;
	private Spanned mSpanned;
	private int mPositiveBackground = R.drawable.selector_btn_green;
	private int mPositiveVisibility = View.VISIBLE;
	private int mNegativeVisibility = View.VISIBLE;

	public static ConfirmDialog newInstance() {
		return new ConfirmDialog();
	}

	@Override
	protected void initView() {
		setContentView(R.layout.dialog_confirm);
		tvContent = getViewById(R.id.tv_content);
	}

	@Override
	protected void processLogic() {
		if (mSpanned != null) {
			setContent(mSpanned);
		} else {
			setContent(mContent);
		}
		setPositiveBtnBackground(mPositiveBackground);
		setPositiveVisibility(mPositiveVisibility);
		setNegativeVisibility(mNegativeVisibility);
	}


	public void setContent(String content) {
		mContent = content;
		if (tvContent != null) {
			tvContent.setText(content);
		}
	}

	public void setContent(Spanned content) {
		mSpanned = content;
		if (tvContent != null) {
			tvContent.setText(content);
		}
	}

	public void setPositiveVisibility(int visibility) {
		mPositiveVisibility = visibility;
		if (btnPositive != null) {
			btnPositive.setVisibility(mPositiveVisibility);
		}
	}

	public void setNegativeVisibility(int visibility) {
		mNegativeVisibility = visibility;
		if (btnNegative != null) {
			btnNegative.setVisibility(mNegativeVisibility);
		}
	}

	public void setPositiveBtnBackground(@DrawableRes int bg) {
		mPositiveBackground = bg;
		if (btnPositive != null) {
			btnPositive.setBackgroundResource(mPositiveBackground);
		}
	}
}
