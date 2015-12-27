package com.oplay.giftassistant.ui.fragment.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.oplay.giftassistant.AssistantApp;
import com.oplay.giftassistant.ui.activity.base.BaseAppCompatActivity;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/13
 */
public abstract class BaseFragment extends BaseFragmentLog {
    protected String TAG;
    protected AssistantApp mApp;
    protected View mContentView;
    protected BaseAppCompatActivity mActivity;
	// 用于判断是否初始化结束
	protected boolean mIsPrepared = false;
	// 用于判断是否已经处于加载中
	protected boolean mIsLoading = false;
	// 用于避免重复加载
	protected boolean mHasData = false;
	private String mFragName;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        TAG = this.getClass().getSimpleName();
        mApp = AssistantApp.getInstance();
        mActivity = (BaseAppCompatActivity) context;
    }

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser) {
			onUserVisible();
		} else {
			onUserInVisible();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// 避免多次从xml中加载布局文件
		if (mContentView == null) {
			initView(savedInstanceState);
		} else {
			ViewGroup parent = (ViewGroup) mContentView.getParent();
			if (parent != null) {
				parent.removeView(mContentView);
			}
		}
		return mContentView;
	}

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!mIsPrepared) {
            setListener();
            processLogic(savedInstanceState);
        }
        mIsPrepared = true;
        lazyLoad();
    }

    protected void setContentView(@LayoutRes int layoutResID) {
		mContentView = LayoutInflater.from(mApp).inflate(layoutResID, null);
	}

	public String getFragName() {
		return mFragName;
	}

	public void setFragName(String fragName) {
		mFragName = fragName;
	}

	/**
	 * 初始化View控件
	 */
	protected abstract void initView(Bundle savedInstanceState);

	/**
	 * 给View控件添加事件监听器，在initView方法后执行
	 */
	protected abstract void setListener();

	/**
	 * 处理业务逻辑，状态恢复等操作，在setListener方法后执行
	 */
	protected abstract void processLogic(Bundle savedInstanceState);

	/**
	 * 可在此方法内重写加载网络数据
	 */
	protected abstract void lazyLoad();

	/**
	 * fragment可见的情况下，会调用该方法，默认进行初始化等判断和执行懒加载方法
	 */
	protected void onUserVisible() {
		if (!mIsPrepared || mIsLoading || mHasData) {
			return;
		}
		lazyLoad();
	}

	/**
	 * 当fragment不可见时，会调用该方法，可重载实现网络中断任务操作
	 */
	protected void onUserInVisible(){};

	/**
	 * 查找View
	 *
	 * @param id   控件的id
	 * @param <VT> View类型
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected <VT extends View> VT getViewById(@IdRes int id) {
		return (VT) mContentView.findViewById(id);
	}

    @SuppressWarnings("unchecked")
    protected <VT extends View> VT getViewById(View contentView, @IdRes int id) {
        return (VT) contentView.findViewById(id);
    }

	protected void showToast(String text) {
		Toast.makeText(mActivity, text, Toast.LENGTH_SHORT).show();
	}

	protected void showLoadingDialog() {
		mActivity.showLoadingDialog();
	}

	protected void dismissLoadingDialog() {
		if (isVisible()) {
			mActivity.hideLoadingDialog();
		}
	}
}
