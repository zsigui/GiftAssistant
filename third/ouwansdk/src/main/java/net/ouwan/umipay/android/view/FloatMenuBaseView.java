package net.ouwan.umipay.android.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.NinePatch;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.ouwan.umipay.android.Utils.Util_Resource;
import net.ouwan.umipay.android.api.UmipayFloatMenu;
import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.manager.FloatmenuCacheManager;
import net.ouwan.umipay.android.manager.UmipayAccountManager;
import net.youmi.android.libs.common.util.Util_System_Display;

import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

public class FloatMenuBaseView implements OnClickListener, OnTouchListener,
		OnGestureListener, Observer {

	private static final int ADD_FLOATMENU = 0;
	private static final int REMOVE_FLOATMENU = 1;
	private static final int RESET_FLOATMENU = 2;
	private static final int MOVE_FLOATMENU = 3;
	private static final int SHOW_OUWAN = 4;
	private static final int HIDE_OUWAN = 5;
	private static final int SHOW_TAB = 6;
	private static final int HIDE_TAB = 7;
	private static final int UPDATE_BUBBLE = 8;
	private static final int DESTROY_FLOATMENU = 9;

	private long lastTouchTime = 0;
	private int mOrientation;// 默认状态
	private int mScreenWidth;
	private int mScreenHeight;

	private int mTabWidth;
	private int mTabHeight;

	private Context mContext;
	private Activity mActivity;
	private Handler mHandler;

	private WindowManager mWindowManager;
	private ViewGroup mRootLayout;
	private ViewGroup mTabLayout;
	private ViewGroup mTabContentLayout;

	private FloatMenuTabLinearLayout mTabContent_Parent;
	private FrameLayout mOuwanFrameLayout;

	private TextView mOunwan_menu_Bubble_TextView;
	private int menu_Bubble = 0;

//    private static boolean mEnableFloatMenuDialog = true;//是否显示小红点隐藏提示框

	private boolean mIsFloatMenuShow = false;
	private boolean mIsOuwanShow = true;// ouwan浮标状态
	private boolean mIsHidingOuwan = false;// 隐藏动画中
	private boolean mIsTouch = false;
	private boolean mIsSliding = false;
	private Animation mShowTabAnimation;
	private Animation mShowTabAnimation_Mirror;
	private Animation mHideOwanAnimation_Left;
	private Animation mHideOwanAnimation_Right;
	private GestureDetector mGestureDetector;
	private LayoutParams mOuWanParams;
	private LayoutParams mTabParams;
	private Timer timer;
	private TimerTask move_timerTask;

	//    private FloatMenuHideDialog dialog;
	private Bitmap TabContent_Background;

	/**
	 * @param mActivity FloatMenu绑定的activity
	 *                  默认显示所有功能
	 */
	public FloatMenuBaseView(Activity mActivity) {
		this.mActivity = mActivity;
		this.mContext = mActivity;
		mGestureDetector = new GestureDetector(mContext, this);
		initResources();
		initAnimationListener();
		initMembers();
		initHandler();
		initListener();
		initFloatMenu();
	}

	/**
	 * 初始化资源
	 */
	private void initResources() {
		try {

			mShowTabAnimation = AnimationUtils.loadAnimation(mContext,
					Util_Resource.getIdByReflection(mContext, "anim",
							"umipay_tab_anim")
			);
			mShowTabAnimation_Mirror = AnimationUtils.loadAnimation(mContext,
					Util_Resource.getIdByReflection(mContext, "anim",
							"umipay_tab_anim_mirror")
			);
			//隐藏ouwan的动画效果
			mHideOwanAnimation_Left = AnimationUtils.loadAnimation(mContext,
					Util_Resource.getIdByReflection(mContext, "anim",
							"umipay_ouwan_hide_left"));
			mHideOwanAnimation_Right = AnimationUtils.loadAnimation(mContext,
					Util_Resource.getIdByReflection(mContext, "anim",
							"umipay_ouwan_hide_right"));

		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

	/**
	 * 初始化成员
	 */
	private void initMembers() {
		if (mActivity == null || mContext == null) {
			return;
		}
		try {
			mWindowManager = (WindowManager) mActivity.getSystemService(Context.WINDOW_SERVICE);
			DisplayMetrics displayMetrics = new DisplayMetrics();
			mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);
			mOrientation = mActivity.getResources().getConfiguration().orientation;
			mScreenWidth = displayMetrics.widthPixels;
			mScreenHeight = displayMetrics.heightPixels;
			mRootLayout = (ViewGroup) View.inflate(mContext, Util_Resource
					.getIdByReflection(mContext, "layout",
							"umipay_floatmemu_layout"), null);
			mOuwanFrameLayout = (FrameLayout) mRootLayout.findViewById(Util_Resource
					.getIdByReflection(mContext, "id", "umipay_memu_ouwan"));
			mOunwan_menu_Bubble_TextView = (TextView) mRootLayout.findViewById(Util_Resource
					.getIdByReflection(mContext, "id", "umipay_memu_ouwan_bubble"));

			mTabLayout = (ViewGroup) View.inflate(mContext, Util_Resource
					.getIdByReflection(mContext, "layout", "umipay_tab_layout"), null);
			mTabContentLayout = (ViewGroup) mTabLayout.findViewById(Util_Resource
					.getIdByReflection(mContext, "id", "umipay_tab_content"));
			mTabContent_Parent = new FloatMenuTabLinearLayout(mContext, this);
			if(mTabContentLayout != null) {
				mTabContentLayout.addView(mTabContent_Parent, 1);
			}
//            dialog = new FloatMenuHideDialog(mContext);

			FloatmenuCacheManager.getInstance(mContext).addObserver(FloatMenuBaseView.this);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

	/**
	 * 初始化Listenr
	 */
	private void initListener() {
		try {
			if (mOuwanFrameLayout != null) {
				mOuwanFrameLayout.setOnTouchListener(this);
			}
			if (mTabLayout != null) {
				mTabLayout.setOnTouchListener(this);
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}


	/**
	 * 初始化浮动菜单
	 */
	private void initFloatMenu() {
		try {
			if (mOuWanParams == null) {
				mOuWanParams = new LayoutParams();
				mOuWanParams.type = LayoutParams.TYPE_APPLICATION; // 设置window type
				mOuWanParams.format = PixelFormat.TRANSLUCENT; // 设置图片格式，效果为背景透明
				// 即使在该window在可获得焦点情况下，仍然把该window之外的任何event发送到该window之后的其他window.
				mOuWanParams.flags = 40;
				mOuWanParams.gravity = Gravity.TOP | Gravity.LEFT;
				mOuWanParams.x = 0;
				mOuWanParams.y = (mScreenHeight - Util_System_Display.dip2px(mContext, 50)) / 2;
				// 设置悬浮窗口长宽数据
				mOuWanParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
				mOuWanParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
			}
			if (mTabParams == null) {
				mTabParams = new LayoutParams();
				mTabParams.type = LayoutParams.TYPE_APPLICATION; // 设置window type
				mTabParams.format = PixelFormat.TRANSLUCENT; // 设置图片格式，效果为背景透明
				// 即使在该window在可获得焦点情况下，仍然把该window之外的任何event发送到该window之后的其他window.
				mTabParams.flags = 40 | LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
				mTabParams.gravity = Gravity.TOP | Gravity.LEFT;
				mTabParams.x = 0;
				mTabParams.y = 0;
				// 设置悬浮窗口长宽数据
				mTabParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
				mTabParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
			}
			if (mTabLayout != null) {
				//通过调用measure方法获得mTabLayout的大小，当使用getWidth和getHeight所得大小为0时使用mTabWidth、mTabHeight作为默认大小
				int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
				int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
				mTabLayout.measure(w, h);
				mTabWidth = mTabLayout.getMeasuredWidth();
				mTabHeight = mTabLayout.getMeasuredHeight();
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}


	/**
	 * 初始化隐藏ouwan的动画设置
	 */
	private void initAnimationListener() {
		try {
			if (mHideOwanAnimation_Left != null) {
				mHideOwanAnimation_Left.setAnimationListener(getAnimationListener());
			}
			if (mHideOwanAnimation_Right != null) {
				mHideOwanAnimation_Right.setAnimationListener(getAnimationListener());
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}

	}

	/**
	 * 获得贴边隐藏动画
	 */
	private AnimationListener getAnimationListener() {
		AnimationListener animationListener = new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				mIsHidingOuwan = true;
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				if (mOuwanFrameLayout != null && mOuWanParams != null) {
					try {
						String bg;
						TranslateAnimation translate_animation;
						float toXValue = 0;
						FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout
								.LayoutParams.WRAP_CONTENT, Util_System_Display.dip2px(mContext, 15));
						lp.gravity = Gravity.RIGHT | Gravity.TOP;
						if (mOuWanParams.x < mScreenWidth / 2) {
							//贴左边隐藏
							bg = "umipay_btn_ouwan_hide_left";
							lp.setMargins(0, 0, Util_System_Display.dip2px(mContext, 15), 0);
							toXValue = -0.5f;
						} else {
							//贴右边隐藏
							bg = "umipay_btn_ouwan_hide_right";
							lp.setMargins(0, 0, Util_System_Display.dip2px(mContext, 10), 0);
							toXValue = 0.5f;
						}
						setBackground(mOuwanFrameLayout, Util_Resource.getIdByReflection(
								mContext, "drawable", bg));
						translate_animation = new TranslateAnimation(Animation
								.RELATIVE_TO_SELF,
								toXValue, Animation.RELATIVE_TO_SELF, 0.0f,
								Animation.RELATIVE_TO_SELF, 0.0f,
								Animation.RELATIVE_TO_SELF, 0.0f);
						if (translate_animation != null) {
							translate_animation.setDuration(200);//
							mOuwanFrameLayout.startAnimation(translate_animation);
						}
						if (mOunwan_menu_Bubble_TextView != null && mContext != null) {
							mOunwan_menu_Bubble_TextView.setLayoutParams(lp);
						}
						mIsHidingOuwan = false;
						mIsOuwanShow = false;
						lastTouchTime = System.currentTimeMillis();
					} catch (Throwable e) {
						Debug_Log.e(e);
					}
				}
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}
		};
		return animationListener;
	}

	/**
	 * mHandle处理FloatMenu的相关UI操作
	 */
	private void initHandler() {
		mHandler = new Handler(mContext.getMainLooper()) {
			@Override
			public void handleMessage(Message msg) {
				try {
					switch (msg.what) {
						case ADD_FLOATMENU:
							AddToWindow();
							mHandler.removeMessages(REMOVE_FLOATMENU);
							break;
						case REMOVE_FLOATMENU:
							RemoveFromWindow();
							mHandler.removeMessages(ADD_FLOATMENU);
							break;
						case DESTROY_FLOATMENU:
							destroy();//销毁时释放资源,释放后仍会跑完最后一次，必须return;
							mHandler.removeMessages(ADD_FLOATMENU);
							mHandler.removeMessages(REMOVE_FLOATMENU);
							break;
						case RESET_FLOATMENU:
							ReSet();
							break;
						case MOVE_FLOATMENU:
							Slide();
							break;
						case SHOW_OUWAN:
							ShowOuWan();
							mHandler.removeMessages(HIDE_OUWAN);
							break;
						case HIDE_OUWAN:
							if (EnableSlide() != true) {
								//贴边不可滑动的时候才允许执行HideOuWan
								HideOuWan();
							}
							mHandler.removeMessages(SHOW_OUWAN);
							break;
						case SHOW_TAB:
							if (EnableSlide() != true) {
								//贴边不可滑动的时候才允许执行ShowTab
								ShowTab();
							}
							mHandler.removeMessages(HIDE_TAB);
							break;
						case HIDE_TAB:
							HideTab();
							mHandler.removeMessages(SHOW_TAB);
							break;
						case UPDATE_BUBBLE:
							updateBubble();
							break;
						default:
							break;
					}
				} catch (Throwable e) {
					Debug_Log.e(e);
				}
			}
		};
	}

	/**
	 * 初始化定时器
	 */
	private void initTimer() {
		if (mContext == null) {
			return;
		}
		try {
			if (timer == null) {
				//使Timer线程为deamon线程，应用结束则结束
				timer = new Timer(true);
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

	/**
	 * 取消定时器
	 */
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
	 * 取消滑动定时任务
	 */
	private void cancelSlideTimerTask() {
		try {
			if (move_timerTask != null && mIsSliding == true) {
				move_timerTask.cancel();
				move_timerTask = null;
				mIsSliding = false;
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

	/**
	 * 更新floatmenu
	 */
	public void updateFloatMenu() {
		try {
			if (mActivity == null || mActivity.isFinishing() == true) {
				Destroy();
				return;
			}
			if (mIsTouch == true || mIsSliding == true || mHandler == null) {
				//拖动时、自动移动时不更新
				return;
			}
			long currentTime = System.currentTimeMillis();
			if (mIsFloatMenuShow == false) {
				//隐藏浮动菜单时=
				if (isAddToWindow() == true) {
					mHandler.sendEmptyMessage(REMOVE_FLOATMENU);
				}
				return;
			} else {
				//显示浮动菜单时
				if (UmipayAccountManager.getInstance(mContext).isLogin() == false) {
					//没登陆时
					if (isAddToWindow() == true) {
						mHandler.sendEmptyMessage(REMOVE_FLOATMENU);
					}
					return;
				} else {
					//已经登录时
					if (isAddToWindow() == false) {
						//若未添加到屏幕
						mHandler.sendEmptyMessage(ADD_FLOATMENU);
						mHandler.sendEmptyMessage(RESET_FLOATMENU);
					}
					if (checkOrientation() == true) {
						mHandler.sendEmptyMessage(RESET_FLOATMENU);
						return;
					}
					if ((currentTime - lastTouchTime) > UmipayFloatMenu.HIDE_INTERVAL_TIME
							&& mIsOuwanShow == true && isTabShow() == false) {
						mHandler.sendEmptyMessage(HIDE_OUWAN);
						return;
					}
				}
			}

		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

	/**
	 * 设置自动贴边定时任务
	 */
	private void setSlideTimerTask() {
		if (timer == null || mIsSliding == true) {
			return;
		}
		try {
			cancelSlideTimerTask();
			move_timerTask =
					new TimerTask() {

						@Override
						public void run() {
							try {
								if (mActivity.isFinishing()) {
									Destroy();
									return;
								}
								if (isAddToWindow() == false) {
									cancelSlideTimerTask();
									return;
								}
								if (mIsTouch == true || mIsHidingOuwan) {
									//触摸中或隐藏动画中不滑动
									return;
								}
								if (mIsOuwanShow == false) {
									//小红点处于隐藏状态时先显示小红点
									if (mHandler != null) {
										mHandler.sendEmptyMessage(SHOW_OUWAN);
									}
								}
								if (EnableSlide()) {
									if (mHandler != null) {
										mHandler.sendEmptyMessage(MOVE_FLOATMENU);
									}
									return;
								} else {
									if (mIsSliding == true) {
										cancelSlideTimerTask();
									}
								}
							} catch (Throwable e) {
								Debug_Log.e(e);
							}
						}

					};
			if (move_timerTask != null) {
				mIsSliding = true;
				timer.schedule(move_timerTask, 0, UmipayFloatMenu.MOVE_INTERVAL_TIME);
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

	/**
	 * 检查是否已经将悬浮窗加到window
	 */
	private boolean isAddToWindow() {
		if (mRootLayout == null || mTabLayout == null || mRootLayout.getParent() == null || mTabLayout.getParent() ==
				null) {
			return false;
		}
		return true;
	}

	/**
	 * 将悬浮窗中的view加入窗口（UI线程中调用）
	 */
	private void AddToWindow() {
		// 设置Ouwan菜单背景
		try {
			if (mActivity != null) {
				if (mRootLayout == null || mTabLayout == null ||
						mWindowManager == null || mActivity == null) {
					return;
				}
				if (mActivity.isFinishing() == false) {
					// 然后，就可以将需要加到悬浮窗口中的View加入到窗口中了：
					// 如果view没有被加入到某个父组件中，则加入WindowManager中
					if (mRootLayout.getParent() == null) {
						mWindowManager.addView(mRootLayout, mOuWanParams);
					}
					if (mTabLayout.getParent() == null) {
						mWindowManager.addView(mTabLayout, mTabParams);
					}
					if (mTabContent_Parent != null) {
						mTabContent_Parent.update();
					}
					updateBubble();
				}
			}
		} catch (Throwable e) {
			Debug_Log.e("不能添加浮动菜单到当前activity中，请检测当前floatmenu所绑定activity是否正确。");
			Debug_Log.e(e);
			Destroy();
		}

	}

	/**
	 * 将悬浮窗中的从窗口移除（UI线程中调用）
	 */
	private void RemoveFromWindow() {
		try {
			if (!isAddToWindow() || mWindowManager == null) {
				return;
			}
			if (mRootLayout.getParent() != null) {
				mWindowManager.removeView(mRootLayout);
			}
			if (mTabLayout.getParent() != null) {
				mWindowManager.removeView(mTabLayout);
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

	/**
	 * 显示浮动Ouwan菜单
	 */
	public void ShowFloatMenu() {
		initTimer();
		mIsFloatMenuShow = true;
	}

	/**
	 * 隐藏浮动Ouwan菜单（UI线程中调用）
	 */
	public void HideFloatMenu() {
		if (mActivity == null || mActivity.isFinishing()) {
			return;
		}
		try {
			if (mContext != null) {
				cancelSlideTimerTask();
				cancelTimer();
				mIsFloatMenuShow = false;
			}
			return;
		} catch (Exception e) {
			Debug_Log.e(e);
		}
	}


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (!UmipayAccountManager.getInstance(mContext).isLogin()) {
			return;
		}
		if (v instanceof FloatMenuTabButton) {
			((FloatMenuTabButton) v).jump();
		}
		HideTab();
	}

	/**
	 * 显示中的ouwan（UI线程中调用）
	 */
	private void ShowOuWan() {
		//设置显示中的Ouwan背景
		if (null == mOuwanFrameLayout) {
			return;
		}
		try {
			setBackground(mOuwanFrameLayout, Util_Resource.getIdByReflection(
					mContext, "drawable", "umipay_btn_ouwan_normal"));
			if (mOunwan_menu_Bubble_TextView != null && mContext != null) {
				FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams
						.WRAP_CONTENT, Util_System_Display.dip2px(mContext, 15));

				lp.gravity = Gravity.RIGHT | Gravity.TOP;
				lp.setMargins(0, 0, Util_System_Display.dip2px(mContext, 4), 0);
				mIsOuwanShow = true;
				mOunwan_menu_Bubble_TextView.setLayoutParams(lp);
			}
		} catch (Exception e) {
			Debug_Log.e(e);
		}
	}

	/**
	 * 隐藏中的ouwan
	 */
	private void HideOuWan() {
		//设置隐藏中的Ouwan背景
		try {
			long currentTime = System.currentTimeMillis();
			if (mOuwanFrameLayout == null || mIsHidingOuwan || (currentTime - lastTouchTime) <
					UmipayFloatMenu.HIDE_INTERVAL_TIME || mHideOwanAnimation_Left == null ||
					mHideOwanAnimation_Right ==
							null) {
				return;
			}
			Animation animation = (mOuWanParams.x == 0) ? mHideOwanAnimation_Left : mHideOwanAnimation_Right;
			mOuwanFrameLayout.startAnimation(animation);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}

	}

	/**
	 * 显示Tab（UI线程中调用）
	 */
	private void ShowTab() {
		// TODO Auto-generated method stub
		//隐藏动画中不展开菜单
		if (mIsHidingOuwan == true || !isAddToWindow() || mContext == null) {
			return;
		}
		try {
			//设菜单为可见
			mTabLayout.setVisibility(View.VISIBLE);
			//tab有可能未绘制，getHeight和getWidth会为0
			int tab_width = (mTabLayout.getWidth() == 0) ? mTabWidth : mTabLayout.getWidth();
			int tab_height = (mTabLayout.getHeight() == 0) ? mTabHeight : mTabLayout.getHeight();
			updateTabPosition(mOuWanParams.x - tab_width,
					mOuWanParams.y + (mOuwanFrameLayout.getHeight() - tab_height) / 2);
			//设置Ouwan背景
			String bg_name = (mOuWanParams.x > mScreenWidth / 2) ? "umipay_btn_ouwan_pressed_on_right" :
					"umipay_btn_ouwan_pressed_on_left";
			setBackground(mOuwanFrameLayout, Util_Resource.getIdByReflection(
					mContext, "drawable", bg_name));

			if (isAddToWindow() == false || mTabLayout == null || mTabContent_Parent == null) {
				return;
			}
			mTabContent_Parent.setMirror(mOuWanParams.x > mScreenWidth / 2);
			// 播放动画,tab在左边则向右展开,在右边则向左边展开
			Animation animation = (mOuWanParams.x > mScreenWidth / 2) ? mShowTabAnimation_Mirror : mShowTabAnimation  ;

			int marginLeft = (mOuWanParams.x > mScreenWidth / 2)?Util_System_Display.dip2px(mContext, 7):Util_System_Display.dip2px(mContext, 2);
			int marginRight = (mOuWanParams.x > mScreenWidth / 2)?Util_System_Display.dip2px(mContext, 2):Util_System_Display.dip2px(mContext, 7);

			if (animation != null && mTabContentLayout != null) {
				LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mTabContentLayout.getLayoutParams();
				lp.setMargins(marginLeft,0,marginRight,0);
				mTabLayout.setLayoutParams(lp);
				mTabContentLayout.startAnimation(animation);
			}
			//展开菜单时不显示偶玩君上的小红点
			if (mOunwan_menu_Bubble_TextView != null) {
				mOunwan_menu_Bubble_TextView.setVisibility(View.GONE);
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

	/**
	 * 隐藏Tab（UI线程中调用）
	 */
	private void HideTab() {
		//设置Ouwan背景
		if (isTabShow()) {
			try {
				setBackground(mOuwanFrameLayout, Util_Resource.getIdByReflection(
						mContext, "drawable", "umipay_btn_ouwan_normal"));
				if (!isAddToWindow()) {
					return;
				}
				if (mTabLayout != null) {
					mTabLayout.setVisibility(View.GONE);
				}
				if (mOunwan_menu_Bubble_TextView != null) {
					int visibility = (menu_Bubble > 0) ? View.VISIBLE : View.GONE;
					mOunwan_menu_Bubble_TextView.setVisibility(visibility);
				}
			} catch (Throwable e) {
				Debug_Log.e(e);
			}
		}
	}

	/**
	 * 判断是否隐藏Tab
	 */
	private boolean isTabShow() {
		if (mTabLayout != null && mTabLayout.getVisibility() == View.VISIBLE) {
			return true;
		}
		return false;
	}


	/**
	 * 更新Ouwan显示位置（UI线程中调用）
	 *
	 * @param x x坐标
	 * @param y y坐标
	 */
	private void updateOuwanPosition(int x, int y) {
		try {
			mOuWanParams.x = x;
			mOuWanParams.y = y;
			if (mActivity != null) {
				if (!isAddToWindow()) {
					return;
				}
				if (mWindowManager == null || mRootLayout == null || mActivity == null) {
					return;
				}
				if (!mActivity.isFinishing() && mRootLayout.getParent() != null) {
					mWindowManager.updateViewLayout(mRootLayout, mOuWanParams);
				}
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

	/**
	 * 更新Tab显示位置（UI线程中调用）
	 *
	 * @param x x坐标
	 * @param y y坐标
	 */
	private void updateTabPosition(int x, int y) {
		int newPositionX = x;
		try {
			if (x < 0) {
				//拖动Ouwan菜单的时候收起Tab，所以展开Tab的时候Ouwan必定为贴边
				newPositionX = mOuWanParams.x + mOuwanFrameLayout.getWidth();
				//Tab默认在屏幕右边展开，如果Ouwan菜单贴左边，则换成Tab在屏幕左边时的形态
				if ((newPositionX + mTabLayout.getWidth()) > mScreenWidth) {
					if (y - mOuwanFrameLayout.getHeight() < 0) {
						y += mOuwanFrameLayout.getHeight();
					} else {
						y -= mOuwanFrameLayout.getHeight();
					}
					x = 0;
				} else {
					x = newPositionX;
				}
			}
			mTabParams.x = x;
			mTabParams.y = y;
			//更新Ouwan菜单
			if (mActivity != null) {
				if (mWindowManager != null && mTabLayout != null && mActivity != null) {
					if (!mActivity.isFinishing() && mTabLayout.getParent() != null) {
						mWindowManager.updateViewLayout(mTabLayout, mTabParams);
					}
				}
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return;
	}

	/**
	 * 检查横竖屏是否有切换，有则ReSet并返回true，否则返回false
	 */
	private boolean checkOrientation() {
		try {
			int currentOrientation = mActivity.getResources()
					.getConfiguration().orientation;
			if (mOrientation != currentOrientation) {
				return true;
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return false;
	}

	/**
	 * 横竖屏切换的时候跟新屏幕宽高的值，同时刷新Ouwan菜单的显示，隐藏Tab（UI线程中调用）
	 */
	private void ReSet() {
		try {
			mOrientation = mActivity.getResources().getConfiguration().orientation;
			//Reset时切换回正常显示ouwan
			mIsOuwanShow = true;
			mIsHidingOuwan = false;
			mIsSliding = false;
			mIsTouch = false;
			lastTouchTime = System.currentTimeMillis();
			ShowOuWan();
			//如果菜单展开则收起
			HideTab();
			DisplayMetrics displayMetrics = new DisplayMetrics();
			mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);

			int x = mOuWanParams.x;
			int y = mOuWanParams.y;

			if (x < mScreenWidth / 2) {
				mScreenWidth = displayMetrics.widthPixels;
				mOuWanParams.x = 0;
			} else {
				mScreenWidth = displayMetrics.widthPixels;
				mOuWanParams.x = mScreenWidth - mOuwanFrameLayout.getWidth();
			}

			if (y > (mScreenHeight - mOuwanFrameLayout.getHeight())) {
				mScreenHeight = displayMetrics.heightPixels;
				mOuWanParams.y = mScreenHeight - mOuwanFrameLayout
						.getHeight();
			}
			updateOuwanPosition(mOuWanParams.x, mOuWanParams.y);
			if(mTabContent_Parent != null){
				mTabContent_Parent.setMirror(mOuWanParams.x > mScreenWidth / 2);
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}

	}

	/**
	 * 判断是否已经贴边
	 */
	private boolean EnableSlide() {
		if (0 == mOuWanParams.x || mOuWanParams.x == (mScreenWidth - mOuwanFrameLayout.getWidth())) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 自动贴边（在UI线程中调用）
	 */
	private void Slide() {
		if (isTabShow()) {
			return;
		}
		int x = 0;
		try {
			if (mOuWanParams.x < (mScreenWidth) / 2) {
				x = mOuWanParams.x - Util_System_Display.dip2px(mContext, 10);
				if (x > 0) {
					updateOuwanPosition(x, mOuWanParams.y);
				} else {
					updateOuwanPosition(0, mOuWanParams.y);
				}

			} else {
				x = mOuWanParams.x + Util_System_Display.dip2px(mContext, 10);
				if (x < (mScreenWidth - mOuwanFrameLayout.getWidth())) {
					updateOuwanPosition(x, mOuWanParams.y);
				} else {
					updateOuwanPosition(mScreenWidth - mOuwanFrameLayout.getWidth(), mOuWanParams.y);
				}
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}

	}

	public void Destroy() {
		cancelSlideTimerTask();
		cancelTimer();
		if (mHandler != null) {
			mHandler.sendEmptyMessage(DESTROY_FLOATMENU);
		}
	}

	/**
	 * 销毁悬浮菜单，注意销毁后不可再调用该实例中的方法
	 * 如果有使用悬浮菜单，要在在其绑定的activity中重载onDestroy
	 * 并调用该方法来确保销毁相应悬浮菜单的资源
	 * UI线程中调用
	 */
	private void destroy() {
		try {
//            if (dialog != null) {
//                dialog = null;
//            }
			if (mActivity != null) {
				RemoveFromWindow();
				mActivity = null;
			}
			if (mContext != null) {
				recycle(mTabContent_Parent);
				recycle(mOuwanFrameLayout);
				mContext = null;
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

	/**
	 * 设置背景资源,每次设置前先清空原来背景（UI线程中调用）
	 */
	private void setBackground(View view, int id) {
		try {
			recycle(view);
			if (view == null || id == 0 || mContext == null) {
				return;
			}
			Bitmap bm = BitmapFactory.decodeResource(mContext.getResources(), id);
			if (bm == null) {
				return;
			}
			Drawable drawable = null;
			if (NinePatch.isNinePatchChunk(bm.getNinePatchChunk())) {
				//9png图片
				TabContent_Background = bm;
				drawable = new NinePatchDrawable(mContext.getResources(), TabContent_Background,
						TabContent_Background.getNinePatchChunk(), new Rect(), null);
			} else {
				//普通图片
				drawable = new BitmapDrawable(mContext.getResources(), bm);
			}
			view.setBackgroundDrawable(drawable);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

	/**
	 * 释放控件背景资源（UI线程中调用）
	 */
	private void recycle(View view) {
		try {
			if (view == null) {
				return;
			}
			view.setBackgroundResource(0);//避免onDraw刷新时出现used a recycled bitmap
			Drawable d = view.getBackground();
			if (d != null) {
				d.setCallback(null);
			}
			if (view.equals(mTabContent_Parent)) {
				if (TabContent_Background != null && !TabContent_Background.isRecycled()) {
					TabContent_Background.recycle();
				}
				return;
			} else {
				BitmapDrawable bd = (BitmapDrawable) d;
				if (bd != null && !bd.getBitmap().isRecycled()) {
					bd.getBitmap().recycle();
				}
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}




	public void onShake() {
		if (timer == null) {
			ShowFloatMenu();
		} else {
			HideFloatMenu();
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int i = event.getAction();
		if (v.equals(mOuwanFrameLayout)) {
			lastTouchTime = System.currentTimeMillis();
			try {
				if (checkOrientation()) {
					//横竖屏切换后必须先ReSet再触发展开/隐藏
					mHandler.sendEmptyMessage(RESET_FLOATMENU);
					return false;
				}
				if (mIsHidingOuwan == true) {
					//隐藏动画时不处理
					return false;
				}

				if (v.equals(mOuwanFrameLayout)) {
					mGestureDetector.onTouchEvent(event);
				}
				switch (i) {
					case MotionEvent.ACTION_DOWN:
						mIsTouch = true;
						if (mIsOuwanShow == false) {
							//按下时，若小红点处于隐藏状态时先显示小红点
							ShowOuWan();
						}
						break;
					case MotionEvent.ACTION_UP:
						//松开手指的时候，重置最后触摸时间
						mIsTouch = false;
						//不贴边时自动移动
						if (mIsSliding != true && EnableSlide() == true) {
							setSlideTimerTask();
						}
						break;
				}

				return true;
			} catch (Throwable e) {
				Debug_Log.e(e);
			}
		} else if (v.equals(mTabLayout)) {

			//小红点外的区域点击隐藏tab

			Rect rect = new Rect();
			mTabLayout.getGlobalVisibleRect(rect);
			Rect rect1 = new Rect();
			mOuwanFrameLayout.getGlobalVisibleRect(rect1);

			int y = (rect1.bottom - rect.bottom) / 2 + 1;
			Rect leftRect = new Rect(0 - rect1.right, 0 - y, rect.right, rect.bottom + y);
			Rect rightRect = new Rect(0, 0 - y, rect.right + rect1.right, rect.bottom + y);

			if (i == MotionEvent.ACTION_OUTSIDE) {
				if (isTabShow()) {
					lastTouchTime = System.currentTimeMillis();
					if (mOuWanParams.x == 0) {
						if (!leftRect.contains((int) event.getX(), (int) event.getY())) {
							HideTab();
						}
					} else {
						if (!rightRect.contains((int) event.getX(), (int) event.getY())) {
							HideTab();
						}
					}
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		Debug_Log.dd("onSingleTapUp");
		if (mHandler != null) {
			if (isTabShow() == true) {
				mHandler.sendEmptyMessage(HIDE_TAB);
			} else if (!mIsHidingOuwan) {
				//隐藏动画结束才允许展开Tab
				mHandler.sendEmptyMessage(SHOW_TAB);
			}
		}
		return true;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
	                        float distanceY) {
		try {
			//新位置坐标，结束的绝对坐标-开始的相对坐标
			int newOuwanPositionX = (int) (e2.getRawX() - e1.getX());
			int newOuwanPositionY = (int) (e2.getRawY() - e1.getY());
			//超出屏幕处理
			newOuwanPositionX = newOuwanPositionX < 0 ? 0 : newOuwanPositionX;
			newOuwanPositionX = newOuwanPositionX + mOuwanFrameLayout.getWidth() > mScreenWidth ? mScreenWidth
					- mOuwanFrameLayout.getWidth()
					: newOuwanPositionX;
			newOuwanPositionY = newOuwanPositionY < 0 ? 0 : newOuwanPositionY;
			newOuwanPositionY = newOuwanPositionY + mOuwanFrameLayout.getHeight() > mScreenHeight ? mScreenHeight
					- mOuwanFrameLayout.getHeight()
					: newOuwanPositionY;
			HideTab();
			updateOuwanPosition(newOuwanPositionX, newOuwanPositionY);
		} catch (Throwable e) {
			Debug_Log.e(e);
			return false;
		}
		return true;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
	                       float velocityY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void update(Observable observable, Object data) {
		//如果观察到的是FloatMenu_Proxy_Bubble_CacheManager的变化，就调用更新未读信息方法
		if (observable instanceof FloatmenuCacheManager) {
			if (mHandler != null) {
				mHandler.sendEmptyMessage(UPDATE_BUBBLE);
			}
		}
	}

	private void updateBubble() {

		if (mContext == null || mOunwan_menu_Bubble_TextView == null) {
			return;
		}
		try {
			if (mTabContent_Parent != null) {
				mTabContent_Parent.update();
			}
			menu_Bubble = FloatmenuCacheManager.getInstance(mContext).getNoticeBubbleNum() + FloatmenuCacheManager
					.getInstance(mContext).getBoardBubbleNum();
			//统计、更新ouwan菜单总未读信息
			mOunwan_menu_Bubble_TextView.setText("" + menu_Bubble);
			if (menu_Bubble > 0) {
				mOunwan_menu_Bubble_TextView.setVisibility(View.VISIBLE);
			} else {
				mOunwan_menu_Bubble_TextView.setVisibility(View.GONE);
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}
}
