package com.oplay.giftcool.util;

import android.content.Context;
import android.support.annotation.StringRes;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.model.json.base.JsonRespBase;

import retrofit.Response;

/**
 * @author JackieZhuang
 * @email zsigui@foxmail.com
 * @date 2015/12/27
 */
public class ToastUtil {
	public static void show(final CharSequence msg, final int duration) {
		ThreadUtil.runInUIThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(AssistantApp.getInstance().getApplicationContext(), msg, duration).show();
			}
		});
	}

	public static void show(@StringRes final int resId, final int duration) {
		ThreadUtil.runInUIThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(AssistantApp.getInstance().getApplicationContext(), resId, duration).show();
			}
		});
	}

	public static void showLong(CharSequence msg) {
		show(msg, Toast.LENGTH_LONG);
	}

	public static void showLong(@StringRes int resId) {
		show(resId, Toast.LENGTH_LONG);
	}

	public static void showShort(final CharSequence msg) {
		show(msg, Toast.LENGTH_SHORT);
	}

	public static void showShort(@StringRes int resId) {
		show(resId, Toast.LENGTH_SHORT);
	}

	public static void blurThrow(String prefix) {
		ToastUtil.showShort(prefix + "-网络异常");
	}

	public static void blurErrorResp(String prefix, Response response) {
		ToastUtil.showShort(prefix + (response == null ? "返回出错" : response.message()));
	}

	public static void blurErrorMsg(String prefix, JsonRespBase response) {
		ToastUtil.showShort(prefix + (response == null ? "解析错误" : response.getMsg()));
	}

	/**
	 * 提示获取积分
	 */
	public static void showScoreReward(String taskName, int rewardCount) {
		Context context = AssistantApp.getInstance().getApplicationContext();
		View v = LayoutInflater.from(context).inflate(R.layout.view_toast_reward, null);
		TextView tvTask = ViewUtil.getViewById(v, R.id.tv_task);
		TextView tvScore = ViewUtil.getViewById(v, R.id.tv_score);
		tvTask.setText(taskName);
		tvScore.setText(String.valueOf(rewardCount));
		Toast toast = new Toast(context);
		toast.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 150);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setView(v);
		toast.show();
	}
}
