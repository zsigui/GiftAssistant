package com.oplay.giftcool.manager;

import android.app.Activity;
import android.app.Fragment;

import com.socks.library.KLog;

import java.util.ArrayList;

/**
 * 观察者模式管理器
 * 职责: 用于回调监听APP各种状态变更，以及时响应界面变化
 *
 * Created by zsigui on 16-1-5.
 */
public class ObserverManager {

	public class STATUS {
		// 可以设置32个tag
		public static final int UPDATE_INDEX = 0x00000001;
		public static final int UPDATE_DETAIL = 0x00000002;
		public static final int UPDATE_ALL = 0xFFFFFFFF;
	}

	private static ObserverManager sInstance;

	private ObserverManager(){}

	public static ObserverManager getInstance() {
		if (sInstance == null) {
			sInstance = new ObserverManager();
		}
		return sInstance;
	}

	private ArrayList<UserUpdateListener> mUserObservers = new ArrayList<>();

	private ArrayList<GiftUpdateListener> mGiftObservers = new ArrayList<>();

	public void addUserUpdateListener(UserUpdateListener observer) {
		if (observer == null) return;
		mUserObservers.add(observer);
	}

	public void removeUserUpdateListener(UserUpdateListener observer) {
		if (observer == null) return;
		mUserObservers.remove(observer);
	}

	public void addGiftUpdateListener(GiftUpdateListener observer) {
		if (observer == null) return;
		mGiftObservers.add(observer);
	}

	public void removeGiftUpdateListener(GiftUpdateListener observer) {
		if (observer == null) return;
		mGiftObservers.remove(observer);
	}

	public void notifyUserUpdate() {
		for (UserUpdateListener observer : mUserObservers) {
			if (observer != null) {
				if (observer instanceof Fragment && ((Fragment)observer).isRemoving()) {
					// 当为fragment且正被移除出界面，不更新，正确？
					return;
				}
				if (observer instanceof Activity && (((Activity)observer).isFinishing())) {
					// 当Activity即将被销毁，无须更新
					return;
				}
				observer.onUserUpdate();
			}
		}
	}



	public void notifyGiftUpdate() {
		for (GiftUpdateListener observer : mGiftObservers) {
			if (observer != null) {
				if (observer instanceof Fragment && ((Fragment)observer).isRemoving()) {
					// 当为fragment且正被移除出界面，不更新，正确？
					KLog.d("notify", "isRemove = " + observer + ", " + ((Fragment) observer).isRemoving());
					return;
				}
				if (observer instanceof Activity && ((Activity)observer).isFinishing()) {
					// 当Activity即将被销毁，无须更新
					KLog.d("notify", "isFinishing = " + observer + ", " + ((Activity) observer).isFinishing());
					return;
				}
				observer.onGiftUpdate();
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
		void onGiftUpdate();
	}
}