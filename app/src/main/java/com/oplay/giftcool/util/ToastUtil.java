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
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.model.json.base.JsonRespBase;

import retrofit2.Response;

/**
 * @author JackieZhuang
 * @email zsigui@foxmail.com
 * @date 2015/12/27
 */
public class ToastUtil {

    private static final int DISTANCE_TO_TOP = 300;

    private static Toast makeText(Context context, CharSequence text, int duration) {
        Toast result = new Toast(context);

        LayoutInflater inflate = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflate.inflate(R.layout.view_toast_default, null);
        TextView tv = (TextView) v.findViewById(R.id.tv_msg);
        tv.setText(text);
        result.setView(v);
        result.setGravity(Gravity.CENTER, 0, 0);
        result.setDuration(duration);
        return result;
    }

    private static Toast makeText(Context context, @StringRes int textId, int duration) {
        return makeText(context, context.getString(textId), duration);
    }

    public static void show(final CharSequence msg, final int duration) {
        ThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                makeText(AssistantApp.getInstance().getApplicationContext(), msg, duration).show();
            }
        });
    }

    public static void show(@StringRes final int resId, final int duration) {
        ThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                makeText(AssistantApp.getInstance().getApplicationContext(), resId, duration).show();
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
        ToastUtil.showShort(prefix + "-哎呦，网络出现问题了！");
    }

    public static void blurErrorResp(String prefix, Response response) {
        AppDebugConfig.warn(AppDebugConfig.TAG_DEBUG_INFO, response);
        ToastUtil.showShort(prefix + (response == null ? "-嚄，服务器返回异常了！" : "-" + response.message()));
    }

    public static void blurErrorMsg(String prefix, JsonRespBase response) {
        AppDebugConfig.warn(AppDebugConfig.TAG_DEBUG_INFO, response);
        ToastUtil.showShort(prefix + "-呜，临时故障了！");
    }

    /**
     * 提示获取金币
     */
    public static void showScoreReward(String taskName, int rewardCount) {
        Context context = AssistantApp.getInstance().getApplicationContext();
        View v = LayoutInflater.from(context).inflate(R.layout.view_toast_reward, null);
        TextView tvTask = ViewUtil.getViewById(v, R.id.tv_task);
        TextView tvScore = ViewUtil.getViewById(v, R.id.tv_score);
        if (tvTask != null) {
            tvTask.setText(taskName);
        }
        if (tvScore != null) {
            tvScore.setText(String.valueOf(rewardCount));
        }
        Toast toast = new Toast(context);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(v);
        toast.show();
    }
}
