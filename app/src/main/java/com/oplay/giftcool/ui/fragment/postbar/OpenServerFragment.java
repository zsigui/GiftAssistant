package com.oplay.giftcool.ui.fragment.postbar;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.listener.CallbackListener;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.ui.activity.ServerInfoActivity;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.oplay.giftcool.util.DateUtil;
import com.oplay.giftcool.util.ToastUtil;

import java.util.ArrayList;

/**
 * @author JackieZhuang
 * @email zsigui@foxmail.com
 * @date 2016/8/31
 */
public class OpenServerFragment extends BaseFragment implements CallbackListener<Boolean> {


    private final int INDEX_COUNT = 6;
    private final int INDEX_BEFORE = 0;
    private final int INDEX_YESTERDAY = 1;
    private final int INDEX_TODAY = 2;
    private final int INDEX_TOMORROW = 3;
    private final int INDEX_AFTER = 4;
    private final int INDEX_FOCUS = 5;

    private CheckedTextView tvBefore;
    private CheckedTextView tvYesterday;
    private CheckedTextView tvToday;
    private CheckedTextView tvTomorrow;
    private CheckedTextView tvAfter;
    private LinearLayout llAnchors;

    private int mType;
    private ArrayList<ServerInfoListFragment> mFragments;
    private ArrayList<String> mStartDates;
    private ArrayList<CheckedTextView> mViews;
    private int mLastIndex = INDEX_TODAY;

    public static OpenServerFragment newInstance(int type) {
        OpenServerFragment fragment = new OpenServerFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(KeyConfig.KEY_TYPE, type);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.fragment_server_info);
        tvBefore = getViewById(R.id.tv_before);
        tvYesterday = getViewById(R.id.tv_yesterday);
        tvToday = getViewById(R.id.tv_today);
        tvTomorrow = getViewById(R.id.tv_tomorrow);
        tvAfter = getViewById(R.id.tv_after);
        llAnchors = getViewById(R.id.ll_anchors);
    }

    @Override
    protected void setListener() {
        tvBefore.setOnClickListener(this);
        tvYesterday.setOnClickListener(this);
        tvToday.setOnClickListener(this);
        tvTomorrow.setOnClickListener(this);
        tvAfter.setOnClickListener(this);
        if (getActivity() instanceof ServerInfoActivity) {
            ((ServerInfoActivity) getActivity()).addListener(this);
        }
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        if (getArguments() == null) {
            ToastUtil.showShort(ConstString.TOAST_MISS_STATE);
            return;
        }
        mType = getArguments().getInt(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_OPEN_SERVER);

        mFragments = new ArrayList<>(INDEX_COUNT);
        mStartDates = new ArrayList<>(INDEX_COUNT);
        int[] offset = new int[]{-2, -1, 0, 1, 2, 0};
        for (int i = 0; i < INDEX_COUNT; i++) {
            mFragments.add(null);
            mStartDates.add(DateUtil.getDate("yyyy-MM-dd", offset[i]));
        }
        mViews = new ArrayList<>(INDEX_COUNT - 1);
        mViews.add(tvBefore);
        mViews.add(tvYesterday);
        mViews.add(tvToday);
        mViews.add(tvTomorrow);
        mViews.add(tvAfter);

        if(AssistantApp.getInstance().isReadAttention()) {
            handleCheckClick(INDEX_FOCUS);
        } else {
            handleCheckClick(INDEX_TODAY);
        }
    }

    @Override
    protected void lazyLoad() {

    }

    @Override
    public void doCallBack(Boolean data) {
        if (data) {
            if (AccountManager.getInstance().isLogin()) {
                handleCheckClick(INDEX_FOCUS);
            } else {
                // 对于没有登录的，提示先登录才能显示关注
                ToastUtil.showShort(ConstString.TOAST_LOGIN_FIRST);
                AssistantApp.getInstance().setIsReadAttention(false);
                if (getActivity() instanceof ServerInfoActivity) {
                    ((ServerInfoActivity) getActivity()).setTbState(false);
                }
            }
        } else {
            handleCheckClick(mLastIndex);
        }
    }

    @Override
    public void release() {
        super.release();
        if (getActivity() instanceof ServerInfoActivity) {
            ((ServerInfoActivity) getActivity()).removeListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.tv_before:
                handleCheckClick(INDEX_BEFORE);
                break;
            case R.id.tv_yesterday:
                handleCheckClick(INDEX_YESTERDAY);
                break;
            case R.id.tv_today:
                handleCheckClick(INDEX_TODAY);
                break;
            case R.id.tv_tomorrow:
                handleCheckClick(INDEX_TOMORROW);
                break;
            case R.id.tv_after:
                handleCheckClick(INDEX_AFTER);
                break;
        }
    }

    private void handleCheckClick(int index) {
        if (index != INDEX_FOCUS) {
            for (int i = 0; i < mViews.size(); i++) {
                mViews.get(i).setChecked(i == index);
            }
            mLastIndex = index;
        }
        if (mFragments.get(index) == null) {
            mFragments.set(index, ServerInfoListFragment.newInstance(mType, index == INDEX_FOCUS, mStartDates.get(index)));
        }
        replaceFrag(R.id.fl_content, mFragments.get(index), mStartDates.get(index) + index, false);
        if (llAnchors != null) {
            llAnchors.setVisibility((index == INDEX_FOCUS ? View.GONE : View.VISIBLE));
        }
    }

    public void replaceFrag(@IdRes int id, Fragment newFrag, String tag, boolean isAddToBackStack) {
        if (isRemoving()) {
            return;
        }
        // 查找特定tag的Fragment
        Fragment f = getChildFragmentManager().findFragmentByTag(tag);
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        if (f != null) {
            // 查找的tag已经存在，直接显示
            ft.show(f);
        } else {
            // 查找的tag不存在
            if (newFrag == null) {
                // 需要新添加的Fragment为null，不处理
                return;
            }
            if (newFrag.isAdded()) {
                // 需要新添加的Fragment已经添加，直接显示
                ft.show(newFrag);
            } else {
                // 没有添加，调用replace方法
                ft.replace(id, newFrag, tag);
            }
        }
        if (isAddToBackStack) {
            ft.addToBackStack(tag);
        }
        ft.commitAllowingStateLoss();
    }

    @Override
    public String getPageName() {
        return (mType == KeyConfig.TYPE_ID_OPEN_SERVER ? "开服表" : "开测表");
    }
}
