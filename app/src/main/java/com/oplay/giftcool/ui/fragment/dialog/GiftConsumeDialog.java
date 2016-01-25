package com.oplay.giftcool.ui.fragment.dialog;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.View;
import android.view.ViewStub;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.GiftTypeUtil;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.manager.ObserverManager;
import com.oplay.giftcool.manager.OuwanSDKManager;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Dialog;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.ReflectUtil;

/**
 * Created by zsigui on 16-1-12.
 */
public class GiftConsumeDialog extends BaseFragment_Dialog implements ObserverManager.UserUpdateListener {

	private static final String TITLE_BEAN_SCORE = "积分或偶玩豆消耗";
	private static final String TITLE_SCORE = "积分消耗";
	private static final String TITLE_BEAN = "偶玩豆消耗";
	private static final String REMAIN_BOTH = "余额：<img src='ic_score'/>%d 和 <img src='ic_bean'> %d";
	private static final String REMAIN_SCORE = "余额：<img src='ic_score'/> %d";
	private static final String REMAIN_BEAN = "余额：<img src='ic_bean'> %d";
	private static final String BEAN_SCORE_BOTH_BASE = "抢该礼包将消耗：<img src='ic_score'/> <font color='#f85454'>%d</font> " +
			"或 <img src='ic_bean'/> <font color='#f85454'>%d</font>，";
	private static final String BEAN_SCORE_BOTH_NOT_ENOUGH = BEAN_SCORE_BOTH_BASE + "余额不足。";
	private static final String BEAN_SCORE_BOTH_ENOUGH = BEAN_SCORE_BOTH_BASE + "确认抢号吗?";
	private static final String SCORE_BASE = "抢该礼包将消耗：<img src='ic_score'/> <font color='#f85454'>%d</font>，";
	private static final String SCORE_NOT_ENOUGH = SCORE_BASE + "余额不足。";
	private static final String SCORE_ENOUGH = SCORE_BASE + "确认抢号吗?";
	private static final String BEAN_BASE = "抢该礼包将消耗：<img src='ic_bean'/> <font color='#f85454'>%d</font>，";
	private static final String BEAN_NOT_ENOUGH = BEAN_BASE + "余额不足。";
	private static final String BEAN_ENOUGH = BEAN_BASE + "确认抢号吗?";

	private TextView tvContent;
	private TextView tvRemain;
	private LinearLayout llPayBean;
	private LinearLayout llPayScore;
	private CheckedTextView ctvCheckBean;
	private CheckedTextView ctvCheckScore;
	private TextView tvPayBean;
	private TextView tvPayScore;

	private int mScoreConsume;
	private int mBeanConsume;
	private int mConsumeType;
	// 用户选择的支付方式
	private int mPayType = GiftTypeUtil.PAY_TYPE_BOTH;
	private OnDialogClickListener mWrapperListener;

	/**
	 * 用于不足时包装原确定按键为赚积分接口
	 */
	private OnDialogClickListener mGetScoreListener = new OnDialogClickListener() {
		@Override
		public void onCancel() {
			if (mListener != null) {
				mListener.onCancel();
			} else {
				dismissAllowingStateLoss();
			}
		}

		@Override
		public void onConfirm() {
			IntentUtil.jumpEarnScore(getContext());
			dismissAllowingStateLoss();
		}
	};
	/**
	 * 用于不足时包装原确定按键为充值偶玩豆接口
	 */
	private OnDialogClickListener mGetBeanListener = new OnDialogClickListener() {
		@Override
		public void onCancel() {
			if (mListener != null) {
				mListener.onCancel();
			} else {
				dismissAllowingStateLoss();
			}
		}

		@Override
		public void onConfirm() {
			// 跳转充值偶玩豆界面
			OuwanSDKManager.getInstance().recharge();
			dismissAllowingStateLoss();
		}
	};
	private Html.ImageGetter mImageGetter = new Html.ImageGetter() {
		@Override
		public Drawable getDrawable(String source) {
			int id = ReflectUtil.getDrawableId(getContext(), source);
			Drawable drawable = getResources().getDrawable(id);
			drawable.getPadding(new Rect(0, 0, 0, 4));
			drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
			return drawable;
		}
	};

	/**
	 * 简单实例化，后面需要调用setConsume
	 */
	public static GiftConsumeDialog newInstance() {
		return newInstance(0, 0, GiftTypeUtil.PAY_TYPE_SCORE);
	}

	public static GiftConsumeDialog newInstance(int bean, int score, int type) {
		GiftConsumeDialog dialog = new GiftConsumeDialog();
		dialog.setConsume(bean, score, type);
		return dialog;
	}

	public int getPayType() {
		return mPayType;
	}

	@Override
	protected void initView() {
		setContentView(R.layout.dialog_gift_consume);
		tvContent = getViewById(R.id.tv_content);
		tvRemain = getViewById(R.id.tv_remain_count);
	}

	public void setConsume(int bean, int score, int type) {
		mBeanConsume = bean;
		mScoreConsume = score;
		mConsumeType = type;
		if (mIsPrepared) {
			resetUI();
		}
	}

	/**
	 * 重新设置弹窗界面
	 */
	private void resetUI() {
		mWrapperListener = null;
		int hasBean = AccountManager.getInstance().getUserInfo().bean;
		int hasScore = AccountManager.getInstance().getUserInfo().score;

		setPositiveBtnText(getResources().getString(R.string.st_dialog_btn_confirm));
		mPayType = GiftTypeUtil.PAY_TYPE_NONE;
		if (mConsumeType == GiftTypeUtil.PAY_TYPE_SCORE) {
			setTitle(TITLE_SCORE);
			mPayType = GiftTypeUtil.PAY_TYPE_SCORE;
			tvRemain.setText(Html.fromHtml(String.format(REMAIN_SCORE, hasScore), mImageGetter, null));
			if (hasScore < mScoreConsume) {
				// 可选积分支付方式，但积分不足
				tvContent.setText(Html.fromHtml(String.format(SCORE_NOT_ENOUGH, mScoreConsume), mImageGetter, null));
				setPositiveBtnText("赚积分");
				mWrapperListener = mGetScoreListener;
			} else {
				// 可选积分支付方式，积分充足
				tvContent.setText(Html.fromHtml(String.format(SCORE_ENOUGH, mBeanConsume), mImageGetter, null));
			}
		} else if (mConsumeType == GiftTypeUtil.PAY_TYPE_BEAN) {
			setTitle(TITLE_BEAN);
			mPayType = GiftTypeUtil.PAY_TYPE_BEAN;
			tvRemain.setText(Html.fromHtml(String.format(REMAIN_BEAN, hasBean), mImageGetter, null));
			if (hasBean < mBeanConsume) {
				// 可选偶玩豆支付方式，但偶玩豆不足
				tvContent.setText(Html.fromHtml(String.format(BEAN_NOT_ENOUGH, mBeanConsume), mImageGetter, null));
				setPositiveBtnText("充值余额");
				mWrapperListener = mGetBeanListener;
			} else {
				// 可选偶玩豆支付方式，偶玩豆充足
				tvContent.setText(Html.fromHtml(String.format(BEAN_ENOUGH, mBeanConsume), mImageGetter, null));
			}
		} else {
			setTitle(TITLE_BEAN_SCORE);
			mPayType = GiftTypeUtil.PAY_TYPE_SCORE;
			tvRemain.setText(Html.fromHtml(String.format(REMAIN_BOTH, hasScore, hasBean), mImageGetter, null));
			if (hasBean < mBeanConsume && hasScore < mScoreConsume) {
				// 可选积分或偶玩豆支付方式，但积分和偶玩豆都不足
				tvContent.setText(Html.fromHtml(String.format(BEAN_SCORE_BOTH_NOT_ENOUGH, mScoreConsume,
						mBeanConsume), mImageGetter, null));
				setPositiveBtnText("充值余额");
				mWrapperListener = mGetBeanListener;
			} else {
				// 可选积分或偶玩豆支付方式，积分和偶玩豆至少可抢一种
				tvContent.setText(Html.fromHtml(String.format(BEAN_SCORE_BOTH_ENOUGH, mScoreConsume,
						mBeanConsume), mImageGetter, null));
			}
		}

		if (mConsumeType == GiftTypeUtil.PAY_TYPE_BOTH
				&& (mBeanConsume <= hasBean || mScoreConsume <= hasScore)) {
			if (llPayBean == null || llPayScore == null) {
				// 还未填充，进行XML填充
				ViewStub vs = getViewById(R.id.vs_pay_method);
				vs.inflate();
				llPayBean = getViewById(R.id.ll_pay_bean);
				llPayScore = getViewById(R.id.ll_pay_score);
				ctvCheckBean = getViewById(R.id.ctv_check_bean);
				ctvCheckScore = getViewById(R.id.ctv_check_score);
				tvPayBean = getViewById(R.id.tv_pay_bean);
				tvPayScore = getViewById(R.id.tv_pay_score);
			}
			if (mBeanConsume <= hasBean) {
				ctvCheckBean.setChecked(true);
				llPayBean.setEnabled(true);
				ctvCheckBean.setEnabled(true);
				tvPayBean.setEnabled(true);
				mPayType = GiftTypeUtil.PAY_TYPE_BEAN;
				tvPayBean.setTextColor(getResources().getColor(R.color.co_common_text_main));
			} else {
				ctvCheckBean.setChecked(false);
				llPayBean.setEnabled(false);
				ctvCheckBean.setEnabled(false);
				tvPayBean.setEnabled(false);
				tvPayBean.setTextColor(getResources().getColor(R.color.co_common_text_second));
			}
			if (mScoreConsume <= hasScore) {
				ctvCheckScore.setChecked(true);
				llPayScore.setEnabled(true);
				ctvCheckScore.setEnabled(true);
				tvPayScore.setEnabled(true);
				mPayType = GiftTypeUtil.PAY_TYPE_SCORE;
				tvPayScore.setTextColor(getResources().getColor(R.color.co_common_text_main));
			} else {
				ctvCheckScore.setChecked(false);
				llPayScore.setEnabled(false);
				ctvCheckScore.setEnabled(false);
				tvPayScore.setEnabled(false);
				tvPayScore.setTextColor(getResources().getColor(R.color.co_common_text_second));
			}

			if (mScoreConsume <= hasScore
					&& mBeanConsume <= hasBean) {
				ctvCheckScore.setChecked(true);
				ctvCheckBean.setChecked(false);
				tvPayScore.setEnabled(true);
				tvPayBean.setEnabled(true);
				tvPayScore.setTextColor(getResources().getColor(R.color.co_common_text_main));
				tvPayBean.setTextColor(getResources().getColor(R.color.co_common_text_main));
				mPayType = GiftTypeUtil.PAY_TYPE_SCORE;
			}
			llPayBean.setOnClickListener(this);
			llPayScore.setOnClickListener(this);
		}
	}

	@Override
	protected void processLogic() {
		setConsume(mBeanConsume, mScoreConsume, mConsumeType);
		ObserverManager.getInstance().addUserUpdateListener(this);
	}

	@Override
	public void onClick(View v) {
		// 重写按键事件
		switch (v.getId()) {
			case R.id.btn_confirm:
				if (mWrapperListener != null) {
					mWrapperListener.onConfirm();
					return;
				}
				if (mListener != null) {
					mListener.onConfirm();
				}
				break;
			case R.id.btn_cancel:
				if (mWrapperListener != null) {
					mWrapperListener.onCancel();
					return;
				}
				if (mListener != null) {
					mListener.onCancel();
				}
				break;
			case R.id.ll_pay_bean:
				if (llPayBean.isEnabled()) {
					ctvCheckBean.setChecked(true);
					ctvCheckScore.setChecked(false);
					mPayType = GiftTypeUtil.PAY_TYPE_BEAN;
				}
				break;
			case R.id.ll_pay_score:
				if (llPayScore.isEnabled()) {
					ctvCheckScore.setChecked(true);
					ctvCheckBean.setChecked(false);
					mPayType = GiftTypeUtil.PAY_TYPE_SCORE;
				}
				break;
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mIsPrepared = false;
	}

	@Override
	public void onUserUpdate() {
		setConsume(mBeanConsume, mScoreConsume, mConsumeType);
	}
}
