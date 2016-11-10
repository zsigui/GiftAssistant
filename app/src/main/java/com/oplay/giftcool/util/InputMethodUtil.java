package com.oplay.giftcool.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.oplay.giftcool.AssistantApp;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/21
 */
public class InputMethodUtil {

    public static void toggleSoftInput(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static boolean showSoftInput(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        return imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
    }

    public static boolean showSoftInput(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            final boolean result = imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
            AssistantApp.getInstance().getSoftInputHeight(activity);
            return result;

        }
        return false;
    }

    public static boolean hideSoftInput(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        return imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static boolean hideSoftInput(Activity activity) {
        if (activity.getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            return imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
        return false;
    }

    public static boolean isActive(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        return imm.isActive();
    }

    public static boolean isActive(View v) {
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        return imm.isActive(v);
    }

    /**
     * 获取软键盘的高度
     */
    public static int getSoftInputHeight(Activity activity) {
        Rect rect = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        final int screenHeight = activity.getWindow().getDecorView().getRootView().getHeight();
        int softInputHeight = screenHeight - rect.bottom;
        if (Build.VERSION.SDK_INT >= 18) {
            softInputHeight = softInputHeight - getSoftButtonsBarHeight(activity);
        }
        return softInputHeight;
    }

    /**
     * 获取可能存在的虚拟按键高度
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private static int getSoftButtonsBarHeight(Activity activity) {
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int usableHeight = dm.heightPixels;
        activity.getWindowManager().getDefaultDisplay().getRealMetrics(dm);
        int realHeight = dm.heightPixels;
        if (realHeight > usableHeight) {
            return realHeight - usableHeight;
        } else {
            return 0;
        }
    }

    /**
     * 根据指定根视图的可见区域变化来判断是否弹出了输入框
     */
    public static boolean isKeyBoardShown(View rootView) {
        final int softKeyboardHeight = 100;
        Rect r = new Rect();
        rootView.getWindowVisibleDisplayFrame(r);
        DisplayMetrics dm = rootView.getResources().getDisplayMetrics();
        int heightDiff = rootView.getBottom() - r.bottom;
        return heightDiff > softKeyboardHeight * dm.density;
    }
}
