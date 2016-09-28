package net.ouwan.umipay.android.fragment;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import net.ouwan.umipay.android.Utils.Util_Resource;
import net.ouwan.umipay.android.Utils.Util_ScreenShot;
import net.ouwan.umipay.android.api.UmipaySDKManager;
import net.ouwan.umipay.android.api.UmipaySDKStatusCode;
import net.ouwan.umipay.android.asynctask.TaskCMD;
import net.ouwan.umipay.android.config.SDKCacheConfig;
import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.entry.UmipayAccount;
import net.ouwan.umipay.android.entry.gson.Gson_Login;
import net.ouwan.umipay.android.manager.ListenerManager;
import net.ouwan.umipay.android.manager.UmipayAccountManager;
import net.ouwan.umipay.android.view.UmipayLoginInfoDialog;

/**
 * 一键注册成功
 * Created by jimmy on 2016/8/14.
 */
public class QuickRegisterSuccessFragment extends BaseFragment {
	private UmipayAccount mLoginAccount;
	private EditText mRegSuccessAccountEditor;
	private EditText mRegSuccessPswEditor;
	private Button mRegSuccessEnterGameBtn;

	public static QuickRegisterSuccessFragment newInstance() {
		QuickRegisterSuccessFragment fragment = new QuickRegisterSuccessFragment();
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mLoginAccount = UmipayAccountManager.getInstance(getActivity()).getCurrentAccount();
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		try {
			mRootLayout = inflater.inflate(Util_Resource.getIdByReflection(getActivity(), "layout",
					"umipay_regist_success_layout"), container, false);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		initViews();
		initListener();
		if (mRootLayout != null) {
			//未截图前不能进入游戏
			mRegSuccessEnterGameBtn.setEnabled(false);
			mRootLayout.postDelayed(new Runnable() {
				@Override
				public void run() {
					screenShot();
					//截图后可以进入游戏
					if (mRegSuccessEnterGameBtn != null) {
						mRegSuccessEnterGameBtn.setEnabled(true);
					}
				}
			}, 300);
		}
		return mRootLayout;
	}


	private void initViews() {
		if (mRootLayout != null) {
			mRegSuccessAccountEditor = (EditText) mRootLayout.findViewById(Util_Resource.getIdByReflection
					(getActivity(),

					"id",
					"umipay_reg_success_account_box"));
			mRegSuccessPswEditor = (EditText) mRootLayout.findViewById(Util_Resource.getIdByReflection(getActivity(),
					"id",
					"umipay_reg_success_psw_box"));
			mRegSuccessEnterGameBtn = (Button) mRootLayout.findViewById(Util_Resource.getIdByReflection(getActivity(),
					"id",
					"umipay_reg_success_entergame_btn"));
		}

		if (mLoginAccount != null) {
			if (mRegSuccessAccountEditor != null) {
				mRegSuccessAccountEditor.setText(mLoginAccount.getUserName());
				mRegSuccessAccountEditor.setEnabled(false);
			}
			if (mRegSuccessPswEditor != null) {
				mRegSuccessPswEditor.setText(mLoginAccount.getPsw());
				mRegSuccessPswEditor.setEnabled(false);
			}
			if (mRegSuccessEnterGameBtn != null) {
				mRegSuccessEnterGameBtn.setOnClickListener(this);
			}
		}
	}

	private void initListener() {
		if (mRegSuccessEnterGameBtn != null) {
			mRegSuccessEnterGameBtn.setOnClickListener(this);
		}
	}


	@Override
	protected void handleOnClick(View v) {
		if (v.equals(mRegSuccessEnterGameBtn)) {
			enterGame();
			return;
		}
	}

	// 一键注册 进入游戏
	private void enterGame() {
		sendLoginResultMsg(UmipaySDKStatusCode.SUCCESS, null, UmipayLoginInfoDialog.QUCIK_REGISTER, mLoginAccount);
		if(SDKCacheConfig.getInstance(getActivity()).isShowBoard()) {
			replaceFragmentFromActivityFragmentManager(UmipayAnnouncementFragment.newInstance());
		}else {
			getActivity().finish();
		}
	}

	private void sendLoginResultMsg(int code, String msg, int loginType, UmipayAccount account) {
		Gson_Login gsonLogin = new Gson_Login(UmipaySDKManager.getShowLoginViewContext(), code,
				msg, null);
		if (code == UmipaySDKStatusCode.SUCCESS) {
			Gson_Login.Login_Data loginData = gsonLogin.new Login_Data();
			loginData.setLoginType(loginType);
			loginData.setAccount(account);
			gsonLogin.setData(loginData);
		}
		ListenerManager.sendMessage(TaskCMD.MP_CMD_OPENLOGIN, gsonLogin);
	}

	private void screenShot() {

		String msg = "图片保存成功,请到相册查看";
		try {
			/*
			 * 此处是新增加的截图部分，用于注册成功回调时截图，截图命名为用户名的hash值，保存到媒体库中
			 */
			Util_ScreenShot.shot(mLoginAccount.getUserName(), getActivity(), getActivity().getWindow().getDecorView());
		} catch (Throwable e) {
			Debug_Log.e(e);
			msg = "图片保存失败！";
		}
		Toast toast = Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
		toast.show();
	}
}
