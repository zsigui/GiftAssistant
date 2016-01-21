package com.oplay.giftcool.ui.fragment.base;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.ui.fragment.base.stat.TdStatInterface;
import com.tendcloud.tenddata.TCAgent;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/19
 */
public abstract class BaseFragmentLog extends Fragment implements TdStatInterface{


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (AppDebugConfig.IS_FRAG_DEBUG) {
            AppDebugConfig.logMethodName(this);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (AppDebugConfig.IS_FRAG_DEBUG) {
            AppDebugConfig.logMethodName(this);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AppDebugConfig.IS_FRAG_DEBUG) {
            AppDebugConfig.logMethodName(this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (AppDebugConfig.IS_FRAG_DEBUG) {
            AppDebugConfig.logMethodName(this);
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (AppDebugConfig.IS_FRAG_DEBUG) {
            AppDebugConfig.logMethodName(this);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (AppDebugConfig.IS_FRAG_DEBUG) {
            AppDebugConfig.logMethodName(this);
        }
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (AppDebugConfig.IS_FRAG_DEBUG) {
            AppDebugConfig.logMethodName(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (AppDebugConfig.IS_FRAG_DEBUG) {
            AppDebugConfig.logMethodName(this);
        }
        if (!TextUtils.isEmpty(getPageName())) {
            TCAgent.onPageStart(getContext(), getPageName());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (AppDebugConfig.IS_FRAG_DEBUG) {
            AppDebugConfig.logMethodName(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (AppDebugConfig.IS_FRAG_DEBUG) {
            AppDebugConfig.logMethodName(this);
        }
        if (!TextUtils.isEmpty(getPageName())) {
            TCAgent.onPageEnd(getContext(), getPageName());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (AppDebugConfig.IS_FRAG_DEBUG) {
            AppDebugConfig.logMethodName(this);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (AppDebugConfig.IS_FRAG_DEBUG) {
            AppDebugConfig.logMethodName(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (AppDebugConfig.IS_FRAG_DEBUG) {
            AppDebugConfig.logMethodName(this);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (AppDebugConfig.IS_FRAG_DEBUG) {
            AppDebugConfig.logMethodName(this);
        }
    }
}
