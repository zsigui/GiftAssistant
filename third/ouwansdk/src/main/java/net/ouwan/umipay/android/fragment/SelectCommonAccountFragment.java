package net.ouwan.umipay.android.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import net.ouwan.umipay.android.Utils.Util_Resource;
import net.ouwan.umipay.android.api.CommonAccountViewListener;
import net.ouwan.umipay.android.api.UmipaySDKManager;
import net.ouwan.umipay.android.api.UmipaySDKStatusCode;
import net.ouwan.umipay.android.asynctask.TaskCMD;
import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.entry.UmipayAccount;
import net.ouwan.umipay.android.entry.UmipayCommonAccount;
import net.ouwan.umipay.android.entry.gson.Gson_Login;
import net.ouwan.umipay.android.interfaces.Interface_Account_Listener_Login;
import net.ouwan.umipay.android.manager.ListenerManager;
import net.ouwan.umipay.android.manager.UmipayCommonAccountCacheManager;
import net.ouwan.umipay.android.view.UmipayCommonAccountSelectAdapter;
import net.ouwan.umipay.android.view.UmipayLoginInfoDialog;

import java.util.ArrayList;

/**
 * Created by jimmy on 2016/8/14.
 */
public class SelectCommonAccountFragment extends BaseFragment implements AdapterView.OnItemClickListener,
        Interface_Account_Listener_Login, CommonAccountViewListener.ResultActionCallback {


    public ArrayList<UmipayCommonAccount> mSelectAccountList;
    private UmipayCommonAccountSelectAdapter mUmipayAccountSelectAdapter;
    private ListView mCommonAccountListView;
    private int mSelectAccount = 0;
    private Button mLoginBtn;
    private Button mReturnBtn;

    public static SelectCommonAccountFragment newInstance() {
        SelectCommonAccountFragment fragment = new SelectCommonAccountFragment();
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
                    "umipay_select_common_account_layout"), container, false);
        } catch (Throwable e) {
            Debug_Log.e(e);
        }
        mSelectAccountList = UmipayCommonAccountCacheManager.getInstance(getActivity()).getCommonAccountList
		        (UmipayCommonAccountCacheManager.COMMON_ACCOUNT);
        initViews();
        initListener();
        return mRootLayout;
    }

    private void initViews() {
        if (mRootLayout != null) {
            mLoginBtn = (Button) mRootLayout.findViewById(Util_Resource
                    .getIdByReflection(getActivity(), "id", "umipay_login_btn"));
            mReturnBtn = (Button) mRootLayout.findViewById(Util_Resource
                    .getIdByReflection(getActivity(), "id", "umipay_cancel_btn"));
            mCommonAccountListView = (ListView) mRootLayout.findViewById(Util_Resource.getIdByReflection
                    (getActivity(),
                            "id",
                            "umipay_mobile_login_account_list"));
        }
        if (mCommonAccountListView != null) {
            mUmipayAccountSelectAdapter = new UmipayCommonAccountSelectAdapter(getActivity(),
                    mSelectAccountList);
            mCommonAccountListView.setAdapter(mUmipayAccountSelectAdapter);

        }
    }

    private void initListener() {
        if (mLoginBtn != null) {
            mLoginBtn.setOnClickListener(this);
        }
        if (mReturnBtn != null) {
            mReturnBtn.setOnClickListener(this);
        }
        if (mCommonAccountListView != null) {
            mCommonAccountListView.setOnItemClickListener(this);
        }
    }

    private void login() {
        if (mSelectAccountList != null && mSelectAccount < mSelectAccountList.size()) {
            UmipayCommonAccount account = mSelectAccountList.get(mSelectAccount);
//            ListenerManager.setCommandLoginListener(this);
//            UmipayCommandTaskManager.getInstance(getActivity()).AutoLoginCommandTask(account.getUserName(), account
//		            .getUid(), account.getSession());
            if(ListenerManager.getCommonAccountViewListener() != null) {
                startProgressDialog();
                ListenerManager.getCommonAccountViewListener().onChooseAccount(
                        CommonAccountViewListener.CODE_SELECT_ACCOUNT,
                        account,
                        this
                );
            }
        } else {
            toast("账号信息异常");
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
        if (parent.equals(mCommonAccountListView) && mUmipayAccountSelectAdapter != null) {
            mUmipayAccountSelectAdapter.onItemClick(position);
        }
        mSelectAccount = position;
    }

    @Override
    protected void handleOnClick(View v) {
        if (v.equals(mLoginBtn)) {
            login();
        }
        if (v.equals(mReturnBtn)) {
//            if (SDKCacheConfig.getInstance(getActivity()).isLightSDK()) {
//                replaceFragmentFromActivityFragmentManager(LightSDKLoginFragment.newInstance());
//            } else {
//                replaceFragmentFromActivityFragmentManager(RegisterFragment.newInstance());
//            }
            if(ListenerManager.getCommonAccountViewListener() != null) {
                ListenerManager.getCommonAccountViewListener().onChooseAccount(
                        CommonAccountViewListener.CODE_SELECT_ACCOUNT,
                        null,
                        this
                );
            }
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
                    sendLoginResultMsg(UmipaySDKStatusCode.SUCCESS, null, UmipayLoginInfoDialog.NORMAL_LOGIN,
                            account);
                    getActivity().finish();
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

    @Override
    public void onSuccess(Object obj) {
        stopProgressDialog();
        getActivity().finish();
    }

    @Override
    public void onFailed(int code, String msg) {
        stopProgressDialog();
    }

    @Override
    public void onCancel() {
        stopProgressDialog();
    }
}
