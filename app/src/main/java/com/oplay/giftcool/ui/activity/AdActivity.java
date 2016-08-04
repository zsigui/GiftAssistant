package com.oplay.giftcool.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.Sea;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.util.MixUtil;
import com.oplay.giftcool.util.ThreadUtil;

import java.io.File;
import java.util.Locale;

/**
 * Created by zsigui on 16-8-3.
 */
public class AdActivity extends BaseAppCompatActivity {

    private ImageView ivSplash;
    private TextView tvPass;

    private int remainTime;
    private String prefixTag;

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (remainTime == 0) {
                jumpMain();
            } else {
                tvPass.setText(String.format(Locale.CHINA, "%s%d", prefixTag, remainTime));
                remainTime--;
                ThreadUtil.runOnUiThread(mRunnable, 1000);
            }
        }
    };

    @Override
    protected void processLogic() {
        tvPass.setOnClickListener(this);
        ivSplash.setOnClickListener(this);
        if (Sea.getInstance().isShowAd()) {
            File f = ImageLoader.getInstance().getDiskCache().get(Sea.getInstance().getAdInfo().img);
            // 此前f已经验证
            Bitmap b = BitmapFactory.decodeFile(f.getAbsolutePath());
            if (b != null) {
                ivSplash.setImageBitmap(b);
            }
            remainTime = Sea.getInstance().getAdInfo().displayTime;
            if (Sea.getInstance().getAdInfo().canPass) {
                prefixTag = "跳过";
            } else {
                prefixTag = "剩余";
            }
            ThreadUtil.runOnUiThread(mRunnable);
            return;
        }
        jumpMain();
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_ad);
        ivSplash = getViewById(R.id.iv_splash);
        tvPass = getViewById(R.id.tv_pass);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.tv_pass:
                if (Sea.getInstance().getAdInfo().canPass) {
                    jumpMain();
                }
                break;
            case R.id.iv_splash:
                MixUtil.handleViewUri(this, Uri.parse(Sea.getInstance().getAdInfo().uri));
                finish();
                break;
        }
    }

    private void jumpMain() {
        startActivity(new Intent(AdActivity.this, MainActivity.class));
        finish();
    }
}
