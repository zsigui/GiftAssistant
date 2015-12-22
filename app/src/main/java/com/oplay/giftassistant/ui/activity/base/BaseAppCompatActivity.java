package com.oplay.giftassistant.ui.activity.base;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.oplay.giftassistant.AssistantApp;
import com.oplay.giftassistant.R;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/13
 */
public abstract class BaseAppCompatActivity extends BaseAppCompatActivityLog implements View.OnClickListener{

    protected AssistantApp mApp;
    private SweetAlertDialog mLoadingDialog;
    private Toolbar mToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApp = AssistantApp.getInstance();
        initView();
        mToolbar = getViewById(R.id.toolbar);
        if (mToolbar != null) {
            final View backIcon = mToolbar.findViewById(R.id.iv_bar_back);
            if (backIcon != null) {
                backIcon.setOnClickListener(this);
            }
            initMenu(mToolbar);
        }
    }

    protected abstract void initView();

    protected void initMenu(@NonNull Toolbar toolbar) {}

    protected <V extends View> V getViewById(@IdRes int id) {
        View child = findViewById(id);
        return (child != null ? (V)child : null);
    }

    protected <V extends View> V getViewById(View v, @IdRes int id) {
        View child = v.findViewById(id);
        return (child != null ? (V)child : null);
    }

    protected void setBarTitle(@StringRes int res) {
        if (mToolbar != null) {
            TextView tv = getViewById(mToolbar, R.id.tv_bar_title);
            if (tv != null) {
                tv.setText(res);
            }
        }
    }

    protected void setBarTitle(String title) {
        if (mToolbar != null) {
            TextView tv = getViewById(mToolbar, R.id.tv_bar_title);
            if (tv != null) {
                tv.setText(title);
            }
        }
    }

    public void showLoadingDialog() {
        if (this.mLoadingDialog == null) {
            mLoadingDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
            mLoadingDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.red_btn_bg_color));
            mLoadingDialog.setCancelable(false);
            mLoadingDialog.setTitleText("数据加载中...");
        }
        mLoadingDialog.show();
    }

    public void hideLoadingDialog() {
        if (this.mLoadingDialog != null) {
            mLoadingDialog.dismiss();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_bar_back) {
            this.finish();
        }
    }

}
