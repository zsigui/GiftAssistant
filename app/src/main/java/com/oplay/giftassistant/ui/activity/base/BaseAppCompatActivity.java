package com.oplay.giftassistant.ui.activity.base;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.oplay.giftassistant.AssistantApp;
import com.oplay.giftassistant.R;
import com.oplay.giftassistant.ui.fragment.LoadingFragment;
import com.oplay.giftassistant.ui.widget.LoadAndRetryViewManager;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/13
 */
public abstract class BaseAppCompatActivity extends BaseAppCompatActivityLog implements View.OnClickListener{

    protected AssistantApp mApp;
    private SweetAlertDialog mLoadingDialog;
    private Toolbar mToolbar;
    protected boolean mNeedWorkCallback = false;
	protected LoadingFragment mLoadingFragment;
    // 封装加载和等待等页面的管理器对象
    protected LoadAndRetryViewManager mViewManager;
    protected boolean mIsLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApp = AssistantApp.getInstance();
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.co_status_bar_bg));
        }
        initView();
        mToolbar = getViewById(R.id.toolbar);
        if (mToolbar != null) {
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

    protected void initMenu(@NonNull Toolbar toolbar) {}

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
        return (child != null ? (V)child : null);
    }

    protected void setBarTitle(@StringRes int res) {
        if (mToolbar != null) {
            TextView tv = getViewById(mToolbar, R.id.tv_bar_title);
            if (tv != null) {
                tv.setText(res);
            }
        }
    }

    protected void setBarTitle(String title) {
        if (mToolbar != null) {
            TextView tv = getViewById(mToolbar, R.id.tv_bar_title);
            if (tv != null) {
                tv.setText(title);
            }
        }
    }

    protected void initViewManger(@LayoutRes int layoutResID) {
        mViewManager = LoadAndRetryViewManager.generate(this, layoutResID);
        setContentView(mViewManager.getContainer());
    }

    public void showLoadingDialog() {
        if (this.mLoadingDialog == null) {
            mLoadingDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
            mLoadingDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.red_btn_bg_color));
            mLoadingDialog.setCancelable(false);
            mLoadingDialog.setTitleText("数据加载中...");
        }
        mLoadingDialog.show();
    }

    public void hideLoadingDialog() {
        if (this.mLoadingDialog != null) {
            mLoadingDialog.dismiss();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_bar_back) {
            this.finish();
        }
    }

	protected void replaceFrag(@IdRes int id, Fragment newFrag) {
		reattachFrag(id, newFrag, newFrag.getClass().getSimpleName());
	}

	protected void replaceFrag(@IdRes int id, Fragment newFrag, String tag) {
		Fragment f = getSupportFragmentManager().findFragmentByTag(tag);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		if (f != null) {
			ft.show(f);
		} else {
			ft.replace(id, newFrag, tag);
		}
		ft.commitAllowingStateLoss();
	}

	/**
	 * 重新<code>attach</code>添加Fragment到视图资源ID处，会先<code>detach</code>该ID处已拥有的视图，
	 * 然后根据tag查找该Fragment是否已经存在且被add了，是则直接<code>attach</code>，否则执行<code>add<code/>，
	 * 最后执行<code>show</code>显示视图
	 *
	 * @param id 进行添加的资源ID名，会先判断是否已存在该ID下的Fragment，存在则先<code>Detach</code>
	 * @param newFrag 需要<code>attach</code>的新Fragment名
	 * @param tag 当Fragment此前未被<code>add<code/>，需要先进行添加设置的Tag
	 */
	protected void reattachFrag(@IdRes int id, Fragment newFrag, String tag) {
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

	@Override
	public void onBackPressed() {
        this.mNeedWorkCallback = false;
		super.onBackPressed();
		this.finish();
	}
}
