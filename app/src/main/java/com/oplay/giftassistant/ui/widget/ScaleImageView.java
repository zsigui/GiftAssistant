package com.oplay.giftassistant.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.oplay.giftassistant.R;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/20
 */
public class ScaleImageView extends ImageView {

    private boolean mMeasureByHeight;
    private float mFraction = 1f;

    public ScaleImageView(Context context) {
        super(context);
    }

    public ScaleImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScaleImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray t = context.obtainStyledAttributes(attrs, R.styleable.ScaleView, defStyleAttr, 0);
        mMeasureByHeight = t.getBoolean(R.styleable.ScaleView_measureByHeight, false);
        mFraction = t.getFloat(R.styleable.ScaleView_fraction, mFraction);
        t.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (mMeasureByHeight) {
            width = (int) (height * mFraction);
        } else {
            height = (int) (width * mFraction);
        }

    }
}
