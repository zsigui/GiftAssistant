package com.oplay.giftcool.ui.fragment.dialog;

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
		setContent(mContent);
		setPositiveVisibility(mPositiveVisibility);
		setNegativeVisibility(mNegativeVisibility);
	}


	public void setContent(String content) {
		mContent = content;
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
}
