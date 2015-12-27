package com.oplay.giftassistant.ui.widget.ShapeLayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

import com.oplay.giftassistant.R;
import com.socks.library.KLog;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/26
 */
public class RoundedLayout extends ShapeLayout {

    private static final float DEFAULT_FLOAT = 0f;

    private float mRadius;

    public RoundedLayout(Context context) {
        this(context, null);
    }

    public RoundedLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundedLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);


      TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RoundedLayout, defStyleAttr, 0);

        mRadius = ta.getDimension(R.styleable.RoundedLayout_rl_radius, DEFAULT_FLOAT);
        ta.recycle();
        mRadius = dpToPx(getResources().getDisplayMetrics(), (int) mRadius);
    }

    @Override
    protected void drawPath(Path path, int srcWidth, int srcHeight, float scale, float translateX, float translateY) {
        RectF rectF = new RectF(0, 0, srcWidth, srcHeight);
        KLog.d(mRadius);
        path.addRoundRect(rectF, mRadius, mRadius, Path.Direction.CW);
    }

}
