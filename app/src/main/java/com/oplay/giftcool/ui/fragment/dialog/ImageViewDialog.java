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
import com.oplay.giftcool.config.util.BannerTypeUtil;
import com.oplay.giftcool.manager.StatisticsManager;
import com.oplay.giftcool.model.data.resp.IndexBanner;
import com.oplay.giftcool.util.ViewUtil;

import java.io.File;

/**
 * Created by zsigui on 16-2-1.
 */
public class ImageViewDialog extends DialogFragment implements View.OnClickListener {

    private static final String KEY_BANNER = "key_data_banner";
    private IndexBanner mData;

    public static ImageViewDialog newInstance(IndexBanner banner) {
        ImageViewDialog dialog = new ImageViewDialog();
        Bundle b = new Bundle();
        b.putSerializable(KEY_BANNER, banner);
        dialog.setArguments(b);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View contentView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_banner, null);
        ImageView iv = ViewUtil.getViewById(contentView, R.id.iv_banner);
        if (getArguments() != null && getArguments().getSerializable(KEY_BANNER) != null) {
            mData = (IndexBanner) getArguments().getSerializable(KEY_BANNER);
        }
        if (mData != null && iv != null) {
            File f = ImageLoader.getInstance().getDiskCache().get(mData.url);
            Bitmap bitmap = null;
            try {
                bitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
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
                AppDebugConfig.w(AppDebugConfig.TAG_DEBUG_INFO, e);
            }
            iv.setImageBitmap(bitmap);
            iv.setOnClickListener(this);
        }
        return new AlertDialog.Builder(getContext(), R.style.DefaultCustomDialog)
                .setView(contentView)
                .create();
    }

    @Override
    public void onClick(View v) {
        StatisticsManager.getInstance().trace(getContext(),
                StatisticsManager.ID.CLICK_FIRST_LOGIN_DIALOG,
                StatisticsManager.ID.STR_CLICK_FIRST_LOGIN_DIALOG);
        BannerTypeUtil.handleBanner(getContext(), mData);
        dismissAllowingStateLoss();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
    }
}
