package net.ouwan.umipay.android.api;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.text.TextUtils;

import net.ouwan.umipay.android.config.SDKCacheConfig;
import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.entry.UmipayAccount;
import net.ouwan.umipay.android.entry.UmipayCommonAccount;
import net.ouwan.umipay.android.manager.UmipayAccountManager;
import net.ouwan.umipay.android.manager.UmipayCommonAccountCacheManager;
import net.ouwan.umipay.android.view.FloatMenuBaseView;
import net.ouwan.umipay.android.view.UmipayAccountChangeDialog;
import net.youmi.android.libs.common.global.Global_Final_Common_Millisecond;
import net.youmi.android.libs.common.util.Util_System_Runtime;

import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class UmipayFloatMenu implements SensorEventListener {
	public static final long CHECK_INTERVAL_TIME = 2000;
	public static final long MOVE_INTERVAL_TIME = 8;
	public static final long HIDE_INTERVAL_TIME = 10 * Global_Final_Common_Millisecond.oneSecond_ms;
	// 摇一摇功能相关参数
	public static final long SHAKE_INTERVAL_TIME = 1 * Global_Final_Common_Millisecond.oneSecond_ms;
	public static final long SPEED = 10;
	public static final int SHAKE_COUNT = 2;

	public final static String SRC_PACKAGENAME = "src_packagename";
	public final static String ACTION_ACCOUNT_CHANGE_CALLBACK = "action_callback";
	public final static String ACTION_ACCOUNT_CHANGE_CALLBACK_TIMEOUT = "action_account_change_timeout";
	public final static String DEST_ACTIVITY_REORDER_TO_FRONT = "dest_activity_reorder_to_front";
	public final static String DEST_PACKAGENAME = "dest_packagename";
	public final static String DEST_CLASSNAME = "dest_classname";
	public final static String DEST_TASKID = "dest_taskid";

	private static UmipayFloatMenu mInstance = new UmipayFloatMenu();
	public static long update_Bubble_Interval_Time = 10 * Global_Final_Common_Millisecond.oneSecond_ms;

	private Context mContext;
	private Activity mLastHasFocusActivity;
	private UmipayAccountChangeBroadcastReceiver mBroadcastReceiver;
	private ConcurrentHashMap<Activity, FloatMenuBaseView> FloatMenuMap;
	private Timer timer;
	private TimerTask check_timerTask;
	private SensorManager mSensorManager;// 传感器管理器
	private Sensor mSensor;// 传感器
	private int mShakeCount = 0;
	private float mX = -11;//初始化的X
	private float mY = -11;//初始化的Y
	private float mZ = -11;//初始化的Z
	private long lastShakeTime = 0;
	private long lastHide_ShowTime = 0;
	private long lastPullTime = 0;

	private UmipayFloatMenu() {
		FloatMenuMap = new ConcurrentHashMap<Activity, FloatMenuBaseView>();
		cancelCheckTimerTask();
		cancelTimer();

	}

	public static UmipayFloatMenu getInstance() {
		return mInstance;
	}

	public boolean create(Activity activity) {
		if (activity == null) {
			return false;
		}
		if (!SDKCacheConfig.getInstance(activity).isEnableFloatMemu()) {
			return false;
		}
		if (FloatMenuMap != null && activity != null) {
			try {
				if (FloatMenuMap.get(activity) == null) {
					FloatMenuMap.put(activity, new FloatMenuBaseView(activity));
					if (timer == null && FloatMenuMap.size() != 0) {
						mContext = activity.getApplicationContext();
						mBroadcastReceiver = new UmipayAccountChangeBroadcastReceiver();
						IntentFilter filter = new IntentFilter();
						// 礼包酷直接自定义了接收方式，不采用SDK的
//						filter.addAction(UmipayCommonAccountCacheManager.ACTION_ACCOUNT_CHANGE);
						mContext.registerReceiver(mBroadcastReceiver,filter);
						setUpdateBubbleTimeInterval(SDKCacheConfig.getInstance(activity).getRedpointTime());
						initTimer();
						initSensor();
						setCheckTimerTask();
//						show(activity);
					}
					return true;
				}
			} catch (Throwable e) {
				Debug_Log.e(e);
			}
		}
		return false;
	}

	public boolean cancel(Activity activity) {
		if (FloatMenuMap != null && activity != null && mContext != null) {
			try {
				FloatMenuBaseView fm = FloatMenuMap.get(activity);
				if(mLastHasFocusActivity == activity){
					mLastHasFocusActivity = null;
				}
				if (fm != null) {
					fm.Destroy();
					FloatMenuMap.remove(activity);
					if (FloatMenuMap.size() == 0) {
						cancelSensor();
						cancelCheckTimerTask();
						cancelTimer();
					}
					return true;
				}
			} catch (Throwable e) {
				Debug_Log.e(e);
			}
		}
		return false;
	}

	public void recycle() {
		try {
			if (FloatMenuMap != null) {
				for (Map.Entry<Activity, FloatMenuBaseView> e : FloatMenuMap
						.entrySet()) {
					e.getValue().Destroy();
				}
				FloatMenuMap.clear();
				mLastHasFocusActivity = null;
			}
			if(mContext != null && mBroadcastReceiver != null){
				mContext.unregisterReceiver(mBroadcastReceiver);
				mBroadcastReceiver = null;
			}
			cancelSensor();
			cancelCheckTimerTask();
			cancelTimer();
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

	public boolean show(Activity activity) {
		if (FloatMenuMap != null && activity != null && mContext != null) {
			try {
				FloatMenuBaseView fm = FloatMenuMap.get(activity);
				if (fm != null) {
					fm.ShowFloatMenu();
					return true;
				}
			} catch (Throwable e) {
				Debug_Log.e(e);
			}
		}
		return false;
	}

	public boolean hide(Activity activity) {
		if (FloatMenuMap != null && activity != null && mContext != null) {
			try {
				FloatMenuBaseView fm = FloatMenuMap.get(activity);
				if (fm != null) {
					fm.HideFloatMenu();
					return true;
				}
			} catch (Throwable e) {
				Debug_Log.e(e);
			}
		}
		return false;
	}

	/**
	 * 初始化传感器
	 */
	private void initSensor() {
		mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);// 获得传感器管理器
		if (mSensorManager != null) {
			mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);// 获得重力传感器
		}
		if (mSensorManager != null && mSensor != null) {
			mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI);
//			FloatMenuBaseView.setEnableFloatMemuDialog(true);//手机支持加速度感应器则初始化该值
//		} else {
//			FloatMenuBaseView.setEnableFloatMemuDialog(false);//如果手机不支持加速度感应器,则不显示隐藏提示框
		}
	}

	private void cancelSensor() {
		if (mSensorManager != null) {
			mSensorManager.unregisterListener(this);
			mSensorManager = null;
			mSensor = null;
		}
	}

	/**
	 * 初始化定时器
	 */
	private void initTimer() {
		try {
			if (timer == null) {
				// 使Timer线程为deamon线程，应用结束则结束
				timer = new Timer(true);
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

	/**
	 * 设置任务
	 */
	private void setCheckTimerTask() {
		// 保证任何时候最多只有一个定时器
		if (timer == null) {
			return;
		}

		try {
			cancelCheckTimerTask();

			check_timerTask = new TimerTask() {
				@Override
				public void run() {
					try {
						for (Map.Entry<Activity, FloatMenuBaseView> e : FloatMenuMap
								.entrySet()) {
							e.getValue().updateFloatMenu();
							if(e.getKey().hasWindowFocus()){
								mLastHasFocusActivity = e.getKey();
							}
						}
						if (UmipayAccountManager.getInstance(mContext).isLogin() == false) {
							//未登录时始终设置lastPullTime为0，使下次登录时必定执行一次更新
							lastPullTime = 0;
						} else {
							//只在登录时执行更新
							long currentTime = System.currentTimeMillis();
							if ((currentTime - lastPullTime) > update_Bubble_Interval_Time) {
								PullNotice();
							}
						}
					} catch (Throwable e) {
						Debug_Log.e(e);
					}
				}
			};
			// 定时器，每隔一段时间检查一次FloatMenu
			timer.schedule(check_timerTask, 0, UmipayFloatMenu.CHECK_INTERVAL_TIME);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

	private void cancelTimer() {
		try {
			if (timer != null) {
				timer.cancel();
				timer = null;
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

	/**
	 * 取消定时任务
	 */
	private void cancelCheckTimerTask() {
		try {
			if (check_timerTask != null) {
				check_timerTask.cancel();
				check_timerTask = null;
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

	/**
	 * 访问后台更新未读信息
	 */
	private void PullNotice() {
		if (mContext == null) {
			return;
		}
		try {
			lastPullTime = System.currentTimeMillis();
			Debug_Log.dd("Pull notice");
			Intent serviceIntent = new Intent(mContext, UmipayService.class);
			serviceIntent.putExtra("action", UmipayService.ACTION_FLOATMENU_PULL);
			mContext.startService(serviceIntent);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

	/**
	 * 设置小红点通知间歇时间
	 */
	public static void setUpdateBubbleTimeInterval(long timeInterval) {
		if (timeInterval > 0) {
			update_Bubble_Interval_Time = timeInterval
					* Global_Final_Common_Millisecond.oneMinute_ms;
		}
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		try {
			if (!UmipayAccountManager.getInstance(mContext).isLogin()) {
				return;
			}
			if (Math.abs(mX) > 10 && Math.abs(mY) > 10 && Math.abs(mZ) > 10) {
				mX = event.values[SensorManager.DATA_X];
				mY = event.values[SensorManager.DATA_Y];
				mZ = event.values[SensorManager.DATA_Z];
				return;
			}

			float x = event.values[SensorManager.DATA_X];
			float y = event.values[SensorManager.DATA_Y];
			float z = event.values[SensorManager.DATA_Z];
			long now = System.currentTimeMillis();
			float speed = (Math.abs(mX - x) + Math.abs(mY - y) + Math.abs(mZ - z)) / 3;
			if (speed > UmipayFloatMenu.SPEED) {
				if ((now - lastShakeTime) < UmipayFloatMenu.SHAKE_INTERVAL_TIME) {
					mShakeCount++;
				} else {
					mShakeCount = 0;//超时清0
				}
				lastShakeTime = now;
			}
			if (mShakeCount > UmipayFloatMenu.SHAKE_COUNT) {
				mShakeCount = 0;
				//最后一次摇一摇效果触发到本次效果触发之间需要一定间隔时间才会触发隐藏/显示
				if (now - lastHide_ShowTime > UmipayFloatMenu.SHAKE_INTERVAL_TIME) {
					for (Map.Entry<Activity, FloatMenuBaseView> e : FloatMenuMap
							.entrySet()) {
						e.getValue().onShake();
					}
				}
				//每触发一次摇一摇效果也算一次触摸(防止用户一直摇)
				lastHide_ShowTime = now;
			}
			mX = event.values[SensorManager.DATA_X];
			mY = event.values[SensorManager.DATA_Y];
			mZ = event.values[SensorManager.DATA_Z];
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}


	class UmipayAccountChangeBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(final Context context, Intent intent) {
			try {
				if(mLastHasFocusActivity == null || mLastHasFocusActivity.isFinishing()){
					return;
				}
				String action = intent.getAction();
				if (UmipayCommonAccountCacheManager.ACTION_ACCOUNT_CHANGE.equalsIgnoreCase(action)) {
					Debug_Log.dd("receiver account change at " + new Date().toString());
					String srcPackageName = intent.getExtras().getString(SRC_PACKAGENAME);
					String actionCallBack = intent.getExtras().getString(ACTION_ACCOUNT_CHANGE_CALLBACK);
					boolean shouldReorderToFront = intent.getExtras().getBoolean(DEST_ACTIVITY_REORDER_TO_FRONT,false);
					String current_packageName = mLastHasFocusActivity.getComponentName().getPackageName();
					String current_className =mLastHasFocusActivity.getComponentName().getClassName();
					int current_taskid = mLastHasFocusActivity.getTaskId();

					if (UmipayAccountManager.getInstance(context).isLogin()) {
						UmipayCommonAccount account = UmipayCommonAccountCacheManager.getInstance(context).getCommonAccountByPackageName(mLastHasFocusActivity.getPackageName(), UmipayCommonAccountCacheManager.COMMON_ACCOUNT_TO_CHANGE);
						UmipayAccount currentAccount = UmipayAccountManager.getInstance(context).getCurrentAccount();
						if (account != null && currentAccount != null) {
							if (currentAccount.getUid() != account.getUid()) {
								//账号不同时调出切换账号界面
								Util_System_Runtime.getInstance().runInUiThread(new Runnable() {
									@Override
									public void run() {
										try {
											UmipayAccountChangeDialog dialog = UmipayAccountChangeDialog.getInstance(mLastHasFocusActivity);
											if(dialog != null && !dialog.isShowing()){
												dialog.show();
											}
										}catch (Throwable e){
											Debug_Log.e(e);
										}
									}
								});
							} else {
								//账号相同时pop出登录态不处理
								UmipayCommonAccountCacheManager.getInstance(context).popCommonAccountToChange();
							}
							if(!TextUtils.isEmpty(srcPackageName) && !TextUtils.isEmpty(actionCallBack) && mLastHasFocusActivity != null){
								Intent broadcastIntent = new Intent();
								broadcastIntent.setAction(actionCallBack);
								broadcastIntent.setPackage(srcPackageName);
								broadcastIntent.putExtra(DEST_PACKAGENAME, current_packageName);
								broadcastIntent.putExtra(DEST_CLASSNAME, current_className);
								broadcastIntent.putExtra(DEST_TASKID, current_taskid);
								context.sendBroadcast(broadcastIntent);
							}

							if(shouldReorderToFront) {
								mLastHasFocusActivity.getIntent();
								Intent moveToFrontIntent = new Intent(mLastHasFocusActivity, mLastHasFocusActivity.getClass());
								moveToFrontIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
								mLastHasFocusActivity.getApplicationContext().startActivity(moveToFrontIntent);
							}

						}
					}
				}
			} catch (Throwable e) {
				Debug_Log.e(e);
			}
		}
	}
}