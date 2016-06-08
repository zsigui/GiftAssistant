package com.oplay.giftcool.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.oplay.giftcool.R;
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
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        final ImageView iv1 = ViewUtil.getViewById(this, R.id.iv_arrow_1);
        ImageView iv2 = ViewUtil.getViewById(this, R.id.iv_arrow_2);
        if (iv1 != null && iv2 != null) {
//            ObjectAnimator trans1 = ObjectAnimator.ofFloat(iv1, "translationX", 0, 50);
//            ObjectAnimator fade1 = ObjectAnimator.ofFloat(iv1, "alpha", 1.0f, 0f);
//            ObjectAnimator trans2 = ObjectAnimator.ofFloat(iv2, "translationX", 0, 50);
//            ObjectAnimator fade2 = ObjectAnimator.ofFloat(iv2, "alpha", 1.0f, 0f);
//            trans1.setRepeatCount(Integer.MAX_VALUE);
//            fade1.setRepeatCount(Integer.MAX_VALUE);
//            trans1.setRepeatCount(ValueAnimator.RESTART);
//            fade1.setRepeatCount(ValueAnimator.RESTART);
//            trans2.setRepeatCount(Integer.MAX_VALUE);
//            fade2.setRepeatCount(Integer.MAX_VALUE);
//            trans2.setRepeatCount(ValueAnimator.RESTART);
//            fade2.setRepeatCount(ValueAnimator.RESTART);
//            trans1.setDuration(500);
//            fade2.setDuration(500);
//            trans2.setDuration(500);
//            fade2.setDuration(500);
//            trans2.setStartDelay(250);
//            fade2.setStartDelay(250);
//            Interpolator interpolator1 = new LinearInterpolator();
//            Interpolator interpolator2 = new Interpolator() {
//                @Override
//                public float getInterpolation(float input) {
//                    if (input == 0.5f || input == 0.9f)
//                        return input;
//                    return 1.0f;
//                }
//            };
//            final int duration = 500;
//            TranslateAnimation trans1 = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0,
//                    Animation.RELATIVE_TO_SELF, 2,
//                    Animation.RELATIVE_TO_SELF, 0,
//                    Animation.RELATIVE_TO_SELF, 0);
//            trans1.setDuration(duration);
//            trans1.setRepeatCount(-1);
//            trans1.setRepeatMode(Animation.RESTART);
//            trans1.setInterpolator(interpolator1);
//            AlphaAnimation alpha1 = new AlphaAnimation(1.0f, 0.0f);
//            alpha1.setDuration(duration);
//            alpha1.setRepeatCount(-1);
//            alpha1.setRepeatMode(Animation.RESTART);
//            alpha1.setInterpolator(interpolator1);
//            final AnimationSet animationSet = new AnimationSet(true);
//            animationSet.addAnimation(trans1);
//            animationSet.addAnimation(alpha1);
//
//            TranslateAnimation trans2 = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0,
//                    Animation.RELATIVE_TO_SELF, 2,
//                    Animation.RELATIVE_TO_SELF, 0,
//                    Animation.RELATIVE_TO_SELF, 0);
//            trans2.setDuration(duration);
//            trans2.setRepeatCount(-1);
//            trans2.setRepeatMode(Animation.RESTART);
//            trans2.setInterpolator(interpolator1);
//            trans2.setStartOffset(duration >> 1);
//            AlphaAnimation alpha2 = new AlphaAnimation(1.0f, 0.0f);
//            alpha2.setDuration(duration);
//            alpha2.setRepeatCount(-1);
//            alpha2.setRepeatMode(Animation.RESTART);
//            alpha2.setInterpolator(interpolator1);
//            alpha2.setStartOffset(duration >> 1);
//
//            animationSet.addAnimation(trans2);
//            animationSet.addAnimation(alpha2);
//            AnimationSet set = AnimationUtils.loadAnimation(getContext(), R.anim.arrow_run_1);
//            iv1.startAnimation(animationSet);
            Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.arrow_run_1);
            iv1.startAnimation(animation);
            Animation animation1 = AnimationUtils.loadAnimation(getContext(), R.anim.arrow_run_1);
            animation1.setStartOffset(250);
            iv1.startAnimation(animation1);
//            animationSet.setAnimationListener(new Animation.AnimationListener() {
//                @Override
//                public void onAnimationStart(Animation animation) {
//                    iv1.startAnimation(animationSet);
//                }
//
//                @Override
//                public void onAnimationEnd(Animation animation) {
//
//                }
//
//                @Override
//                public void onAnimationRepeat(Animation animation) {
//
//                }
//            });

//            trans1.setInterpolator(interpolator1);
//            fade1.setInterpolator(interpolator1);
//            trans2.setInterpolator(interpolator2);
//            fade2.setInterpolator(interpolator2);
//            trans1.start();
//            fade1.start();
//            trans2.start();
//            fade2.start();
//            AnimatorSet animatorSet = new AnimatorSet();
//            AnimatorSet animatorSet1 = new AnimatorSet();
//            animatorSet1.playTogether(trans1, fade1);
//            animatorSet1.setDuration(500);
//            animatorSet1.setInterpolator(interpolator1);
//            AnimatorSet animatorSet2 = new AnimatorSet();
//            animatorSet2.playTogether(trans2, fade2);
//            animatorSet2.setDuration(500);
//            animatorSet2.setInterpolator(interpolator2);
//            animatorSet2.setStartDelay(250);
//            animatorSet.playTogether(animatorSet1, animatorSet2);
//            animatorSet.start();

        }

    }
}
