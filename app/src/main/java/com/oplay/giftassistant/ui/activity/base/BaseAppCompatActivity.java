package com.oplay.giftassistant.ui.activity.base;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.oplay.giftassistant.AssistantApp;
import com.oplay.giftassistant.R;
import com.oplay.giftassistant.config.AppDebugConfig;
import com.oplay.giftassistant.listener.OnBackPressListener;
import com.oplay.giftassistant.ui.fragment.LoadingFragment;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment_WithName;
import com.oplay.giftassistant.ui.fragment.dialog.LoadingDialog;
import com.oplay.giftassistant.ui.widget.LoadAndRetryViewManager;
import com.socks.library.KLog;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/13
 */
public abstract class BaseAppCompatActivity extends BaseAppCompatActivityLog implements View.OnClickListener,
		FragmentManager.OnBackStackChangedListener {

	protected AssistantApp mApp;
	protected Toolbar mToolbar;
	private TextView tvTitle;

	private LoadingDialog mLoadingDialog;
	protected boolean mNeedWorkCallback = false;
	protected LoadingFragment mLoadingFragment;
	// 封装加载和等待等页面的管理器对象
	protected LoadAndRetryViewManager mViewManager;
	protected boolean mIsLoading;
	private Handler mHandler = new Handler(Looper.getMainLooper());

	// 保存当前栈顶对象
	private Fragment mCurTopFragment;
	// fragment处理onActivityResult
	private Fragment mFragmentForResult;
	private int mFragmentRequestCode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mApp = AssistantApp.getInstance();
		if (Build.VERSION.SDK_INT >= 21) {
			getWindow().setStatusBarColor(getResources().getColor(R.color.co_status_bar_bg));
		}
		getSupportFragmentManager().addOnBackStackChangedListener(this);
		initView();
		mToolbar = getViewById(R.id.toolbar);
		if (mToolbar != null) {
			setSupportActionBar(mToolbar);
			final View backIcon = mToolbar.findViewById(R.id.iv_bar_back);
			if (backIcon != null) {
				backIcon.setOnClickListener(this);
			}
			initMenu(mToolbar);
		}
		processLogic();
	}

	protected abstract void processLogic();

	/**
	 * this will be called before {@code super.onCreate()} when you override {@code onCreate()} method <br />
	 * Note: all views initial work have better implemented here
	 */
	protected abstract void initView();

	protected void initMenu(@NonNull Toolbar toolbar) {
	}

	@SuppressWarnings("unchecked")
	protected <V extends View> V getViewById(@IdRes int id) {
		if (mViewManager == null) {
			View child = findViewById(id);
			return (child != null ? (V) child : null);
		} else {
			return getViewById(mViewManager.getContentView(), id);
		}
	}

	@SuppressWarnings("unchecked")
	protected <V extends View> V getViewById(View v, @IdRes int id) {
		View child = v.findViewById(id);
		return (child != null ? (V) child : null);
	}

	public void setBarTitle(@StringRes int res) {
		setBarTitle(getResources().getString(res));
	}

	public void setBarTitle(String title) {
		if (mToolbar != null) {
			if (tvTitle == null) {
				tvTitle = getViewById(mToolbar, R.id.tv_bar_title);
			}
			if (tvTitle != null) {
				tvTitle.setText(title);
			}
		}
	}

	protected void initViewManger(@LayoutRes int layoutResID) {
		mViewManager = LoadAndRetryViewManager.generate(this, layoutResID);
		setContentView(mViewManager.getContainer());
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.iv_bar_back) {
			handleBackPressed();
		}
	}

	public void replaceFragWithTitle(@IdRes int id, BaseFragment_WithName newFrag, String title) {
		newFrag.setTitleName(title);
		replaceFrag(id, newFrag);
		setBarTitle(title);
	}

	public void replaceFragWithTitle(@IdRes int id, BaseFragment_WithName newFrag, String title,
	                                 boolean isAddToBackStack) {
		newFrag.setTitleName(title);
		replaceFrag(id, newFrag, isAddToBackStack);
		setBarTitle(title);
	}

	public void replaceFrag(@IdRes int id, Fragment newFrag) {
		replaceFrag(id, newFrag, newFrag.getClass().getSimpleName(), true);
	}

	public void replaceFrag(@IdRes int id, Fragment newFrag, boolean isAddToBackStack) {
		replaceFrag(id, newFrag, newFrag.getClass().getSimpleName(), isAddToBackStack);
	}

	public void replaceFrag(@IdRes int id, Fragment newFrag, String tag, boolean isAddToBackStack) {
		Fragment f = getSupportFragmentManager().findFragmentByTag(tag);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		if (f != null) {
			ft.show(f);
		} else {
			ft.replace(id, newFrag, tag);
		}
		if (isAddToBackStack) {
			ft.addToBackStack(tag);
		}
		ft.commitAllowingStateLoss();
	}

	/**
	 * 重新<code>attach</code>添加Fragment到视图资源ID处，会先<code>detach</code>该ID处已拥有的视图，
	 * 然后根据tag查找该Fragment是否已经存在且被add了，是则直接<code>attach</code>，否则执行<code>add<code/>，
	 * 最后执行<code>show</code>显示视图
	 *
	 * @param id      进行添加的资源ID名，会先判断是否已存在该ID下的Fragment，存在则先<code>Detach</code>
	 * @param newFrag 需要<code>attach</code>的新Fragment名
	 * @param tag     当Fragment此前未被<code>add<code/>，需要先进行添加设置的Tag
	 */
	public void reattachFrag(@IdRes int id, Fragment newFrag, String tag) {
		Fragment f = getSupportFragmentManager().findFragmentById(id);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		if (f != null && f == newFrag) {
			ft.attach(f);
		} else {
			if (f != null && !f.isDetached()) {
				ft.detach(f);
			}
			Fragment self_f = getSupportFragmentManager().findFragmentByTag(tag);
			if (self_f == null || !self_f.isAdded()) {
				ft.add(id, newFrag, tag);
			}
			ft.attach(newFrag);
		}
		ft.show(newFrag);
		ft.commitAllowingStateLoss();
	}

	/**
	 * 隐藏当前栈顶Fragment，
	 *
	 * @param id
	 * @param newFrag
	 * @param newTag
	 * @param oldTag
	 */
	public void reshowFrag(@IdRes int id, Fragment newFrag, String newTag, String oldTag) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		Fragment f = getSupportFragmentManager().findFragmentByTag(oldTag);
		if (f != null && !f.isHidden()) {
			ft.hide(f);
		}
		f = getSupportFragmentManager().findFragmentByTag(newTag);
		if (f != null) {
			ft.show(f);
		} else {
			ft.add(id, newFrag, newTag);
		}
		ft.commitAllowingStateLoss();
	}

	/**
	 * 显示加载中页面
	 *
	 * @param resId 加载位置资源ID
	 */
	protected void displayLoadingUI(@IdRes int resId) {
		if (mLoadingFragment == null) {
			mLoadingFragment = LoadingFragment.newInstance();
		}
		reattachFrag(resId, mLoadingFragment, LoadingFragment.class.getSimpleName());
	}

	/**
	 * 执行Fragment出栈操作，栈中有Fragment时返回true，否则返回false
	 */
	public boolean popFrag() {
		if (isFinishing()) {
			return false;
		}
		if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
			if (isMainThread()) {
				return getSupportFragmentManager().popBackStackImmediate();
			} else {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						getSupportFragmentManager().popBackStackImmediate();
					}
				});
			}
			return true;
		}
		return false;
	}

	/**
	 * 根据tag判断fragment是否处于栈顶
	 */
	public boolean isTopFragment(String tag) {
		return getTopFragment() != null && tag.equals(getTopFragment().getTag());
	}

	/**
	 * 获取当前栈顶Fragment
	 */
	public Fragment getTopFragment() {
		if (mCurTopFragment == null) {
			int count = getSupportFragmentManager().getBackStackEntryCount();
			if (count > 0) {
				mCurTopFragment = getSupportFragmentManager()
						.findFragmentByTag(getSupportFragmentManager().getBackStackEntryAt(count - 1).getName());
			}
		}
		return mCurTopFragment;
	}

	/**
	 * 执行fragment出栈 或者 activity终结操作
	 */
	public void handleBackPressed() {
		if (getTopFragment() != null && getTopFragment() instanceof OnBackPressListener
				&& ((OnBackPressListener) getTopFragment()).onBack()) {
			// back事件被处理
			return;
		}
		if (!popFrag() && !isFinishing()) {
			finish();
		} else {
			if (getTopFragment() instanceof BaseFragment_WithName) {
				setBarTitle(((BaseFragment_WithName) getTopFragment()).getTitleName());
			}
		}
	}

	/**
	 * 判断是否处于主线程
	 */
	private boolean isMainThread() {
		return Thread.currentThread() == getMainLooper().getThread();
	}

	@Override
	public void onBackPressed() {
		this.mNeedWorkCallback = false;
		handleBackPressed();
	}

	/**
	 * fragment发起需要处理返回信息时调用
	 *
	 * @param sponsor     发起的Fragment
	 * @param requestCode 请求码
	 * @param target      带有目标信息的Intent
	 */
	public void openPageForResult(BaseFragment sponsor, int requestCode, Intent target) {
		if (sponsor == null) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.d("openPageForResult get a null fragment");
			}
			return;
		}
		mFragmentForResult = sponsor;
		mFragmentRequestCode = requestCode;
		startActivityForResult(target, requestCode);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == mFragmentRequestCode && mFragmentForResult != null) {
			// 交由 Fragment 处理
			mFragmentForResult.onActivityResult(requestCode, resultCode, data);
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onBackStackChanged() {
		int count = getSupportFragmentManager().getBackStackEntryCount();
		if (count > 0) {
			mCurTopFragment = getSupportFragmentManager()
					.findFragmentByTag(getSupportFragmentManager().getBackStackEntryAt(count - 1).getName());
		}
	}

	public void showSuccessHint(final String title) {
		if (isMainThread()) {
			new SweetAlertDialog(BaseAppCompatActivity.this, SweetAlertDialog.SUCCESS_TYPE)
					.setTitleText(title)
					.show();
		} else {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					new SweetAlertDialog(BaseAppCompatActivity.this, SweetAlertDialog.SUCCESS_TYPE)
							.setTitleText(title)
							.show();
				}
			});
		}
	}

	public void showLoadingDialog() {
		showLoadingDialog(getResources().getString(R.string.st_view_loading_more));
	}

	public void showLoadingDialog(final String loadText) {
		if (isMainThread()) {
			if (mLoadingDialog == null) {
				mLoadingDialog = LoadingDialog.newInstance();
			}
			mLoadingDialog.setLoadText(loadText);
			mLoadingDialog.show(getSupportFragmentManager(), LoadingDialog.class.getSimpleName());
		} else {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					if (mLoadingDialog == null) {
						mLoadingDialog = LoadingDialog.newInstance();
					}
					mLoadingDialog.setLoadText(loadText);
					mLoadingDialog.show(getSupportFragmentManager(), LoadingDialog.class.getSimpleName());
				}
			});
		}
	}

	public void hideLoadingDialog() {
		if (mLoadingDialog != null) {
			mLoadingDialog.dismissAllowingStateLoss();
		}
	}
}
