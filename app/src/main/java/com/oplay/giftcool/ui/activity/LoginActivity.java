package com.oplay.giftcool.ui.activity;

import android.content.Intent;
import android.support.annotation.IdRes;
import android.view.View;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.listener.OnBackPressListener;
import com.oplay.giftcool.model.data.resp.UserModel;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.oplay.giftcool.ui.fragment.login.BindOwanFragment;
import com.oplay.giftcool.ui.fragment.login.OuwanLoginFragment;
import com.oplay.giftcool.ui.fragment.login.PhoneLoginFragment;
import com.oplay.giftcool.ui.fragment.login.PhoneLoginNewFragment;
import com.oplay.giftcool.util.InputMethodUtil;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zsigui on 16-1-6.
 */
public class LoginActivity extends BaseAppCompatActivity {

    private List<Integer> mTypeHierarchy;

    @Override
    protected void initView() {
        setContentView(R.layout.activity_common_with_back);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (mTypeHierarchy == null) {
            mTypeHierarchy = new ArrayList<>();
        }
        int type = mTypeHierarchy.size() == 0 ?
                KeyConfig.TYPE_ID_DEFAULT : mTypeHierarchy.get(mTypeHierarchy.size() - 1);
        if (intent != null
                && type != KeyConfig.TYPE_ID_DEFAULT
                && type != intent.getIntExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_DEFAULT)) {
            handleRedirect(intent);
        }
    }

    @Override
    protected void processLogic() {
        mTypeHierarchy = new ArrayList<>(5);
        handleRedirect(getIntent());
    }

    private void handleRedirect(Intent intent) {

        if (intent == null) {
            ToastUtil.showShort(ConstString.TOAST_MISS_STATE);
            AppDebugConfig.d(AppDebugConfig.TAG_ACTIVITY, "no intent");
            return;
        }
        int type = intent.getIntExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_DEFAULT);
        if (type == KeyConfig.TYPE_ID_DEFAULT) {
            AppDebugConfig.d(AppDebugConfig.TAG_ACTIVITY, "no type");
            ToastUtil.showShort(ConstString.TOAST_MISS_STATE);
            return;
        }
        mTypeHierarchy.add(type);
        switch (type) {
            case KeyConfig.TYPE_ID_OUWAN_LOGIN:
                replaceFragWithTitle(R.id.fl_container, OuwanLoginFragment.newInstance(),
                    getResources().getString(R.string.st_login_ouwan_title), false);
                break;
            case KeyConfig.TYPE_ID_PHONE_LOGIN:
                if (AssistantApp.getInstance().getPhoneLoginType() == 1) {
                    replaceFragWithTitle(R.id.fl_container, PhoneLoginNewFragment.newInstance(),
                            getResources().getString(R.string.st_login_phone_new_title), false);
                } else {
                    replaceFragWithTitle(R.id.fl_container, PhoneLoginFragment.newInstance(),
                            getResources().getString(R.string.st_login_phone_title), false);
                }
                break;
            case KeyConfig.TYPE_ID_BIND_OUWAN:
                UserModel um = (UserModel) intent.getSerializableExtra(KeyConfig.KEY_DATA);
                if (um == null || um.userInfo == null || um.userSession == null) {
                    ToastUtil.showShort(ConstString.TOAST_MISS_STATE);
                    return;
                }
                replaceFragWithTitle(R.id.fl_container, BindOwanFragment.newInstance(um),
                        getResources().getString(um.userInfo.phoneCanUseAsUname ?
                                R.string.st_login_bind_owan_title_1 : R.string.st_login_bind_owan_title_2), false);
                break;
            default:
                mTypeHierarchy.remove(mTypeHierarchy.size() - 1);
                AppDebugConfig.d(AppDebugConfig.TAG_ACTIVITY, "type = " + type);
                ToastUtil.showShort(ConstString.TOAST_MISS_STATE);
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
            doLoginBack();
        }
    }

    /**
     * 重新复写处理后退事件
     */
    @Override
    public boolean onBack() {
        InputMethodUtil.hideSoftInput(this);
        if (getTopFragment() != null && getTopFragment() instanceof OnBackPressListener
                && ((OnBackPressListener) getTopFragment()).onBack()) {
            // back事件被处理
            return false;
        }
        if (!popFrag() && !isFinishing()) {
            mNeedWorkCallback = false;
            if (MainActivity.sGlobalHolder == null) {
                IntentUtil.jumpHome(this, false);
            }
            finish();
        } else {
            if (getTopFragment() instanceof BaseFragment) {
                setBarTitle(((BaseFragment) getTopFragment()).getTitleName());
            }
            if (mTypeHierarchy != null && mTypeHierarchy.size() > 0) {
                mTypeHierarchy.remove(mTypeHierarchy.size() - 1);
            }
        }
        return true;
    }

    public void doLoginBack() {
        InputMethodUtil.hideSoftInput(this);
        mNeedWorkCallback = false;
        doBeforeFinish();
        finish();
    }
}
