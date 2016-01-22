package com.oplay.giftcool.manager;

import android.content.Context;
import android.support.v4.app.FragmentManager;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.model.data.req.ReqTaskReward;
import com.oplay.giftcool.model.data.resp.TaskReward;
import com.oplay.giftcool.model.data.resp.UserModel;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Dialog;
import com.oplay.giftcool.ui.fragment.dialog.LoginDialog;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.NetworkUtil;
import com.oplay.giftcool.util.ToastUtil;
import com.socks.library.KLog;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by zsigui on 16-1-18.
 */
public class ScoreManager {

	private static ScoreManager manager;

	private ScoreManager() {
	}

	public static ScoreManager getInstance() {
		if (manager == null) {
			manager = new ScoreManager();
		}
		return manager;
	}

	// 表明当前是否任务列表状态
	public boolean mInWorking = false;
	public int mRewardType = RewardType.NOTHING;

	public boolean isInWorking() {
		return mInWorking;
	}

	public void setInWorking(boolean inWorking) {
		mInWorking = inWorking;
	}

	public int getRewardType() {
		return mRewardType;
	}

	public void setRewardType(int rewardType) {
		mRewardType = rewardType;
	}

	public void toastByCallback(TaskReward task) {
		if (task != null && task.rewardPoints != 0) {
			// 评论任务完成，奖励积分
			ToastUtil.showScoreReward(task.taskName, task.rewardPoints);
		}
	}

	/**
	 * 每天首次启动APP显示欢迎弹窗
	 */
	public void showWelComeDialog(final FragmentManager fm, final Context context, final TaskReward task) {
		if (task == null) {
			// 未登录 task == null
			final LoginDialog unloginDialog = LoginDialog.newInstance(R.layout.dialog_welcome_unlogin);
			unloginDialog.setPositiveBtnText(context.getResources().getString(R.string.st_welcome_unlogin_btn));
			unloginDialog.setListener(new BaseFragment_Dialog.OnDialogClickListener() {
				@Override
				public void onCancel() {
					unloginDialog.dismissAllowingStateLoss();
				}

				@Override
				public void onConfirm() {
					IntentUtil.jumpLogin(context);
					unloginDialog.dismissAllowingStateLoss();
				}
			});
			unloginDialog.show(fm, LoginDialog.class.getSimpleName());
		} else if (task.rewardPoints > 0) {
			final LoginDialog loginDialog;
			if (task.rewardPoints >= 100) {
				// 首次登录积分 >= 100
				 loginDialog = LoginDialog.newInstance(R.layout.dialog_welcome_login_first);
			} else {
				// 再次登录积分 < 100
				loginDialog = LoginDialog.newInstance(R.layout.dialog_welcome_login);
			}
			loginDialog.setScore(task.rewardPoints);
			loginDialog.setPositiveBtnText(context.getResources().getString(R.string.st_welcome_login_btn));
			loginDialog.setListener(new BaseFragment_Dialog.OnDialogClickListener() {
				@Override
				public void onCancel() {
					loginDialog.dismissAllowingStateLoss();
				}

				@Override
				public void onConfirm() {
					IntentUtil.jumpEarnScore(context);
					loginDialog.dismissAllowingStateLoss();
				}
			});
			loginDialog.show(fm, LoginDialog.class.getSimpleName());
			if (AccountManager.getInstance().isLogin()) {
				// 清除登录信息
				UserModel user = AccountManager.getInstance().getUser();
				user.rewardPoints = 0;
				user.taskName = null;
			}
		}
	}

	public void reward() {
		final Context context = AssistantApp.getInstance().getApplicationContext();
		Global.THREAD_POOL.execute(new Runnable() {
			@Override
			public void run() {
				if (NetworkUtil.isConnected(context)) {
					return;
				}
				ReqTaskReward data = new ReqTaskReward();
				data.type = getRewardType();
				Global.getNetEngine().obtainTaskReward(new JsonReqBase<ReqTaskReward>(data))
						.enqueue(new Callback<JsonRespBase<TaskReward>>() {
							@Override
							public void onResponse(Response<JsonRespBase<TaskReward>> response, Retrofit retrofit) {
								if (response != null && response.isSuccess()) {
									if (response.body() != null && response.body().isSuccess()) {
										if (response.body().getData() == null || response.body().getData().rewardPoints == 0) {
											return;
										}
										TaskReward task = response.body().getData();
										UserModel model = AccountManager.getInstance().getUser();
										model.userInfo.score += task.rewardPoints;
										AccountManager.getInstance().setUser(model);
										ToastUtil.showScoreReward(task.taskName, task.rewardPoints);
										ObserverManager.getInstance().notifyUserUpdate();
									}
								}
							}

							@Override
							public void onFailure(Throwable t) {
								if (AppDebugConfig.IS_DEBUG) {
									KLog.e(t);
								}
							}
						});
			}
		});
	}

	public static abstract interface RewardType {
		public static final int NOTHING = 0;
		public static final int BIND_OUWAN = 1;
		public static final int BIND_PHONE = 2;
		public static final int BIND_DOWNLOAD = 3;
		public static final int SHARE_NORMAL = 4;
		public static final int SHARE_LIMIT = 5;
	}
}
