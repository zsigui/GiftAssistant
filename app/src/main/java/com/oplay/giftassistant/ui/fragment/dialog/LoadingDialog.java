package com.oplay.giftassistant.ui.fragment.dialog;

import android.app.Dialog;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.util.ViewUtil;
import com.socks.library.KLog;

/**
 * Created by zsigui on 16-1-12.
 */
public class LoadingDialog extends DialogFragment {

	private ImageView ivLoad;
	private TextView tvLoad;
	private String mLoadText;
	private AnimationDrawable animationDrawable;

	public static LoadingDialog newInstance() {
		return new LoadingDialog();
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		View contentView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_loading, null);
		ivLoad = ViewUtil.getViewById(contentView, R.id.iv_load);
		tvLoad = ViewUtil.getViewById(contentView, R.id.tv_load);
		animationDrawable = (AnimationDrawable) ivLoad.getBackground();
		if (TextUtils.isEmpty(mLoadText)) {
			mLoadText = getResources().getString(R.string.st_view_loading_more);
		}
		setLoadText(mLoadText);
		animationDrawable.start();
		return new AlertDialog.Builder(getContext(), R.style.DefaultCustomDialog_NoDim)
				.setView(contentView)
				.setCancelable(false)
				.create();
	}

	public void setLoadText(String loadText) {
		mLoadText = loadText;
		if (tvLoad != null) {
			tvLoad.setText(mLoadText);
		}
	}


	@Override
	public int show(FragmentTransaction transaction, String tag) {
		return super.show(transaction, tag);
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser) {
			KLog.e(animationDrawable);
			if (animationDrawable != null) {
				animationDrawable.start();
			}
		}
	}

	@Override
	public void dismiss() {
		if (animationDrawable.isRunning()) {
			animationDrawable.stop();
		}
		super.dismiss();
	}
}
