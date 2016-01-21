package com.oplay.giftcool.handler;

import android.content.Context;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.manager.ObserverManager;
import com.oplay.giftcool.model.data.req.ReqTaskReward;
import com.oplay.giftcool.model.data.resp.TaskReward;
import com.oplay.giftcool.model.data.resp.TaskRewardDetail;
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
public class ScoreHandler {

	// 表明当前是否任务列表状态
	public static boolean sIsTasking = false;

	public static void reward(final int type) {
		final Context context = AssistantApp.getInstance().getApplicationContext();
		Global.THREAD_POOL.execute(new Runnable() {
			@Override
			public void run() {
				if (NetworkUtil.isConnected(context)) {
					return;
				}
				ReqTaskReward data = new ReqTaskReward();
				data.type = type;
				Global.getNetEngine().obtainTaskReward(new JsonReqBase<ReqTaskReward>(type))
						.enqueue(new Callback<JsonRespBase<TaskReward>>() {
							@Override
							public void onResponse(Response<JsonRespBase<TaskReward>> response, Retrofit retrofit) {
								if (response != null && response.isSuccess()) {
									if (response.body() != null && response.body().isSuccess()) {
										if (response.body().getData() == null || response.body().getData().data == null) {
											return;
										}
										TaskRewardDetail task = response.body().getData().data;
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
}
