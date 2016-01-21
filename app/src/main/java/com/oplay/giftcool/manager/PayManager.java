package com.oplay.giftcool.manager;

import android.content.Context;
import android.content.Intent;

import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.GiftTypeUtil;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.StatusCode;
import com.oplay.giftcool.model.data.req.ReqGetCode;
import com.oplay.giftcool.model.data.req.ReqPayCode;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;
import com.oplay.giftcool.model.data.resp.PayCode;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.activity.LoginActivity;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Dialog;
import com.oplay.giftcool.ui.fragment.dialog.GetCodeDialog;
import com.oplay.giftcool.ui.fragment.dialog.GiftConsumeDialog;
import com.oplay.giftcool.ui.widget.button.GiftButton;
import com.oplay.giftcool.util.NetworkUtil;
import com.oplay.giftcool.util.ToastUtil;
import com.socks.library.KLog;

import net.ouwan.umipay.android.api.PayCallbackListener;
import net.ouwan.umipay.android.api.UmipaySDKStatusCode;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * 支付相关操作管理器
 * <p/>
 * Created by zsigui on 16-1-14.
 */
public class PayManager {
	private static PayManager manager;

	private PayManager() {
	}

	public static PayManager getInstance() {
		if (manager == null) {
			manager = new PayManager();
		}
		return manager;
	}


	private long mLastClickTime = 0;


	/**
	 * 取消订单支付
	 */
	public void quickTrade(final String tradeNo) {
		Global.THREAD_POOL.execute(new Runnable() {
			@Override
			public void run() {
				ReqGetCode reqData = new ReqGetCode();
				reqData.tradeNo = tradeNo;
				Global.getNetEngine().notifyTradeFail(new JsonReqBase<ReqGetCode>(reqData))
						.enqueue(new Callback<JsonRespBase<Void>>() {
							@Override
							public void onResponse(Response<JsonRespBase<Void>> response, Retrofit retrofit) {
								if (response != null && response.isSuccess()) {
									if (response.body() != null && response.body().isSuccess()) {
										return;
									}
									if (AppDebugConfig.IS_DEBUG) {
										KLog.d(AppDebugConfig.TAG_MANAGER, response.body() == null ?
												"解析失败" : response.body().getMsg());
									}
									return;
								}
								if (AppDebugConfig.IS_DEBUG) {
									KLog.d(AppDebugConfig.TAG_MANAGER, "错误返回");
								}
							}

							@Override
							public void onFailure(Throwable t) {
								if (AppDebugConfig.IS_DEBUG) {
									KLog.d(AppDebugConfig.TAG_MANAGER, t);
								}
							}
						});
			}
		});
	}

	/**
	 * 执行抢礼包操作
	 *
	 * @param context 上下文
	 * @param gift    礼包详情信息
	 * @param button  礼包按钮
	 */
	public void seizeGift(Context context, IndexGiftNew gift, GiftButton button) {
		if (!AccountManager.getInstance().isLogin()) {
			Intent intent = new Intent(context, LoginActivity.class);
			context.startActivity(intent);
		}
		switch (GiftTypeUtil.getItemViewType(gift)) {
			case GiftTypeUtil.TYPE_NORMAL_SEIZE:
			case GiftTypeUtil.TYPE_LIMIT_SEIZE:
				PayManager.getInstance().chargeGift(context, gift, button);
				break;
			case GiftTypeUtil.TYPE_NORMAL_SEARCH:
			case GiftTypeUtil.TYPE_NORMAL_SEARCHED:
				PayManager.getInstance().searchGift(context, gift, button);
				break;
		}
	}


	/**
	 * 执行淘号操作，直接回调
	 */
	private void searchGift(final Context context, final IndexGiftNew gift, GiftButton button) {
		handleScorePay(context, gift, button, false);
	}


	/**
	 * 请求执行抢号操作的购买服务
	 *
	 * @param context
	 * @param gift
	 */
	private void chargeGift(final Context context, final IndexGiftNew gift, final GiftButton button) {

		if (gift.priceType == GiftTypeUtil.PAY_TYPE_SCORE
				&& gift.score <= AccountManager.getInstance().getUserInfo().score) {
			// 只有积分支付方式且积分充足，直接执行抢号
			handleScorePay(context, gift, button, true);
			return;
		}
		// 积分或偶玩豆支付方式
		final GiftConsumeDialog consumeDialog = GiftConsumeDialog.newInstance();
		consumeDialog.setListener(new BaseFragment_Dialog.OnDialogClickListener() {
			@Override
			public void onCancel() {
				consumeDialog.dismiss();
			}

			@Override
			public void onConfirm() {
				// 执行抢号操作
				long nowClickTime = System.currentTimeMillis();
				if (System.currentTimeMillis() - mLastClickTime <= 2000) {
					ToastUtil.showShort("请求过于频繁，请勿连续点击");
					return;
				}
				mLastClickTime = nowClickTime;
				// 根据选择类型判断支付方式
				if (consumeDialog.getPayType() == GiftTypeUtil.PAY_TYPE_BEAN) {
					handleBeanPay(context, gift, button, true);
				} else if (consumeDialog.getPayType() == GiftTypeUtil.PAY_TYPE_SCORE) {
					handleScorePay(context, gift, button, true);
				} else {
					ToastUtil.showShort("选择支付类型有误，请重新选择");
					return;
				}
				consumeDialog.dismiss();
			}
		});
		consumeDialog.setConsume(gift.bean, gift.score, gift.priceType);
		consumeDialog.show(((BaseAppCompatActivity) context).getSupportFragmentManager(),
				GiftConsumeDialog.class.getSimpleName());
	}

	/**
	 * 处理使用偶玩豆抢号的一系列请求
	 */
	private void handleBeanPay(final Context context, final IndexGiftNew gift, final GiftButton button,
	                           final boolean isSeize) {
		showLoading(context);
		Global.THREAD_POOL.execute(new Runnable() {
			@Override
			public void run() {
				if (!NetworkUtil.isConnected(context)) {
					ToastUtil.showShort("网络连接异常");
					hideLoading(context);
					return;
				}
				final ReqPayCode payCode = new ReqPayCode();
				payCode.id = gift.id;
				payCode.type = GiftTypeUtil.PAY_TYPE_BEAN;
				Global.getNetEngine().payGiftCode(new JsonReqBase<ReqPayCode>(payCode))
						.enqueue(new Callback<JsonRespBase<PayCode>>() {
							@Override
							public void onResponse(Response<JsonRespBase<PayCode>> response, Retrofit retrofit) {
								hideLoading(context);
								if (response != null && response.isSuccess()) {
									if (response.body() != null && response.body().getCode() == StatusCode.SUCCESS) {
										GetCodeDialog.newInstance(response.body().getData().giftCode)
												.show(((BaseAppCompatActivity) context).getSupportFragmentManager(),
														GetCodeDialog.class.getSimpleName());
										if (button != null) {
											if (isSeize) {
												// 抢号状态
												button.setState(GiftTypeUtil.TYPE_LIMIT_SEIZE);
											} else {
												// 淘号状态
												button.setState(GiftTypeUtil.TYPE_NORMAL_SEARCHED);
											}
										}
										ObserverManager.getInstance().notifyGiftUpdate();
										return;
									}
									ToastUtil.showShort("抢号失败 - " + (response.body() == null ?
											"解析失败" : response.body().getMsg()));
									return;
								}
								ToastUtil.showShort("抢号失败 - 返回出错");
							}

							@Override
							public void onFailure(Throwable t) {
								hideLoading(context);
								if (AppDebugConfig.IS_DEBUG) {
									KLog.d(AppDebugConfig.TAG_UTIL, t);
								}
								ToastUtil.showShort("抢号失败 - 网络异常");
							}
						});
			}
		});
	}

	/**
	 * 处理使用积分抢号的一系列请求
	 */
	private void handleScorePay(final Context context, final IndexGiftNew gift, final GiftButton button,
	                            final boolean isSeize) {
		showLoading(context);
		Global.THREAD_POOL.execute(new Runnable() {
			@Override
			public void run() {
				if (!NetworkUtil.isConnected(context)) {
					ToastUtil.showShort("网络连接异常");
					hideLoading(context);
					return;
				}
				ReqPayCode payCode = new ReqPayCode();
				payCode.id = gift.id;
				payCode.type = GiftTypeUtil.PAY_TYPE_SCORE;
				Global.getNetEngine().payGiftCode(new JsonReqBase<ReqPayCode>(payCode))
						.enqueue(new Callback<JsonRespBase<PayCode>>() {
							@Override
							public void onResponse(Response<JsonRespBase<PayCode>> response, Retrofit retrofit) {
								hideLoading(context);
								if (response != null && response.isSuccess()) {
									if (response.body() != null && response.body().getCode() == StatusCode.SUCCESS) {

										GetCodeDialog.newInstance(response.body().getData().giftCode)
												.show(((BaseAppCompatActivity) context).getSupportFragmentManager(),
														GetCodeDialog.class.getSimpleName());
										if (button != null) {
											if (isSeize) {
												// 抢号状态
												button.setState(GiftTypeUtil.TYPE_LIMIT_SEIZE);
											} else {
												// 淘号状态
												button.setState(GiftTypeUtil.TYPE_NORMAL_SEARCHED);
											}
										}
										ObserverManager.getInstance().notifyGiftUpdate();
										return;
									}
									ToastUtil.showShort("抢号失败 - " + (response.body() == null ?
											"解析失败" : response.body().getMsg()));
									return;
								}
								ToastUtil.showShort("抢号失败 - 返回出错");
							}

							@Override
							public void onFailure(Throwable t) {
								hideLoading(context);
								if (AppDebugConfig.IS_DEBUG) {
									KLog.d(AppDebugConfig.TAG_UTIL, t);
								}
								ToastUtil.showShort("抢号失败 - 网络异常");
							}
						});
			}
		});
	}

	private void showLoading(Context context) {
		if (context instanceof BaseAppCompatActivity) {
			((BaseAppCompatActivity) context).showLoadingDialog();
		}
	}

	private void hideLoading(Context context) {
		if (context instanceof BaseAppCompatActivity) {
			((BaseAppCompatActivity) context).hideLoadingDialog();
		}
	}

	private class SdkPayCallbackListener implements PayCallbackListener {

		private GiftButton mButton;
		private String mTradeNo;
		private Context mContext;

		private SdkPayCallbackListener(Context context, GiftButton button, String tradeNo) {
			mContext = context;
			mButton = button;
			mTradeNo = tradeNo;
		}

		@Override
		public void onPay(int code) {
			if (code == UmipaySDKStatusCode.PAY_FINISH
					|| code == UmipaySDKStatusCode.SUCCESS) {
				// 执行查号操作
				ReqGetCode getCode = new ReqGetCode();
				getCode.tradeNo = mTradeNo;
				Global.getNetEngine().getSpecificGiftCode(new JsonReqBase<ReqGetCode>(getCode))
						.enqueue(new Callback<JsonRespBase<PayCode>>() {
							@Override
							public void onResponse(Response<JsonRespBase<PayCode>> response, Retrofit retrofit) {
								hideLoading(mContext);
								if (response != null && response.isSuccess()) {
									if (response.body() != null && response.body().isSuccess()) {

										GetCodeDialog.newInstance(response.body().getData().giftCode)
												.show(((BaseAppCompatActivity) mContext).getSupportFragmentManager(),
														GetCodeDialog.class.getSimpleName());
										if (mButton != null) {
											mButton.setState(GiftTypeUtil.TYPE_LIMIT_SEIZED);
										}
										ObserverManager.getInstance().notifyGiftUpdate();
										return;
									}
									KLog.d((response.body() == null ? "解析失败" : response.body().getMsg()));
								}
								ToastUtil.showShort("查询礼包码失败, 请稍后重新查看");
							}

							@Override
							public void onFailure(Throwable t) {
								hideLoading(mContext);
								if (AppDebugConfig.IS_DEBUG) {
									KLog.d(AppDebugConfig.TAG_UTIL, t);
								}
								ToastUtil.showShort("查询礼包码失败 - 网络异常");
							}
						});
			} else {
				// 取消支付，通知取消订单
				PayManager.getInstance().quickTrade(mTradeNo);
			}
		}
	}

}
