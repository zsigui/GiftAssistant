package com.oplay.giftcool.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.SPConfig;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.util.SPUtil;
import com.oplay.giftcool.util.ToastUtil;

import java.io.File;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/19
 */
public class SplashActivity extends BaseAppCompatActivity {

	private Handler mHandler = new Handler();
	private long mLastClickTime = 0;
	private long mFirstInitTime = 0;

	/**
	 * 进行初始化或者闪屏的等待操作
	 */
	private Runnable mInitRunnable = new Runnable() {
		@Override
		public void run() {
			long initDuration = System.currentTimeMillis() - mFirstInitTime;
			long minSplashDuration = 1234;
			long maxInitDuration = 4000;
			if (initDuration > maxInitDuration) {
				ToastUtil.showShort("初始化失败");
				mApp.exit();
				SplashActivity.this.finish();
				return;
			}
			if ((mApp.isGlobalInit() && initDuration > minSplashDuration)) {
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
		if (!mApp.isGlobalInit()) {

			mFirstInitTime = System.currentTimeMillis();
			mHandler.post(mInitRunnable);
		} else {
			judgeToMain();
		}
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

	private void judgeToMain() {
		startActivity(new Intent(SplashActivity.this, MainActivity.class));
		this.finish();
	}

	private void saveImageUrl(String url) {
		SPUtil.putString(this, SPConfig.SP_CACHE_FILE, SPConfig.KEY_SPLASH_URL, url);
	}

	private String getImageUrl() {
		return SPUtil.getString(this, SPConfig.SP_CACHE_FILE, SPConfig.KEY_SPLASH_URL, null);
	}

	@Override
	public void onBackPressed() {
		if (System.currentTimeMillis() - mLastClickTime <= 1000) {
			mLastClickTime = System.currentTimeMillis();
			mApp.exit();
			finish();
			System.exit(0);
		}
	}

}
