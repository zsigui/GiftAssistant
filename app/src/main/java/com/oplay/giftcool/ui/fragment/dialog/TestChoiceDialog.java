package com.oplay.giftcool.ui.fragment.dialog;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.SPConfig;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Dialog;
import com.oplay.giftcool.util.SPUtil;

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
		String data = SPUtil.getString(AssistantApp.getInstance().getApplicationContext(),
				SPConfig.SP_APP_DEVICE_FILE,
				SPConfig.KEY_TEST_REQUEST_URI,
				"http://test.lbapi.ouwan.com/api/\nhttp://test.giftcool.ouwan.com/");
		if (!TextUtils.isEmpty(data)) {
			tvContent.setText(data);
		}
	}

	public String getContent() {
		return tvContent == null ? "" : tvContent.getText().toString();
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
			case R.id.tv_normal:
				tvContent.setText("http://lbapi.ouwan.com/api/\nhttp://libao.ouwan.com/");
				break;
			case R.id.tv_test:
				tvContent.setText("http://test.lbapi.ouwan.com/api/\nhttp://test.giftcool.ouwan.com/");
				break;
		}
	}
}
