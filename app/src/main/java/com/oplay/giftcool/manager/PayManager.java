package com.oplay.giftcool.manager;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.TypeStatusCode;
import com.oplay.giftcool.config.util.GiftTypeUtil;
import com.oplay.giftcool.listener.WebViewInterface;
import com.oplay.giftcool.model.data.req.ReqChangeFocus;
import com.oplay.giftcool.model.data.req.ReqPayCode;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;
import com.oplay.giftcool.model.data.resp.PayCode;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Dialog;
import com.oplay.giftcool.ui.fragment.dialog.ConfirmDialog;
import com.oplay.giftcool.ui.fragment.dialog.GetCodeDialog;
import com.oplay.giftcool.ui.fragment.dialog.GiftConsumeDialog;
import com.oplay.giftcool.ui.fragment.gift.GiftDetailFragment;
import com.oplay.giftcool.ui.widget.button.GiftButton;
import com.oplay.giftcool.util.DateUtil;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.NetworkUtil;
import com.oplay.giftcool.util.ThreadUtil;
import com.oplay.giftcool.util.ToastUtil;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 支付相关操作管理器(我就不想吐槽这日了狗的需求耦合了)
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
    private long mLastDialogClickTime = 0;

    /**
     * 执行抢礼包操作
     *
     * @param context 上下文
     * @param gift    礼包详情信息
     * @param button  礼包按钮
     */
    public int seizeGift(FragmentActivity context, IndexGiftNew gift, GiftButton button) {
        return seizeGift(context, gift, button, null);
    }

    /**
     * 执行抢礼包操作，为了配合超级坑的弹窗回调指引
     */
    public int seizeGift(FragmentActivity context, IndexGiftNew gift, GiftButton button, GiftDetailFragment fragment) {
        if (context == null) {
//            ToastUtil.showShort("哎呦，执行出了点小问题，要不重启试试？");
            return WebViewInterface.RET_PARAM_ERR;
        }
        if (!AccountManager.getInstance().isLogin()) {
            IntentUtil.jumpLogin(context);
            return WebViewInterface.RET_OTHER_ERR;
        }
        long nowClickTime = System.currentTimeMillis();
        if (nowClickTime - mLastClickTime <= Global.CLICK_TIME_INTERVAL) {
            return WebViewInterface.RET_OTHER_ERR;
        }
        staticsPay(context, StatisticsManager.ID.GIFT_SEIZE_CLICK,
                StatisticsManager.ID.STR_GIFT_SEIZE_CLICK,
                StatisticsManager.ID.STR_GIFT_SEIZE_NO_PAY,
                gift, 0);
        mLastClickTime = nowClickTime;
        switch (gift.buttonState) {
            case GiftTypeUtil.BUTTON_TYPE_SEARCH:
                handleScorePay(context, gift, button, false);
                break;
            case GiftTypeUtil.BUTTON_TYPE_ACTIVITY_JOIN:
                IntentUtil.jumpPostDetail(context, gift.activityId);
                break;
            case GiftTypeUtil.BUTTON_TYPE_RESERVE_EMPTY:
                chargeReservedFailed(context, gift);
                break;
            case GiftTypeUtil.BUTTON_TYPE_RESERVE:
            case GiftTypeUtil.BUTTON_TYPE_SEIZE:
            case GiftTypeUtil.BUTTON_TYPE_RESERVE_TAKE:
                chargeGift(context, gift, button, fragment);
                break;
            default:
                ToastUtil.showShort(String.format(Locale.CHINA, "%s(%d)",
                        ConstString.TOAST_BTN_STATE_ERROR, gift.buttonState));
        }
        return WebViewInterface.RET_SUCCESS;
    }

    /**
     * 请求执行抢号操作的购买服务
     *
     * @param context
     * @param gift
     */
    private void chargeGift(final FragmentActivity context, final IndexGiftNew gift, final GiftButton button,
                            final GiftDetailFragment fragment) {

        if (gift.priceType == GiftTypeUtil.PAY_TYPE_NONE
                || gift.priceType == GiftTypeUtil.PAY_TYPE_BOTH) {
            // 未知金额类型，此时自己定义
            if (gift.bean == 0) {
                gift.priceType = GiftTypeUtil.PAY_TYPE_SCORE;
            } else if (gift.score == 0) {
                gift.priceType = GiftTypeUtil.PAY_TYPE_BEAN;
            } else {
                gift.priceType = GiftTypeUtil.PAY_TYPE_BOTH;
            }
        }
        if (gift.priceType == GiftTypeUtil.PAY_TYPE_SCORE
                && gift.score <= AccountManager.getInstance().getUserInfo().score) {
            // 只有金币支付方式且金币充足，直接执行抢号
            handleScorePay(context, gift, button, true, fragment);
            return;
        }
        // 金币或偶玩豆支付方式
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
                if (nowClickTime - mLastDialogClickTime <= 2000) {
                    ToastUtil.showShort(ConstString.TOAST_FREQUENT_REQUEST);
                    return;
                }
                mLastDialogClickTime = nowClickTime;

                // 根据选择类型判断支付方式
                if (consumeDialog.getPayType() == GiftTypeUtil.PAY_TYPE_BEAN) {
                    handleBeanPay(context, gift, button, true);
                } else if (consumeDialog.getPayType() == GiftTypeUtil.PAY_TYPE_SCORE) {
                    handleScorePay(context, gift, button, true);
                } else {
                    ToastUtil.showShort(ConstString.TOAST_PAY_METHOD_ERROR);
                    return;
                }
                consumeDialog.dismiss();
            }
        });
        consumeDialog.setConsume(gift.bean, gift.score, gift.priceType);
        consumeDialog.show(context.getSupportFragmentManager(),
                GiftConsumeDialog.class.getSimpleName());
    }

    /**
     * 处理使用偶玩豆抢号的一系列请求
     */
    private void handleBeanPay(final FragmentActivity context, final IndexGiftNew gift, final GiftButton button,
                               final boolean isSeize) {
        showLoading(context);
        ThreadUtil.runInThread(new Runnable() {
            @Override
            public void run() {
                if (!NetworkUtil.isConnected(context)) {
                    ToastUtil.showShort(ConstString.TOAST_NET_ERROR);
                    hideLoading(context);
                    return;
                }
                final ReqPayCode payCode = new ReqPayCode();
                payCode.id = gift.id;
                payCode.type = GiftTypeUtil.PAY_TYPE_BEAN;
                Global.getNetEngine().payGiftCode(new JsonReqBase<ReqPayCode>(payCode))
                        .enqueue(new Callback<JsonRespBase<PayCode>>() {
                            @Override
                            public void onResponse(Call<JsonRespBase<PayCode>> call, Response<JsonRespBase<PayCode>>
                                    response) {
                                hideLoading(context);
                                if (call.isCanceled()) {
                                    return;
                                }
                                beanPayBack(response, context, gift, button, isSeize);
                            }

                            @Override
                            public void onFailure(Call<JsonRespBase<PayCode>> call, Throwable t) {
                                hideLoading(context);
                                if (call.isCanceled()) {
                                    return;
                                }
                                ToastUtil.blurThrow(t);
                            }
                        });
            }
        });
    }

    /**
     * 处理偶玩豆支付请求发送后的返回
     */
    private void beanPayBack(Response<JsonRespBase<PayCode>> response, FragmentActivity context,
                             IndexGiftNew gift, GiftButton button, boolean isSeize) {
        if (response != null && response.isSuccessful()) {
            if (response.body() != null && response.body().isSuccess()) {
                beanPaySuccess(response.body().getData(), context, gift, button, isSeize);
                return;
            }
            if (response.body() != null) {
                if (AccountManager.getInstance().judgeIsSessionFailed(response.body())) {
                    ToastUtil.showShort(ConstString.TOAST_SESSION_UNAVAILABLE);
                    IntentUtil.jumpLoginNoToast(context);
                    return;
                }
                ConfirmDialog dialog = ConfirmDialog.newInstance();
                dialog.setTitle(context.getResources().getString(R.string
                        .st_dialog_seize_failed));
                dialog.setNegativeVisibility(View.GONE);
                dialog.setPositiveVisibility(View.VISIBLE);
                dialog.setPositiveBtnText(context.getResources().getString(R.string
                        .st_dialog_btn_ok));
                dialog.setContent(response.body().getMsg());
                dialog.show(context.getSupportFragmentManager(), "seize failed");
                return;
            }
        }
        ToastUtil.blurErrorResp(response);
    }

    /**
     * 偶玩豆支付成功后的处理
     */
    private void beanPaySuccess(PayCode codeData, FragmentActivity context, IndexGiftNew gift,
                                GiftButton button, boolean isSeize) {
        // 更新部分用户信息
        AccountManager.getInstance().updatePartUserInfo();
        // 构造支付弹窗
        GetCodeDialog dialog = GetCodeDialog.newInstance(codeData);
        dialog.setTitle(context.getResources().getString(R.string
                .st_dialog_seize_success));
        dialog.show(context.getSupportFragmentManager(), GetCodeDialog.class.getSimpleName());
        // 统计
        staticsPay(context, StatisticsManager.ID.GIFT_BEAN_SEIZE,
                StatisticsManager.ID.STR_GIFT_SEIZE_CLICK,
                StatisticsManager.ID.STR_GIFT_BEAN_SEIZE,
                gift, 1);

        if (button != null) {
            if (isSeize) {
                // 抢号状态
                gift.seizeStatus = GiftTypeUtil.SEIZE_TYPE_SEIZED;
                button.setState(GiftTypeUtil.BUTTON_TYPE_SEIZED);
            } else {
                // 淘号状态
                gift.seizeStatus = GiftTypeUtil.SEIZE_TYPE_SEARCHED;
                button.setState(GiftTypeUtil.BUTTON_TYPE_SEARCH);
            }
        }
        if (codeData.gameInfo != null) {
            doFocusOperation(codeData.gameInfo.id);
        }
        ObserverManager.getInstance()
                .notifyGiftUpdate(ObserverManager.STATUS.GIFT_UPDATE_PART);
    }

    /**
     * 处理使用金币抢号的一系列请求
     */
    private void handleScorePay(final FragmentActivity context, final IndexGiftNew gift, final GiftButton button,
                                final boolean isSeize) {
        handleScorePay(context, gift, button, isSeize, null);
    }

    /**
     * 处理使用金币抢号的一系列请求
     */
    private void handleScorePay(final FragmentActivity context, final IndexGiftNew gift, final GiftButton button,
                                final boolean isSeize, final GiftDetailFragment fragment) {
        showLoading(context);
        ThreadUtil.runInThread(new Runnable() {
            @Override
            public void run() {
                if (!NetworkUtil.isConnected(context)) {
                    ToastUtil.showShort(ConstString.TOAST_NET_ERROR);
                    hideLoading(context);
                    return;
                }
                ReqPayCode payCode = new ReqPayCode();
                payCode.id = gift.id;
                payCode.type = GiftTypeUtil.PAY_TYPE_SCORE;
                Global.getNetEngine().payGiftCode(new JsonReqBase<ReqPayCode>(payCode))
                        .enqueue(new Callback<JsonRespBase<PayCode>>() {
                            @Override
                            public void onResponse(Call<JsonRespBase<PayCode>> call, Response<JsonRespBase<PayCode>>
                                    response) {
                                hideLoading(context);
                                if (call.isCanceled()) {
                                    return;
                                }
                                AppDebugConfig.d(AppDebugConfig.TAG_WARN, "mFragment = " + fragment);
                                scorePayBack(response, isSeize, context, gift, button, fragment);
                            }

                            @Override
                            public void onFailure(Call<JsonRespBase<PayCode>> call, Throwable t) {
                                hideLoading(context);
                                if (call.isCanceled()) {
                                    return;
                                }
                                ToastUtil.blurThrow(t);
                            }
                        });
            }
        });
    }

    /**
     * 处理积分支付网络请求后的返回
     */
    private void scorePayBack(Response<JsonRespBase<PayCode>> response, boolean isSeize, FragmentActivity context,
                              IndexGiftNew gift, GiftButton button, GiftDetailFragment fragment) {
        if (response != null && response.isSuccessful()) {
            if (response.body() != null && response.body().isSuccess()) {
                if (GiftTypeUtil.getItemViewType(gift) == GiftTypeUtil.TYPE_CHARGE_UN_RESERVE) {
                    chargeReservedSuccess(context, gift, button);
                } else {
                    scorePaySuccess(response.body().getData(), isSeize, context, gift, button, fragment);
                }
                return;
            }
            if (response.body() != null) {
                if (AccountManager.getInstance().judgeIsSessionFailed(response.body())) {
                    ToastUtil.showShort(ConstString.TOAST_SESSION_UNAVAILABLE);
                    IntentUtil.jumpLoginNoToast(context);
                    return;
                }
                if (GiftTypeUtil.getItemViewType(gift) == GiftTypeUtil.TYPE_CHARGE_UN_RESERVE) {
                    // 预约首充券失败提示
                    chargeReservedFailed(context, gift);
                } else {
                    ConfirmDialog dialog = ConfirmDialog.newInstance();
                    if (isSeize) {
                        dialog.setTitle(context.getResources().getString(R.string
                                .st_dialog_seize_failed));
                    } else {
                        dialog.setTitle(context.getResources().getString(R.string
                                .st_dialog_search_failed));
                    }
                    dialog.setNegativeVisibility(View.GONE);
                    dialog.setPositiveVisibility(View.VISIBLE);
                    dialog.setPositiveBtnText(context.getResources().getString(R.string
                            .st_dialog_btn_ok));
                    dialog.setContent(response.body().getMsg());
                    dialog.show(context.getSupportFragmentManager(), "search failed");
                }
            }
        }
        ToastUtil.blurErrorResp(response);
    }

    /**
     * 首充券预约成功后的提示
     */
    private void chargeReservedSuccess(FragmentActivity context, IndexGiftNew gift, GiftButton button) {
        DialogManager.getInstance().showHintDialog(context.getSupportFragmentManager(),
                "恭喜，预约成功！",
                String.format(Locale.CHINA, "已为您预留一枚首充券兑换码到%s", DateUtil.optDate(gift.reserveDeadline)),
                String.format(Locale.CHINA,
                        ConstString.TEXT_GIFT_FREE_SEIZE, DateUtil.formatUserReadDate(gift.freeStartTime)),
                "reserve_hint");
        gift.seizeStatus = GiftTypeUtil.SEIZE_TYPE_RESERVED;
        if (button != null) {
            button.setState(GiftTypeUtil.BUTTON_TYPE_RESERVED);
        }
        ObserverManager.getInstance().notifyGiftUpdate(ObserverManager.STATUS.GIFT_UPDATE_PART);
    }

    /**
     * 首充券预约失败后的提示
     */
    private void chargeReservedFailed(FragmentActivity context, IndexGiftNew gift) {
        DialogManager.getInstance().showHintDialog(context.getSupportFragmentManager(),
                "天啦，预约号被抢光了！",
                "预约号已满，免费开抢时间再来抢吧！",
                String.format(Locale.CHINA,
                        ConstString.TEXT_GIFT_FREE_SEIZE, DateUtil.formatUserReadDate(gift.freeStartTime)),
                "reserve_hint");
    }

    /**
     * 积分支付成功后的处理
     */
    private void scorePaySuccess(PayCode codeData, boolean isSeize, FragmentActivity context, IndexGiftNew gift,
                                 GiftButton button, GiftDetailFragment fragment) {
        // 更新部分用户信息
        AccountManager.getInstance().updatePartUserInfo();
        // 弹窗
        GetCodeDialog dialog = GetCodeDialog.newInstance(codeData);
        if (codeData.gameInfo != null) {
            doFocusOperation(codeData.gameInfo.id);
        }
        if (isSeize) {
            dialog.setTitle(context.getResources().getString(R.string
                    .st_dialog_seize_success));
        } else {
            dialog.setTitle(context.getResources().getString(R.string
                    .st_dialog_search_success));
        }
        AppDebugConfig.d(AppDebugConfig.TAG_DEBUG_INFO, "mFragment = " + fragment
                + ", mType = " + GiftTypeUtil.getItemViewType(gift));
        dialog.setCouponCharge(GiftTypeUtil.getItemViewType(gift), fragment);
        // 统计
        staticsPay(context, StatisticsManager.ID.GIFT_GOLD_SEIZE,
                StatisticsManager.ID.STR_GIFT_SEIZE_CLICK,
                StatisticsManager.ID.STR_GIFT_GOLD_SEIZE,
                gift, 2);
        dialog.show(context.getSupportFragmentManager(),
                GetCodeDialog.class.getSimpleName());
        if (isSeize) {
            // 抢号状态
            gift.seizeStatus = GiftTypeUtil.SEIZE_TYPE_SEIZED;
            if (button != null) {
                button.setState(GiftTypeUtil.BUTTON_TYPE_SEIZED);
            }
        } else {
            // 淘号状态
            gift.seizeStatus = GiftTypeUtil.SEIZE_TYPE_SEARCHED;
            if (button != null) {
                button.setState(GiftTypeUtil.BUTTON_TYPE_SEARCH);
            }
        }
        ObserverManager.getInstance()
                .notifyGiftUpdate(ObserverManager.STATUS.GIFT_UPDATE_PART);
    }

    /**
     * 统计抢礼包时间，payType=1||2 分别表示成功的偶玩豆或金币支付事件，才用计算事件统计
     */

    private void staticsPay(Context context, String tag, String title, String subTitle, IndexGiftNew gift, int
            payType) {
        Map<String, String> kv = new HashMap<String, String>();
        kv.put("所属游戏名", gift.gameName);
        kv.put("名称", gift.name);
        String payTypeStr;
        String payMoney;
        String giftType;
        kv.put("型号", String.valueOf(gift.totalType));
        kv.put("礼包类型", String.valueOf(gift.giftType));
        switch (gift.giftType) {
            case GiftTypeUtil.GIFT_TYPE_NORMAL:
                giftType = "普通";
                break;
            case GiftTypeUtil.GIFT_TYPE_NORMAL_FREE:
                giftType = "普通免费";
                break;
            case GiftTypeUtil.GIFT_TYPE_LIMIT:
                giftType = "限量";
                break;
            case GiftTypeUtil.GIFT_TYPE_LIMIT_FREE:
                giftType = "限时免费";
                break;
            default:
                giftType = "未知:" + gift.giftType;
                break;
        }
        switch (payType) {
            case 1:
                payTypeStr = "偶玩豆";
                payMoney = String.valueOf(gift.bean);
                break;
            case 2:
                payTypeStr = "金币";
                payMoney = String.valueOf(gift.score);
                break;
            default:
                payTypeStr = "未知";
                payMoney = "0";
        }
        kv.put("支付类型", payTypeStr);
        kv.put("价格", payMoney);
        kv.put("总计", String.format("%s-%s-%s-%s-%s", gift.gameName, gift.name, giftType, payTypeStr, payMoney));
        StatisticsManager.getInstance().trace(context, tag, title, subTitle, kv, gift.bean);
    }


    /**
     * 取消关注的请求实体
     */
    private JsonReqBase<ReqChangeFocus> mQuickFocusReqBase;

    /**
     * 执行判断添加游戏关注
     */
    private void doFocusOperation(int id) {
        // 没有开启自动关注
        if (!AssistantApp.getInstance().isShouldAutoFocus()) {
            return;
        }
        if (mQuickFocusReqBase == null) {
            ReqChangeFocus focus = new ReqChangeFocus();
            focus.status = TypeStatusCode.FOCUS_ON;
            mQuickFocusReqBase = new JsonReqBase<>(focus);
        }
        mQuickFocusReqBase.data.gameId = id;
        Global.getNetEngine().changeGameFocus(mQuickFocusReqBase).enqueue(new Callback<JsonRespBase<Void>>() {
            @Override
            public void onResponse(Call<JsonRespBase<Void>> call, Response<JsonRespBase<Void>> response) {
                if (call.isCanceled()) {
                    return;
                }
                AppDebugConfig.warnResp(AppDebugConfig.TAG_MANAGER, response);
            }

            @Override
            public void onFailure(Call<JsonRespBase<Void>> call, Throwable t) {
                AppDebugConfig.w(AppDebugConfig.TAG_MANAGER, t);
            }
        });
    }

    private void showLoading(Context context) {
        if (context != null && context instanceof BaseAppCompatActivity) {
            ((BaseAppCompatActivity) context).showLoadingDialog();
        }
    }

    private void hideLoading(Context context) {
        if (context != null && context instanceof BaseAppCompatActivity) {
            ((BaseAppCompatActivity) context).hideLoadingDialog();
        }
    }


//	/**
//	 * 取消订单支付
//	 */
//	public void quickTrade(final String tradeNo) {
//		ThreadUtil.runInThread(new Runnable() {
//			@Override
//			public void run() {
//				ReqGetCode reqData = new ReqGetCode();
//				reqData.tradeNo = tradeNo;
//				Global.getNetEngine().notifyTradeFail(new JsonReqBase<ReqGetCode>(reqData))
//						.enqueue(new Callback<JsonRespBase<Void>>() {
//							@Override
//							public void onResponse(Response<JsonRespBase<Void>> response, Retrofit retrofit) {
//								if (response != null && response.isSuccess()) {
//									if (response.body() != null && response.body().isSuccess()) {
//										return;
//									}
//									if (AppDebugConfig.IS_DEBUG) {
//										KLog.d(AppDebugConfig.TAG_MANAGER, response.body() == null ?
//												"解析失败" : response.body().getMsg());
//									}
//									return;
//								}
//								if (AppDebugConfig.IS_DEBUG) {
//									KLog.d(AppDebugConfig.TAG_MANAGER, "错误返回");
//								}
//							}
//
//							@Override
//							public void onFailure(Throwable t) {
//								if (AppDebugConfig.IS_DEBUG) {
//									KLog.d(AppDebugConfig.TAG_MANAGER, t);
//								}
//							}
//						});
//			}
//		});
//	}

//	private class SdkPayCallbackListener implements PayCallbackListener {
//
//		private GiftButton mButton;
//		private String mTradeNo;
//		private Context mContext;
//
//		private SdkPayCallbackListener(Context context, GiftButton button, String tradeNo) {
//			mContext = context;
//			mButton = button;
//			mTradeNo = tradeNo;
//		}
//
//		@Override
//		public void onPay(int code) {
//			if (code == UmipaySDKStatusCode.PAY_FINISH
//					|| code == UmipaySDKStatusCode.SUCCESS) {
//				// 执行查号操作
//				ReqGetCode getCode = new ReqGetCode();
//				getCode.tradeNo = mTradeNo;
//				Global.getNetEngine().getSpecificGiftCode(new JsonReqBase<ReqGetCode>(getCode))
//						.enqueue(new Callback<JsonRespBase<PayCode>>() {
//							@Override
//							public void onResponse(Response<JsonRespBase<PayCode>> response, Retrofit retrofit) {
//								hideLoading(mContext);
//								if (response != null && response.isSuccess()) {
//									if (response.body() != null && response.body().isSuccess()) {
//
//										GetCodeDialog.newInstance(response.body().getData().giftCode)
//												.show(((BaseAppCompatActivity) mContext).getSupportFragmentManager(),
//														GetCodeDialog.class.getSimpleName());
//										if (mButton != null) {
//											mButton.setState(GiftTypeUtil.TYPE_LIMIT_SEIZED);
//										}
//										ObserverManager.getInstance().notifyGiftUpdate();
//										return;
//									}
//									KLog.d((response.body() == null ? "解析失败" : response.body().getMsg()));
//								}
//								ToastUtil.showShort("查询礼包码失败, 请稍后重新查看");
//							}
//
//							@Override
//							public void onFailure(Throwable t) {
//								hideLoading(mContext);
//								if (AppDebugConfig.IS_DEBUG) {
//									KLog.d(AppDebugConfig.TAG_UTIL, t);
//								}
//								ToastUtil.showShort("查询礼包码失败 - 网络异常");
//							}
//						});
//			} else {
//				// 取消支付，通知取消订单
//				PayManager.getInstance().quickTrade(mTradeNo);
//			}
//		}
//	}

}
