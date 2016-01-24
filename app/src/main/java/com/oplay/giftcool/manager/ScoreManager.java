package com.oplay.giftcool.manager;

import android.content.Context;
import android.support.v4.app.FragmentManager;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.SPConfig;
import com.oplay.giftcool.model.data.req.ReqTaskReward;
import com.oplay.giftcool.model.data.resp.TaskReward;
import com.oplay.giftcool.model.data.resp.UserModel;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Dialog;
import com.oplay.giftcool.ui.fragment.dialog.LoginDialog;
import com.oplay.giftcool.util.DateUtil;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.SPUtil;
import com.oplay.giftcool.util.ToastUtil;
import com.socks.library.KLog;

import net.youmi.android.libs.common.coder.Coder_Md5;

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

	private boolean mIsDownloadToday = true;
	private boolean mIsShareNormalToday = true;
	private boolean mIsShareLimitToday = true;
	private boolean mIsSearchToday = true;
	private boolean mIsBuyByBeanToday = true;

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
		if (task != null && task.rewardPoints != 0 && AccountManager.getInstance().isLogin()) {
			// 评论任务完成，奖励积分
			ToastUtil.showScoreReward(task.taskName, task.rewardPoints);
			// 通知刷新积分
			AccountManager.getInstance().updatePartUserInfo();
			setInWorking(false);
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

	/**
	 * 对需要本地通知任务进行通知获取奖励
	 *
	 * @param ptype 分享类型采用setRewardType并设置该值为RewardType.NOTHING
	 */
	public void reward(int ptype) {
		final int type;
		if (ptype == RewardType.NOTHING) {
			type = getRewardType();
		} else {
			type = ptype;
		}
		KLog.d("reward type = " + type);
		switch (type) {
			case RewardType.DOWNLOAD:
				if (!mIsDownloadToday) return;
				break;
			case RewardType.SEARCH:
				if (!mIsSearchToday) return;
				break;
			case RewardType.BUY_BY_BEAN:
				if (!mIsBuyByBeanToday) return;
				break;
			case RewardType.NOTHING:
				// 通知类型出错，返回
				return;
		}
		Global.THREAD_POOL.execute(new Runnable() {
			@Override
			public void run() {
				final ReqTaskReward data = new ReqTaskReward();
				data.type = type;
				Global.getNetEngine().obtainTaskReward(new JsonReqBase<ReqTaskReward>(data))
						.enqueue(new Callback<JsonRespBase<TaskReward>>() {
							@Override
							public void onResponse(Response<JsonRespBase<TaskReward>> response, Retrofit retrofit) {
								if (response != null && response.isSuccess()) {
									if (response.body() != null && response.body().isSuccess()) {
										if (!AccountManager.getInstance().isLogin()) {
											return;
										}
										KLog.d("search data = " + response.body().getData());
										toastByCallback(response.body().getData());
										writeLocalTaskState(data.type);
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

	/**
	 * 将指定任务的最后完成时间写入SP
	 */
	private void writeLocalTaskState(int type) {
		Context context = AssistantApp.getInstance().getApplicationContext();
		String spFile = Coder_Md5.md5(String.valueOf(AccountManager.getInstance().getUserSesion().uid));
		long curTime = System.currentTimeMillis();
		switch (type) {
			case RewardType.DOWNLOAD:
				mIsDownloadToday = false;
				SPUtil.putLong(context, spFile, SPConfig.KEY_DOWNLOAD_LAST_TIME, curTime);
				break;
			case RewardType.SEARCH:
				mIsSearchToday = false;
				SPUtil.putLong(context, spFile, SPConfig.KEY_SEARCH_LAST_TIME, curTime);
				break;
			case RewardType.BUY_BY_BEAN:
				mIsBuyByBeanToday = false;
				SPUtil.putLong(context, spFile, SPConfig.KEY_BUY_BY_BEAN_LAST_TIME, curTime);
				break;
		}
		mIsSearchToday = true;
	}

	/**
	 * 重设需要本地通知服务器的任务的最后成功写入时间
	 */
	public void resetLocalTaskState() {
		Context context = AssistantApp.getInstance().getApplicationContext();
		if (AccountManager.getInstance().isLogin()) {
			long lastTime;
			String spFile = Coder_Md5.md5(String.valueOf(AccountManager.getInstance().getUserSesion().uid));
			lastTime = SPUtil.getLong(context, spFile, SPConfig.KEY_DOWNLOAD_LAST_TIME, 0);
			mIsDownloadToday = (lastTime == 0 || !DateUtil.isToday(lastTime));

			lastTime = SPUtil.getLong(context, spFile, SPConfig.KEY_SHARE_NORMAL_LAST_TIME, 0);
			mIsShareNormalToday = (lastTime == 0 || !DateUtil.isToday(lastTime));

			lastTime = SPUtil.getLong(context, spFile, SPConfig.KEY_SHARE_LIMIT_LAST_TIME, 0);
			mIsShareLimitToday = (lastTime == 0 || !DateUtil.isToday(lastTime));

			lastTime = SPUtil.getLong(context, spFile, SPConfig.KEY_SEARCH_LAST_TIME, 0);
			mIsSearchToday = (lastTime == 0 || !DateUtil.isToday(lastTime));

			lastTime = SPUtil.getLong(context, spFile, SPConfig.KEY_BUY_BY_BEAN_LAST_TIME, 0);
			mIsBuyByBeanToday = (lastTime == 0 || !DateUtil.isToday(lastTime));
		} else {
			mIsDownloadToday = false;
			mIsShareNormalToday = false;
			mIsShareLimitToday = false;
			mIsSearchToday = false;
			mIsBuyByBeanToday = false;
		}
	}

	public static abstract interface RewardType {
		public static final int NOTHING = 0;
		public static final int BIND_OUWAN = 1;
		public static final int BIND_PHONE = 2;
		public static final int DOWNLOAD = 3;
		public static final int SHARE_NORMAL = 4;
		public static final int SHARE_LIMIT = 5;
		public static final int SEARCH = 6;
		public static final int BUY_BY_BEAN = 7;
	}
}
