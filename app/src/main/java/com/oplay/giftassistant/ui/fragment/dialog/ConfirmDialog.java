package com.oplay.giftassistant.ui.fragment.dialog;

import android.widget.TextView;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment_Dialog;

/**
 * Created by zsigui on 16-1-6.
 */
public class ConfirmDialog extends BaseFragment_Dialog{

	private TextView tvContent;
	private String mContent;

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
	}


	public void setContent(String content) {
		mContent = content;
		if (tvContent != null) {
			tvContent.setText(content);
		}
	}
}
