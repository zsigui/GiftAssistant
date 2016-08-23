package com.oplay.giftcool.ui.fragment.dialog;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.manager.PayManager;
import com.oplay.giftcool.model.data.resp.message.AwardNotify;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Dialog;
import com.oplay.giftcool.util.ToastUtil;
import com.oplay.giftcool.util.ViewUtil;

/**
 * Created by zsigui on 16-8-22.
 */
public class AwardDialog extends BaseFragment_Dialog implements BaseFragment_Dialog.OnDialogClickListener {

    private TextView tvContent;
    private ImageView ivIcon;
    private AwardNotify mData;
    private FragmentManager fm;

    public static AwardDialog newInstance(AwardNotify data) {
        AwardDialog dialog = new AwardDialog();
        Bundle bundle = new Bundle();
        bundle.putSerializable(KeyConfig.KEY_DATA, data);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    protected void initView() {
        setContentView(R.layout.dialog_show_award);
        tvContent = getViewById(R.id.tv_content);
        ivIcon = getViewById(R.id.iv_icon);
    }

    public void setFm(FragmentManager fm) {
        this.fm = fm;
    }

    @Override
    protected void processLogic() {
        mData = (AwardNotify) getArguments().getSerializable(KeyConfig.KEY_DATA);
        if (mData == null) {
            ToastUtil.showShort(ConstString.TOAST_WRONG_PARAM);
            dismissAllowingStateLoss();
            return;
        }
        tvContent.setText(mData.description);
        if (!TextUtils.isEmpty(mData.icon)) {
            ViewUtil.showImage(ivIcon, mData.icon);
        } else {
            switch (mData.type) {
                case KeyConfig.TYPE_AWARD_BEAN:
                    ViewUtil.showImage(ivIcon, R.drawable.ic_award_bean);
                    break;
                case KeyConfig.TYPE_AWARD_SCORE:
                    ViewUtil.showImage(ivIcon, R.drawable.ic_award_score);
                    break;
                case KeyConfig.TYPE_AWARD_GIFT:
                default:
                    ViewUtil.showImage(ivIcon, R.drawable.ic_award_gift);

            }
        }
        if (mData.type == KeyConfig.TYPE_AWARD_GIFT) {
            btnPositive.setText("领取");
        } else {
            btnPositive.setText("我知道了");
        }
        setListener(this);
    }

    @Override
    public void onCancel() {
        dismissAllowingStateLoss();
    }

    @Override
    public void onConfirm() {
        if (mData.type == KeyConfig.TYPE_AWARD_GIFT && this.fm != null) {
            // 执行领取号码操作
            PayManager.getInstance().handleTakeGift(mData.id, this.fm);
        }
        dismiss();
    }
}
