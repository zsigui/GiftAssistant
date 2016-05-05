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
import com.oplay.giftcool.config.AppConfig;
import com.oplay.giftcool.config.NetUrl;
import com.oplay.giftcool.config.SPConfig;
import com.oplay.giftcool.config.WebViewUrl;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Dialog;
import com.oplay.giftcool.ui.fragment.dialog.TestChoiceDialog;
import com.oplay.giftcool.util.DateUtil;
import com.oplay.giftcool.util.SPUtil;
import com.oplay.giftcool.util.ToastUtil;
import com.socks.library.KLog;

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
				AssistantApp.getInstance().appInit();
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
//        MixUtil.createDesktopShortcut(getApplicationContext());
	}

	@Override
	protected void onResume() {
		super.onResume();
//		GameSdk.queryUpdate(this, new IQueryUpdateCallback() {
//			@Override
//			public void onUpdateResult(int i) {
//				if (AppDebugConfig.IS_DEBUG) {
//					KLog.d(AppDebugConfig.TAG_APP, "onUpdateResult = " + i);
//				}
//			}
//		});
			final TestChoiceDialog dialog = TestChoiceDialog.newInstances();
		if (AppConfig.TEST_MODE) {
			dialog.setListener(new BaseFragment_Dialog.OnDialogClickListener() {
				@Override
				public void onCancel() {
					ToastUtil.showShort("不做修改");
					initAction();
					dialog.dismissAllowingStateLoss();
				}

				@Override
				public void onConfirm() {
					ToastUtil.showShort("使用新的地址重新加载");
					String[] s = dialog.getContent().split("\n");
					NetUrl.REAL_URL = s[0].trim();
					if (s.length > 1) {
						WebViewUrl.REAL_URL = s[1].trim();
					}
					SPUtil.putString(AssistantApp.getInstance().getApplicationContext(),
							SPConfig.SP_APP_DEVICE_FILE,
							SPConfig.KEY_TEST_REQUEST_URI,
							dialog.getContent());
//					AssistantApp.getInstance().resetInitForTest();
					initAction();
					dialog.dismissAllowingStateLoss();
				}
			});
			dialog.setCancelable(false);
			dialog.show(getSupportFragmentManager(), "init");
			return;
		}
		if (!AppConfig.TEST_MODE) {
			initAction();
		}
	}

	private void initAction() {
		judgeFirstOpenToday();
		if (!mApp.isGlobalInit()) {

			mFirstInitTime = System.currentTimeMillis();
			mHandler.post(mInitRunnable);
		} else {
			judgeToMain();
		}
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
				}catch (Throwable e) {
					KLog.e(e);
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
		if (System.currentTimeMillis() - mLastClickTime <= 3000) {
			mLastClickTime = System.currentTimeMillis();
			mApp.appExit();
			finish();
//			System.exit(0);
		}
	}


	/**
	 * 判断是否今日首次登录<br/>
	 * 防止由于后台初始化原因导致该值一直为今日，故设置为此处执行
	 */
	public void judgeFirstOpenToday() {
		long lastOpenTime = SPUtil.getLong(SplashActivity.this,
				SPConfig.SP_USER_INFO_FILE, SPConfig.KEY_LOGIN_LAST_OPEN_TIME, 0);
		// 今日首次登录，首次打开APP并不显示
		MainActivity.sIsTodayFirstOpen = (lastOpenTime != 0 && !DateUtil.isToday(lastOpenTime));
		MainActivity.sIsTodayFirstOpenForBroadcast = MainActivity.sIsTodayFirstOpen;
		// 写入当前时间
		SPUtil.putLong(SplashActivity.this, SPConfig.SP_USER_INFO_FILE, SPConfig.KEY_LOGIN_LAST_OPEN_TIME, System.currentTimeMillis());
	}
}
