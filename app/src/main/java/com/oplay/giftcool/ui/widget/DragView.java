package com.oplay.giftcool.ui.widget;

import android.content.Context;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by zsigui on 16-8-16.
 */
public class DragView extends RelativeLayout {
    public DragView(Context context) {
        this(context, null);
    }

    public DragView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViewDragHelper();
    }

    public interface Orientation {
        int BOTH = 0;
        int HORIZONTAL_TOP = 1;
        int HORIZONTAL_BOTTOM = 2;
        int VERTICAL__LEFT = 3;
        int VERTICAL__RIGHT = 4;
    }

    private ViewDragHelper mDragHelper;
    private View mDragView;
    private int mDragOrientation = Orientation.BOTH;
    private int mDragViewX = -1;
    private int mDragViewY = -1;

    public void configDragView(View dragView, int dragOrientation) {
        mDragView = dragView;
        mDragOrientation = dragOrientation;
    }

    private void initDragXY() {
        if (mDragViewX != -1 && mDragViewY != -1) {
            return;
        }
        mDragViewY = (getHeight() - mDragView.getHeight()) >> 1;
        mDragViewX = getWidth() - mDragView.getWidth();
        switch (mDragOrientation) {
            case Orientation.HORIZONTAL_BOTTOM:
                mDragViewY = getHeight() - mDragView.getHeight();
                break;
            case Orientation.HORIZONTAL_TOP:
                mDragViewY = 0;
                break;
            case Orientation.VERTICAL__LEFT:
                mDragViewX = 0;
                break;
            case Orientation.VERTICAL__RIGHT:
                mDragViewX = getWidth() - mDragView.getWidth();
                break;
        }
    }

    private void initViewDragHelper() {
        mDragHelper = ViewDragHelper.create(this, 1.0f, new ViewDragCallBack());
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_DOWN:
                mDragHelper.cancel();
                break;
        }
        return mDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDragHelper.processTouchEvent(event);
        return true;
    }

    /**
     * 响应拖曳View的回调函数
     */
    private class ViewDragCallBack extends ViewDragHelper.Callback {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return mDragView != null && mDragView.getId() == child.getId();
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if (mDragOrientation == Orientation.VERTICAL__LEFT
                    || mDragOrientation == Orientation.VERTICAL__RIGHT) {
                // 对于垂直移动的情况，则X坐标固定，返回固定值
                return mDragViewX;
            }
            if (getPaddingLeft() > left) {
                return getPaddingLeft();
            }
            if (getWidth() - child.getWidth() < left) {
                return getWidth() - child.getWidth();
            }
            return left;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            if (mDragOrientation == Orientation.HORIZONTAL_TOP
                    || mDragOrientation == Orientation.HORIZONTAL_BOTTOM) {
                // 对于水平移动的情况，则Y坐标固定，返回固定值
                return mDragViewY;
            }
            if (getPaddingTop() > top) {
                return getPaddingTop();
            }
            if (getHeight() - child.getHeight() < top) {
                return getHeight() - child.getHeight();
            }
            return top;
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            initDragXY();
            return (getWidth() - child.getWidth());
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            initDragXY();
            return (getHeight() - child.getHeight());
        }
    }
}
