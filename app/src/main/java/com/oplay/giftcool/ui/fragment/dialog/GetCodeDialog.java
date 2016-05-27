package com.oplay.giftcool.ui.fragment.dialog;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.widget.TextView;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.model.AppStatus;
import com.oplay.giftcool.model.data.resp.GameDownloadInfo;
import com.oplay.giftcool.model.data.resp.PayCode;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Dialog;

/**
 * Created by zsigui on 16-1-12.
 */
public class GetCodeDialog extends BaseFragment_Dialog implements BaseFragment_Dialog.OnDialogClickListener {

	private TextView tvGiftCode;
	private PayCode mPayCode;
	private GameDownloadInfo mAppInfo;

	public static GetCodeDialog newInstance(PayCode payCode) {
		GetCodeDialog dialog = new GetCodeDialog();
		dialog.setGiftCode(payCode);
		return dialog;
	}

	@Override
	protected void initView() {
		setContentView(R.layout.dialog_show_code_new);
		TextView tvContent = getViewById(R.id.tv_content);
		tvGiftCode = getViewById(R.id.tv_gift_code);
		tvContent.setText(Html.fromHtml("礼包码已保存至 <font color='#ffaa17'>我的礼包</font>"));
		setListener(this);
	}

	public void setGiftCode(PayCode payCode) {
		if (payCode == null) {
			return;
		}
		mPayCode = payCode;
		mAppInfo = payCode.gameInfo;
		if (tvGiftCode != null) {
			tvGiftCode.setText(Html.fromHtml(String.format("礼包码：<font color='#ffaa17'>%s</font>", mPayCode.giftCode)));
		}
		if (getContext() != null) {
			ClipboardManager cmb = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
			cmb.setPrimaryClip(ClipData.newPlainText("礼包码", mPayCode.giftCode));
			if (mAppInfo != null) {
				mAppInfo.initAppInfoStatus(getContext());
			}
		}
	}

	@Override
	protected void processLogic() {
		setGiftCode(mPayCode);
		if (mAppInfo != null
				&& !TextUtils.isEmpty(mAppInfo.packageName)
				&& AppStatus.OPENABLE.equals(mAppInfo.appStatus)) {
			setPositiveBtnText("打开游戏");
		} else {
			if (AssistantApp.getInstance().isAllowDownload()) {
				setPositiveBtnText("下载游戏");
			} else {
				setPositiveBtnText("确认");
			}
		}
	}

	@Override
	public void onCancel() {
		dismissAllowingStateLoss();
	}

	@Override
	public void onConfirm() {
		if (mAppInfo != null) {
			if (AppStatus.OPENABLE.equals(mAppInfo.appStatus)
					|| AssistantApp.getInstance().isAllowDownload()) {
				mAppInfo.handleOnClick(getChildFragmentManager());
			}
		}
		dismissAllowingStateLoss();
	}
}
