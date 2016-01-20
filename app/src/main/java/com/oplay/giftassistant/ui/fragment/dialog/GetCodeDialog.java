package com.oplay.giftassistant.ui.fragment.dialog;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.Html;
import android.widget.TextView;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment_Dialog;

/**
 * Created by zsigui on 16-1-12.
 */
public class GetCodeDialog extends BaseFragment_Dialog {

	private TextView tvGiftCode;
	private String mGiftCode;

	public static GetCodeDialog newInstance(String giftCode) {
		GetCodeDialog dialog = new GetCodeDialog();
		dialog.setGiftCode(giftCode);
		return dialog;
	}

	@Override
	protected void initView() {
		setContentView(R.layout.dialog_show_code);
		TextView tvContent = getViewById(R.id.tv_content);
		tvGiftCode = getViewById(R.id.tv_gift_code);
		tvContent.setText(Html.fromHtml("礼包码已保存至 <font color='#ffaa17'>我的礼包</font>"));
	}

	public void setGiftCode(String giftCode) {
		mGiftCode = giftCode;
		if (tvGiftCode != null) {
			tvGiftCode.setText(Html.fromHtml(String.format("礼包码：<font color='#ffaa17'>%s</font>", mGiftCode)));
		}
		if(getContext() != null) {
			ClipboardManager cmb = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
			cmb.setPrimaryClip(ClipData.newPlainText("礼包码", giftCode));
		}
	}

	@Override
	protected void processLogic() {
		setGiftCode(mGiftCode);
		setPositiveBtnText("打开游戏");
	}
}
