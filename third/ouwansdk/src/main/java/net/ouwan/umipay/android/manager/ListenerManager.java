package net.ouwan.umipay.android.manager;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import net.ouwan.umipay.android.api.AccountCallbackListener;
import net.ouwan.umipay.android.api.ActionCallbackListener;
import net.ouwan.umipay.android.api.CommonAccountViewListener;
import net.ouwan.umipay.android.api.InitCallbackListener;
import net.ouwan.umipay.android.api.PayCallbackListener;
import net.ouwan.umipay.android.asynctask.TaskCMD;
import net.ouwan.umipay.android.asynctask.handler.CommonRspHandler;
import net.ouwan.umipay.android.asynctask.handler.RspHandler_CMD_INIT;
import net.ouwan.umipay.android.asynctask.handler.RspHandler_Cmd_DeleteAccount;
import net.ouwan.umipay.android.asynctask.handler.RspHandler_Cmd_GetAccountList;
import net.ouwan.umipay.android.asynctask.handler.RspHandler_Cmd_Login;
import net.ouwan.umipay.android.asynctask.handler.RspHandler_Cmd_Logout;
import net.ouwan.umipay.android.asynctask.handler.RspHandler_Cmd_PushGameInfo;
import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.entry.UmipayAccount;
import net.ouwan.umipay.android.interfaces.Interface_Account_Listener_GetAccountList;
import net.ouwan.umipay.android.interfaces.Interface_Account_Listener_Get_Registrable_Account;
import net.ouwan.umipay.android.interfaces.Interface_Account_Listener_Login;
import net.ouwan.umipay.android.interfaces.Interface_Account_Listener_Register;
import net.ouwan.umipay.android.interfaces.Interface_Account_Listener_Validate_Session;
import net.ouwan.umipay.android.interfaces.Interface_Bind_Oauth_Listener;
import net.ouwan.umipay.android.interfaces.Interface_GetPush_Listener;
import net.ouwan.umipay.android.interfaces.Interface_Mobile_Login_Verificate_Code_Listener;
import net.ouwan.umipay.android.interfaces.Interface_Verificate_SMS_Listener;
import net.youmi.android.libs.common.debug.Debug_SDK;

/**
 * ListenerManager
 *
 * @author zacklpx
 *         date 15-1-28
 *         description
 */
public class ListenerManager {

    private static AccountCallbackListener mAccountCallbackListener;

    private static InitCallbackListener mInitCallbackListener;

    private static PayCallbackListener mPayCallbackListener;

    private static Interface_Account_Listener_Login mCommandLoginListener;

    private static Interface_Verificate_SMS_Listener mCommandVerificateSMSListener;

    private static Interface_Mobile_Login_Verificate_Code_Listener mCommandMobileLoginVerificateSMSListener;

    private static Interface_Account_Listener_Register mCommandRegistListener;

    private static Interface_GetPush_Listener mCommandGetPushListener;

    private static ActionCallbackListener mActionCallbackListener;

    private static Interface_Account_Listener_Get_Registrable_Account mCommandGetRegistrableAccountListener;

    private static Interface_Account_Listener_GetAccountList mCommandGetAccountList;

    private static Interface_Bind_Oauth_Listener mCommandBindOauth;

    private static Interface_Account_Listener_Validate_Session mCommanValidateSession;

    private static Handler mHandler = new InternalHandler(Looper.getMainLooper());

    private static CommonAccountViewListener mCommonAccountViewListener;

    public static AccountCallbackListener getAccountCallbackListener() {
        return mAccountCallbackListener;
    }

    public static void setAccountCallbackListener(AccountCallbackListener accountCallbackListener) {
        mAccountCallbackListener = accountCallbackListener;
    }

    public static InitCallbackListener getInitCallbackListener() {
        return mInitCallbackListener;
    }

    public static void setInitCallbackListener(InitCallbackListener initCallbackListener) {
        mInitCallbackListener = initCallbackListener;
    }

    public static PayCallbackListener getPayCallbackListener() {
        return mPayCallbackListener;
    }

    public static void setPayCallbackListener(PayCallbackListener payCallbackListener) {
        mPayCallbackListener = payCallbackListener;
    }

    public static Interface_Account_Listener_Login getCommandLoginListener() {
        return mCommandLoginListener;
    }

    public static void setCommandLoginListener(Interface_Account_Listener_Login commandLoginListener) {
        mCommandLoginListener = commandLoginListener;
    }

    public static Interface_Verificate_SMS_Listener getCommandVerificateSMSListener() {
        return mCommandVerificateSMSListener;
    }

    public static void setCommandVerificateSMSListener(Interface_Verificate_SMS_Listener
                                                               commandVerificateSMSListener) {
        mCommandVerificateSMSListener = commandVerificateSMSListener;
    }

    public static Interface_Account_Listener_Register getCommandRegistListener() {
        return mCommandRegistListener;
    }

    public static void setCommandRegistListener(Interface_Account_Listener_Register commandRegistListener) {
        mCommandRegistListener = commandRegistListener;
    }

    public static Interface_GetPush_Listener getCommandGetPushListener() {
        return mCommandGetPushListener;
    }

    public static void setCommandGetPushListener(Interface_GetPush_Listener commandGetPushListener) {
        mCommandGetPushListener = commandGetPushListener;
    }

    public static ActionCallbackListener getActionCallbackListener() {
        return mActionCallbackListener;
    }

    public static void setActionCallbackListener(ActionCallbackListener mActionCallbackListener) {
        ListenerManager.mActionCallbackListener = mActionCallbackListener;
    }

    public static Interface_Mobile_Login_Verificate_Code_Listener getCommandMobileLoginGetCodeListener() {
        return mCommandMobileLoginVerificateSMSListener;
    }

    public static void setCommandMobileLoginVerificateSMSListener(Interface_Mobile_Login_Verificate_Code_Listener
                                                                          commandMobileLoginVerificateSMSListener) {
        mCommandMobileLoginVerificateSMSListener = commandMobileLoginVerificateSMSListener;
    }

    public static Interface_Account_Listener_Get_Registrable_Account getCommandGetRegistrableAccountListener() {
        return mCommandGetRegistrableAccountListener;
    }

    public static void setCommandGetRegistrableAccountListener(Interface_Account_Listener_Get_Registrable_Account commandGetRegistrableAccountListener) {
        mCommandGetRegistrableAccountListener = commandGetRegistrableAccountListener;
    }

    public static Interface_Account_Listener_GetAccountList getCommandGetAccountList() {
        return mCommandGetAccountList;
    }

    public static void setCommandGetAccountListListener(Interface_Account_Listener_GetAccountList commandGetAccountList) {
        mCommandGetAccountList = commandGetAccountList;
    }

    public static Interface_Bind_Oauth_Listener getmCommandBindOauth(){
        return mCommandBindOauth;
    }

    public static void setmCommandBindOauth(Interface_Bind_Oauth_Listener interface_bind_oauth_listener){
        mCommandBindOauth = interface_bind_oauth_listener;
    }
    public static Interface_Account_Listener_Validate_Session getCommanValidateSession(){
        return mCommanValidateSession;
    }

    public static void setCommanValidateSession(Interface_Account_Listener_Validate_Session interface_account_listener_validate_session){
        mCommanValidateSession = interface_account_listener_validate_session;
    }

    public static CommonAccountViewListener getCommonAccountViewListener() {
        return mCommonAccountViewListener;
    }

    public static void setCommonAccountViewListener(CommonAccountViewListener commonAccountViewListener) {
        mCommonAccountViewListener = commonAccountViewListener;
    }

    public static void callbackInitFinish(int code, String msg) {
        try {
            mInitCallbackListener.onSdkInitFinished(code, msg);
        } catch (Throwable e) {
            Debug_Log.e(e);
        }
    }

    public static void callbackPay(int code) {
        try {
            if (getPayCallbackListener() != null) {
                getPayCallbackListener().onPay(code);
            }
        } catch (Throwable e) {
            Debug_Log.e(e);
        }
    }

    public static void callbackAction(final int action, final int code) {
        try {
            if (mActionCallbackListener != null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mActionCallbackListener.onActionCallback(action, code);
                    }
                }).start();
            }
        } catch (Throwable e) {
            Debug_SDK.e(e);
        }
    }


    public static String getUserName(String usernamemail, int accountType) {
        switch (accountType) {
            case UmipayAccount.TYPE_QQ:
                usernamemail = "QQ用户";
                break;
            case UmipayAccount.TYPE_SINA:
                usernamemail = "微博用户";
                break;
            case UmipayAccount.TYPE_VISITOR:
                usernamemail = "游客用户";
                break;
        }
        return usernamemail;
    }

    public static void sendMessage(int what, Object obj) {
        Message msg = mHandler.obtainMessage(what, obj);
        msg.sendToTarget();
    }

    private static class InternalHandler extends Handler {
        public InternalHandler(Looper looper) {
            super(looper);
        }

        @SuppressWarnings({"unchecked", "RawUseOfParameterizedType"})
        @Override
        public void handleMessage(Message msg) {
            CommonRspHandler handler = null;
            switch (msg.what) {
                case TaskCMD.MP_CMD_INIT:
                    handler = new RspHandler_CMD_INIT();
                    break;
                case TaskCMD.MP_CMD_GETACCOUNTLIST:
                    handler = new RspHandler_Cmd_GetAccountList();
                    break;
                case TaskCMD.MP_CMD_OPENLOGIN:
                    handler = new RspHandler_Cmd_Login();
                    break;
                case TaskCMD.MP_CMD_OPENUSERDELETE:
                    handler = new RspHandler_Cmd_DeleteAccount();
                    break;
                case TaskCMD.MP_CMD_PUSHROLEINFO:
                    handler = new RspHandler_Cmd_PushGameInfo();
                    break;
                case TaskCMD.MP_CMD_LOGOUT:
                    handler = new RspHandler_Cmd_Logout();
                    break;
            }
            if (handler != null) {
                handler.toHandle(msg.obj);
            }
        }
    }
}
