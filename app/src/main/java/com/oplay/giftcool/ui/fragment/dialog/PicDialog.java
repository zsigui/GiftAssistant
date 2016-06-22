package com.oplay.giftcool.ui.fragment.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.manager.StatisticsManager;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Dialog;
import com.oplay.giftcool.util.ViewUtil;

import java.io.File;

/**
 * Created by zsigui on 16-4-19.
 */
public class PicDialog extends DialogFragment implements View.OnClickListener {

    private static final String KEY_PIC = "key_pic";
    private String mPicPath;
    private BaseFragment_Dialog.OnDialogClickListener mDialogClickListener;

    public static PicDialog newInstance(String picPath) {
        PicDialog dialog = new PicDialog();
        Bundle b = new Bundle();
        b.putSerializable(KEY_PIC, picPath);
        dialog.setArguments(b);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View contentView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_banner, null);
        ImageView iv = ViewUtil.getViewById(contentView, R.id.iv_banner);
        if (getArguments() != null && getArguments().getSerializable(KEY_PIC) != null) {
            mPicPath = getArguments().getString(KEY_PIC);
        }
        if (mPicPath != null && iv != null) {
            Bitmap bitmap = null;
            try {
                if (mPicPath.startsWith("drawable://")) {
                    bitmap = BitmapFactory.decodeResource(getResources(), Integer.parseInt(mPicPath.substring(11)));
                } else {
                    File f = ImageLoader.getInstance().getDiskCache().get(mPicPath);
                    bitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
                }
                DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                int minWidth = 3 * dm.widthPixels / 4;
                if (bitmap.getWidth() < minWidth) {
                    width = minWidth;
                    height = bitmap.getHeight() * width / bitmap.getWidth();
                }
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, height);
                iv.setLayoutParams(lp);
            } catch (Throwable e) {
                AppDebugConfig.w(AppDebugConfig.TAG_DEBUG_INFO, AppDebugConfig.TAG_UTIL, e);
            }
            iv.setImageBitmap(bitmap);
            iv.setOnClickListener(this);
        }
        return new AlertDialog.Builder(getContext(), R.style.DefaultCustomDialog)
                .setView(contentView)
                .create();
    }

    public void setDialogClickListener(BaseFragment_Dialog.OnDialogClickListener dialogListener) {
        mDialogClickListener = dialogListener;
    }

    @Override
    public void onClick(View v) {
        StatisticsManager.getInstance().trace(getContext(), StatisticsManager.ID.CLICK_FIRST_LOGIN_DIALOG, "签到弹窗");
        if (mDialogClickListener != null) {
            mDialogClickListener.onConfirm();
        }
        dismissAllowingStateLoss();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (mDialogClickListener != null) {
            mDialogClickListener.onCancel();
        }
        dialog.dismiss();
    }
}