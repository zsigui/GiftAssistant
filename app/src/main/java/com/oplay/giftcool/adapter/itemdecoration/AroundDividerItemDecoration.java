package com.oplay.giftcool.adapter.itemdecoration;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * 四周分割线的装饰器
 * <p>
 * Created by zsigui on 16-4-6.
 */
public class AroundDividerItemDecoration extends DividerItemDecoration {


    public AroundDividerItemDecoration(Context context, int orientation) {
        super(context, orientation);
    }

    public AroundDividerItemDecoration(Context context, int orientation, int dividerColor, int dividerSize) {
        super(context, orientation, dividerColor, dividerSize);
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        final int childSize = parent.getChildCount();
        for (int pos = 0; pos < childSize; pos++) {
            View child = parent.getChildAt(pos);
            RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) child.getLayoutParams();
            if (mOrientation == LinearLayoutManager.VERTICAL) {
                // 画两边跟底部3条线
                drawLeft(c, child, lp);
                drawRight(c, child, lp);
                drawBottom(c, child, lp);
                if (pos == 0) {
                    // 画多一条顶部线
                    drawTop(c, child, lp);
                }
            } else {
                // 画上下跟右边的3条线
                drawRight(c, child, lp);
                drawBottom(c, child, lp);
                drawTop(c, child, lp);
                if (pos == 0) {
                    drawLeft(c, child, lp);
                }
            }
        }
    }

    /**
     * 绘制表格项的上分割线
     */
    private void drawTop(Canvas c, View child, RecyclerView.LayoutParams lp) {
        final int left = child.getLeft() - lp.leftMargin;
        final int right = child.getRight() + lp.rightMargin;
        final int bottom = child.getTop() - lp.topMargin;
        final int top = bottom - mDividerSize;
        c.drawRect(left, top, right, bottom, mPaint);
    }

    /**
     * 绘制表格项的下分割线
     */
    private void drawBottom(Canvas c, View child, RecyclerView.LayoutParams lp) {
        final int left = child.getLeft() - lp.leftMargin;
        final int right = child.getRight() + lp.rightMargin;
        final int top = child.getBottom() + lp.bottomMargin;
        final int bottom = top + mDividerSize;
        c.drawRect(left, top, right, bottom, mPaint);
    }

    /**
     * 绘制表格项的左分割线
     */
    private void drawLeft(Canvas c, View child, RecyclerView.LayoutParams lp) {
        final int right = child.getLeft() - lp.leftMargin;
        final int left = right - mDividerSize;
        final int top = child.getTop() - lp.topMargin - mDividerSize;
        final int bottom = child.getBottom() + lp.bottomMargin + mDividerSize;
        c.drawRect(left, top, right, bottom, mPaint);
    }

    /**
     * 绘制表格项的右分割线
     */
    private void drawRight(Canvas c, View child, RecyclerView.LayoutParams lp) {
        final int right = child.getLeft() - lp.leftMargin;
        final int left = right - mDividerSize;
        final int top = child.getTop() - lp.topMargin - mDividerSize;
        final int bottom = child.getBottom() + lp.bottomMargin + mDividerSize;
        c.drawRect(left, top, right, bottom, mPaint);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (view.getLayoutParams() == null) {
            return;
        }
        int position = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewAdapterPosition();
        if (position == 0) {
            outRect.set(mDividerSize, mDividerSize, mDividerSize, mDividerSize);
        } else {
            if (mOrientation == LinearLayoutManager.VERTICAL) {
                outRect.set(mDividerSize, 0, mDividerSize, mDividerSize);
            } else {
                outRect.set(0, mDividerSize, mDividerSize, mDividerSize);
            }
        }
    }
}
