package com.oplay.giftcool.manager;

import android.app.Activity;
import android.app.Fragment;

import com.oplay.giftcool.config.AppDebugConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * 观察者模式管理器
 * 职责: 用于回调监听APP各种状态变更，以及时响应界面变化
 * <p/>
 * Created by zsigui on 16-1-5.
 */
public class ObserverManager {

    private static ObserverManager sInstance;

    private ObserverManager() {
    }

    public static ObserverManager getInstance() {
        if (sInstance == null) {
            sInstance = new ObserverManager();
        }
        return sInstance;
    }

    private HashMap<String, UserUpdateListener> mUserObservers = new HashMap<>();

    private HashMap<String, GiftUpdateListener> mGiftObservers = new HashMap<>();

    private HashMap<String, UserActionListener> mUserActionListeners = new HashMap<>();

    public void addUserUpdateListener(UserUpdateListener observer) {
        if (observer == null) return;
        String key = observer.getClass().getName();
        mUserObservers.put(key, observer);
        AppDebugConfig.v(AppDebugConfig.TAG_MANAGER, "addUserUpdateListener : add " + observer.getClass().getName());
    }

    public void removeUserUpdateListener(UserUpdateListener observer) {
        if (observer == null) return;
        UserUpdateListener removed = mUserObservers.remove(observer.getClass().getName());
        AppDebugConfig.v(AppDebugConfig.TAG_MANAGER, "removeUserUpdateListener : remove "
                + (removed == null ? "null" : removed.getClass().getName()));
    }

    public void addGiftUpdateListener(GiftUpdateListener observer) {
        if (observer == null) return;
        String key = observer.getClass().getName();
        mGiftObservers.put(key, observer);
        AppDebugConfig.v(AppDebugConfig.TAG_MANAGER, "addGiftUpdateListener : add " + observer.getClass().getName());
    }

    public void removeGiftUpdateListener(GiftUpdateListener observer) {
        if (observer == null) return;
        GiftUpdateListener removed = mGiftObservers.remove(observer.getClass().getName());
        AppDebugConfig.v(AppDebugConfig.TAG_MANAGER, "removeGiftUpdateListener : remove "
                + (removed == null ? "null" : removed.getClass().getName()));
    }

    public void addUserActionListener(UserActionListener observer) {
        if (observer == null) return;
        String key = observer.getClass().getName();
        mUserActionListeners.put(key, observer);
        AppDebugConfig.v(AppDebugConfig.TAG_MANAGER, "addUserActionListener : add " + observer.getClass().getName());
    }

    public void removeActionListener(UserActionListener observer) {
        if (observer == null) return;
        UserActionListener removed = mUserActionListeners.remove(observer.getClass().getName());
        AppDebugConfig.v(AppDebugConfig.TAG_MANAGER, "removeActionListener : remove "
                + (removed == null ? "null" : removed.getClass().getName()));
    }

    public void notifyUserUpdate(int action) {
        for (Map.Entry<String, UserUpdateListener> entry : mUserObservers.entrySet()) {
            UserUpdateListener observer = entry.getValue();
            try {
                if (observer != null) {
                    if (observer instanceof Fragment && ((Fragment) observer).isRemoving()) {
                        // 当为fragment且正被移除出界面，不更新，正确？
                        AppDebugConfig.v(AppDebugConfig.TAG_MANAGER,
                                "removed removing fragment observer { " + observer + " }");
                        continue;
                    }
                    if (observer instanceof Activity && ((Activity) observer).isFinishing()) {
                        // 当Activity即将被销毁，无须更新
                        AppDebugConfig.v(AppDebugConfig.TAG_MANAGER,
                                "removed activity finishing observer { " + observer + " }");
                        continue;
                    }
                    observer.onUserUpdate(action);
                }
            } catch (Throwable e) {
                AppDebugConfig.w(AppDebugConfig.TAG_MANAGER, e);
            }
        }
    }

    /**
     * 通知礼包已更新 <br />
     * PS: 该方法不保证运行在UI线程中
     *
     * @param action 执行动作
     */
    public synchronized void notifyGiftUpdate(int action) {
        for (GiftUpdateListener observer : mGiftObservers.values()) {
            if (observer != null) {
                if (observer instanceof Fragment && ((Fragment) observer).isRemoving()) {
                    // 当为fragment且正被移除出界面，不更新，正确？
                    AppDebugConfig.v(AppDebugConfig.TAG_MANAGER,
                            "removed removing fragment observer { " + observer + " }");
                    continue;
                }
                if (observer instanceof Activity && ((Activity) observer).isFinishing()) {
                    // 当Activity即将被销毁，无须更新
                    AppDebugConfig.v(AppDebugConfig.TAG_MANAGER,
                            "removed activity finishing observer { " + observer + " }");
                    continue;
                }
                observer.onGiftUpdate(action);
            }
        }
    }

    public synchronized void notifyUserActionUpdate(int action, int code) {
        for (UserActionListener observer : mUserActionListeners.values()) {
            if (observer != null) {
                if (observer instanceof Fragment && ((Fragment) observer).isRemoving()) {
                    // 当为fragment且正被移除出界面，不更新，正确？
                    AppDebugConfig.v(AppDebugConfig.TAG_MANAGER,
                            "removed removing fragment observer { " + observer + " }");
                    continue;
                }
                if (observer instanceof Activity && ((Activity) observer).isFinishing()) {
                    // 当Activity即将被销毁，无须更新
                    AppDebugConfig.v(AppDebugConfig.TAG_MANAGER,
                            "removed activity finishing observer { " + observer + " }");
                    continue;
                }
                observer.onUserActionFinish(action, code);
            }
        }
    }


    /**
     * 账号状态变更监听接口
     */
    public interface UserUpdateListener {
        void onUserUpdate(int action);
    }

    /**
     * 礼包状态变更监听接口
     */
    public interface GiftUpdateListener {
        void onGiftUpdate(int action);
    }

    public interface STATUS {
        int DEFAULT_FOR_ALL = 0x0;
        int GIFT_UPDATE_ALL = 0x010;
        int GIFT_UPDATE_PART = 0x11;
        int GIFT_UPDATE_LIKE = 0x12;
        int USER_UPDATE_ALL = 0x020;
        int USER_UPDATE_PART = 0x021;
        int USER_UPDATE_TASK = 0x022;
        int USER_UPDATE_PUSH_MESSAGE = 0x023;
    }

    public interface UserActionListener {
        int ACTION_DEFAULT = 0;
        int ACTION_MODIFY_PSW = 1;
        int ACTION_CHANGE_PHONE = 2;
        int ACTION_BIND_PHONE = 3;
        int ACTION_BIND_OUWAN = 4;
        int ACTION_CHANGE_NET_STATE = 100;


        int ACTION_CODE_DEFAULT = -1;
        int ACTION_CODE_FAILED = 0;
        int ACTION_CODE_SUCCESS = 1;

        void onUserActionFinish(int action, int code);
    }
}
