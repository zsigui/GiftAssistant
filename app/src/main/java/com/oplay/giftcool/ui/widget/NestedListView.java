package com.oplay.giftcool.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * 此View保证在ScrollView中可以嵌套ListView <br />
 *
 * 原理：重写onMeasure()自测高度，ScrollView需要调用 scrollView.scrollSmoothTo(0, 0)到顶端
 *
 * @author JackieZhuang
 * @email zsigui@foxmail.com
 * @date 2015/12/27
 */
public class NestedListView extends ListView{

    public NestedListView(Context context) {
        super(context);
    }

    public NestedListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NestedListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expendSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expendSpec);
    }
}
