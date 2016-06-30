package com.oplay.giftcool.ui.fragment.dialog;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.widget.TextView;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.util.GiftTypeUtil;
import com.oplay.giftcool.model.AppStatus;
import com.oplay.giftcool.model.data.resp.GameDownloadInfo;
import com.oplay.giftcool.model.data.resp.PayCode;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Dialog;
import com.oplay.giftcool.ui.fragment.gift.GiftDetailFragment;

/**
 * Created by zsigui on 16-1-12.
 */
public class GetCodeDialog extends BaseFragment_Dialog implements BaseFragment_Dialog.OnDialogClickListener {

    private TextView tvGiftCode;
    private PayCode mPayCode;
    private GameDownloadInfo mAppInfo;
    // 用于首充券回调指引判断
    private int mType;
    private GiftDetailFragment mFragment;

    public static GetCodeDialog newInstance(PayCode payCode) {
        GetCodeDialog dialog = new GetCodeDialog();
        dialog.setGiftCode(payCode);
        return dialog;
    }

    @Override
    protected void initView() {
        setContentView(R.layout.dialog_show_code_new);
        TextView tvContent = getViewById(R.id.tv_content);
        tvGiftCode = getViewById(R.id.tv_gift_code);
        if ((mType == GiftTypeUtil.TYPE_CHARGE_SEIZE
                || mType == GiftTypeUtil.TYPE_CHARGE_TAKE)) {
            tvContent.setText(Html.fromHtml("兑换码已保存至 <font color='#ffaa17'>我的首充券</font>"));
        } else {
            tvContent.setText(Html.fromHtml("礼包码已保存至 <font color='#ffaa17'>我的礼包</font>"));
        }
        TextView tvHint = getViewById(R.id.tv_hint);
        switch (mType) {
            case GiftTypeUtil.TYPE_NORMAL_SEARCH:
            case GiftTypeUtil.TYPE_NORMAL_SEARCHED:
                tvHint.setText("已复制到粘贴板，淘号的礼包不一定可以用，祝你好运。");
                break;
            case GiftTypeUtil.TYPE_NORMAL_SEIZE:
            case GiftTypeUtil.TYPE_NORMAL_SEIZED:
                tvHint.setText("已复制到粘贴板，请尽快打开游戏兑换，否则会进入淘号。");
                break;
            default:
                tvHint.setText("已复制到粘贴板，请尽快打开游戏兑换。");
                break;
        }
        setListener(this);
    }

    public void setGiftCode(PayCode payCode) {
        if (payCode == null) {
            return;
        }
        mPayCode = payCode;
        mAppInfo = payCode.gameInfo;
        if (tvGiftCode != null) {
            if ((mType == GiftTypeUtil.TYPE_CHARGE_SEIZE
                    || mType == GiftTypeUtil.TYPE_CHARGE_TAKE)) {
                tvGiftCode.setText(Html.fromHtml(
                        String.format("礼包码：<font color='#ffaa17'>%s</font>", mPayCode.giftCode)));
            } else {
                tvGiftCode.setText(Html.fromHtml(
                        String.format("兑换码：<font color='#ffaa17'>%s</font>", mPayCode.giftCode)));
            }
        }
        if (getContext() != null) {
            ClipboardManager cmb = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            cmb.setPrimaryClip(ClipData.newPlainText("礼包码", mPayCode.giftCode));
            if (mAppInfo != null) {
                mAppInfo.initAppInfoStatus(getContext());
            }
        }
    }

    @Override
    protected void processLogic() {
        setGiftCode(mPayCode);
        if (mAppInfo != null
                && !TextUtils.isEmpty(mAppInfo.packageName)
                && AppStatus.OPENABLE.equals(mAppInfo.appStatus)) {
            setPositiveBtnText("打开游戏");
        } else {
            if (AssistantApp.getInstance().isAllowDownload()) {
                setPositiveBtnText("下载游戏");
            } else {
                setPositiveBtnText("确认");
            }
        }
    }

    @Override
    public void onCancel() {
        showCouponGuide();
        dismissAllowingStateLoss();
    }

    @Override
    public void onConfirm() {
        showCouponGuide();
        dismissAllowingStateLoss();
        if (mAppInfo != null) {
            if (AppStatus.OPENABLE.equals(mAppInfo.appStatus)
                    || AssistantApp.getInstance().isAllowDownload()) {
                mAppInfo.handleOnClick(getChildFragmentManager());
            }
        }
    }

    /**
     * 设置首充券回调指引判断的必须属性
     */
    public void setCouponCharge(int type, GiftDetailFragment fragment) {
        mType = type;
        mFragment = fragment;
    }

    private void showCouponGuide() {
        AppDebugConfig.d(AppDebugConfig.TAG_DEBUG_INFO, "mFragment = " + mFragment
        + ", mType = ");
        if (mFragment != null
                && (mType == GiftTypeUtil.TYPE_CHARGE_SEIZE
                || mType == GiftTypeUtil.TYPE_CHARGE_TAKE)) {
            mFragment.showGuidePage();
        }
    }
}
