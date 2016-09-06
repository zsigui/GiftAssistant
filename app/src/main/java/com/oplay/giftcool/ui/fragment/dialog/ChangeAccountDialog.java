package com.oplay.giftcool.ui.fragment.dialog;

import android.text.Html;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.listener.CommonAccountViewListener;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.manager.OuwanSDKManager;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Dialog;

import net.ouwan.umipay.android.entry.UmipayCommonAccount;
import net.ouwan.umipay.android.manager.UmipayCommonAccountCacheManager;

/**
 * 切换账号的提示弹窗
 *
 * Created by zsigui on 16-9-6.
 */
public class ChangeAccountDialog extends BaseFragment_Dialog implements BaseFragment_Dialog.OnDialogClickListener{

    private String mContent;
    private String mHint;
    private TextView tvContent;
    private TextView tvHint;

    private UmipayCommonAccount account;

    public static ChangeAccountDialog newInstance() {
        return new ChangeAccountDialog();
    }

    @Override
    protected void initView() {
        setContentView(R.layout.dialog_change_account);
        tvContent = getViewById(R.id.tv_content);
        tvHint = getViewById(R.id.tv_hint);
    }

    @Override
    protected void processLogic() {
        account = UmipayCommonAccountCacheManager.getInstance(getContext())
                .popCommonAccountToChange();
        if (account == null || !AccountManager.getInstance().isLogin()) {
            AppDebugConfig.d(AppDebugConfig.TAG_DEBUG_INFO, "切换状态丢失，关闭弹窗, isLogin = "
                    + AccountManager.getInstance().isLogin());
            dismiss();
            return;
        }
        setNegativeBtnText(getString(R.string.st_dialog_change_account_cancel));
        setPositiveBtnText(getString(R.string.st_dialog_change_account_ok));
        setTitle(getString(R.string.st_dialog_change_account_title));
        setContent(String.format(getString(R.string.st_dialog_change_account_content),
                account.getOriginApkName(), account.getUserName()));
        setHint(String.format(getString(R.string.st_dialog_change_account_hint),
                AccountManager.getInstance().getUserInfo().username));
    }

    @Override
    protected int getDialogStyle() {
        return R.style.DefaultCustomDialog_NoDim;
    }

    public void setContent(String content) {
        mContent = content;
        if (tvContent != null) {
            tvContent.setText(Html.fromHtml(mContent));
        }
    }

    public void setHint(String hint) {
        mHint = hint;
        if (tvHint != null) {
            tvHint.setText(mHint);
        }
    }

    @Override
    public void onCancel() {
        dismiss();
    }

    @Override
    public void onConfirm() {
        try {
            OuwanSDKManager.getInstance().onChooseAccount(
                    CommonAccountViewListener.CODE_CHANGE_ACCOUNT,
                    account,
                    null
            );
            dismiss();
        } catch (Throwable e) {
            AppDebugConfig.d(AppDebugConfig.TAG_DEBUG_INFO, e);
        }
    }
}
