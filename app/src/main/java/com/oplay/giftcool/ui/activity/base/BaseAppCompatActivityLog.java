package com.oplay.giftcool.ui.activity.base;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.manager.StatisticsManager;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/19
 */
public abstract class BaseAppCompatActivityLog extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppDebugConfig.v();
    }

    @Override
    protected void onStop() {
        super.onStop();
        AppDebugConfig.v();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppDebugConfig.v();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        AppDebugConfig.v();
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            StatisticsManager.getInstance().onPause(this);
            AppDebugConfig.v();
        } catch (Throwable e) {
            AppDebugConfig.w(AppDebugConfig.TAG_ACTIVITY, e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            StatisticsManager.getInstance().onResume(this);
            AppDebugConfig.v();
        } catch (Throwable e) {
            AppDebugConfig.w(AppDebugConfig.TAG_ACTIVITY, e);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        AppDebugConfig.v();
    }

    @Override
    protected void onStart() {
        super.onStart();
        AppDebugConfig.v();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        AppDebugConfig.v();
    }

}
