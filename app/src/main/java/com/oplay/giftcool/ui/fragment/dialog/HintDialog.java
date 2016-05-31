package com.oplay.giftcool.ui.fragment.dialog;

import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Dialog;

/**
 * Created by zsigui on 16-1-12.
 */
public class HintDialog extends BaseFragment_Dialog {

    private TextView tvContent;
    private TextView tvHint;
    private String mContent;
    private String mHint;

    public static HintDialog newInstance() {
        return new HintDialog();
    }

    @Override
    protected void initView() {
        setContentView(R.layout.dialog_reserve_code);
        tvContent = getViewById(R.id.tv_content);
        tvHint = getViewById(R.id.tv_hint);
    }

    @Override
    protected void processLogic() {
        setContent(mContent);
        setHint(mHint);
    }

    public void setContent(String content) {
        mContent = content;
        if (tvContent != null) {
            tvContent.setText(mContent);
        }
    }

    public void setHint(String hint) {
        mHint = hint;
        if (tvHint != null) {
            tvHint.setText(mHint);
        }
    }
}
