package com.oplay.giftcool.ui.fragment.base;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.manager.StatisticsManager;
import com.oplay.giftcool.ui.fragment.base.stat.TdStatInterface;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/19
 */
public abstract class BaseFragmentLog extends Fragment implements TdStatInterface {


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        AppDebugConfig.v();
        if (getContext() == null) {
            return;
        }
        if (!TextUtils.isEmpty(getPageName())) {
            if (AppDebugConfig.IS_STATISTICS_SHOW) {
                if (isVisibleToUser) {
                    StatisticsManager.getInstance().onPageStart(getContext(), getPageName());
                } else {
                    StatisticsManager.getInstance().onPageEnd(getContext(), getPageName());
                }
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        AppDebugConfig.v();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppDebugConfig.v();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AppDebugConfig.v();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AppDebugConfig.v();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        AppDebugConfig.v();
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        AppDebugConfig.v();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!TextUtils.isEmpty(getPageName()) && getUserVisibleHint()) {
            StatisticsManager.getInstance().onPageStart(getContext(), getPageName());
        }
        AppDebugConfig.v();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        AppDebugConfig.v();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!TextUtils.isEmpty(getPageName()) && getUserVisibleHint()) {
            if (AppDebugConfig.IS_STATISTICS_SHOW) {
                StatisticsManager.getInstance().onPageEnd(getContext(), getPageName());
            }
        }
        AppDebugConfig.v();
    }

    @Override
    public void onStop() {
        super.onStop();
        AppDebugConfig.v();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        AppDebugConfig.v();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AppDebugConfig.v();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        AppDebugConfig.v();
    }
}
