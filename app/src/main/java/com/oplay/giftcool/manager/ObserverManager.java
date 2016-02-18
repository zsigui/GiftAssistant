package com.oplay.giftcool.manager;

import android.app.Activity;
import android.app.Fragment;

import com.oplay.giftcool.config.AppDebugConfig;
import com.socks.library.KLog;

import java.util.HashMap;
import java.util.Map;

/**
 * 观察者模式管理器
 * 职责: 用于回调监听APP各种状态变更，以及时响应界面变化
 *
 * Created by zsigui on 16-1-5.
 */
public class ObserverManager {

	private static ObserverManager sInstance;

	private ObserverManager(){}

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
		if (mUserObservers.containsKey(key)) return;
		mUserObservers.put(key, observer);
	}

	public void removeUserUpdateListener(UserUpdateListener observer) {
		if (observer == null) return;
		mUserObservers.remove(observer.getClass().getName());
	}

	public void addGiftUpdateListener(GiftUpdateListener observer) {
		if (observer == null) return;
		String key = observer.getClass().getName();
		if (mGiftObservers.containsKey(key)) return;
		mGiftObservers.put(key, observer);
	}

	public void removeGiftUpdateListener(GiftUpdateListener observer) {
		if (observer == null) return;
		mGiftObservers.remove(observer.getClass().getName());
	}

	public void addUserActionListener(UserActionListener observer) {
		if (observer == null) return;
		String key = observer.getClass().getName();
		if (mUserActionListeners.containsKey(key)) return;
		mUserActionListeners.put(key, observer);
	}

	public void removeActionListener(UserActionListener observer) {
		if (observer == null) return;
		mUserActionListeners.remove(observer.getClass().getName());
	}

	public void notifyUserUpdate() {
		for (Map.Entry<String, UserUpdateListener> entry : mUserObservers.entrySet()) {
			UserUpdateListener observer = entry.getValue();
			try {
				if (observer != null) {
					if (observer instanceof Fragment && ((Fragment)observer).isRemoving()) {
						// 当为fragment且正被移除出界面，不更新，正确？
						AppDebugConfig.logMethodWithParams(UserUpdateListener.class.getSimpleName(),
								"isRemove = " + observer + ", " + ((Fragment) observer).isRemoving());
						continue;
					}
					if (observer instanceof Activity && ((Activity)observer).isFinishing()) {
						// 当Activity即将被销毁，无须更新
						AppDebugConfig.logMethodWithParams(UserUpdateListener.class.getSimpleName(),
								"isRemove = " + observer + ", " + ((Activity) observer).isFinishing());
						continue;
					}
					observer.onUserUpdate();
				}
			} catch (Throwable e) {
				if (AppDebugConfig.IS_DEBUG) {
					KLog.d(AppDebugConfig.TAG_MANAGER, e);
				}
			}
		}
	}

	public void notifyGiftUpdate() {
		notifyGiftUpdate(STATUS.DEFAULT_FOR_ALL);
	}

	public void notifyGiftUpdate(int action) {
		for (GiftUpdateListener observer : mGiftObservers.values()) {
			if (observer != null) {
				if (observer instanceof Fragment && ((Fragment)observer).isRemoving()) {
					// 当为fragment且正被移除出界面，不更新，正确？
					AppDebugConfig.logMethodWithParams(GiftUpdateListener.class.getSimpleName(),
							"isRemove = " + observer + ", " + ((Fragment) observer).isRemoving());
					continue;
				}
				if (observer instanceof Activity && ((Activity)observer).isFinishing()) {
					// 当Activity即将被销毁，无须更新
					AppDebugConfig.logMethodWithParams(GiftUpdateListener.class.getSimpleName(),
							"isRemove = " + observer + ", " + ((Activity) observer).isFinishing());
					continue;
				}
				observer.onGiftUpdate(action);
			}
		}
	}

	public void notifyUserActionUpdate(int action, int code) {
		for (UserActionListener observer : mUserActionListeners.values()) {
			if (observer != null) {
				if (observer instanceof Fragment && ((Fragment)observer).isRemoving()) {
					// 当为fragment且正被移除出界面，不更新，正确？
					AppDebugConfig.logMethodWithParams(UserActionListener.class.getSimpleName(),
							"isRemove = " + observer + ", " + ((Fragment) observer).isRemoving());
					continue;
				}
				if (observer instanceof Activity && ((Activity)observer).isFinishing()) {
					// 当Activity即将被销毁，无须更新
					AppDebugConfig.logMethodWithParams(UserActionListener.class.getSimpleName(),
							"isRemove = " + observer + ", " + ((Activity) observer).isFinishing());
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
		void onUserUpdate();
	}

	/**
	 * 礼包状态变更监听接口
	 */
	public interface GiftUpdateListener {
		void onGiftUpdate(int action);
	}

	public interface STATUS {
		int DEFAULT_FOR_ALL= 0x0;
		int GIFT_UPDATE_ALL = 0x010;
		int GIFT_UPDATE_PART = 0x11;
	}

	public interface UserActionListener {
		public static final int ACTION_DEFAULT = 0;
		public static final int ACTION_MODIFY_PSW = 1;
		public static final int ACTION_CHANGE_PHONE = 2;
		public static final int ACTION_BIND_PHONE = 3;
		public static final int ACTION_BIND_OUWAN = 4;

		public static final int ACTION_CODE_DEFAULT = -1;
		public static final int ACTION_CODE_FAILED = 0;
		public static final int ACTION_CODE_SUCCESS = 1;
		void onUserActionFinish(int action, int code);
	}
}
