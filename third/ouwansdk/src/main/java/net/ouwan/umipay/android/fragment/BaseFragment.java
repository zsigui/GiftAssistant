package net.ouwan.umipay.android.fragment;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import net.ouwan.umipay.android.api.FragmentNavigationDelegate;
import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.view.UmipayProgressDialog;

/**
 * Created by jimmy on 2016/8/14.
 */
public class BaseFragment extends Fragment implements View
		.OnClickListener {
	protected static final int MSG_LOGIN_MOBILE = 0;
	protected static final int MSG_LOGIN = 1;
	protected static final int MSG_VERIFICATESMS = 2;
	protected static final int MSG_REGIST = 3;
	protected static final int MSG_QUICKREGIST = 4;
	protected static final int MSG_GET_REGISTRABLE_ACCOUNT = 5;
	protected static final int MSG_MOBILE_LOGIN_VERIFICATESMS = 6;
	protected static final int MSG_MOBILE_LOGIN_GETACCOUNTLIST = 7;
	protected static final int MSG_BIND_OAUTH = 8;

	protected UmipayProgressDialog progressDialog;
	protected View mRootLayout;
	protected InternalHandler mInternalHandler = new InternalHandler(Looper.getMainLooper());
	protected FragmentNavigationDelegate mFragmentNavigationDelegate;


	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof FragmentNavigationDelegate) {
			mFragmentNavigationDelegate = (FragmentNavigationDelegate) activity;
		}
	}

	public void replaceFragmentFromActivityFragmentManager(final BaseFragment toShow) {
		if (mFragmentNavigationDelegate != null) {
			mFragmentNavigationDelegate.replaceFragmentToActivityFragmentManager(toShow);
		}
	}

	protected void stopProgressDialog() {
		if (progressDialog != null) {
			try {
				progressDialog.cancel();
				progressDialog = null;
			} catch (Throwable e) {
				Debug_Log.e(e);
			}
		}
	}

	protected void startProgressDialog() {
		try {
			if (progressDialog == null) {
				progressDialog = UmipayProgressDialog.createDialog(getActivity());
			}
			progressDialog.show();
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

	protected void toast(String text) {
		Toast toast = Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	protected void sendMessage(int what, Object obj) {
		try {
			Message msg = mInternalHandler.obtainMessage(what, obj);
			msg.sendToTarget();
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}


	protected void handleMobileLoginMsg(MsgData msg) {
	}

	protected void handleLoginMsg(MsgData msg) {
	}

	protected void handleVerificateSMSMsg(MsgData msg) {
	}

	protected void handleBindOauthMsg(MsgData msg) {
	}

	protected void handleMobileLoginVerificateCodeMsg(MsgData msg) {
	}

	protected void handleRegistMsg(MsgData msg) {
	}

	protected void handleQuickRegistMsg(MsgData msg) {
	}

	protected void handleGetRegistrableAccountMsg(MsgData msg) {
	}

	protected void handleGetAccountListMsg(MsgData msg) {
	}

	protected void handleOnClick(View view) {

	}

	public void onWindowFocusChanged(boolean hasFocus) {
	}

	@Override
	public void onClick(View v) {
		try {
			handleOnClick(v);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

	protected class InternalHandler extends Handler {
		public InternalHandler(Looper mainLooper) {
			super(mainLooper);
		}

		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
				case MSG_LOGIN_MOBILE:
					handleMobileLoginMsg((MsgData) msg.obj);
					break;
				case MSG_LOGIN:
					handleLoginMsg((MsgData) msg.obj);
					break;
				case MSG_VERIFICATESMS:
					handleVerificateSMSMsg((MsgData) msg.obj);
					break;
				case MSG_BIND_OAUTH:
					handleBindOauthMsg((MsgData) msg.obj);
					break;
				case MSG_MOBILE_LOGIN_VERIFICATESMS:
					handleMobileLoginVerificateCodeMsg((MsgData) msg.obj);
					break;
				case MSG_REGIST:
					handleRegistMsg((MsgData) msg.obj);
					break;
				case MSG_QUICKREGIST:
					handleQuickRegistMsg((MsgData) msg.obj);
					break;
				case MSG_GET_REGISTRABLE_ACCOUNT:
					handleGetRegistrableAccountMsg((MsgData) msg.obj);
					break;
				case MSG_MOBILE_LOGIN_GETACCOUNTLIST:
					handleGetAccountListMsg((MsgData) msg.obj);
					break;
			}
		}
	}


	protected class MsgData {
		private int code;
		private String msg;
		private Object data;

		public MsgData(int code, String msg, Object data) {
			this.code = code;
			this.msg = msg;
			this.data = data;
		}

		public int getCode() {
			return code;
		}

		public void setCode(int code) {
			this.code = code;
		}

		public String getMsg() {
			return msg;
		}

		public void setMsg(String msg) {
			this.msg = msg;
		}

		public Object getData() {
			return data;
		}

		public void setData(Object data) {
			this.data = data;
		}
	}
}
