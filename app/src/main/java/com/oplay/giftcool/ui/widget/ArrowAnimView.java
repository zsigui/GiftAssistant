package com.oplay.giftcool.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.util.ThreadUtil;
import com.oplay.giftcool.util.ViewUtil;

/**
 * Created by zsigui on 16-6-8.
 */
public class ArrowAnimView extends FrameLayout {

    public ArrowAnimView(Context context) {
        this(context, null);
    }

    public ArrowAnimView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ArrowAnimView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        WIDTH = context.getResources().getDimensionPixelSize(R.dimen.di_list_item_gap_big);
        SMALL_WIDTH = context.getResources().getDimensionPixelSize(R.dimen.di_list_item_gap_small);
    }

    private ImageView iv1;
    private ImageView iv2;
    private int WIDTH;
    private int SMALL_WIDTH;
    private boolean mIsRun = false;

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        iv1 = ViewUtil.getViewById(this, R.id.iv_arrow_1);
        iv2 = ViewUtil.getViewById(this, R.id.iv_arrow_2);
    }

    public void start() {
        if (iv1 != null && iv2 != null) {
            final Animation animation1 = AnimationUtils.loadAnimation(getContext(), R.anim.arrow_run_1);
            final Animation animation2 = AnimationUtils.loadAnimation(getContext(), R.anim.arrow_run_2);
            final Runnable r1 = new Runnable() {
                @Override
                public void run() {
                    if (mIsRun) {
                        iv1.startAnimation(animation1);
                    }
                }
            };
            final Runnable r2 = new Runnable() {
                @Override
                public void run() {
                    if (mIsRun) {
                        iv2.startAnimation(animation2);
                    }
                }
            };
            animation2.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    iv2.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    iv2.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            animation1.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    iv1.setVisibility(View.INVISIBLE);
                    ThreadUtil.runOnUiThread(r2, 600);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    iv1.setVisibility(View.INVISIBLE);
                    ThreadUtil.runOnUiThread(r1, 900);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            iv1.startAnimation(animation1);
        }
    }

    public void setViewVisibility(boolean isShow) {
        if (iv1 != null && iv2 != null) {
            ViewGroup.LayoutParams lp = getLayoutParams();
            if (isShow) {
                iv1.setVisibility(View.VISIBLE);
                iv2.setVisibility(View.VISIBLE);
                lp.width = WIDTH;
                mIsRun = true;
                start();
            } else {
                iv1.setVisibility(View.INVISIBLE);
                iv2.setVisibility(View.INVISIBLE);
                lp.width = SMALL_WIDTH;
                mIsRun = false;
            }
            setLayoutParams(lp);
        }
    }
}
