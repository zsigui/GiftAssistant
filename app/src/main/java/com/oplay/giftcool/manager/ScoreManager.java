package com.oplay.giftcool.manager;

import android.content.Context;
import android.support.v4.app.FragmentManager;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.model.data.req.ReqTaskReward;
import com.oplay.giftcool.model.data.resp.MissionReward;
import com.oplay.giftcool.model.data.resp.TaskReward;
import com.oplay.giftcool.model.data.resp.UserModel;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Dialog;
import com.oplay.giftcool.ui.fragment.dialog.WelcomeDialog;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.ToastUtil;
import com.socks.library.KLog;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

	@Deprecated
	public void toastByCallback(TaskReward task) {
		toastByCallback(task, true);
	}

	@Deprecated
	public void toastByCallback(TaskReward task, boolean needNotify) {
		if (task != null && task.rewardPoints != 0 && AccountManager.getInstance().isLogin()) {
			// 评论任务完成，奖励金币
			ToastUtil.showScoreReward(task.taskName, task.rewardPoints);
			if (needNotify) {
				// 通知刷新金币
				AccountManager.getInstance().updatePartUserInfo();
			}
			setInWorking(false);
		}
	}

	public void toastByCallback(MissionReward reward, boolean needNotify) {
		if (reward != null && reward.code == 0) {
			ToastUtil.showScoreReward(reward.data.displayName, reward.data.rewardPoint);
			ObserverManager.getInstance().notifyUserUpdate(ObserverManager.STATUS.USER_UPDATE_TASK);
			if (needNotify) {
				// 通知刷新金币
				AccountManager.getInstance().updatePartUserInfo();
			}
			setInWorking(false);
		}
	}

	/**
	 * 每天首次启动APP显示欢迎弹窗
	 */
	public void showWelComeDialog(final FragmentManager fm, final Context context, final TaskReward task) {
		ScoreManager.getInstance().toastByCallback(task, false);
//		if (task == null) {
//			// 未登录 task == null
//			final WelcomeDialog unloginDialog = WelcomeDialog.newInstance(R.layout.dialog_welcome_unlogin);
//			unloginDialog.setPositiveBtnText(context.getResources().getString(R.string.st_welcome_unlogin_btn));
//			unloginDialog.setListener(new BaseFragment_Dialog.OnDialogClickListener() {
//				@Override
//				public void onCancel() {
//					unloginDialog.dismissAllowingStateLoss();
//				}
//
//				@Override
//				public void onConfirm() {
//					IntentUtil.jumpLoginNoToast(context);
//					unloginDialog.dismissAllowingStateLoss();
//				}
//			});
//			unloginDialog.show(fm, WelcomeDialog.class.getSimpleName());
//		}
		if (task != null && task.rewardPoints > 0) {
			final WelcomeDialog loginDialog;
			if (task.rewardPoints >= 100) {
				// 首次登录金币 >= 100
				loginDialog = WelcomeDialog.newInstance(R.layout.dialog_welcome_login_first);
			} else {
				// 再次登录金币 < 100
				loginDialog = WelcomeDialog.newInstance(R.layout.dialog_welcome_login);
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
			loginDialog.show(fm, WelcomeDialog.class.getSimpleName());
			if (AccountManager.getInstance().isLogin()) {
				// 清除登录信息
				UserModel user = AccountManager.getInstance().getUser();
				user.rewardPoints = 0;
				user.taskName = null;
			}
		}
	}


	public void reward(int ptype) {
		reward(ptype, true);
	}

	/**
	 * 通知任务完成的请求实体
	 */
	private JsonReqBase<ReqTaskReward> mRewardReqBase;

	/**
	 * 对需要本地通知任务进行通知获取奖励
	 *
	 * @param ptype      分享类型采用setRewardType并设置该值为RewardType.NOTHING
	 * @param needNotify 是否需要通知
	 */
	public void reward(int ptype, final boolean needNotify) {
		if (!AccountManager.getInstance().isLogin()) {
			return;
		}
		final int type;
		if (ptype == RewardType.NOTHING) {
			type = getRewardType();
		} else {
			type = ptype;
		}
		Global.THREAD_POOL.execute(new Runnable() {
			@Override
			public void run() {
				if (mRewardReqBase == null) {
					ReqTaskReward data = new ReqTaskReward();
					data.type = type;
					mRewardReqBase = new JsonReqBase<ReqTaskReward>(data);
				} else {
					mRewardReqBase.data.type = type;
				}
				Global.getNetEngine().obtainTaskReward(mRewardReqBase)
						.enqueue(new Callback<JsonRespBase<TaskReward>>() {
							@Override
							public void onResponse(Call<JsonRespBase<TaskReward>> call, Response<JsonRespBase
									<TaskReward>> response) {
								if (call.isCanceled()) {
									return;
								}
								if (response != null && response.isSuccessful()) {
									if (response.body() != null && response.body().isSuccess()) {
										if (!AccountManager.getInstance().isLogin()) {
											return;
										}
										toastByCallback(response.body().getData(), needNotify);
									}
									if (AppDebugConfig.IS_DEBUG) {
										KLog.d(AppDebugConfig.TAG_MANAGER, "金币获取-" +
												(response.body() == null ? "解析失败" : response.body().error()));
									}
								}
							}

							@Override
							public void onFailure(Call<JsonRespBase<TaskReward>> call, Throwable t) {
								if (AppDebugConfig.IS_DEBUG) {
									KLog.e(t);
								}
							}
						});
			}
		});
	}

	public static abstract class RewardType {
		public static final int NOTHING = 0;
		public static final int BIND_OUWAN = 1;
		public static final int BIND_PHONE = 2;
		public static final int DOWNLOAD = 3;
		public static final int SHARE_NORMAL = 4;
		public static final int SHARE_LIMIT = 5;
		public static final int SEARCH = 6;
		public static final int BUY_BY_BEAN = 7;
		public static final int SHARE_GCOOL = 8;
	}
}
