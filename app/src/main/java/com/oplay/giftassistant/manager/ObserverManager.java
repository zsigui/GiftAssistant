package com.oplay.giftassistant.manager;

import android.app.Activity;
import android.app.Fragment;

import java.util.ArrayList;

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


	private ArrayList<UserUpdateListener> mUserObservers = new ArrayList<>();

	private ArrayList<GiftUpdateListener> mGiftObservers = new ArrayList<>();

	private ArrayList<MsgUpdateListener> mMsgObservers = new ArrayList<>();

	private ArrayList<OnDownloadListener> mDownloadObservers = new ArrayList<>();

	public void addUserUpdateListener(UserUpdateListener observer) {
		if (observer == null) return;
		mUserObservers.add(observer);
	}

	public void addGiftUpdateListener(GiftUpdateListener observer) {
		if (observer == null) return;
		mGiftObservers.add(observer);
	}

	public void addMsgUpdateListener(MsgUpdateListener observer) {
		if (observer == null) return;
		mMsgObservers.add(observer);
	}

	/**
	 * 进行下载监听观察者注册
	 */
	public void addOnDownloadListener(OnDownloadListener observer) {
		if (observer == null)
			return;
		mDownloadObservers.add(observer);
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
					return;
				}
				if (observer instanceof Activity && ((Activity)observer).isFinishing()) {
					// 当Activity即将被销毁，无须更新
					return;
				}
				observer.onGiftUpdate();
			}
		}
	}

	public void notifyMsgUpdate() {
		for (MsgUpdateListener observer : mMsgObservers) {
			if (observer != null) {
				if (observer instanceof Fragment && ((Fragment)observer).isRemoving()) {
					// 当为fragment且正被移除出界面，不更新，正确？
					return;
				}
				if (observer instanceof Activity && ((Activity)observer).isFinishing()) {
					// 当Activity即将被销毁，无须更新
					return;
				}
				observer.onMsgUpdate();
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

	/**
	 * 消息通知状态变更监听接口
	 */
	public interface MsgUpdateListener {
		void onMsgUpdate();
	}

	/**
	 * 对于下载过程的变化监听端口
	 */
	public interface OnDownloadListener {

		void onDownloadStart(String url);

		void onProgressUpdate(String url, float downloadSize, float totalSize, float rate);

		void onDownloadFinished(String url);
	}
}
