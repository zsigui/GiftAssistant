package com.oplay.giftassistant.ui.fragment.base;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.oplay.giftassistant.AssistantApp;
import com.oplay.giftassistant.R;
import com.oplay.giftassistant.manager.ObserverManager;
import com.oplay.giftassistant.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftassistant.ui.widget.LoadAndRetryViewManager;
import com.oplay.giftassistant.util.IntentUtil;
import com.oplay.giftassistant.util.ToastUtil;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/13
 */
public abstract class BaseFragment extends BaseFragmentLog implements View.OnClickListener,
		ObserverManager.UserUpdateListener, ObserverManager.GiftUpdateListener{
	protected String TAG;
	protected AssistantApp mApp;
	protected View mContentView;
	protected BaseAppCompatActivity mActivity;
	// 用于判断是否初始化结束
	protected boolean mIsPrepared = false;
	// 用于判断是否已经处于加载中
	protected boolean mIsLoading = false;
	// 用于判断是否可以显示UI，避免延迟加载而Activity已经finished出现崩溃
	protected boolean mCanShowUI = false;
	// 用于避免重复加载
	protected boolean mHasData = false;
	// 封装加载和等待等页面的管理器对象
	protected LoadAndRetryViewManager mViewManager;
	private String mFragName;
	protected Handler mHandler = new Handler(Looper.myLooper());
	// 是否处于刷新页面请求中
	protected boolean mIsSwipeRefresh = false;
	protected boolean mIsNotifyRefresh = false;
	// Fragment标题
	private String mTitleName;

	public String getTitleName() {
		return mTitleName;
	}

	public void setTitleName(String titleName) {
		mTitleName = titleName;
	}

	protected LoadAndRetryViewManager.OnRetryListener mRetryListener = new LoadAndRetryViewManager.OnRetryListener() {
		@Override
		public void onRetry(View retryView) {
			View iv = getViewById(retryView, R.id.v_err);
			iv.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mHasData = false;
					lazyLoad();
				}
			});
			View tv = getViewById(retryView, R.id.v_wifi);
			tv.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					IntentUtil.jumpWifiSetting(getContext());
				}
			});

		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mApp = AssistantApp.getInstance();
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		TAG = this.getClass().getSimpleName();
		mActivity = (BaseAppCompatActivity) context;
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser) {
			onUserVisible();
		} else {
			onUserInvisible();
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

			if (mViewManager != null) {
				mViewManager.setOnRetryListener(mRetryListener);
			}
			setListener();
			processLogic(savedInstanceState);
		}
		mIsPrepared = true;
		mCanShowUI = true;
		lazyLoad();
	}

	protected void initViewManger(@LayoutRes int layoutResID) {
		mViewManager = LoadAndRetryViewManager.generate(getContext(), layoutResID);
		mContentView = mViewManager.getContainer();
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


	protected void refreshInitConfig() {
		mIsLoading = true;
		if (!mIsSwipeRefresh && !mIsNotifyRefresh) {
			if (mViewManager != null) {
				mViewManager.showLoading();
			}
			mHasData = false;
		}
	}

	protected void refreshFailEnd() {
		if (!mIsSwipeRefresh && !mIsNotifyRefresh) {
			if (mViewManager != null) {
				mViewManager.showErrorRetry();
			}
			mHasData = false;
		}
		mIsLoading = mIsSwipeRefresh = mIsNotifyRefresh = false;
	}

	protected void refreshSuccessEnd() {
		if (mViewManager != null) {
			mViewManager.showContent();
		}
		mHasData = true;
		mIsLoading = mIsSwipeRefresh = mIsNotifyRefresh = false;
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
	protected void onUserInvisible() {
	}

	;

	/**
	 * 查找View
	 *
	 * @param id   控件的id
	 * @param <VT> View类型
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected <VT extends View> VT getViewById(@IdRes int id) {
		if (mViewManager != null && mViewManager.getContentView() != null) {
			return getViewById(mViewManager.getContentView(), id);
		}
		return (VT) mContentView.findViewById(id);
	}

	@SuppressWarnings("unchecked")
	protected <VT extends View> VT getViewById(View contentView, @IdRes int id) {
		return (VT) contentView.findViewById(id);
	}

	protected void showToast(String text) {
		ToastUtil.showShort(text);
	}


	@Override
	public void onDestroyView() {
		mCanShowUI = false;
		super.onDestroyView();
		ObserverManager.getInstance().removeGiftUpdateListener(this);
		ObserverManager.getInstance().removeUserUpdateListener(this);
	}

	@Override
	public void onClick(View v) {

	}

	@Override
	public void onGiftUpdate() {
		if (mIsSwipeRefresh || mIsNotifyRefresh) {
			return;
		}
		mIsNotifyRefresh = true;
		lazyLoad();
	}

	@Override
	public void onUserUpdate() {

	}
}
