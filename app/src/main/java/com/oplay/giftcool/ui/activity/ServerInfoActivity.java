package com.oplay.giftcool.ui.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.listener.CallbackListener;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.postbar.ServerInfoFragment;
import com.oplay.giftcool.ui.widget.ToggleButton;
import com.oplay.giftcool.util.ToastUtil;

import java.util.ArrayList;

/**
 * Created by zsigui on 16-8-24.
 */
public class ServerInfoActivity extends BaseAppCompatActivity implements ToggleButton.OnToggleChanged {

    private ArrayList<CallbackListener<Boolean>> mCallbackListeners;
    private ToggleButton tbFocus;

    @Override
    protected void processLogic() {
        handleIntent(getIntent());
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_toolbar_with_focus_toggle);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent == null) {
            ToastUtil.showShort(ConstString.TOAST_MISS_STATE);
            return;
        }
        int type = intent.getIntExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_DEFAULT);
        switch (type) {
            default:
                replaceFragWithTitle(R.id.fl_container,
                        ServerInfoFragment.newInstance(),
                        getString(R.string.st_server_info_title), false);
        }
    }

    @Override
    protected void initMenu(@NonNull Toolbar toolbar) {
        super.initMenu(toolbar);
        tbFocus = getViewById(toolbar, R.id.tb_focus);
        tbFocus.setOnToggleChanged(this);
        // 初始化
        if (AssistantApp.getInstance().isReadAttention()) {
            tbFocus.setToggleOn();
        } else {
            tbFocus.setToggleOff();
        }
    }

    /**
     * 设置ToggleButton状态，不触发onToggleChanged
     *
     * @param on
     */
    public void setTbState(boolean on) {
        if (on) {
            tbFocus.setToggleOn();
        } else {
            tbFocus.setToggleOff();
        }
    }

    public synchronized void addListener(CallbackListener<Boolean> listener) {
        if (mCallbackListeners == null) {
            mCallbackListeners = new ArrayList<>();
        }
        if (!mCallbackListeners.contains(listener)) {
            mCallbackListeners.add(listener);
        }
    }

    public synchronized void removeListener(CallbackListener<Boolean> listener) {
        if (mCallbackListeners == null) {
            return;
        }
        mCallbackListeners.remove(listener);
    }

    @Override
    public void onToggle(boolean on) {

        AssistantApp.getInstance().setIsReadAttention(on);
        if (mCallbackListeners != null) {
            for (CallbackListener<Boolean> listener : mCallbackListeners) {
                listener.doCallBack(on);
            }
        }
    }
}
