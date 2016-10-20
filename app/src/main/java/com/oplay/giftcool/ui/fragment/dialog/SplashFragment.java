package com.oplay.giftcool.ui.fragment.dialog;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.asynctask.AsyncTask_InitApplication;
import com.oplay.giftcool.asynctask.AsyncTask_NetworkInit;
import com.oplay.giftcool.config.AppConfig;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.NetUrl;
import com.oplay.giftcool.config.SPConfig;
import com.oplay.giftcool.config.WebViewUrl;
import com.oplay.giftcool.listener.CallbackListener;
import com.oplay.giftcool.model.data.resp.AdInfo;
import com.oplay.giftcool.ui.activity.MainActivity;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Dialog;
import com.oplay.giftcool.util.DateUtil;
import com.oplay.giftcool.util.MixUtil;
import com.oplay.giftcool.util.SPUtil;
import com.oplay.giftcool.util.ThreadUtil;
import com.oplay.giftcool.util.ToastUtil;

import net.youmi.android.libs.common.compatibility.Compatibility_AsyncTask;

import java.io.File;
import java.util.Locale;

import static com.oplay.giftcool.util.ThreadUtil.runOnUiThread;

/**
 * Created by zsigui on 16-10-19.
 */

public class SplashFragment extends DialogFragment implements CallbackListener<Bitmap>, View.OnClickListener {

    public static boolean sHasShow = false;
    private View mContentView;
    private ImageView ivSplash;
    private ImageView ivAdLoad;
    private TextView tvPass;
    private TestChoiceDialog mDialog;

    private final int DEFAULT_AD_TIME = 4;

    private int remainTime;
    private String mCurrentImg = "";

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRemoving()) {
                return;
            }
            if (remainTime == 0) {
                jumpToMain();
            } else {
//                tvPass.setText(String.format(Locale.CHINA, "%s%ds", prefixTag, remainTime));
                tvPass.setText(Html.fromHtml(String.format(Locale.CHINA,
                        "跳过 <font color='#ffaa17'>%ds</font>", remainTime)));
                remainTime--;
                runOnUiThread(mRunnable, 1000);
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle
            savedInstanceState) {
        if (mContentView == null) {
            mContentView = inflater.inflate(R.layout.activity_splash, container);
            initView();
            processLogic();
        }
        return mContentView;
    }

    private void initView() {
        ivSplash = getViewById(R.id.iv_splash);
        tvPass = getViewById(R.id.tv_pass);
        ivAdLoad = getViewById(R.id.iv_splash_ad);
    }

    @SuppressWarnings("unchecked")
    private <T extends View> T getViewById(@IdRes int id) {
        return (T) mContentView.findViewById(id);
    }

    private void processLogic() {
        if (!AssistantApp.getInstance().isGlobalInit()
                && !AsyncTask_InitApplication.isIniting()) {
            AppDebugConfig.d(AppDebugConfig.TAG_DEBUG_INFO, "app async task initial!");
            Compatibility_AsyncTask.executeParallel(new AsyncTask_InitApplication(getContext()));
        }
        Compatibility_AsyncTask.executeParallel(new AsyncTask_NetworkInit(getContext()));

        AssistantApp.getInstance().setSplashAdListener(this);
        tvPass.setOnClickListener(this);
        ivSplash.setOnClickListener(this);
        ivAdLoad.setOnClickListener(this);

        Bitmap b = null;
        AdInfo adInfo = AssistantApp.getInstance().getAdInfo();
        if (adInfo != null && !TextUtils.isEmpty(adInfo.img)) {
            File f = ImageLoader.getInstance().getDiskCache().get(adInfo.img);
            // 此前f已经验证
            b = BitmapFactory.decodeFile(f.getAbsolutePath());
            remainTime = AssistantApp.getInstance().getAdInfo().displayTime;
            mCurrentImg = adInfo.img;
        }
        if (b == null) {
            b = BitmapFactory.decodeResource(getResources(), R.drawable.pic_splash_2016);
        }
//        prefixTag = (adInfo != null && adInfo.showPass) ? "跳过" : "剩余";
        showPass(adInfo);
        remainTime = (adInfo == null || adInfo.displayTime < DEFAULT_AD_TIME) ? DEFAULT_AD_TIME : adInfo.displayTime;
        ivSplash.setImageBitmap(b);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (AppConfig.TEST_MODE) {
            if (mDialog != null) {
                return;
            }
            mDialog = TestChoiceDialog.newInstances();
            mDialog.setListener(new BaseFragment_Dialog.OnDialogClickListener() {
                @Override
                public void onCancel() {
                    ToastUtil.showShort("不做修改");
                    initAction();
                    mDialog.dismissAllowingStateLoss();
                }

                @Override
                public void onConfirm() {
                    ToastUtil.showShort("使用新的地址重新加载");
                    String[] s = mDialog.getContent().split("\n");
                    NetUrl.REAL_URL = s[0].trim();
                    if (s.length > 2) {
                        WebViewUrl.REAL_URL = s[1].trim();
                        NetUrl.REAL_SOCKET_URL = s[2].trim();
                    } else if (s.length > 1) {
                        WebViewUrl.REAL_URL = s[1].trim();
                    }
                    SPUtil.putString(AssistantApp.getInstance().getApplicationContext(),
                            SPConfig.SP_APP_DEVICE_FILE,
                            SPConfig.KEY_TEST_REQUEST_URI,
                            mDialog.getContent());
                    AssistantApp.getInstance().setGlobalInit(false);
                    AssistantApp.getInstance().appInit();
                    initAction();
                    mDialog.dismissAllowingStateLoss();
                }
            });
            mDialog.setCancelable(false);
            mDialog.show(getChildFragmentManager(), "init");
            ToastUtil.showShort("当前渠道号: " + AssistantApp.getInstance().getChannelId());
        } else {
            initAction();
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        AssistantApp.getInstance().setSplashAdListener(null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    private void disappearView(View iv) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(iv, "alpha", 1f, 0f);
        animator.setDuration(600);
        animator.start();
    }

    private void showPass(AdInfo adInfo) {
        if (adInfo != null && adInfo.showPass) {
            tvPass.setVisibility(View.VISIBLE);
        } else {
            tvPass.setVisibility(View.GONE);
        }
    }

    private void jumpToMain() {
        ThreadUtil.remove(mRunnable);
        this.dismiss();
    }

    @Override
    public void doCallBack(final Bitmap data) {
        final AdInfo newInfo = AssistantApp.getInstance().getAdInfo();
        if (!mCurrentImg.equalsIgnoreCase(newInfo.img) && data != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ivAdLoad.setImageBitmap(data);
                    showPass(newInfo);
                    disappearView(ivSplash);
                    mCurrentImg = newInfo.img;
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        AdInfo adInfo = AssistantApp.getInstance().getAdInfo();
        switch (v.getId()) {
            case R.id.tv_pass:
//                if (adInfo == null || adInfo.showPass) {
                jumpToMain();
//                }
                break;
            case R.id.iv_splash:
            case R.id.iv_splash_ad:
                if (adInfo != null && !TextUtils.isEmpty(adInfo.uri)) {
                    ThreadUtil.remove(mRunnable);
                    this.dismiss();
                    MixUtil.handleViewUri(getContext(), Uri.parse(adInfo.uri));
                } else {
                    jumpToMain();
                }
                break;
        }
    }

    private void initAction() {
        judgeFirstOpenToday();
        ThreadUtil.runOnUiThread(mRunnable);
    }

    /**
     * 判断是否今天首次登录<br/>
     * 防止由于后台初始化原因导致该值一直为今天，故设置为此处执行
     */
    public void judgeFirstOpenToday() {
        long lastOpenTime = SPUtil.getLong(getContext(),
                SPConfig.SP_USER_INFO_FILE, SPConfig.KEY_LOGIN_LATEST_OPEN_TIME, 0);
        // 今天首次登录，首次打开APP并不显示
        MainActivity.sIsTodayFirstOpen = (lastOpenTime != 0 && !DateUtil.isToday(lastOpenTime));
        MainActivity.sIsTodayFirstOpenForBroadcast = MainActivity.sIsTodayFirstOpen;
        // 写入当前时间
        SPUtil.putLong(getContext(), SPConfig.SP_USER_INFO_FILE, SPConfig.KEY_LOGIN_LATEST_OPEN_TIME, System
                .currentTimeMillis());
    }
}
