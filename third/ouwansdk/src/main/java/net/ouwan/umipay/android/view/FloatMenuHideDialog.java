//package net.ouwan.umipay.android.view;
//
//import android.app.Dialog;
//import android.content.Context;
//import android.graphics.drawable.AnimationDrawable;
//import android.graphics.drawable.BitmapDrawable;
//import android.graphics.drawable.Drawable;
//import android.view.Gravity;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.WindowManager;
//import android.view.animation.Animation;
//import android.view.animation.AnimationUtils;
//import android.widget.Button;
//import android.widget.CheckBox;
//import android.widget.CompoundButton;
//import android.widget.ImageView;
//
//import net.ouwan.umipay.android.Utils.Util_Resource;
//import net.ouwan.umipay.android.debug.Debug_Log;
//import net.youmi.android.libs.common.util.Util_System_Display;
//
//public class FloatMenuHideDialog extends Dialog implements View.OnClickListener,
//		CompoundButton.OnCheckedChangeListener {
//
//	Context mContext;
//
//	private ImageView mTechImg;
//	private Button mBtn;
//	private CheckBox mDiaLogShowCheckBox;
//	private ViewGroup mRootLayout;
//
//	public FloatMenuHideDialog(Context context) {
//
//		super(context, Util_Resource.getIdByReflection(context, "style",
//				"umipay_progress_dialog_theme"));
//		this.mContext = context;
//		initViews();
//		initListener();
//		setContentView(mRootLayout);
//		setLayoutParams();
//		this.setCancelable(false);//禁止使用回退键取消对话框
//		try {
//			//开始是否勾选不再显示，正常弹出dialog应该都是不勾选
//			if (mDiaLogShowCheckBox != null) {
//				mDiaLogShowCheckBox.setChecked(!FloatMenuBaseView.getEnableFloatMenuDialog());
//			}
//		} catch (Throwable e) {
//			Debug_Log.e(e);
//		}
//	}
//
//	private void setLayoutParams() {
//		try {
//			WindowManager.LayoutParams mLayoutParams = getWindow().getAttributes();  //获取对话框当前的参数值
//			if (mContext != null) {
//				mLayoutParams.width = Util_System_Display.dip2px(mContext, 250);
//			}
//			mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
//			mLayoutParams.gravity = Gravity.CENTER_VERTICAL;   //高度设置为屏幕的高度
//			getWindow().setAttributes(mLayoutParams);     //设置生效
//		} catch (Throwable e) {
//			Debug_Log.e(e);
//		}
//	}
//
//
//	private void initViews() {
//		if (mContext == null) {
//			return;
//		}
//		try {
//			mRootLayout = (ViewGroup) ViewGroup.inflate(mContext, Util_Resource.getIdByReflection(mContext, "layout",
//					"umipay_floatmenu_hide_dialog_layout"), null);
//			mTechImg = (ImageView) mRootLayout.findViewById(Util_Resource.getIdByReflection(mContext, "id",
//					"umipay_floatmenu_techImageView"));
//			mBtn = (Button) mRootLayout.findViewById(Util_Resource.getIdByReflection(mContext, "id",
//					"umipay_floatmenu_btn"));
//			mDiaLogShowCheckBox = (CheckBox) mRootLayout.findViewById(Util_Resource.getIdByReflection(mContext, "id",
//					"umipay_floatmenudialog_checkBox"));
//
//			if (mTechImg != null) {
//				mTechImg.post(new Runnable() {
//					@Override
//					public void run() {
//						try{
//							((AnimationDrawable)mTechImg.getDrawable()).start();
//						}catch (Throwable e){
//							Debug_Log.e(e);
//						}
//					}
//				});
//			}
//		} catch (Throwable e) {
//			Debug_Log.e(e);
//		}
//	}
//
//	@Override
//	public void onClick(View v) {
//		try {
//			if (v.equals(mBtn)) {
//				//取消
//				dismiss();
//			}
//		} catch (Throwable e) {
//			Debug_Log.e(e);
//		}
//	}
//
//	private void initListener() {
//		try {
//			if (mBtn != null) {
//				mBtn.setOnClickListener(this);
//			}
//			if (mDiaLogShowCheckBox != null) {
//				mDiaLogShowCheckBox.setOnCheckedChangeListener(this);
//			}
//		} catch (Throwable e) {
//			Debug_Log.e(e);
//		}
//	}
//
//
//	@Override
//	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//		if (buttonView.equals(mDiaLogShowCheckBox) && mContext != null) {
//			FloatMenuBaseView.setEnableFloatMemuDialog(!isChecked);
//		}
//	}
//
//}
//
//
//
