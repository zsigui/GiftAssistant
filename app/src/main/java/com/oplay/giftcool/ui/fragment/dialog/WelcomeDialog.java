package com.oplay.giftcool.ui.fragment.dialog;

import android.support.annotation.LayoutRes;
import android.text.Html;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Dialog;

import java.util.Locale;

/**
 * Created by zsigui on 16-1-22.
 */
public class WelcomeDialog extends BaseFragment_Dialog {

    private TextView tvScore;
    private int mScore;
    private int mPercent;
    private String mTitleHint;
    private TextView tvPercent;
    private TextView tvTitleHint;

    public static WelcomeDialog newInstance(@LayoutRes int resId) {
        WelcomeDialog dialog = new WelcomeDialog();
        dialog.setContentView(resId);
        return dialog;
    }

    @Override
    protected void initView() {
        tvScore = getViewById(R.id.tv_score);
        tvPercent = getViewById(R.id.tv_percent);
    }

    @Override
    protected void processLogic() {
        setScore(mScore);
        setPercent(mPercent);
        setTitleHint(mTitleHint);
    }

    @Override
    protected void setContentView(@LayoutRes int layoutId) {
        mContentView = LayoutInflater.from(AssistantApp.getInstance().getApplicationContext())
                .inflate(layoutId, null);
    }

    public void setScore(int score) {
        mScore = score;
        if (tvScore != null) {
            tvScore.setText(String.valueOf(score));
        }
    }

    public void setPercent(int percent) {
        mPercent = percent;
        if (tvPercent != null) {
            tvPercent.setText(String.format(Locale.CHINA, "%d%%", percent));
        }
    }

    public void setTitleHint(String titleHint) {
        mTitleHint = titleHint;
        if (tvTitleHint != null) {
            tvTitleHint.setText(Html.fromHtml(titleHint));
        }
    }
}
