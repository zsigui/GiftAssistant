package com.jackiez.giftassistant.ui.activity;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.jackiez.giftassistant.R;
import com.jackiez.giftassistant.ui.AssistantApp;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/13
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected String TAG;
    protected AssistantApp mApp;
    private SweetAlertDialog mLoadingDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG = this.getClass().getSimpleName();
        mApp = AssistantApp.getInstance();
        initView(savedInstanceState);
        setListener();
        processLogic(savedInstanceState);
    }

    protected <V extends View> V getViewById(@IdRes int id) {
        return (V) findViewById(id);
    }

    protected abstract void initView(Bundle savedInstanceState);

    protected abstract void setListener();

    protected abstract void processLogic(Bundle savedInstanceState);

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
}
