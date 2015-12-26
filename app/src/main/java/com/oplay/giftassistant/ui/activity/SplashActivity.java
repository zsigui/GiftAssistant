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
import com.oplay.giftassistant.config.AppDebugConfig;
import com.oplay.giftassistant.config.SPConfig;
import com.oplay.giftassistant.config.account.AccountManager;
import com.oplay.giftassistant.model.MobileInfoModel;
import com.oplay.giftassistant.model.UserModel;
import com.oplay.giftassistant.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftassistant.util.CommonUtil;
import com.oplay.giftassistant.util.SPUtil;
import com.socks.library.KLog;

import net.youmi.android.libs.common.global.Global_SharePreferences;

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
			long maxInitDuration = 2500;
			if ((mApp.isGlobalInit() && initDuration > minSplashDuration) ||  initDuration > maxInitDuration) {
				judgeToMain();
			} else {
				doInit();
				mHandler.postDelayed(mInitRunnable, 100);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);

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
		super.onBackPressed();
		if (System.currentTimeMillis() - mLastClickTime <= 1000) {
			mLastClickTime = System.currentTimeMillis();
			mApp.exit();
			finish();
		}
	}

	/**
	 * 需要在此完成一些APP全局常量初始化的获取工作
	 */
	private void doInit() {
		if (mApp.isGlobalInit()) {
			return;
		}

		// 初始化设备状态
		if (!MobileInfoModel.getInstance().isInit()) {
			CommonUtil.initMobileInfoModel(getApplicationContext());
		}
		// 获取用户信息
		// 该信息使用salt加密存储再SharedPreference中
		UserModel user = null;
		try {
			String userJson = Global_SharePreferences.getStringFromSharedPreferences(getApplicationContext(),
					SPConfig.SP_USER_INFO_FILE, SPConfig.KEY_SESSION, SPConfig.SALT_USER_INFO, null);
			if (AppDebugConfig.IS_DEBUG) {
				KLog.d(AppDebugConfig.TAG_APP, "get from sp: user = " + userJson);
			}
			user = mApp.getGson().fromJson(userJson, UserModel.class);
		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(AppDebugConfig.TAG_APP, e);
			}
		}
		AccountManager.getInstance().setUser(user);
		mApp.setGlobalInit(true);
	}
}
