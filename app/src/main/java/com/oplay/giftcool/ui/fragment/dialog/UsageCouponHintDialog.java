package com.oplay.giftcool.ui.fragment.dialog;

import android.os.Bundle;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.model.AppStatus;
import com.oplay.giftcool.model.data.resp.GameDownloadInfo;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Dialog;
import com.oplay.giftcool.util.ToastUtil;

import java.util.Locale;

/**
 * Created by zsigui on 16-1-6.
 */
@SuppressWarnings("ResourceType")
public class UsageCouponHintDialog extends BaseFragment_Dialog implements BaseFragment_Dialog.OnDialogClickListener {

	private TextView tvContent;
	private String mContent;
	private Spanned mSpanned;
	private int mPositiveVisibility = View.VISIBLE;
	private int mNegativeVisibility = View.VISIBLE;
	private GameDownloadInfo mAppInfo;

	public static UsageCouponHintDialog newInstance(GameDownloadInfo mAppInfo) {
		UsageCouponHintDialog dialog =  new UsageCouponHintDialog();
		Bundle b = new Bundle();
		b.putSerializable(KeyConfig.KEY_DATA, mAppInfo);
		dialog.setArguments(b);
		return dialog;
	}

	@Override
	protected void initView() {
		setContentView(R.layout.dialog_confirm_new);
		tvContent = getViewById(R.id.tv_content);
	}

	@Override
	protected void processLogic() {
		if (getArguments() == null) {
			ToastUtil.showShort(ConstString.TOAST_MISS_STATE);
			dismiss();
			return;
		}
		mAppInfo = (GameDownloadInfo) getArguments().getSerializable(KeyConfig.KEY_DATA);

		setListener(this);
		if (mSpanned != null) {
			setContent(mSpanned);
		} else {
			setContent(mContent);
		}
		setTitle("进入游戏使用首充券");
		setPositiveVisibility(mPositiveVisibility);
		setNegativeVisibility(mNegativeVisibility);
		if (mAppInfo != null) {
			mAppInfo.initAppInfoStatus(getContext());
		}
		if (mAppInfo != null
				&& !TextUtils.isEmpty(mAppInfo.packageName)
				&& AppStatus.OPENABLE.equals(mAppInfo.appStatus)) {
			setPositiveBtnText("打开游戏");
		} else {
			if (mAppInfo != null && !TextUtils.isEmpty(mAppInfo.downloadUrl)
					&& AssistantApp.getInstance().isAllowDownload()) {
				setPositiveBtnText("下载游戏");
			} else {
				setPositiveBtnText("确认");
			}
		}
		setContent(String.format(Locale.CHINA, "请使用您的偶玩账号(%s)登录游戏后直接使用",
				AccountManager.getInstance().getUserInfo().username));
	}


	public void setContent(String content) {
		mContent = content;
		if (tvContent != null) {
			tvContent.setText(content);
		}
	}

	public void setContent(Spanned content) {
		mSpanned = content;
		if (tvContent != null) {
			tvContent.setText(content);
		}
	}

	public void setPositiveVisibility(int visibility) {
		mPositiveVisibility = visibility;
		if (btnPositive != null) {
			btnPositive.setVisibility(mPositiveVisibility);
		}
	}

	public void setNegativeVisibility(int visibility) {
		mNegativeVisibility = visibility;
		if (btnNegative != null) {
			btnNegative.setVisibility(mNegativeVisibility);
		}
	}


	@Override
	public void onCancel() {
		dismissAllowingStateLoss();
	}

	@Override
	public void onConfirm() {
		dismissAllowingStateLoss();
		if (mAppInfo != null) {
			if (AppStatus.OPENABLE.equals(mAppInfo.appStatus)
					|| AssistantApp.getInstance().isAllowDownload()) {
				mAppInfo.handleOnClick(getChildFragmentManager());
			}
		}
	}
}
