package net.ouwan.umipay.android.view;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import net.ouwan.umipay.android.Utils.Util_Resource;
import net.ouwan.umipay.android.api.ExitDialogCallbackListener;
import net.ouwan.umipay.android.api.UmipayFloatMenu;
import net.ouwan.umipay.android.api.UmipaySDKStatusCode;
import net.ouwan.umipay.android.config.SDKCacheConfig;
import net.ouwan.umipay.android.debug.Debug_Log;
import net.youmi.android.libs.common.util.Util_System_Display;
import net.youmi.android.libs.common.util.Util_System_Package;

public class UmipayExitDialog extends Dialog implements View.OnClickListener {

	Context mContext;

	private ViewGroup mRootLayout;
	private ViewStub mViewStub;
	private LinearLayout mExitDialogLayout;

	private View mExitBtn;
	private View mCancelBtn;
	private Button mExitToCommunityBtn;

	private ExitDialogCallbackListener mExitDialogCallbackListener;

	public UmipayExitDialog(Context context, ExitDialogCallbackListener exitDialogCallbackListener) {

		super(context, Util_Resource.getIdByReflection(context, "style",
				"umipay_progress_dialog_theme"));
		this.mContext = context;
		this.mExitDialogCallbackListener = exitDialogCallbackListener;
		initViews();
		initListener();
		setContentView(mRootLayout);
		setLayoutParams();
		this.setCancelable(false);//禁止使用回退键取消对话框
	}

	private void setLayoutParams() {
		try {
			WindowManager.LayoutParams mLayoutParams = getWindow().getAttributes();  //获取对话框当前的参数值
			if (mContext != null) {
				mLayoutParams.width = Util_System_Display.dip2px(mContext, 300);
			}
			mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
			mLayoutParams.gravity = Gravity.CENTER_VERTICAL;
			getWindow().setAttributes(mLayoutParams);//设置生效
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

	private void initViews() {
		if (mContext == null) {
			return;
		}
		try {
			mRootLayout = (ViewGroup) ViewGroup.inflate(mContext, Util_Resource.getIdByReflection(mContext, "layout",
					"umipay_exitdialog"), null);
			String fieldName = SDKCacheConfig.getInstance(mContext).isEnableExitToCommunity() ?
					"umipay_exitdilog_tocommuniy_viewstub" : "umipay_exitdilog_viewstub";

			mViewStub = (ViewStub) mRootLayout.findViewById(Util_Resource.getIdByReflection(mContext,
					"id",
					fieldName));

			if (mExitDialogLayout == null && mViewStub != null) {
				mExitDialogLayout = (LinearLayout) mViewStub.inflate();
			}

			mCancelBtn = mExitDialogLayout.findViewById(Util_Resource.getIdByReflection(mContext, "id",
					"umipay_exit_cancel_btn"));
			mExitBtn = mExitDialogLayout.findViewById(Util_Resource.getIdByReflection(mContext, "id",
					"umipay_exit_btn"));

			if (SDKCacheConfig.getInstance(mContext).isEnableExitToCommunity()) {

				mExitToCommunityBtn = (Button) mExitDialogLayout.findViewById(Util_Resource.getIdByReflection
						(mContext, "id",
								"umipay_exit_tocommunity_btn"));
				String mExitToCommunityBtnText = SDKCacheConfig.getInstance(mContext)
						.getExitDialogCommunityBtnText();
				String mExitToDownloadBtnText = SDKCacheConfig.getInstance(mContext)
						.getExitDialogDownloadBtnText();
				String mPackageName = SDKCacheConfig.getInstance(mContext).getOuwanPackageName();
				if (mExitToCommunityBtn != null && mPackageName != null) {
					if (Util_System_Package.isPakcageInstall(mContext, mPackageName)) {
						if (mExitToCommunityBtnText != null) {
							mExitToCommunityBtn.setText(mExitToCommunityBtnText);
						}
					} else {
						if (mExitToCommunityBtnText != null) {
							mExitToCommunityBtn.setText(mExitToDownloadBtnText);
						}
					}
				}
			}

		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}


	@Override
	public void onClick(View v) {
		try {
			if (v.equals(mExitBtn)) {
				dismiss();
				onExit();
				if (mExitDialogCallbackListener != null) {
					mExitDialogCallbackListener.onExit(UmipaySDKStatusCode.EXIT_FINISH);
				}
				return;
			}
			if (v.equals(mExitToCommunityBtn)) {
				String mPackageName = SDKCacheConfig.getInstance(mContext).getOuwanPackageName();
				String mCommunityUrl = SDKCacheConfig.getInstance(mContext).getOuwanCommunityUrl();
				String mDownloadUrl = SDKCacheConfig.getInstance(mContext).getOuwanDownloadUrl();
				if (mPackageName != null && Util_System_Package.isPakcageInstall(mContext, mPackageName) == true) {
					try {
						if (mCommunityUrl != null) {
							Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mCommunityUrl));
							UmipayExitDialog.this.getContext().startActivity(intent);

						}
					} catch (Throwable e) {
						Debug_Log.e(e);
					}
				} else if (mDownloadUrl != null) {
					try {
						Uri uri = Uri.parse(mDownloadUrl);
						Intent intent = new Intent(Intent.ACTION_VIEW, uri);
						UmipayExitDialog.this.getContext().startActivity(intent);
					} catch (Throwable e) {
						Debug_Log.e(e);
					}
				}
				dismiss();
				onExit();
				if (mExitDialogCallbackListener != null) {
					mExitDialogCallbackListener.onExit(UmipaySDKStatusCode.EXIT_FINISH);
				}
				return;
			}
			if (v.equals(mCancelBtn)) {
				dismiss();
				if (mExitDialogCallbackListener != null) {
					mExitDialogCallbackListener.onExit(UmipaySDKStatusCode.CANCEL);//取消退出
				}
				return;
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

	private void initListener() {
		try {
			if (mExitBtn != null) {
				mExitBtn.setOnClickListener(this);
			}
			if (mExitToCommunityBtn != null) {
				mExitToCommunityBtn.setOnClickListener(this);
			}
			if (mCancelBtn != null) {
				mCancelBtn.setOnClickListener(this);
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

	/**
	 * 退出sdk时需要执行的相关操作
	 */
	public void onExit() {
		//回收FloatMenu相关资源
		UmipayFloatMenu.getInstance().recycle();
	}
}

		

