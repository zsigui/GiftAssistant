package com.oplay.giftcool.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.asynctask.AsyncTask_NetworkInit;
import com.oplay.giftcool.config.AppConfig;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.NetUrl;
import com.oplay.giftcool.config.SPConfig;
import com.oplay.giftcool.config.WebViewUrl;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Dialog;
import com.oplay.giftcool.ui.fragment.dialog.TestChoiceDialog;
import com.oplay.giftcool.util.DateUtil;
import com.oplay.giftcool.util.SPUtil;
import com.oplay.giftcool.util.ToastUtil;

import net.youmi.android.libs.common.compatibility.Compatibility_AsyncTask;

import java.io.File;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/19
 */
public class SplashActivity extends BaseAppCompatActivity {

    private long mLastClickTime = 0;
    private long mFirstInitTime = 0;

    /**
     * 进行初始化或者闪屏的等待操作
     */
    private Runnable mInitRunnable = new Runnable() {
        @Override
        public void run() {
            long initDuration = System.currentTimeMillis() - mFirstInitTime;
            long minSplashDuration = 1000;
            long maxInitDuration = 4000;
            if ((mApp.isGlobalInit() && initDuration > minSplashDuration) || initDuration > maxInitDuration) {
                judgeToMain();
            } else {
                mHandler.postDelayed(mInitRunnable, 100);
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
        } else {
            initAction();
        }

    }

    private void initAction() {
        judgeFirstOpenToday();
        mFirstInitTime = System.currentTimeMillis();
        mHandler.post(mInitRunnable);
    }

    @Override
    protected void initView() {
        AssistantApp.getInstance().initImageLoader();
        ImageView iv = new ImageView(this);
        iv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        boolean useCacheImg = false;
        String imageUrl = AssistantApp.getInstance().getStartImg();
        if (imageUrl != null) {
            File file = ImageLoader.getInstance().getDiskCache().get(imageUrl);
            if (file != null && file.exists()) {
                Bitmap bitmap = null;
                try {
                    bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                } catch (Throwable e) {
                    AppDebugConfig.w(AppDebugConfig.TAG_ACTIVITY, e);
                }
                iv.setImageBitmap(bitmap);
                useCacheImg = true;
            } else {
                ImageLoader.getInstance().loadImage(imageUrl, null);
            }
        }
        if (!useCacheImg) {
            iv.setImageResource(R.drawable.pic_splash_2016);
        }
        setContentView(iv);
    }

    private void judgeToMain() {
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
        this.finish();
    }

    @Override
    public void onBackPressed() {
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

}
