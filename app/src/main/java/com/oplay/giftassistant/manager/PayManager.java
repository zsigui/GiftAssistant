package com.oplay.giftassistant.manager;

import android.content.Context;
import android.content.Intent;

import com.oplay.giftassistant.config.AppDebugConfig;
import com.oplay.giftassistant.config.GiftTypeUtil;
import com.oplay.giftassistant.config.Global;
import com.oplay.giftassistant.config.StatusCode;
import com.oplay.giftassistant.model.data.req.ReqGetCode;
import com.oplay.giftassistant.model.data.req.ReqPayCode;
import com.oplay.giftassistant.model.data.resp.IndexGiftNew;
import com.oplay.giftassistant.model.data.resp.PayCode;
import com.oplay.giftassistant.model.json.base.JsonReqBase;
import com.oplay.giftassistant.model.json.base.JsonRespBase;
import com.oplay.giftassistant.ui.activity.LoginActivity;
import com.oplay.giftassistant.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment_Dialog;
import com.oplay.giftassistant.ui.fragment.dialog.GetCodeDialog;
import com.oplay.giftassistant.ui.fragment.dialog.GiftConsumeDialog;
import com.oplay.giftassistant.ui.widget.button.GiftButton;
import com.oplay.giftassistant.util.NetworkUtil;
import com.oplay.giftassistant.util.ToastUtil;
import com.socks.library.KLog;

import net.ouwan.umipay.android.api.PayCallbackListener;
import net.ouwan.umipay.android.api.UmipayBrowser;
import net.ouwan.umipay.android.api.UmipaySDKStatusCode;
import net.ouwan.umipay.android.config.SDKConstantConfig;
import net.ouwan.umipay.android.global.Global_Url_Params;
import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.webjs.view.webview.Flags_Browser_Config;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.List;

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
	 * 显示偶玩支付界面
	 */
	public void showOuwanChargeView(Context context) {
		try {
			List<NameValuePair> paramsList = addOuwanClientParams(context);
			final String title = "充值";
			UmipayBrowser.postUrl(
					context,
					title,
					SDKConstantConfig.get_UMIPAY_PAY_URL(context),
					paramsList,
					Flags_Browser_Config.FLAG_AUTO_CHANGE_TITLE | Flags_Browser_Config.FLAG_USE_YOUMI_JS_INTERFACES,
					null, null, UmipayBrowser.PAY_OUWAN
			);
		} catch (Throwable e) {
			if (AppDebugConfig.IS_DEBUG) {
				Debug_SDK.e(e);
			}
		}
	}

	private List<NameValuePair> addOuwanClientParams(Context context) {

		List<NameValuePair> paramsList = Global_Url_Params.getDefaultRequestParams(context,
				SDKConstantConfig.get_UMIPAY_ACCOUNT_URL(context));
		if (AccountManager.getInstance().isLogin()) {
			paramsList.add(new BasicNameValuePair("recharge_source", String.valueOf(3)));//指定偶玩豆充值
			paramsList.add(new BasicNameValuePair("bankType", "upmp")); //指定银行卡充值类型
		}
		return paramsList;
	}

	/**
	 * 执行淘号操作，直接回调
	 */
	public void searchGift(final Context context, final IndexGiftNew gift, GiftButton button) {
		showLoading(context);
		handleScorePay(context, gift, button, false);
	}

	/**
	 * 请求执行抢号操作的购买服务
	 *
	 * @param context
	 * @param gift
	 */
	public void chargeGift(final Context context, final IndexGiftNew gift, final GiftButton button) {
		if (AccountManager.getInstance().isLogin()) {
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
						handleBeanPay(context, gift, button);
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
		} else {
			Intent intent = new Intent(context, LoginActivity.class);
			context.startActivity(intent);
		}
	}

	/**
	 * 处理使用偶玩豆抢号的一系列请求
	 */
	private void handleBeanPay(final Context context, final IndexGiftNew gift, final GiftButton button) {
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
								if (response != null && response.isSuccess()) {
									if (response.body() != null && response.body().getCode() == StatusCode.SUCCESS) {
										PayCode payCode = response.body().getData();
										OuwanSDKManager.getInstance().pay(payCode.tradeNo, payCode.payNumber,
												payCode.orderDesc, payCode.uid, new SdkPayCallbackListener(context,
														button, payCode.tradeNo));
										return;
									}
									hideLoading(context);
									ToastUtil.showShort("抢号失败 - " + (response.body() == null ?
											"解析失败" : response.body().error()));
									return;
								}
								hideLoading(context);
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
	private void handleScorePay(final Context context, final IndexGiftNew gift, final GiftButton button, final boolean isSeize) {
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
										if (isSeize) {
											// 抢号状态
											button.setState(GiftTypeUtil.TYPE_LIMIT_SEIZE);
										} else {
											// 淘号状态
											button.setState(GiftTypeUtil.TYPE_NORMAL_SEARCHED);
										}
										ObserverManager.getInstance().notifyGiftUpdate();
										return;
									}
									ToastUtil.showShort("抢号失败 - " + (response.body() == null ?
											"解析失败" : response.body().error()));
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
									if (response.body() != null && response.body().getCode() == StatusCode.SUCCESS) {

										GetCodeDialog.newInstance(response.body().getData().giftCode)
												.show(((BaseAppCompatActivity) mContext).getSupportFragmentManager(),
														GetCodeDialog.class.getSimpleName());
										mButton.setState(GiftTypeUtil.TYPE_LIMIT_SEIZED);
										ObserverManager.getInstance().notifyGiftUpdate();
										return;
									}
									KLog.d((response.body() == null ? "解析失败" : response.body().error()));
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
			}
		}
	}

}
