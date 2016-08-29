package com.oplay.giftcool.manager;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.google.gson.reflect.TypeToken;
import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.config.AppConfig;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.config.NetStatusCode;
import com.oplay.giftcool.config.SPConfig;
import com.oplay.giftcool.config.WebViewUrl;
import com.oplay.giftcool.config.util.UserTypeUtil;
import com.oplay.giftcool.listener.OnFinishListener;
import com.oplay.giftcool.model.MobileInfoModel;
import com.oplay.giftcool.model.data.resp.UpdateSession;
import com.oplay.giftcool.model.data.resp.UserInfo;
import com.oplay.giftcool.model.data.resp.UserModel;
import com.oplay.giftcool.model.data.resp.UserSession;
import com.oplay.giftcool.model.data.resp.message.AwardNotify;
import com.oplay.giftcool.model.data.resp.message.MessageCentralUnread;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.activity.MainActivity;
import com.oplay.giftcool.util.DateUtil;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.NetworkUtil;
import com.oplay.giftcool.util.SPUtil;
import com.oplay.giftcool.util.ToastUtil;
import com.oplay.giftcool.util.encrypt.NetDataEncrypt;

import net.youmi.android.libs.common.global.Global_SharePreferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 管理用户账号信息的管理器 <br/>
 * Created by zsigui on 15-12-25.
 */
public class AccountManager implements OnFinishListener {

    private static AccountManager manager;
    private static Context mContext = AssistantApp.getInstance().getApplicationContext();

    private AccountManager() {
    }

    public static AccountManager getInstance() {
        if (manager == null) {
            manager = new AccountManager();
        }
        return manager;
    }

    private UserModel mUser;
    private int lastLoginType = UserTypeUtil.TYPE_POHNE;

    public boolean isPhoneLogin() {
        return lastLoginType == UserTypeUtil.TYPE_POHNE;
    }

    public UserModel getUser() {
        return mUser;
    }


    /**
     * 设置当前用户，默认进行 USER_UPDATE_PART 状态通知
     *
     * @param user
     */
    public void notifyUserPart(UserModel user) {
        notifyUser(user, false);
    }

    /**
     * 设置当前用户，默认进行 USER_UPDATE_ALL 状态通知
     *
     * @param user
     */
    public void notifyUserAll(UserModel user) {
        notifyUser(user, true);
    }

    /**
     * 设置当前用户，会引起监听该变化的接口调用进行通知
     *
     * @param notifyAll 是否进行 USER_UPDATE_ALL 状态通知
     */
    private void notifyUser(UserModel user, boolean notifyAll) {
        if (mUser != null && mUser.userInfo != null) {
            lastLoginType = mUser.userInfo.loginType;
            if (user != null && user.userInfo != null) {
                // 保留用户登录状态
                user.userInfo.loginType = mUser.userInfo.loginType;
            }
        } else {
            lastLoginType = UserTypeUtil.TYPE_POHNE;
        }
        mUser = user;

        // 同步cookie
        syncCookie();
        if (isLogin()) {
            NetDataEncrypt.getInstance().initDecryptDataModel(getUserSesion().uid, getUserSesion().session);
        } else {
            NetDataEncrypt.getInstance().initDecryptDataModel(0, "");
        }

        // 当用户变化，需要进行通知
        if (notifyAll) {
            ObserverManager.getInstance().notifyUserUpdate(ObserverManager.STATUS.USER_UPDATE_ALL);
            ObserverManager.getInstance().notifyGiftUpdate(ObserverManager.STATUS.GIFT_UPDATE_ALL);
            // 更新未读消息数量
            obtainUnreadPushMessageCount();
            PushMessageManager.getInstance().updateJPushTagAndAlias(mContext);
        } else {
            ObserverManager.getInstance().notifyUserUpdate(ObserverManager.STATUS.USER_UPDATE_PART);
        }

        if (isLogin()) {
            OuwanSDKManager.getInstance().login();
        } else {
            OuwanSDKManager.getInstance().logout();
        }

        Global.THREAD_POOL.execute(new Runnable() {
            @Override
            public void run() {
                // 写入SP中
                String userJson = AssistantApp.getInstance().getGson().toJson(mUser, UserModel.class);
                Global_SharePreferences.saveEncodeStringToSharedPreferences(mContext,
                        SPConfig.SP_USER_INFO_FILE, SPConfig.KEY_USER_INFO, userJson, SPConfig.SALT_USER_INFO);
                AppDebugConfig.d(AppDebugConfig.TAG_MANAGER, "save to sp : " + userJson);
            }
        });
    }

    public boolean isLogin() {
        return (mUser != null
                && getUserSesion() != null
                && getUserInfo() != null
                && !TextUtils.isEmpty(getUserSesion().session)
                && getUserInfo().uid != 0);
    }

    public UserInfo getUserInfo() {
        return mUser == null ? null : mUser.userInfo;
    }

    public UserSession getUserSesion() {
        return mUser == null ? null : mUser.userSession;
    }

    /**
     * 获取用户全部信息的网络请求声明
     */
    private Call<JsonRespBase<UserModel>> mCallGetUserInfo;

    /**
     * 重新请求服务器以更新用户全部信息
     */
    public void updateUserInfo() {
        if (isLogin()) {
            NetDataEncrypt.getInstance().initDecryptDataModel(getUserSesion().uid, getUserSesion().session);
            Global.THREAD_POOL.execute(new Runnable() {
                @Override
                public void run() {
                    if (!NetworkUtil.isConnected(mContext)) {
                        return;
                    }
                    if (mCallGetUserInfo != null) {
                        mCallGetUserInfo.cancel();
                        mCallGetUserInfo = mCallGetUserInfo.clone();
                    } else {
                        mCallGetUserInfo = Global.getNetEngine().getUserInfo(new JsonReqBase<Void>());
                    }
                    mCallGetUserInfo.enqueue(new Callback<JsonRespBase<UserModel>>() {
                        @Override
                        public void onResponse(Call<JsonRespBase<UserModel>> call,
                                               Response<JsonRespBase<UserModel>> response) {
                            if (call.isCanceled() || !isLogin()) {
                                return;
                            }
                            if (response != null && response.isSuccessful()) {
                                if (response.body() != null && response.body().isSuccess()) {
                                    final UserModel userModel = response.body().getData();
                                    if (userModel == null) {
                                        return;
                                    }
                                    if (!isLogin()) {
                                        // 重置登录
                                        notifyUserAll(null);
                                        return;
                                    }
                                    UserModel user = getUser();
                                    user.userInfo = userModel.userInfo;
                                    notifyUserAll(user);
//                                    updateJPushTagAndAlias();
                                    return;
                                }
                                // 登录状态失效，原因包括: 已在其他地方登录，更新失败
                                sessionFailed(response.body());
                            }
                            AppDebugConfig.w(AppDebugConfig.TAG_MANAGER, response);
                        }

                        @Override
                        public void onFailure(Call<JsonRespBase<UserModel>> call, Throwable t) {
                            AppDebugConfig.w(AppDebugConfig.TAG_MANAGER, t);
                        }
                    });
                }
            });
        }
    }

    /**
     * 登录态失效
     */
    private void sessionFailed(JsonRespBase response) {
        if (judgeIsSessionFailed(response)) {
//            ToastUtil.showShort(mContext.getResources().getString(R.string.st_hint_un_login));
            ToastUtil.showShort(ConstString.TOAST_SESSION_UNAVAILABLE);
            MainActivity.sIsLoginStateUnavailableShow = true;
        }
    }

    public boolean judgeIsSessionFailed(JsonRespBase response) {
        if (response != null
                && (response.getCode() == NetStatusCode.ERR_UN_LOGIN
                || response.getCode() == NetStatusCode.ERR_BAD_USER_SERVER)) {
            notifyUserAll(null);
            return true;
        }
        return false;
    }

    /**
     * 更新用户部分信息的网络请求声明
     */
    private Call<JsonRespBase<UserInfo>> mCallUpdatePartInfo;

    private boolean isUpdatePart = false;
    /**
     * 更新用户部分信息: 偶玩豆，金币，礼包数
     */
    public void updatePartUserInfo() {
        if (isLogin()) {
            if (!isUpdatePart) {
                isUpdatePart = true;
                Global.THREAD_POOL.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (!NetworkUtil.isConnected(mContext)) {
                            isUpdatePart = false;
                            return;
                        }

                        if (mCallUpdatePartInfo != null) {
                            mCallUpdatePartInfo.cancel();
                            mCallUpdatePartInfo = mCallUpdatePartInfo.clone();
                        } else {
                            mCallUpdatePartInfo = Global.getNetEngine().getUserPartInfo(new JsonReqBase<Void>());
                        }
                        mCallUpdatePartInfo.enqueue(new Callback<JsonRespBase<UserInfo>>() {
                            @Override
                            public void onResponse(Call<JsonRespBase<UserInfo>> call, Response<JsonRespBase
                                    <UserInfo>> response) {
                                isUpdatePart = false;
                                if (call.isCanceled() || !isLogin()) {
                                    return;
                                }
                                if (response != null && response.isSuccessful()) {
                                    if (response.body() != null && response.body().isSuccess()) {
                                        UserModel user = getUser();
                                        if (!isLogin()) {
                                            // 重置登录信息，表示未登录
                                            notifyUserAll(null);
                                            return;
                                        }
                                        UserInfo info = response.body().getData();
                                        if (info == null) {
                                            return;
                                        }
                                        user.userInfo.score = info.score;
                                        user.userInfo.bean = info.bean;
                                        user.userInfo.giftCount = info.giftCount;
                                        notifyUserPart(user);
                                        return;
                                    }
                                    sessionFailed(response.body());
                                }
                                AppDebugConfig.warnResp(AppDebugConfig.TAG_MANAGER, response);
                            }

                            @Override
                            public void onFailure(Call<JsonRespBase<UserInfo>> call, Throwable t) {
                                isUpdatePart = false;
                                AppDebugConfig.w(AppDebugConfig.TAG_MANAGER, t);
                            }
                        });
                    }
                });
            }
        }
    }


    /**
     * 同步Cookie
     */
    public void syncCookie(String baseUrl, ArrayList<String> cookies) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().removeAllCookies(null);
            if (cookies != null && !cookies.isEmpty()) {
                for (String cookie : cookies) {
                    CookieManager.getInstance().setCookie(baseUrl, cookie);
                }
            }
            CookieManager.getInstance().flush();
        } else {
            CookieSyncManager.createInstance(mContext);
            CookieManager.getInstance().setAcceptCookie(true);
            CookieManager.getInstance().removeAllCookie();
            if (cookies != null && !cookies.isEmpty()) {
                for (String cookie : cookies) {
                    CookieManager.getInstance().setCookie(baseUrl, cookie);
                }
            }
            CookieSyncManager.getInstance().sync();
        }
    }

    /**
     * 登录或者登出后进行全局的浏览器Cookie重设
     */
    public void syncCookie() {
        ArrayList<String> cookies = new ArrayList<String>();
        String expiredDate = DateUtil.getGmtDate(48);
        if (isLogin()) {
            cookies.add(String.format("cuid=%s;Domain=%s;Expires=%s;Path=/;HttpOnly",
                    getUserSesion().uid, WebViewUrl.URL_DOMAIN, expiredDate));
            cookies.add(String.format("sessionid=%s;Domain=%s;Expires=%s;Path=/;HttpOnly",
                    getUserSesion().session, WebViewUrl.URL_DOMAIN, expiredDate));
        }

        HashMap<String, String> cookieMap = new HashMap<>();
        cookieMap.put("version", String.valueOf(AppConfig.SDK_VER));
        cookieMap.put("version_code", String.valueOf(AppConfig.SDK_VER));
        cookieMap.put("version_name", AppConfig.SDK_VER_NAME);
        cookieMap.put("imei", MobileInfoModel.getInstance().getImei());
        cookieMap.put("imsi", MobileInfoModel.getInstance().getImsi());
        cookieMap.put("cid", MobileInfoModel.getInstance().getCid());
        cookieMap.put("mac", MobileInfoModel.getInstance().getMac());
        cookieMap.put("apn", MobileInfoModel.getInstance().getApn());
        cookieMap.put("cn", MobileInfoModel.getInstance().getCn());
        cookieMap.put("dd", MobileInfoModel.getInstance().getDd());
        cookieMap.put("dv", MobileInfoModel.getInstance().getDv());
        cookieMap.put("os", MobileInfoModel.getInstance().getOs());
        cookieMap.put("chn", String.valueOf(MobileInfoModel.getInstance().getChn()));
        cookieMap.put("chnid", String.valueOf(MobileInfoModel.getInstance().getChn()));
        cookieMap.put("X-Client-Info", AssistantApp.getInstance().getHeaderValue());

        for (Map.Entry<String, String> cookie : cookieMap.entrySet()) {
            cookies.add(String.format("%s=%s;Domain=%s;Expires=%s;Path=/;", cookie.getKey(), cookie.getValue()
                    , WebViewUrl.URL_DOMAIN, expiredDate));
        }
        syncCookie(WebViewUrl.URL_BASE, cookies);
    }

    /**
     * 更新用户的Session
     */
    public void updateUserSession() {
        if (isLogin()) {
            // 当天登录过，无须再次重新更新 session
            long lastTime = AssistantApp.getInstance().getLastLaunchTime();
            if (DateUtil.isToday(lastTime)) {
                updateUserInfo();
                return;
            }
            // session只能保持7天，一旦超时，需要重新登录
            // 不对超时做处理
//            if (getUserSesion().lastUpdateTime + 7 * 24 * 60 * 60 * 1000 > System.currentTimeMillis()) {
//                ToastUtil.showShort("登录超时，需要重新登录");
//                notifyUserAll(null);
//                return;
//            }
            NetDataEncrypt.getInstance().initDecryptDataModel(getUserSesion().uid, getUserSesion().session);
            updateSessionNetRequest();
        }
    }

    /**
     * 进行更新用户登录Session的网络请求声明
     */
    private Call<JsonRespBase<UpdateSession>> mCallUpdateSession;

    private long mLastRequestTime = 0;

    /**
     * 更新Session的实际网络请求方法
     */
    public void updateSessionNetRequest() {
        if (NetworkUtil.isConnected(mContext)) {
            long curTime = System.currentTimeMillis();
            if (curTime - mLastRequestTime < Global.CLICK_TIME_INTERVAL) {
                mLastRequestTime = curTime;
                return;
            }
            if (mCallUpdateSession != null) {
                mCallUpdateSession.cancel();
                mCallUpdateSession = mCallUpdateSession.clone();
            } else {
                mCallUpdateSession = Global.getNetEngine().updateSession(new JsonReqBase<String>());
            }
            mCallUpdateSession.enqueue(new Callback<JsonRespBase<UpdateSession>>() {

                @Override
                public void onResponse(Call<JsonRespBase<UpdateSession>> call, Response<JsonRespBase
                        <UpdateSession>> response) {
                    if (call.isCanceled() || !isLogin()) {
                        return;
                    }
                    if (response != null && response.isSuccessful()) {
                        if (response.body() != null) {
                            if (response.body().isSuccess()) {
                                if (getUserSesion() == null) {
                                    // 重置登录信息，表示未登录
                                    notifyUserAll(null);
                                    return;
                                }
                                UpdateSession sessionData = response.body().getData();
                                if (sessionData == null) {
                                    // 获取为空，再次进行session请求
                                    updateSessionNetRequest();
                                    return;
                                }
                                getUserSesion().session = sessionData.session;
                                if (AssistantApp.getInstance().getSetupOuwanAccount() == KeyConfig.KEY_LOGIN_NOT_BIND
                                        || sessionData.info.bindOuwanStatus == 1) {
                                    notifyUserPart(mUser);
                                    // 请求更新数据
                                    updateUserInfo();
                                    StatisticsManager.getInstance().trace(mContext,
                                            StatisticsManager.ID.USER_LOGIN_WITH_SESSION,
                                            StatisticsManager.ID.STR_USER_LOGIN_WITH_SESSION);
                                } else {
                                    IntentUtil.jumpBindOwan(mContext, mUser);
                                }
                                return;
                            }
                            judgeIsSessionFailed(response.body());
                        }
                    }
                    AppDebugConfig.warnResp(AppDebugConfig.TAG_MANAGER, response);
                }

                @Override
                public void onFailure(Call<JsonRespBase<UpdateSession>> call, Throwable t) {
                    AppDebugConfig.w(AppDebugConfig.TAG_MANAGER, t);
                }
            });
        }
    }

    /**
     * 通知退出登录的网络请求声明
     */
    private Call<JsonRespBase<Void>> mCallLogout;

    /**
     * 登出当前账号，会通知服务器并刷新整个页面
     */
    public void logout() {
        if (!isLogin()) {
            return;
        }
        PushMessageManager.getInstance().unsetAlias(mContext, String.valueOf(getUserInfo().uid));
        Global.THREAD_POOL.execute(new Runnable() {
            @Override
            public void run() {
                if (mCallLogout != null) {
                    mCallLogout.cancel();
                    mCallLogout = mCallLogout.clone();
                } else {
                    mCallLogout = Global.getNetEngine().logout(new JsonReqBase<Object>());
                }
                mCallLogout.enqueue(new Callback<JsonRespBase<Void>>() {
                    @Override
                    public void onResponse(Call<JsonRespBase<Void>> call, Response<JsonRespBase<Void>> response) {
                        AppDebugConfig.warnResp(AppDebugConfig.TAG_MANAGER, response);
                    }

                    @Override
                    public void onFailure(Call<JsonRespBase<Void>> call, Throwable t) {
                        AppDebugConfig.w(AppDebugConfig.TAG_MANAGER, t);
                    }
                });
            }
        });
        AccountManager.getInstance().notifyUserAll(null);
        SocketIOManager.getInstance().close();
    }


    private int mUnreadMessageCount = 0;

    public void setUnreadMessageCount(int unreadMessageCount) {
        mUnreadMessageCount = unreadMessageCount;
    }

    /**
     * 获取未读消息数量
     *
     * @return
     */
    public int getUnreadMessageCount() {
        return mUnreadMessageCount;
    }


    /**
     * 获取未读推送消息的网络请求声明
     */
    private Call<JsonRespBase<MessageCentralUnread>> mCallObtainUnread;

    /**
     * 获取未读推送消息数量
     */
    public void obtainUnreadPushMessageCount() {
        if (!isLogin()) {
            // 未登录，推送消息默认为0
            setUnreadMessageCount(0);
            ObserverManager.getInstance().notifyUserUpdate(ObserverManager.STATUS.USER_UPDATE_PUSH_MESSAGE);
            return;
        }
        if (mCallObtainUnread != null) {
            mCallObtainUnread.cancel();
            mCallObtainUnread = mCallObtainUnread.clone();
        } else {
            mCallObtainUnread = Global.getNetEngine().obtainUnreadMessageCount(new JsonReqBase<Void>());
        }
        mCallObtainUnread.enqueue(new Callback<JsonRespBase<MessageCentralUnread>>() {
            @Override
            public void onResponse(Call<JsonRespBase<MessageCentralUnread>> call,
                                   Response<JsonRespBase<MessageCentralUnread>>
                                           response) {
                if (call.isCanceled()) {
                    return;
                }
                if (response != null && response.isSuccessful()) {
                    if (response.body() != null && response.body().isSuccess()) {
                        final MessageCentralUnread unread = response.body().getData();
                        if (unread != null) {
                            final int count = unread.unreadAdmireCount + unread.unreadNewGiftCount
                                    + unread.unreadCommentCount + unread.unreadSystemCount;
                            setUnreadMessageCount(count);
                            Global.updateMsgCentralData(mContext, unread);
//                            if ((int)(Math.random() * 2) == 0) {
//                                // 测试数据
//                                unread.mAwardNotifies = new ArrayList<AwardNotify>();
//                                for (int i = 0; i < 3; i++) {
//                                    AwardNotify notify = new AwardNotify();
//                                    notify.type = KeyConfig.TYPE_AWARD_GIFT;
//                                    switch (notify.type){
//                                        case KeyConfig.TYPE_AWARD_GIFT:
//                                            notify.description = "【刀塔传奇】白金礼包X1";
//                                            break;
//                                        case KeyConfig.TYPE_AWARD_BEAN:
//                                            notify.description = "金币X" + ((int)(Math.random() * 100) + 100);
//                                            break;
//                                        case KeyConfig.TYPE_AWARD_SCORE:
//                                            notify.description = "偶玩豆X" + ((int)(Math.random() * 10) + 10);
//                                            break;
//                                        default:
//                                            notify.description = "爱奇艺账号X1";
//                                    }
//                                    unread.mAwardNotifies.add(notify);
//                                }
//                            }
                            if (unread.mAwardNotifies != null && unread.mAwardNotifies.size() > 0) {
                                AppDebugConfig.d(AppDebugConfig.TAG_WARN, "award is write : " + unread.mAwardNotifies.size());
                                String s = AssistantApp.getInstance().getGson().toJson(unread.mAwardNotifies,
                                        new TypeToken<ArrayList<AwardNotify>>(){}.getType());
                                SPUtil.putString(mContext, SPConfig.SP_USER_INFO_FILE, SPConfig.KEY_USER_AWARD, s);
                            }
                            ObserverManager.getInstance().notifyUserUpdate(ObserverManager.STATUS
                                    .USER_UPDATE_PUSH_MESSAGE);
                            return;
                        }
                    }
                }
                AppDebugConfig.warnResp(AppDebugConfig.TAG_MANAGER, response);
            }

            @Override
            public void onFailure(Call<JsonRespBase<MessageCentralUnread>> call, Throwable t) {
                AppDebugConfig.w(AppDebugConfig.TAG_MANAGER, t);
            }
        });
    }

    @Override
    public void release() {
        if (mCallGetUserInfo != null) {
            mCallGetUserInfo.cancel();
            mCallGetUserInfo = null;
        }
        if (mCallUpdatePartInfo != null) {
            mCallUpdatePartInfo.cancel();
            mCallUpdatePartInfo = null;
        }
        if (mCallUpdateSession != null) {
            mCallUpdateSession.cancel();
            mCallUpdateSession = null;
        }
        if (mCallLogout != null) {
            mCallLogout.cancel();
            mCallLogout = null;
        }
        if (mCallObtainUnread != null) {
            mCallObtainUnread.cancel();
            mCallObtainUnread = null;
        }
    }


    public void writePhoneAccount(String val, ArrayList<String> history, boolean isRemove) {
        writeToHistory(val, SPConfig.KEY_LOGIN_PHONE, history, isRemove);
    }

    public void writeOuwanAccount(String val, ArrayList<String> history, boolean isRemove) {
        writeToHistory(val, SPConfig.KEY_LOGIN_OUWAN, history, isRemove);
    }

    public ArrayList<String> readPhoneAccount() {
        return readFromHistory(SPConfig.KEY_LOGIN_PHONE);
    }

    public ArrayList<String> readOuwanAccount() {
        return readFromHistory(SPConfig.KEY_LOGIN_OUWAN);
    }

    /**
     * 调用此方法向SP写入登录账号记录信息
     *
     * @param value    待新添加的值
     * @param key      SP中存放的key，分偶玩和手机登录
     * @param history  历史记录数据列表
     * @param isRemove 是否移除键值操作
     */
    private void writeToHistory(String value, String key, ArrayList<String> history, boolean isRemove) {
        if (TextUtils.isEmpty(value) || (value.contains(",") && value.indexOf(",") == 0)) {
            return;
        }
        if (history == null) {
            history = new ArrayList<>();
        }
        if (isRemove) {
            history.remove(value);
        } else {
            for (int i = history.size() - 1; i >= 0; i--) {
                String s = history.get(i);
                if (s.split(",")[0].equals(value.split(",")[0])) {
                    history.remove(i);
                    break;
                }
            }
            if (history.size() > 15) {
                history.remove(14);
            }
            history.add(0, value);
        }
        StringBuilder historyStr = new StringBuilder();
        for (String s : history) {
            historyStr.append(s).append(":");
        }
        if (historyStr.length() > 0) {
            historyStr.deleteCharAt(historyStr.length() - 1);
        }
        SPUtil.putString(mContext, SPConfig.SP_LOGIN_FILE, key, historyStr.toString());
    }

    /**
     * 调用该方法从SP中读取保存的登录账号记录信息
     *
     * @param key SP中的Key，区分偶玩和手机登录
     * @return 历史记录列表
     */
    private ArrayList<String> readFromHistory(String key) {
        String history = SPUtil.getString(mContext, SPConfig.SP_LOGIN_FILE, key, null);
        ArrayList<String> result = null;
        if (history != null) {
            String[] names = history.split(":");
            result = new ArrayList<String>();
            for (String n : names) {
                if (!TextUtils.isEmpty(n) && (!n.contains(",") || n.indexOf(",") != 0)) {
                    result.add(n);
                }
            }
        }
        return result;
    }
}
