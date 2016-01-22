package com.oplay.giftcool.ui.fragment.base;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.oplay.giftcool.R;

/**
 * Created by zsigui on 16-1-22.
 */
public abstract class BaseFragment_Dialog_NoButton extends DialogFragment {

	private View mContentView;

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final Dialog dialog = new Dialog(getActivity(), R.style.DefaultCustomDialog);
		dialog.setCanceledOnTouchOutside(true);
		dialog.setCancelable(true);
		return dialog;
	}

	@Nullable
	@Override
	public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mContentView = getContentView(inflater, container, savedInstanceState);
		return inflater.inflate(R.layout.dialog_base_no_button, null);
	}

	@Override
	public final void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		TextView dialogTitle = (TextView) view.findViewById(R.id.tv_title);
		dialogTitle.setText(getTitle());
		ViewGroup contentViewGroup = (FrameLayout) view.findViewById(R.id.layout_dialog_base_content);
		contentViewGroup.addView(mContentView);
		bindViewWithData(mContentView, savedInstanceState);
	}

	protected abstract void bindViewWithData(View contentView, @Nullable Bundle savedInstanceState);

	protected abstract String getTitle();

	protected abstract View getContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);
}
