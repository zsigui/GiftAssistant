package com.oplay.giftassistant.handler;

import android.content.Context;

import com.oplay.giftassistant.AssistantApp;
import com.oplay.giftassistant.config.AppDebugConfig;
import com.oplay.giftassistant.config.Global;
import com.oplay.giftassistant.manager.AccountManager;
import com.oplay.giftassistant.manager.ObserverManager;
import com.oplay.giftassistant.model.data.req.ReqTaskReward;
import com.oplay.giftassistant.model.data.resp.TaskReward;
import com.oplay.giftassistant.model.json.base.JsonReqBase;
import com.oplay.giftassistant.model.json.base.JsonRespBase;
import com.oplay.giftassistant.util.NetworkUtil;
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
										AccountManager.getInstance().getUserInfo().score
												+= response.body().getData().rewardPoints;
										// 临时，不设置存储
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
