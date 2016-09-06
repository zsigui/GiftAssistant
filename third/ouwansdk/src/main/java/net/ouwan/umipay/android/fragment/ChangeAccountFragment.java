package net.ouwan.umipay.android.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.ouwan.umipay.android.Utils.Util_Resource;
import net.ouwan.umipay.android.api.UmipaySDKManager;
import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.entry.UmipayCommonAccount;
import net.ouwan.umipay.android.manager.UmipayCommonAccountCacheManager;

/**
 * Created by jimmy on 2016/8/14.
 */
public class ChangeAccountFragment extends BaseFragment {

	private View mChangeBtn;
	private View mCancelBtn;
	private TextView mTitleTv;
	private TextView mTipsTv;

	public static ChangeAccountFragment newInstance() {
		ChangeAccountFragment fragment = new ChangeAccountFragment();
		return fragment;
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		try {
			mRootLayout = inflater.inflate(Util_Resource.getIdByReflection(getActivity(), "layout",
					"umipay_change_account_layout"), container, false);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		initViews();
		initListener();
		return mRootLayout;
	}

	private void initViews() {
		if (mRootLayout != null) {

			mCancelBtn = mRootLayout.findViewById(Util_Resource.getIdByReflection(getActivity(), "id",
					"umipay_change_account_cancel_btn"));
			mChangeBtn = mRootLayout.findViewById(Util_Resource.getIdByReflection(getActivity(), "id",
					"umipay_change_account_btn"));
			mTitleTv = (TextView) mRootLayout.findViewById(Util_Resource.getIdByReflection(getActivity(), "id",
					"umipay_title_tv"));
			mTipsTv = (TextView) mRootLayout.findViewById(Util_Resource.getIdByReflection(getActivity(), "id",
					"umipay_change_account_title_tv"));

			if(mTitleTv != null){
				mTitleTv.setText("切换账号");
			}


			setUserName(mTipsTv);
		}
	}

	private void setUserName(TextView tv){
		String username = null;
		String orginApkName = null;
		UmipayCommonAccount account = UmipayCommonAccountCacheManager.getInstance(getActivity()).getCommonAccountByPackageName(getActivity().getPackageName(),UmipayCommonAccountCacheManager.COMMON_ACCOUNT_TO_CHANGE);
		if(account != null){
			username = account.getUserName();
			orginApkName = account.getOriginApkName();
		}

		if(tv == null || tv.getText() == null || TextUtils.isEmpty(username)|| TextUtils.isEmpty(orginApkName)){
			return;
		}
		try {
			StringBuffer content = new StringBuffer();
			content.append(String.format(tv.getText().toString(), orginApkName,username));
			SpannableString spannableString = new SpannableString(content);
			setSpannableString(spannableString,orginApkName);
			setSpannableString(spannableString,username);
			tv.setText(spannableString);
		}catch (Throwable e){
			Debug_Log.e(e);
		}
	}

	private void setSpannableString(SpannableString content,String str){
			if(TextUtils.isEmpty(content) || TextUtils.isEmpty(str)){
				return ;
			}
			int start = content.toString().indexOf(str.toString());
			int end = start+str.length();
			ForegroundColorSpan span = new ForegroundColorSpan(Color.YELLOW);
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
	protected void handleOnClick(View v) {
		try {
			if (v.equals(mChangeBtn)) {
				UmipaySDKManager.logoutAccount(getActivity(),null);
				getActivity().finish();
				return;
			}
			if (v.equals(mCancelBtn)) {
				getActivity().finish();
				return;
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

}
