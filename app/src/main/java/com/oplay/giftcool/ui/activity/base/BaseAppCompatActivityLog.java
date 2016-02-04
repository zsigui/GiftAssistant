package com.oplay.giftcool.ui.activity.base;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.oplay.giftcool.config.AppDebugConfig;
import com.socks.library.KLog;
import com.tendcloud.tenddata.TCAgent;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/19
 */
public class BaseAppCompatActivityLog extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AppDebugConfig.IS_DEBUG) {
            AppDebugConfig.logMethodName(this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (AppDebugConfig.IS_DEBUG) {
            AppDebugConfig.logMethodName(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (AppDebugConfig.IS_DEBUG) {
            AppDebugConfig.logMethodName(this);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (AppDebugConfig.IS_DEBUG) {
            AppDebugConfig.logMemoryInfo();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            if (AppDebugConfig.IS_DEBUG) {
                AppDebugConfig.logMethodName(this);
            }
        } catch (Throwable e) {
            if (AppDebugConfig.IS_DEBUG) {
                KLog.e(e);
            }
        }
	    if (AppDebugConfig.IS_TCAGENT_SHOW) {
		    TCAgent.onPause(this);
	    }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (AppDebugConfig.IS_DEBUG) {
                AppDebugConfig.logMethodName(this);
            }
        } catch (Throwable e) {
            if (AppDebugConfig.IS_DEBUG) {
                KLog.e(e);
            }
        }
	    if (AppDebugConfig.IS_TCAGENT_SHOW) {
		    TCAgent.onResume(this);
	    }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (AppDebugConfig.IS_DEBUG) {
            AppDebugConfig.logMethodName(this);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (AppDebugConfig.IS_DEBUG) {
            AppDebugConfig.logMethodName(this);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (AppDebugConfig.IS_DEBUG) {
            AppDebugConfig.logMethodName(this);
        }
    }

}
