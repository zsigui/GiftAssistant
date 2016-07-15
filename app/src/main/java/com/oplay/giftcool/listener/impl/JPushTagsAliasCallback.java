package com.oplay.giftcool.listener.impl;

import android.content.Context;

import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.manager.PushMessageManager;
import com.oplay.giftcool.util.ThreadUtil;

import java.util.Set;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

/**
 * Created by zsigui on 16-3-3.
 */
public class JPushTagsAliasCallback implements TagAliasCallback {

    private Context mContext;
    private Runnable mRunnable;
    private static int count = 0;

    public JPushTagsAliasCallback(Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    public void gotResult(int code, final String alias, Set<String> tag) {
        if (code == 0) {
            AppDebugConfig.d(AppDebugConfig.TAG_PUSH, "set alias success : " + alias);
            PushMessageManager.getInstance().orHasSetAliasSign(PushMessageManager.SdkType.JPUSH);
            count = 0;
        } else {
            AppDebugConfig.d(AppDebugConfig.TAG_PUSH, "set alias failed : " + alias + ", code = " + code);
            if (mRunnable == null) {
                mRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if (PushMessageManager.getInstance().needSetJPush()) {
                            JPushInterface.setAlias(mContext, alias, JPushTagsAliasCallback.this);
                        }
                    }
                };
            }
            PushMessageManager.getInstance().andHasSetAliasSign(PushMessageManager.SdkType.JPUSH_F);
            if (count++ < 3) {
                AppDebugConfig.d(AppDebugConfig.TAG_PUSH, "set alias failed, wait for 5s to run again! ");
                ThreadUtil.runOnUiThread(mRunnable, 5000);
            }
        }
    }
}
