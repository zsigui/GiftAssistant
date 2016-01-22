package com.oplay.giftcool.ui.fragment.base;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.oplay.giftcool.R;

/**
 * Created by zsigui on 16-1-12.
 */
public abstract class BaseFragment_Dialog extends DialogFragment implements View.OnClickListener {

	protected boolean mIsPrepared = false;

	protected View mContentView;
	protected TextView btnPositive;
	protected TextView btnNegative;
	protected TextView tvTitle;
	private String mTitle;
	private String mPositiveBtnText;
	private String mNegativeBtnText;
	private boolean mPositiveEnabled = true;
	private boolean mNegativeEnabled = true;

	protected OnDialogClickListener mListener;


	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		initView();
		btnPositive = getViewById(R.id.btn_confirm);
		btnNegative = getViewById(R.id.btn_cancel);
		tvTitle = getViewById(R.id.tv_title);
		if (btnPositive != null) {
			btnPositive.setOnClickListener(this);
		}
		if (btnNegative != null) {
			btnNegative.setOnClickListener(this);
		}

		mIsPrepared = true;

		if (TextUtils.isEmpty(mTitle)) {
			mTitle = getResources().getString(R.string.st_dialog_hint_title);
		}
		if (TextUtils.isEmpty(mPositiveBtnText)) {
			mPositiveBtnText = getResources().getString(R.string.st_dialog_btn_confirm);
		}
		if (TextUtils.isEmpty(mNegativeBtnText)) {
			mNegativeBtnText = getResources().getString(R.string.st_dialog_btn_cancel);
		}
		setTitle(mTitle);
		setPositiveBtnText(mPositiveBtnText);
		setNegativeBtnText(mNegativeBtnText);
		setPositiveEnabled(mPositiveEnabled);
		setNegativeEnabled(mNegativeEnabled);

		processLogic();
		return new AlertDialog.Builder(getActivity(), R.style.DefaultCustomDialog)
				.setView(mContentView)
				.create();
	}

	/**
	 * 在此处进行页面初始化工作，需要调用setContentView()
	 */
	protected abstract void initView();

	protected void setContentView(@LayoutRes int layoutId) {
		mContentView = LayoutInflater.from(getContext()).inflate(layoutId, null);
	}

	protected void setContentView(View v) {
		mContentView = v;
	}

	/**
	 * 操作逻辑集中此处执行
	 */
	protected abstract void processLogic();

	public void setTitle(String title) {
		mTitle = title;
		if (tvTitle != null) {
			tvTitle.setText(mTitle);
		}
	}

	public void setPositiveBtnText(String positiveBtnText) {
		mPositiveBtnText = positiveBtnText;
		if (btnPositive != null) {
			btnPositive.setText(mPositiveBtnText);
		}
	}

	public void setNegativeBtnText(String negativeBtnText) {
		mNegativeBtnText = negativeBtnText;
		if (btnNegative != null) {
			btnNegative.setText(mNegativeBtnText);
		}
	}

	public void setNegativeEnabled(boolean enabled) {
		mNegativeEnabled = enabled;
		if (btnNegative != null) {
			btnNegative.setEnabled(mNegativeEnabled);
		}
	}

	public void setPositiveEnabled(boolean enabled) {
		mPositiveEnabled = enabled;
		if (btnPositive != null) {
			btnPositive.setEnabled(mPositiveEnabled);
		}
	}

	public void setListener(OnDialogClickListener listener) {
		mListener = listener;
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
