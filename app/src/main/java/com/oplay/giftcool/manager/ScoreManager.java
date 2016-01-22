package com.oplay.giftcool.manager;

import android.content.Context;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.model.data.req.ReqTaskReward;
import com.oplay.giftcool.model.data.resp.TaskReward;
import com.oplay.giftcool.model.data.resp.UserModel;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
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

	public void showWelComeDialog(TaskReward task) {

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
