package com.oplay.giftcool.ui.fragment.dialog;

import android.view.View;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Dialog;

/**
 * Created by zsigui on 16-2-25.
 */
public class TestChoiceDialog extends BaseFragment_Dialog {

	private TextView tvContent;
	private TextView tvNormal;
	private TextView tvTest;

	public static TestChoiceDialog newInstances() {
		return new TestChoiceDialog();
	}

	@Override
	protected void initView() {
		setContentView(R.layout.dialog_test_choice);
		tvContent = getViewById(R.id.tv_content);
		tvNormal = getViewById(R.id.tv_normal);
		tvTest = getViewById(R.id.tv_test);
	}

	@Override
	protected void processLogic() {
		tvNormal.setOnClickListener(this);
		tvTest.setOnClickListener(this);
	}

	public String getContent() {
		return tvContent == null ? "" : tvContent.getText().toString();
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
			case R.id.tv_normal:
				tvContent.setText("http://lbapi.ouwan.com/api/\nhttp://giftcool.ouwan.com/");
				break;
			case R.id.tv_test:
				tvContent.setText("http://test.lbapi.ouwan.com/api/\nhttp://test.giftcool.ouwan.com/");
				break;
		}
	}
}
