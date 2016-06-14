package com.oplay.giftcool.ui.activity;

import android.support.annotation.IdRes;
import android.view.View;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.oplay.giftcool.ui.fragment.login.OuwanLoginFragment;
import com.oplay.giftcool.ui.fragment.login.PhoneLoginNewFragment;
import com.oplay.giftcool.util.InputMethodUtil;

/**
 * Created by zsigui on 16-1-6.
 */
public class LoginActivity extends BaseAppCompatActivity {

    private int type = 0;

    @Override
    protected void initView() {
        setContentView(R.layout.activity_common_with_back);
    }

    @Override
    protected void processLogic() {
        if (getIntent() != null)
            type = getIntent().getIntExtra(KeyConfig.KEY_TYPE, 0);

        if (type != KeyConfig.TYPE_ID_OUWAN_LOGIN) {
            replaceFragWithTitle(R.id.fl_container, PhoneLoginNewFragment.newInstance(),
                    getResources().getString(R.string.st_login_phone_title), false);
        } else {
            replaceFragWithTitle(R.id.fl_container, OuwanLoginFragment.newInstance(),
                    getResources().getString(R.string.st_login_ouwan_title), false);
        }
    }

    @Override
    public void replaceFragWithTitle(@IdRes int id, BaseFragment newFrag, String title, boolean isAddToBackStack) {
        super.replaceFragWithTitle(id, newFrag, title, isAddToBackStack);
        mCurTopFragment = newFrag;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_bar_back) {
            InputMethodUtil.hideSoftInput(this);
            mNeedWorkCallback = false;
            doBeforeFinish();
            finish();
        }
    }
}
