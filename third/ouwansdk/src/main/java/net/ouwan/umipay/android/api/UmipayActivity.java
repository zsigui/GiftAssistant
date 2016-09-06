package net.ouwan.umipay.android.api;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import net.ouwan.umipay.android.Utils.Util_Resource;
import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.entry.UmipayAccount;
import net.ouwan.umipay.android.entry.UmipayCommonAccount;
import net.ouwan.umipay.android.fragment.BaseFragment;
import net.ouwan.umipay.android.fragment.ChangeAccountFragment;
import net.ouwan.umipay.android.fragment.SelectCommonAccountFragment;
import net.ouwan.umipay.android.manager.UmipayAccountManager;
import net.ouwan.umipay.android.manager.UmipayCommonAccountCacheManager;

import java.util.ArrayList;

public class UmipayActivity extends FragmentActivity implements FragmentNavigationDelegate {
    private android.support.v4.app.FragmentManager mFragmentManager;
    private View mRootLayout;

    private static final String ACTION_RESTART_CHANGE_ACCOUNT = "change_account";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(Util_Resource.getIdByReflection(this, "layout",
                "umipay_main_dialog"));
        mRootLayout = findViewById(Util_Resource.getIdByReflection(this, "id",
                "umipay_main_content_rl"));
        mFragmentManager = getSupportFragmentManager();
        setDefaultFragment();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void setDefaultFragment() {

        Intent data = getIntent();


        if (data != null && ACTION_RESTART_CHANGE_ACCOUNT.equals(data.getAction())) {
            replaceFragmentToActivityFragmentManager(ChangeAccountFragment.newInstance());
            return;
        } else {
            ArrayList<UmipayCommonAccount> mCommonAccountList = UmipayCommonAccountCacheManager.getInstance
                    (getApplicationContext()).getCommonAccountListExceptWithPacakge(
                    UmipayCommonAccountCacheManager.COMMON_ACCOUNT, getPackageName());
            UmipayAccount account = UmipayAccountManager.getInstance(this).getCurrentAccount();
            if (mCommonAccountList != null && mCommonAccountList.size() > 0
                    && account == null) {
                replaceFragmentToActivityFragmentManager(SelectCommonAccountFragment.newInstance());
                return;
            }
        }
        finish();
    }


    public static void showChangeAccountDialog(Context context) {
        Intent intent = new Intent(context, UmipayActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        intent.setAction(ACTION_RESTART_CHANGE_ACCOUNT);
        context.startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (getTopFragment() != null) {
            getTopFragment().onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (getTopFragment() != null) {
            ((BaseFragment) getTopFragment()).onWindowFocusChanged(hasFocus);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (MotionEvent.ACTION_DOWN == event.getAction()) {
            final int x = (int) event.getX();
            final int y = (int) event.getY();
            final int slop = ViewConfiguration.get(UmipayActivity.this).getScaledWindowTouchSlop();
            final View decorView = UmipayActivity.this.getWindow().getDecorView();
            if ((x < -slop) || (y < -slop) || (x > (decorView.getWidth() + slop)) || (y > (decorView.getHeight() +
                    slop))) {
                //点击外部隐藏键盘，不关闭activity
                closeInputMethod();
                return true;
            }
        }
        return super.onTouchEvent(event);
    }

    /**
     * 关闭输入法
     */
    private void closeInputMethod() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null && imm.isActive()) {
                imm.hideSoftInputFromWindow(UmipayActivity.this.getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
        } catch (Throwable e) {
            Debug_Log.e(e);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        //横竖屏切换时重新调整布局大小
        resize();
        if (getTopFragment() != null) {
            ((BaseFragment) getTopFragment()).onConfigurationChanged(config);
        }
    }

    /**
     * 重新调整布局大小
     */
    protected void resize() {
        try {
            //直接获取dimens对应的width和marginLeft，更新横竖屏切换后布局的大小
            int width = (int) getResources().getDimension(Util_Resource.getIdByReflection(this, "dimen",
                    "umipay_main_diglog_width"));
            int paddingLeft = (int) getResources().getDimension(Util_Resource.getIdByReflection(this, "dimen",
                    "umipay_main_diglog_marginLeft"));
            int paddingRight = (int) getResources().getDimension(Util_Resource.getIdByReflection(this, "dimen",
                    "umipay_main_diglog_marginRight"));
            if (mRootLayout != null) {
                ViewGroup.LayoutParams lp = mRootLayout.getLayoutParams();
                lp.width = width;
                int paddingTop = mRootLayout.getPaddingTop();
                int paddingBottom = mRootLayout.getPaddingBottom();
                mRootLayout.setPadding(paddingLeft, paddingTop, paddingRight,
                        paddingBottom);
                mRootLayout.setLayoutParams(lp);
                mRootLayout.invalidate();
            }
        } catch (Throwable e) {
            Debug_Log.e(e);
        }
    }

    @Override
    public void replaceFragmentToActivityFragmentManager(final BaseFragment fragment) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mFragmentManager != null && fragment != null) {
                    try {
                        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                        final int backStackCount = getSupportFragmentManager().getBackStackEntryCount();
                        if (backStackCount > 0) {
                            ft.setCustomAnimations(Util_Resource.getIdByReflection(UmipayActivity.this, "anim",
                                    "umipay_slide_left_in"), Util_Resource.getIdByReflection(UmipayActivity.this, "anim",

                                    "umipay_slide_right_out"));
                        }
                        ft.replace(Util_Resource.getIdByReflection(UmipayActivity.this, "id", "umipay_main_content"), fragment).addToBackStack(null);
                        ft.commit();
                    } catch (Throwable e) {
                        Debug_Log.e(e);
                    }
                }
            }
        });
    }

    @Override
    public Fragment getTopFragment() {
        return getSupportFragmentManager().findFragmentById(Util_Resource.getIdByReflection(UmipayActivity.this, "id", "umipay_main_content"));
    }

}
