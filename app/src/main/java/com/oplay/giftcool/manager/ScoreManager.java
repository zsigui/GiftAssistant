package com.oplay.giftcool.manager;

import android.content.Context;

import com.google.gson.reflect.TypeToken;
import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.SPConfig;
import com.oplay.giftcool.config.util.TaskTypeUtil;
import com.oplay.giftcool.model.data.req.ReqSignInInit;
import com.oplay.giftcool.model.data.req.ReqTaskReward;
import com.oplay.giftcool.model.data.resp.MissionReward;
import com.oplay.giftcool.model.data.resp.task.TaskInfoDownload;
import com.oplay.giftcool.model.data.resp.task.TaskInfoThree;
import com.oplay.giftcool.model.data.resp.task.TaskSignInfo;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.util.DateUtil;
import com.oplay.giftcool.util.NetworkUtil;
import com.oplay.giftcool.util.SPUtil;
import com.oplay.giftcool.util.SystemUtil;
import com.oplay.giftcool.util.ToastUtil;
import com.socks.library.KLog;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by zsigui on 16-1-18.
 */
public class ScoreManager {

	private static ScoreManager manager;
	/**
	 * 是否任务完成，用于判定回到任务界面时是否进行刷新
	 */
	private boolean mTaskFinished;

	private ScoreManager() {
	}

	public static ScoreManager getInstance() {
		if (manager == null) {
			manager = new ScoreManager();
		}
		return manager;
	}

	public String mRewardCode = null;


	public String getRewardCode() {
		return mRewardCode;
	}

	public void setRewardCode(String rewardCode) {
		mRewardCode = rewardCode;
	}

//	@Deprecated
//	public void toastByCallback(TaskReward task, boolean needNotify) {
//		if (task != null && task.rewardPoints != 0 && AccountManager.getInstance().isLogin()) {
//			// 评论任务完成，奖励金币
//			ToastUtil.showScoreReward(task.taskName, task.rewardPoints);
//			if (needNotify) {
//				// 通知刷新金币
//				AccountManager.getInstance().updatePartUserInfo();
//			}
//		}
//	}

	public void toastByCallback(MissionReward reward, boolean needNotify) {
		if (reward != null && reward.code == 0 && reward.data != null) {
			ToastUtil.showScoreReward(reward.data.displayName, reward.data.rewardPoint);
			ObserverManager.getInstance().notifyUserUpdate(ObserverManager.STATUS.USER_UPDATE_TASK);
			if (needNotify) {
				// 通知刷新金币
				AccountManager.getInstance().updatePartUserInfo();
			}
		}
	}

	/**
	 * 指示签到任务是否完成了
	 */
	private boolean mIsSignInTaskFinished = false;

	/**
	 * 获取签到任务的完成状态
	 */
	public boolean isSignInTaskFinished() {
		return mIsSignInTaskFinished;
	}

	/**
	 * 初始化签到状态
	 */
	public void initSignInState(final Context context) {
		Global.THREAD_POOL.execute(new Runnable() {
			@Override
			public void run() {
				if (!NetworkUtil.isConnected(context)) {
					return;
				}
				ReqSignInInit signIn = new ReqSignInInit();
				signIn.date = DateUtil.getDate("yyyy-MM-dd", 0);
				JsonReqBase<ReqSignInInit> reqData = new JsonReqBase<>(signIn);
				Global.getNetEngine().obtainDailySignInInfo(reqData)
						.enqueue(new Callback<JsonRespBase<TaskSignInfo>>() {
							@Override
							public void onResponse(Call<JsonRespBase<TaskSignInfo>> call,
							                       Response<JsonRespBase<TaskSignInfo>> response) {
								if (response != null && response.isSuccessful()
										&& response.body() != null && response.body().isSuccess()) {
									final boolean isSignIn = response.body().getData().taskFinished.signToday;
									if ((mIsSignInTaskFinished && !isSignIn) || (!mIsSignInTaskFinished && isSignIn)) {
										// 在状态不同的时候进行通知
										mIsSignInTaskFinished = isSignIn;
										ObserverManager.getInstance()
												.notifyUserUpdate(ObserverManager.STATUS.USER_UPDATE_TASK);
									}
								}
							}

							@Override
							public void onFailure(Call<JsonRespBase<TaskSignInfo>> call, Throwable t) {

							}
						});
			}
		});
	}

//	/**
//	 * 每天首次启动APP显示欢迎弹窗
//	 */
//	public void showWelComeDialog(final FragmentManager fm, final Context context, final TaskReward task) {
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
//		if (task != null && task.rewardPoints > 0) {
//			final WelcomeDialog loginDialog;
//			if (task.rewardPoints >= 100) {
//				// 首次登录金币 >= 100
//				loginDialog = WelcomeDialog.newInstance(R.layout.dialog_welcome_login_first);
//			} else {
//				// 再次登录金币 < 100
//				loginDialog = WelcomeDialog.newInstance(R.layout.dialog_welcome_login);
//			}
//			loginDialog.setScore(task.rewardPoints);
//			loginDialog.setPositiveBtnText(context.getResources().getString(R.string.st_welcome_login_btn));
//			loginDialog.setListener(new BaseFragment_Dialog.OnDialogClickListener() {
//				@Override
//				public void onCancel() {
//					loginDialog.dismissAllowingStateLoss();
//				}
//
//				@Override
//				public void onConfirm() {
//					IntentUtil.jumpEarnScore(context);
//					loginDialog.dismissAllowingStateLoss();
//				}
//			});
//			loginDialog.show(fm, WelcomeDialog.class.getSimpleName());
//			if (AccountManager.getInstance().isLogin()) {
//				// 清除登录信息
//				UserModel user = AccountManager.getInstance().getUser();
//			}
//		}
//	}

	/**
	 * 通知任务完成的请求实体
	 */
	private JsonReqBase<ReqTaskReward> mRewardReqBase;

	public boolean reward(String ptype, final boolean replayImdiate) {
		return reward(ptype, "", replayImdiate);
	}

	/**
	 * 对需要本地通知任务进行通知获取奖励
	 *
	 * @param ptype         分享类型采用setRewardType并设置该值为RewardType.NOTHING
	 * @param replayImdiate 是否立即返回结果
	 */
	public synchronized boolean reward(String ptype, final String appId, final boolean replayImdiate) {
		if (!AccountManager.getInstance().isLogin()) {
			return false;
		}
		final String code;
		if (ptype == null) {
			code = getRewardCode();
		} else {
			code = ptype;
		}
		Global.THREAD_POOL.execute(new Runnable() {
			@Override
			public void run() {
				if (mRewardReqBase == null) {
					ReqTaskReward data = new ReqTaskReward();
					mRewardReqBase = new JsonReqBase<ReqTaskReward>(data);
				}
				mRewardReqBase.data.code = code;
				mRewardReqBase.data.appId = appId;
				mRewardReqBase.data.replyNotify = (replayImdiate ? 1 : 0);
				Global.getNetEngine().obtainTaskReward(mRewardReqBase)
						.enqueue(new Callback<JsonRespBase<MissionReward>>() {
							@Override
							public void onResponse(Call<JsonRespBase<MissionReward>> call, Response<JsonRespBase
									<MissionReward>> response) {
								if (call.isCanceled()) {
									return;
								}
								if (response != null && response.isSuccessful()) {
									if (response.body() != null && response.body().isSuccess()) {
										if (AccountManager.getInstance().isLogin()) {
											toastByCallback(response.body().getData(), true);
											return;
										}
									}
									if (AppDebugConfig.IS_DEBUG) {
										KLog.d(AppDebugConfig.TAG_MANAGER, "金币获取-" +
												(response.body() == null ? "解析失败" : response.body().error()));
									}
								}
							}

							@Override
							public void onFailure(Call<JsonRespBase<MissionReward>> call, Throwable t) {
								if (AppDebugConfig.IS_DEBUG) {
									KLog.e(t);
								}
							}
						});
			}
		});
		return true;
	}

	// 正在进行的下载任务列表
	private HashMap<String, TaskInfoDownload> mCurDownloadTaskSet;

	private void setCurDownloadTaskSet(Context context) {
		final String val = AssistantApp.getInstance().getGson().toJson(mCurDownloadTaskSet);
		SPUtil.putString(context, SPConfig.SP_APP_INFO_FILE,
				SPConfig.KEY_TODAY_DOWNLOAD_TASK, val);
	}

	/**
	 * 获取今天进行的下载任务列表
	 */
	private synchronized HashMap<String, TaskInfoDownload> getCurDownloadTaskSet(Context context) {
		synchronized (this) {
			if (mCurDownloadTaskSet == null) {
				String data = SPUtil.getString(context, SPConfig.SP_APP_INFO_FILE,
						SPConfig.KEY_TODAY_DOWNLOAD_TASK, null);
				if (data == null) {
					mCurDownloadTaskSet = new HashMap<>();
				} else {
					mCurDownloadTaskSet = AssistantApp.getInstance().getGson()
							.fromJson(data, new TypeToken<HashMap<String, TaskInfoDownload>>() {
							}.getType());
					boolean needRewrite = false;
					Iterator<Map.Entry<String, TaskInfoDownload>> it = mCurDownloadTaskSet.entrySet().iterator();
					while (it.hasNext()) {
						Map.Entry<String, TaskInfoDownload> entry = it.next();
						if (!entry.getValue().isToday()) {
							it.remove();
							needRewrite = true;
						}
					}
					if (needRewrite) {
						setCurDownloadTaskSet(context);
					}
				}
			}
		}
//		if (!mCurDownloadTaskSet.isEmpty()) {
//			AlarmClockManager.getInstance().setObserverGame(true);
//		}
		return mCurDownloadTaskSet;
	}

	/**
	 * 添加新的下载任务
	 */
	public synchronized void addDownloadWork(Context context, String code, TaskInfoThree info) {

		TaskInfoDownload download = new TaskInfoDownload(code, info);
		// 保存状态，以便下次启动之类的有效
		getCurDownloadTaskSet(context).put(code, download);
		setCurDownloadTaskSet(context);
	}

	/**
	 * 判断当前下载任务包名是否存在列表中
	 */
	public synchronized boolean containDownloadTask(Context context, String packName) {
		return getCurDownloadTaskSet(context).containsKey(packName)
				&& getCurDownloadTaskSet(context).get(packName).isToday();
	}

	/**
	 * 判断试玩游戏前台任务，累积时间
	 */
	public synchronized void judgePlayTime(final Context context, final int elapseTime) {
		Iterator<Map.Entry<String, TaskInfoDownload>> it = getCurDownloadTaskSet(context).entrySet().iterator();
		while (it.hasNext()) {
			final Map.Entry<String, TaskInfoDownload> entry = it.next();
			final TaskInfoDownload info = entry.getValue();
			if (AppDebugConfig.IS_DEBUG) {
				KLog.d(AppDebugConfig.TAG_WARN, "hasPlayTime = " + info.hasPlayTime + ", elapseTime = " + elapseTime
				+ ", isForeground = " + SystemUtil.isForeground(context, info.packName));
			}
			if (SystemUtil.isForeground(context, info.packName)) {
				info.hasPlayTime += elapseTime;
			}
			if (!info.isToday()) {
				it.remove();
			} else {
				if (info.isFinished()) {
					reward(TaskTypeUtil.ID_PLAY_GAME, String.valueOf(info.appId), true);
					it.remove();
					setTaskFinished(true);
				}
			}
		}

		if (!mCurDownloadTaskSet.isEmpty()) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.d(AppDebugConfig.TAG_WARN, "no finished, continue!");
			}
			// 任务没完成，继续监测
			AlarmClockManager.getInstance().setObserverGame(true);
		} else {
			// 任务完成,不监测
			AlarmClockManager.getInstance().setObserverGame(false);
		}
		// 进行一次写入
		setCurDownloadTaskSet(context);
	}

	public boolean isTaskFinished() {
		return mTaskFinished;
	}

	/**
	 * 设置任务状态为已完成，以便于返回时刷新任务界面
	 */
	public void setTaskFinished(boolean taskFinished) {
		mTaskFinished = taskFinished;
	}
}
