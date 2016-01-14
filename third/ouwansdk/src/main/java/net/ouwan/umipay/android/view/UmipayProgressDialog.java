package net.ouwan.umipay.android.view;

import android.app.Dialog;
import android.content.Context;
import android.widget.TextView;

import net.ouwan.umipay.android.Utils.Util_Resource;

/**
 * @Author : liangpeixing
 * @Date : 14-3-5
 */
public class UmipayProgressDialog extends Dialog {

	private static UmipayProgressDialog progressDialog = null;
	TextView mLoadingTV;

	public UmipayProgressDialog(Context context, int theme) {
		super(context, theme);
	}

	public UmipayProgressDialog(Context context, int theme, String message) {
		super(context, theme);
		mLoadingTV = (TextView) findViewById(Util_Resource.getIdByReflection(context, "id",
				"umipay_loading_textview"));
		mLoadingTV.setText(message);
	}

	public static UmipayProgressDialog createDialog(Context context) {
		progressDialog = new UmipayProgressDialog(context, Util_Resource.getIdByReflection(context, "style",
				"umipay_progress_dialog_theme"));
		progressDialog.setContentView(Util_Resource.getIdByReflection(context, "layout",
				"umipay_progress_dialog_layout"));
		return progressDialog;
	}

	public static UmipayProgressDialog createDialog(Context context, String message) {
		progressDialog = new UmipayProgressDialog(context, Util_Resource.getIdByReflection(context, "style",
				"umipay_progress_dialog_theme"), message);
		progressDialog.setContentView(Util_Resource.getIdByReflection(context, "layout",
				"umipay_progress_dialog_layout"));
		return progressDialog;
	}
}
