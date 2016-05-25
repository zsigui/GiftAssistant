package com.oplay.giftcool.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by zsigui on 16-1-29.
 */
public class DeletedTextView extends TextView {

	private Paint paint;

	private boolean showDeletedLine = false;

	public DeletedTextView(Context context) {
		this(context, null);
	}

	public DeletedTextView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DeletedTextView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
//		setPaint(context.getResources().getColor(R.color.co_common_app_main_bg), 1);
	}

	public void setPaint(int color, int strokeWidth) {
		//删除线的颜色和样式
		paint = new Paint();
		paint.setColor(color);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setStrokeWidth(strokeWidth);
		showDeletedLine = true;
		invalidate();
	}

	public void setShowDeletedLine(boolean showDeletedLine) {
		this.showDeletedLine = showDeletedLine;
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
        if (showDeletedLine) {
            //TextView布局的高度和宽度
            float x = this.getWidth();
            float y = this.getHeight();
            //根据Textview的高度和宽度设置删除线的位置
            //四个参数的意思：起始x的位置，起始y的位置，终点x的位置，终点y的位置
            //super最后调用表示删除线在位于文字的上边
            //super方法先调用删除线不显示
            canvas.drawLine(0f, y / 2f, x, y / 2f, paint);
        }
		super.dispatchDraw(canvas);
	}

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
    }


}
