package com.oplay.giftcool.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Dialog;
import com.oplay.giftcool.ui.fragment.dialog.ChangeAccountDialog;
import com.oplay.giftcool.util.ToastUtil;

/**
 * Created by zsigui on 16-9-6.
 */
public class DialogActivity extends FragmentActivity {

    private static final String ACTION_CHANGE_ACCOUNT = "action_change_account";

    private static boolean sSingletonShow = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);
        handleIntent(getIntent());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sSingletonShow = false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (sSingletonShow) {
            finish();
            return;
        }
        sSingletonShow = true;
        if (intent == null || intent.getAction() == null) {
            ToastUtil.showShort(ConstString.TOAST_MISS_STATE);
            finish();
            return;
        }
        if (ACTION_CHANGE_ACCOUNT.equalsIgnoreCase(intent.getAction())) {
            final ChangeAccountDialog dialog = ChangeAccountDialog.newInstance();
            dialog.setCancelable(false);
            dialog.setListener(new BaseFragment_Dialog.OnDialogClickListener() {
                @Override
                public void onCancel() {
                    dialog.onCancel();
                    finish();
                }

                @Override
                public void onConfirm() {
                    dialog.onConfirm();
                    finish();
                }
            });
            dialog.show(getSupportFragmentManager(), ChangeAccountDialog.class.getSimpleName());
        }

    }

    public static void showChangeAccount(Context context) {
        if (DialogActivity.sSingletonShow) {
            // 避免重复调用
            return;
        }
        Intent intent = new Intent(context, DialogActivity.class);
        intent.setAction(ACTION_CHANGE_ACCOUNT);
        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
