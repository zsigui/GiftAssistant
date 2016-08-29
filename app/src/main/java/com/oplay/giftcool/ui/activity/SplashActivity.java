package com.oplay.giftcool.ui.activity;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.asynctask.AsyncTask_NetworkInit;
import com.oplay.giftcool.config.AppConfig;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.NetUrl;
import com.oplay.giftcool.config.SPConfig;
import com.oplay.giftcool.config.WebViewUrl;
import com.oplay.giftcool.listener.CallbackListener;
import com.oplay.giftcool.model.data.resp.AdInfo;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Dialog;
import com.oplay.giftcool.ui.fragment.dialog.TestChoiceDialog;
import com.oplay.giftcool.util.DateUtil;
import com.oplay.giftcool.util.MixUtil;
import com.oplay.giftcool.util.SPUtil;
import com.oplay.giftcool.util.ThreadUtil;
import com.oplay.giftcool.util.ToastUtil;

import net.youmi.android.libs.common.compatibility.Compatibility_AsyncTask;

import java.io.File;
import java.util.Locale;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/19
 */
public class SplashActivity extends BaseAppCompatActivity implements CallbackListener<Bitmap> {

    private ImageView ivSplash;
    private ImageView ivAdLoad;
    private TextView tvPass;
    private final int DEFAULT_AD_TIME = 4;

    private int remainTime;
    private String mCurrentImg = "";
    private String prefixTag;

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (remainTime == 0) {
                jumpToMain();
            } else {
//                tvPass.setText(String.format(Locale.CHINA, "%s%ds", prefixTag, remainTime));
                tvPass.setText(Html.fromHtml(String.format(Locale.CHINA,
                        "跳过 <font color='#ffaa17'>%ds</font>", remainTime)));
                remainTime--;
                ThreadUtil.runOnUiThread(mRunnable, 1000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void processLogic() {
        Compatibility_AsyncTask.executeParallel(new AsyncTask_NetworkInit(getApplicationContext()));

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

    private void showPass(AdInfo adInfo) {
        if (adInfo != null && adInfo.showPass) {
            tvPass.setVisibility(View.VISIBLE);
        } else {
            tvPass.setVisibility(View.GONE);
        }
    }

    private TestChoiceDialog mDialog;

    @Override
    protected void onResume() {
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
                    if (s.length > 1) {
                        WebViewUrl.REAL_URL = s[1].trim();
                    }
                    SPUtil.putString(AssistantApp.getInstance().getApplicationContext(),
                            SPConfig.SP_APP_DEVICE_FILE,
                            SPConfig.KEY_TEST_REQUEST_URI,
                            mDialog.getContent());
                    AssistantApp.getInstance().setGlobalInit(false);
                    mApp.appInit();
                    initAction();
                    mDialog.dismissAllowingStateLoss();
                }
            });
            mDialog.setCancelable(false);
            mDialog.show(getSupportFragmentManager(), "init");
            ToastUtil.showShort("当前渠道号: " + AssistantApp.getInstance().getChannelId());
        } else {
            initAction();
        }

    }

    private void initAction() {
        judgeFirstOpenToday();
        ThreadUtil.runOnUiThread(mRunnable);
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_ad);
        ivSplash = getViewById(R.id.iv_splash);
        tvPass = getViewById(R.id.tv_pass);
        ivAdLoad = getViewById(R.id.iv_splash_ad);
    }

    private void jumpToMain() {
        ThreadUtil.remove(mRunnable);
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
        this.finish();
    }

    @Override
    public void onBackPressed() {
        // 不处理按钮回退事件
    }

    @Override
    public void release() {
        super.release();
        AssistantApp.getInstance().setSplashAdListener(null);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
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
                    MixUtil.handleViewUri(this, Uri.parse(adInfo.uri));
                    finish();
                } else {
                    jumpToMain();
                }
                break;
        }
    }

    /**
     * 判断是否今日首次登录<br/>
     * 防止由于后台初始化原因导致该值一直为今日，故设置为此处执行
     */
    public void judgeFirstOpenToday() {
        long lastOpenTime = SPUtil.getLong(SplashActivity.this,
                SPConfig.SP_USER_INFO_FILE, SPConfig.KEY_LOGIN_LATEST_OPEN_TIME, 0);
        // 今日首次登录，首次打开APP并不显示
        MainActivity.sIsTodayFirstOpen = (lastOpenTime != 0 && !DateUtil.isToday(lastOpenTime));
        MainActivity.sIsTodayFirstOpenForBroadcast = MainActivity.sIsTodayFirstOpen;
        // 写入当前时间
        SPUtil.putLong(SplashActivity.this, SPConfig.SP_USER_INFO_FILE, SPConfig.KEY_LOGIN_LATEST_OPEN_TIME, System
                .currentTimeMillis());
    }

    private void disappearView(View iv) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(iv, "alpha", 1f, 0f);
        animator.setDuration(600);
        animator.start();
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
}
