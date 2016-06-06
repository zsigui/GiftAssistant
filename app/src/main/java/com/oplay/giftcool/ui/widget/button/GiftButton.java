package com.oplay.giftcool.ui.widget.button;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.annotation.ColorRes;
import android.util.AttributeSet;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.util.GiftTypeUtil;

/**
 * 礼包按钮
 * <p/>
 * Created by zsigui on 16-1-5.
 */
public class GiftButton extends TextView {

    private boolean mBiggerButton;
    private CountDownTimer mTimer;

    private int mStatus;

    public GiftButton(Context context) {
        this(context, null);
    }

    public GiftButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GiftButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray t = context.obtainStyledAttributes(attrs, R.styleable.GiftButton, defStyleAttr, 0);
        mBiggerButton = t.getBoolean(R.styleable.GiftButton_gb_isBigger, false);
        t.recycle();
        setRedBg();
    }

    /**
     * 设置按钮状态
     */
    public void setState(int state) {
        mStatus = state;
        setEnabled(false);
        if (state >= GiftTypeUtil.TYPE_CHARGE_SEIZE) {
            setChargeCodeState(state);
        } else {
            setGiftState(state);
        }
    }

    /**
     * 设置礼包对应状态的显示信息
     */
    private void setGiftState(int state) {
        setOrangeBg();
        switch (state) {
            case GiftTypeUtil.TYPE_NORMAL_FINISHED:
            case GiftTypeUtil.TYPE_LIMIT_FINISHED:
                setText(R.string.st_gift_finished);
                break;
            case GiftTypeUtil.TYPE_NORMAL_WAIT_SEARCH:
                setText(R.string.st_gift_wait_search);
                break;
            case GiftTypeUtil.TYPE_NORMAL_WAIT_SEIZE:
            case GiftTypeUtil.TYPE_LIMIT_FREE_WAIT_SEIZE:
            case GiftTypeUtil.TYPE_LIMIT_WAIT_SEIZE:
                setText(R.string.st_gift_wait_seize);
                break;
            case GiftTypeUtil.TYPE_NORMAL_SEARCHED:
            case GiftTypeUtil.TYPE_NORMAL_SEARCH:
                setText(R.string.st_gift_search);
                setEnabled(true);
                break;
            case GiftTypeUtil.TYPE_LIMIT_SEIZED:
            case GiftTypeUtil.TYPE_NORMAL_SEIZED:
            case GiftTypeUtil.TYPE_LIMIT_FREE_SEIZED:
                setText(R.string.st_gift_seized);
                break;
            case GiftTypeUtil.TYPE_LIMIT_EMPTY:
            case GiftTypeUtil.TYPE_LIMIT_FREE_EMPTY:
                setText(R.string.st_gift_empty);
                break;
            case GiftTypeUtil.TYPE_NORMAL_SEIZE:
            case GiftTypeUtil.TYPE_LIMIT_SEIZE:
            case GiftTypeUtil.TYPE_LIMIT_FREE_SEIZE:
            default:
                setText(R.string.st_gift_seize);
                setRedBg();
                setEnabled(true);
                break;
        }
    }

    /**
     * 设置首充券对应状态的显示信息
     */
    private void setChargeCodeState(int state) {
        setGreenBg();
        switch (state) {
            case GiftTypeUtil.TYPE_CHARGE_TAKE:
                setText(R.string.st_gift_take);
                setRedBg();
                setEnabled(true);
                break;
            case GiftTypeUtil.TYPE_CHARGE_RESERVE_EMPTY:
//				setText(R.string.st_gift_reserve_empty);
//				break;
            case GiftTypeUtil.TYPE_CHARGE_UN_RESERVE:
                setText(R.string.st_gift_reserve);
                setEnabled(true);
                break;
            case GiftTypeUtil.TYPE_CHARGE_EMPTY:
                setText(R.string.st_gift_empty);
                break;
            case GiftTypeUtil.TYPE_CHARGE_RESERVED:
                setText(R.string.st_gift_reserved);
                break;
            case GiftTypeUtil.TYPE_CHARGE_SEIZED:
                setText(R.string.st_gift_seized);
                break;
            case GiftTypeUtil.TYPE_CHARGE_DISABLE_RESERVE:
                setText(R.string.st_gift_reserve_disabled);
                break;
            case GiftTypeUtil.TYPE_CHARGE_SEIZE:
            default:
                setText(R.string.st_gift_seize);
                setRedBg();
                setEnabled(true);
        }
    }

    private void setTextColorRes(@ColorRes int colorRes) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            setTextColor(getContext().getResources().getColorStateList(colorRes));
        } else {
            setTextColor(getContext().getResources().getColorStateList(colorRes, null));
        }
    }

    public int getStatus() {
        return mStatus;
    }

    private void setGreenBg() {
        if (mBiggerButton) {
            setBackgroundResource(R.drawable.selector_btn_bigger_green_border);
        } else {
            setBackgroundResource(R.drawable.selector_btn_green_border);
        }
        setTextColorRes(R.color.color_btn_green_border);
    }

    private void setOrangeBg() {
        if (mBiggerButton) {
            setBackgroundResource(R.drawable.selector_btn_bigger_orange);
        } else {
            setBackgroundResource(R.drawable.selector_btn_orange);
        }
        setTextColorRes(R.color.color_btn_orange_border);
    }

    private void setRedBg() {
        if (mBiggerButton) {
            setBackgroundResource(R.drawable.selector_btn_bigger_red);
        } else {
            setBackgroundResource(R.drawable.selector_btn_red);
        }
        setTextColorRes(R.color.color_btn_red_border);
    }
}
