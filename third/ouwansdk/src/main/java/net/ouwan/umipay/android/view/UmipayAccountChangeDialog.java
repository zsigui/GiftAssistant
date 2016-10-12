package net.ouwan.umipay.android.view;

import android.app.Dialog;
import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.ouwan.umipay.android.Utils.Util_Resource;
import net.ouwan.umipay.android.api.UmipaySDKManager;
import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.entry.UmipayCommonAccount;
import net.ouwan.umipay.android.manager.UmipayAccountManager;
import net.ouwan.umipay.android.manager.UmipayCommonAccountCacheManager;
import net.youmi.android.libs.common.util.Util_System_Package;

public class UmipayAccountChangeDialog extends Dialog implements View.OnClickListener {

	private static UmipayAccountChangeDialog mInstance;

	private View mChangeBtn;
	private View mCancelBtn;
	private TextView mTitleTv;
	private TextView mChangeAccountTipsTv;
	private TextView mLogoutTipsTv;

	public static UmipayAccountChangeDialog getInstance(Context context){
		if(mInstance == null){
			mInstance = new UmipayAccountChangeDialog(context);
		}
		return mInstance;
	}

	private UmipayAccountChangeDialog(Context context) {
		super(context, Util_Resource.getIdByReflection(context, "style",
				"umipay_progress_dialog_theme"));
		initViews();
		setupViews();
		initListener();
		this.setCancelable(false);//禁止使用回退键取消对话框
	}


	private void initViews() {
		ViewGroup mRootLayout = (ViewGroup) ViewGroup.inflate(getContext(), Util_Resource.getIdByReflection(getContext(), "layout",
				"umipay_change_account_layout"), null);
		if (mRootLayout != null) {

			mCancelBtn = mRootLayout.findViewById(Util_Resource.getIdByReflection(getContext(), "id",
					"umipay_change_account_cancel_btn"));
			mChangeBtn = mRootLayout.findViewById(Util_Resource.getIdByReflection(getContext(), "id",
					"umipay_change_account_btn"));
			mTitleTv = (TextView) mRootLayout.findViewById(Util_Resource.getIdByReflection(getContext(), "id",
					"umipay_title_tv"));
			mChangeAccountTipsTv = (TextView) mRootLayout.findViewById(Util_Resource.getIdByReflection(getContext(), "id",
					"umipay_change_account_hints_tv"));
			mLogoutTipsTv = (TextView) mRootLayout.findViewById(Util_Resource.getIdByReflection(getContext(), "id",
					"umipay_logout_hints_tv"));

			if(mTitleTv != null){
				mTitleTv.setText(Util_Resource.getIdByReflection(getContext(), "string",
						"umipay_change_account_titile"));
			}
			setContentView(mRootLayout);
		}
	}
	private void setupViews(){
		StringBuffer username = new StringBuffer();
		String orginApkName = null;
		UmipayCommonAccount account = UmipayCommonAccountCacheManager.getInstance(getContext()).getCommonAccountByPackageName(getContext().getPackageName(), UmipayCommonAccountCacheManager.COMMON_ACCOUNT_TO_CHANGE);
		if(account != null) {
			if (!TextUtils.isEmpty(account.getUserName())) {
				if(account.getUserName().length() > 24) {
					username.append(account.getUserName().substring(23)).append("...");
				}else{
					username.append(account.getUserName());
				}
			}

			orginApkName = account.getOriginApkName();
		}

		if( TextUtils.isEmpty(username)|| TextUtils.isEmpty(orginApkName)){
			return;
		}
		try {
			if(mChangeAccountTipsTv != null) {
				StringBuffer content = new StringBuffer();
				String currentApkName = Util_System_Package.getAppNameforCurrentContext(getContext());
				content.append(String.format(mChangeAccountTipsTv.getText().toString(), orginApkName, username, currentApkName));
				SpannableString spannableString = new SpannableString(content);
				setSpannableString(spannableString, orginApkName);
				setSpannableString(spannableString, username.toString());
				mChangeAccountTipsTv.setText(spannableString);
			}

			if(mLogoutTipsTv != null){
				String logoutContent = String.format(mLogoutTipsTv.getText().toString(), UmipayAccountManager.getInstance(getContext()).getCurrentAccount().getUserName());

				if (!TextUtils.isEmpty(logoutContent)) {
					if(account.getUserName().length() > 32) {
						username.append(account.getUserName().substring(31)).append("...");
					}else{
						username.append(account.getUserName());
					}
				}
				mLogoutTipsTv.setText(logoutContent);
			}
		}catch (Throwable e){
			Debug_Log.e(e);
		}
	}

	private void setSpannableString(SpannableString content, String str){
		if(TextUtils.isEmpty(content) || TextUtils.isEmpty(str)){
			return ;
		}
		int start = content.toString().indexOf(str.toString());
		int end = start+str.length();
		ForegroundColorSpan span = new ForegroundColorSpan(getContext().getResources().getColor(Util_Resource.getIdByReflection(getContext(),"color","umipay_orange")));
		content.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	}

	private void initListener() {
		if (mChangeBtn != null) {
			mChangeBtn.setOnClickListener(this);
		}
		if (mCancelBtn != null) {
			mCancelBtn.setOnClickListener(this);
		}
	}

	@Override
	public void onClick(View v) {
		try {
			if (v.equals(mChangeBtn)) {
				UmipaySDKManager.logoutAccount(getContext(),null);
				dismiss();
				mInstance = null;
				return;
			}
			if (v.equals(mCancelBtn)) {
				dismiss();
				mInstance = null;
				return;
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

}

		

