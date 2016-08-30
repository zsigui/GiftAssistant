package net.ouwan.umipay.android.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import net.ouwan.umipay.android.Utils.Util_Resource;
import net.ouwan.umipay.android.api.UmipaySDKManager;
import net.ouwan.umipay.android.api.UmipaySDKStatusCode;
import net.ouwan.umipay.android.asynctask.TaskCMD;
import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.entry.UmipayAccount;
import net.ouwan.umipay.android.entry.gson.Gson_Cmd_Mobile_Login_GetAccountList;
import net.ouwan.umipay.android.entry.gson.Gson_Login;
import net.ouwan.umipay.android.interfaces.Interface_Account_Listener_Login;
import net.ouwan.umipay.android.manager.ListenerManager;
import net.ouwan.umipay.android.manager.UmipayCommandTaskManager;
import net.ouwan.umipay.android.view.UmipayAccountSelectAdapter;
import net.ouwan.umipay.android.view.UmipayLoginInfoDialog;

import java.util.ArrayList;

/**
 * Created by jimmy on 2016/8/14.
 */
public class SelectAccountFragment extends BaseFragment implements AdapterView.OnItemClickListener,
		Interface_Account_Listener_Login {


	public ArrayList<Gson_Cmd_Mobile_Login_GetAccountList.Cmd_Account_List_Item_Data> mSelectAccountList;
	private UmipayAccountSelectAdapter mUmipayAccountSelectAdapter;
	private ListView mMobileLoginAccountListView;
	private int mSelectAccount = 0;
	private Button mLoginBtn;

	public static SelectAccountFragment newInstance() {
		SelectAccountFragment fragment = new SelectAccountFragment();
		return fragment;
	}

	public void setSelectAccountList(ArrayList<Gson_Cmd_Mobile_Login_GetAccountList.Cmd_Account_List_Item_Data> list) {
		mSelectAccountList = list;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		try {
			mRootLayout = inflater.inflate(Util_Resource.getIdByReflection(getActivity(), "layout",
					"umipay_select_account_layout"), container, false);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		initViews();
		initListener();
		return mRootLayout;
	}

	private void initViews() {
		if (mRootLayout != null) {
			mLoginBtn = (Button) mRootLayout.findViewById(Util_Resource
					.getIdByReflection(getActivity(), "id", "umipay_login_btn"));
			mMobileLoginAccountListView = (ListView) mRootLayout.findViewById(Util_Resource.getIdByReflection
					(getActivity(),
							"id",
							"umipay_mobile_login_account_list"));
		}
		if (mMobileLoginAccountListView != null) {
			mUmipayAccountSelectAdapter = new UmipayAccountSelectAdapter(getActivity(),
					mSelectAccountList);
			mMobileLoginAccountListView.setAdapter(mUmipayAccountSelectAdapter);

		}
	}

	private void initListener() {
		if (mLoginBtn != null) {
			mLoginBtn.setOnClickListener(this);
		}
		if (mMobileLoginAccountListView != null) {
			mMobileLoginAccountListView.setOnItemClickListener(this);
		}
	}

	private void login() {
		if (mSelectAccountList != null && mSelectAccount < mSelectAccountList.size()) {
			ListenerManager.setCommandLoginListener(this);
			String username = mSelectAccountList.get(mSelectAccount).getUsername();
			String mobile = mSelectAccountList.get(mSelectAccount).getMobile();
			String calling_code = mSelectAccountList.get(mSelectAccount).getCallingCode();
			int uid = mSelectAccountList.get(mSelectAccount).getUid();
			int ts = mSelectAccountList.get(mSelectAccount).getTs();
			UmipayCommandTaskManager.getInstance(getActivity()).MobileLoginCommandTask(calling_code, mobile, uid, ts);
			startProgressDialog();
		} else {
			toast("账号信息异常");
			return;
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

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (parent.equals(mMobileLoginAccountListView) && mUmipayAccountSelectAdapter != null) {
			mUmipayAccountSelectAdapter.onItemClick(position);
		}
		mSelectAccount = position;
	}

	@Override
	protected void handleOnClick(View v) {
		if (v.equals(mLoginBtn)) {
			login();
		}
	}

	@Override
	public void onLogin(int code, String msg, UmipayAccount account) {
		sendMessage(MSG_LOGIN, new MsgData(code, msg, account));
	}

	@Override
	protected void handleLoginMsg(MsgData data) {
		stopProgressDialog();
		try {
			if (data.getCode() == UmipaySDKStatusCode.SUCCESS) {
				UmipayAccount account = (UmipayAccount) data.getData();
				try {
					if (account.getBindOauth() != 0) {
						replaceFragmentFromActivityFragmentManager(BindOauthFragment
								.newInstance(account));
					} else if (account.getBindMobile() != 0) {
						replaceFragmentFromActivityFragmentManager(BindMobileFragment
								.newInstance(account));
					} else {
						sendLoginResultMsg(UmipaySDKStatusCode.SUCCESS, null, UmipayLoginInfoDialog.NORMAL_LOGIN,
								account);

						getActivity().finish();
					}
				} catch (Throwable e) {
					Debug_Log.e(e);
				}
			} else {
				String msg = UmipaySDKStatusCode.handlerMessage(data.getCode(), data.getMsg());
				toast(msg + "(" + data.getCode() + ")");
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}
}
