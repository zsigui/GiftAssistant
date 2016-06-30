package com.oplay.giftcool.ui.activity;

import android.content.Intent;
import android.view.View;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.WebFragment;
import com.oplay.giftcool.util.ToastUtil;

/**
 * Created by zsigui on 16-1-29.
 */
public class WebActivity extends BaseAppCompatActivity {


    @Override
    protected void initView() {
        setContentView(R.layout.activity_web);
    }

    @Override
    protected void processLogic() {
        if (getIntent() == null) {
            ToastUtil.showShort(ConstString.TOAST_WRONG_PARAM);
            finish();
            return;
        }
        handleRedirect(getIntent());
    }

    private void handleRedirect(Intent intent) {
        String url = intent.getStringExtra(KeyConfig.KEY_DATA);
        String title = intent.getStringExtra(KeyConfig.KEY_TITLE);
        AppDebugConfig.d(AppDebugConfig.TAG_ACTIVITY, "url = " + url);
        replaceFragWithTitle(R.id.fl_container, WebFragment.newInstance(url), title);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_bar_back) {
            finish();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleRedirect(intent);
    }
}
