package com.oplay.giftcool.ui.fragment.dialog;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.NetUrl;
import com.oplay.giftcool.config.SPConfig;
import com.oplay.giftcool.config.WebViewUrl;
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
                String.format("%s\n%s\n%s", NetUrl.TEST_URL_BASE,
                        WebViewUrl.TEST_URL_BASE,
                        NetUrl.TEST_SOCKET_IO_URL));
        tvNormal.setText(String.format("正式地址：\nnet->%s\nweb->%s\nio->%s", NetUrl.URL_BASE,
                WebViewUrl.URL_BASE,
                NetUrl.SOCKET_IO_URL));
        tvTest.setText(String.format("测试地址：\nnet->%s\nweb->%s\nio->%s", NetUrl.TEST_URL_BASE,
                WebViewUrl.TEST_URL_BASE,
                NetUrl.TEST_SOCKET_IO_URL));
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
                tvContent.setText(String.format("%s\n%s\n%s", NetUrl.URL_BASE,
                        WebViewUrl.URL_BASE, NetUrl.SOCKET_IO_URL));
                break;
            case R.id.tv_test:
                tvContent.setText(String.format("%s\n%s\n%s", NetUrl.TEST_URL_BASE,
                        WebViewUrl.TEST_URL_BASE, NetUrl.TEST_SOCKET_IO_URL));
                break;
        }
    }
}
