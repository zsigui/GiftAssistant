package com.oplay.giftassistant.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oplay.giftassistant.R;
import com.oplay.giftassistant.config.SPConfig;
import com.oplay.giftassistant.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftassistant.util.SPUtil;

import java.io.File;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/19
 */
public class SplashActivity extends BaseAppCompatActivity {

    private static int DEFAULT_SPLASH_TIME = 2345;
    private Handler mHandler = new Handler();
    private long mLastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initView() {
        ImageView iv = new ImageView(this);
        iv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        iv.setImageResource(R.drawable.pic_splash);
        String imageUrl = getImageUrl();
        if (imageUrl != null) {
            File file = ImageLoader.getInstance().getDiskCache().get(imageUrl);
            if (file != null && file.exists()) {
                ImageLoader.getInstance().displayImage(imageUrl, iv);
            } else {
                ImageLoader.getInstance().loadImage(imageUrl, null);
            }
        }
        setContentView(iv);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                splashAfter();
            }
        }, DEFAULT_SPLASH_TIME);
    }

    private void splashAfter() {
        if (mApp.isLogin()) {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
        }
        this.finish();
    }

    private void saveImageUrl(String url) {
        SPUtil.putString(this, SPConfig.SP_CACHE_FILE, SPConfig.SP_KEY_SPLASH_URL, url);
    }

    private String getImageUrl() {
        return SPUtil.getString(this, SPConfig.SP_CACHE_FILE, SPConfig.SP_KEY_SPLASH_URL, null);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (System.currentTimeMillis() - mLastClickTime <= 1000) {
            mLastClickTime = System.currentTimeMillis();
            mApp.exit();
            finish();
        }
    }
}
