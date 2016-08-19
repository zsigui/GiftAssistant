package com.oplay.giftcool.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.util.ViewUtil;

/**
 * Created by zsigui on 16-6-8.
 */
public class ClockAnimView extends FrameLayout {

    private ImageView iv;
    private RotateAnimation animation;

    public ClockAnimView(Context context) {
        this(context, null);
    }

    public ClockAnimView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClockAnimView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        iv = ViewUtil.getViewById(this, R.id.iv_pointer);
        startAnim();
    }

    private void startAnim() {
        if (animation == null) {
            animation = new RotateAnimation(0, 360,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.9f);
            animation.setInterpolator(new LinearInterpolator());
            animation.setDuration(1000);
            animation.setRepeatCount(Integer.MAX_VALUE);
            animation.setRepeatMode(Animation.RESTART);
        }
        iv.startAnimation(animation);
    }

    private void stopAnim() {
        if (animation != null) {
            animation.cancel();
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == View.VISIBLE) {
            startAnim();
        } else {
            stopAnim();
        }
    }
}
