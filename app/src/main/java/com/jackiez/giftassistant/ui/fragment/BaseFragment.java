package com.jackiez.giftassistant.ui.fragment;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.View;

import com.jackiez.giftassistant.ui.AssistantApp;
import com.jackiez.giftassistant.ui.activity.BaseActivity;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/13
 */
public abstract class BaseFragment extends Fragment {
    protected String TAG;
    protected AssistantApp mApp;
    protected View mContentView;
    protected BaseActivity mActivity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        TAG = this.getClass().getSimpleName();
        mApp = AssistantApp.getInstance();
        mActivity = (BaseActivity) context;
    }


}
