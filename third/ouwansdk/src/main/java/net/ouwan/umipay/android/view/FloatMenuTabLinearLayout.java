package net.ouwan.umipay.android.view;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import net.ouwan.umipay.android.Utils.Util_Resource;
import net.ouwan.umipay.android.debug.Debug_Log;
import net.youmi.android.libs.common.util.Util_System_Display;

/**
 * Created by mink on 2015/11/18.
 */
public class FloatMenuTabLinearLayout extends LinearLayout {

    private boolean mIsMirror = false;
    private Context mContext;
    private OnClickListener mOnClickListener;



    public FloatMenuTabLinearLayout(Context context, OnClickListener onClickListner) {
        super(context);
        mContext = context;
        mOnClickListener = onClickListner;
        initResource();
    }

    private void initResource() {
        if (mContext == null) {
            return;
        }
        try {
            LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.CENTER_VERTICAL;
            setLayoutParams(lp);
            setOrientation(HORIZONTAL);
            setBackgroundResource(Util_Resource.getIdByReflection(mContext, "drawable", "umipay_tab_bg_mid"));
            for (int type : FloatMenuTabButton.BTNTYPE) {
                FloatMenuTabButton v = new FloatMenuTabButton(mContext, type);
                if (v != null) {
                    v.setOnClickListener(mOnClickListener);
                    addView(v);
                }
            }


        } catch (Throwable e) {
            Debug_Log.e("悬浮窗菜单初始化失败！");
            Debug_Log.e(e);
        }
    }

    public void setMirror(boolean isMirror) {
        if (mIsMirror == isMirror || mContext == null) {
            return;
        }
        try {
            mIsMirror = isMirror;
	        update();
        }catch (Throwable e){
            Debug_Log.e(e);
        }
    }

    public void update() {

        for (int i = 0,visibleView = 0; i < getChildCount(); i++) {
	        int index = (mIsMirror) ? (getChildCount() - 1 - i) : i;//实际显示在左边的第一个
	        View v = getChildAt(index);
            if (v instanceof FloatMenuTabButton) {
	            ((FloatMenuTabButton) v).update();
	            if(v.getVisibility() == VISIBLE){
		            LayoutParams lp = (LayoutParams) v.getLayoutParams();
		            if(visibleView == 0 ){
			            lp.setMargins(Util_System_Display.dip2px(mContext, 6),0,0,0);
		            }else{
			            lp.setMargins(Util_System_Display.dip2px(mContext, 15),0,0,0);
		            }
		            v.setLayoutParams(lp);
		            visibleView++;
	            }
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // TODO Auto-generated method stub
        final int height = getHeight();
        final int count = getChildCount();

        int paddingTop = getPaddingTop();
        int paddingLeft = getPaddingLeft();
        int paddingBottom = getPaddingBottom();

        int childTop = paddingTop;
        int childLeft = 0;
        int childSpace = height - paddingTop - paddingBottom;


        try {
            for (int i = 0, visibleView = 0; i < count; i++) {
                int index = (mIsMirror) ? (count - 1 - i) : i;
                final View child = getChildAt(index);

                if (child == null) {
                    continue;
                } else if (child.getVisibility() != GONE) {
                    //只显示FloatMenuTabButton
                    final int childWidth = child.getMeasuredWidth();
                    final int childHeight = child.getMeasuredHeight();
                    final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                    //直接居中处理
                    childTop = paddingTop + ((childSpace - childHeight) / 2) + lp.topMargin - lp.bottomMargin;

	                childLeft += lp.leftMargin;

                    child.layout(childLeft, childTop, childLeft+childWidth, childTop+childHeight);

                    childLeft += childWidth + lp.rightMargin;

//                    if (child instanceof FloatMenuTabButton) {
//                        int visiblity = (visibleView == 0) ? GONE : VISIBLE;
//                        ((FloatMenuTabButton) child).setDividerVisibility(visiblity);
//                    }
//
                    visibleView++;
                }
            }
        }catch (Throwable e){
            Debug_Log.e(e);
        }
    }
}
