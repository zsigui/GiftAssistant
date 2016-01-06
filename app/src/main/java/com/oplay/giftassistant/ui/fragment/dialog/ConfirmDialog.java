package com.oplay.giftassistant.ui.fragment.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.oplay.giftassistant.R;

/**
 * Created by zsigui on 16-1-6.
 */
public class ConfirmDialog extends DialogFragment implements View.OnClickListener {

	private View mContentView;
	private TextView btnPositive;
	private TextView btnNegative;
	private TextView tvTitle;
	private TextView tvContent;
	private String mTitle;
	private String mContent;

	private OnDialogClickListener mListener;

	public static ConfirmDialog newInstance() {
		return new ConfirmDialog();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		mContentView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_confirm, null);
		btnPositive = getViewById(R.id.btn_confirm);
		btnNegative = getViewById(R.id.btn_cancel);
		tvTitle = getViewById(R.id.tv_title);
		tvContent = getViewById(R.id.tv_content);
		btnPositive.setOnClickListener(this);
		btnNegative.setOnClickListener(this);

		if (!TextUtils.isEmpty(mTitle)) {
			tvTitle.setText(mTitle);
		}
		if (!TextUtils.isEmpty(mContent)) {
			tvContent.setText(mContent);
		}

		return new AlertDialog.Builder(getActivity(), R.style.DefaultCustomDialog)
				.setView(mContentView)
				.create();
	}

	public OnDialogClickListener getListener() {
		return mListener;
	}

	public void setListener(OnDialogClickListener listener) {
		mListener = listener;
	}

	public void setTitle(String title) {
		mTitle = title;
		if (tvTitle != null) {
			tvTitle.setText(title);
		}
	}
	public void setContent(String content) {
		mContent = content;
		if (tvContent != null) {
			tvContent.setText(content);
		}
	}

	@SuppressWarnings("unchecked")
	protected <VT extends View> VT getViewById(@IdRes int id) {
		return (VT) getViewById(mContentView, id);
	}

	@SuppressWarnings("unchecked")
	protected <VT extends View> VT getViewById(View contentView, @IdRes int id) {
		return (VT) contentView.findViewById(id);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_confirm:
				if (mListener != null) {
					mListener.onConfirm();
				}
				break;
			case R.id.btn_cancel:
				if (mListener != null) {
					mListener.onCancel();
				} else {
					this.dismiss();
				}
				break;
		}
	}

	public interface OnDialogClickListener {
		void onCancel();

		void onConfirm();
	}
}
