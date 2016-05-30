package com.oplay.giftcool.ui.fragment.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.oplay.giftcool.R;

/**
 * Created by zsigui on 16-5-30.
 */
public class GuideDialog extends DialogFragment{

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getContext(), R.style.DefaultCustomDialog)
                .setView(R.layout.overlay_hint_focus)
                .create();
    }

}
