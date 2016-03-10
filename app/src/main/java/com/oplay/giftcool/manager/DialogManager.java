package com.oplay.giftcool.manager;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.view.View;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.NetStatusCode;
import com.oplay.giftcool.model.data.req.ReqHopeGift;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Dialog;
import com.oplay.giftcool.ui.fragment.dialog.ConfirmDialog;
import com.oplay.giftcool.ui.fragment.dialog.HopeGiftDialog;
import com.oplay.giftcool.ui.fragment.dialog.LoadingDialog;
import com.oplay.giftcool.util.NetworkUtil;
import com.oplay.giftcool.util.ThreadUtil;
import com.oplay.giftcool.util.ToastUtil;
import com.socks.library.KLog;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * 弹窗管理类
 * <p/>
 * Created by zsigui on 16-3-7.
 */
public class DialogManager {


	private static DialogManager sInstance;
	private LoadingDialog mLoadingDialog = null;

	public static DialogManager getInstance() {
		if (sInstance == null) {
			sInstance = new DialogManager();
		}
		return sInstance;
	}

	private Context mContext;

	private DialogManager() {
		mContext = AssistantApp.getInstance();
	}

	/**
	 * 显示求礼包界面
	 */
	public void showHopeGift(final FragmentManager fm, final int id, final String name, boolean canEdit) {
		final HopeGiftDialog dialog = HopeGiftDialog.newInstance(id, name, canEdit);
		BaseFragment_Dialog.OnDialogClickListener dialogClickListener = new BaseFragment_Dialog.OnDialogClickListener
				() {


			@Override
			public void onCancel() {
				if (dialog != null) {
					dialog.dismissAllowingStateLoss();
				}
				StatisticsManager.getInstance().trace(mContext, StatisticsManager.ID.USER_HOPE_GIFT_QUICK);
			}

			@Override
			public void onConfirm() {
				handleHopeGiftRequest(fm, dialog, dialog.getGameId(), dialog.getName(), dialog.getNote());
				StatisticsManager.getInstance().trace(mContext, StatisticsManager.ID.USER_HOPE_GIFT_SUCCESS);
			}
		};
		dialog.setListener(dialogClickListener);
		dialog.show(fm, "hope_gift");
	}

	/**
	 * 执行求礼包的请求
	 */
	private void handleHopeGiftRequest(final FragmentManager fm, final HopeGiftDialog dialog,
	                                   int id, String name, String note) {
		showLoadingDialog(fm);
		if (!NetworkUtil.isConnected(mContext)) {
			ToastUtil.showShort(ConstString.TEXT_NET_ERROR);
			return;
		}
		ReqHopeGift reqHopeGift = new ReqHopeGift();
		reqHopeGift.gameId = id;
		reqHopeGift.gameName = name;
		reqHopeGift.note = note;
		Global.getNetEngine().commitHopeGift(new JsonReqBase<ReqHopeGift>(reqHopeGift))
				.enqueue(new Callback<JsonRespBase<Void>>() {
					@Override
					public void onResponse(Response<JsonRespBase<Void>> response, Retrofit retrofit) {
						hideLoadingDialog();
						if (response != null && response.isSuccess()) {
							JsonRespBase<Void> resp = response.body();
							if (resp != null) {

								if (resp.isSuccess()
										|| resp.getCode() == NetStatusCode.ERR_GAME_HOPE_GIFT_LIMIT
										|| resp.getCode() == NetStatusCode.ERR_TOTAL_HOPE_GIFT_LIMIT) {
									// 构建确定弹窗
									final ConfirmDialog confirmDialog = ConfirmDialog.newInstance();
									confirmDialog.setNegativeVisibility(View.GONE);
									confirmDialog.setPositiveBtnText(mContext.getString(R.string
											.st_dialog_btn_success_confirm));
									dialog.dismissAllowingStateLoss();
									String title;
									String content;
									switch (resp.getCode()) {
										case NetStatusCode.ERR_GAME_HOPE_GIFT_LIMIT:
											title = mContext.getString(R.string.st_dialog_hope_gift_fail_title);
											content = mContext.getString(R.string
													.st_dialog_hope_gift_fail_game_limit_content);
											break;
										case NetStatusCode.ERR_TOTAL_HOPE_GIFT_LIMIT:
											title = mContext.getString(R.string.st_dialog_hope_gift_fail_title);
											content = mContext.getString(R.string
													.st_dialog_hope_gift_fail_total_limit_content);
											break;
										default:
											title = mContext.getString(R.string.st_dialog_hope_gift_success_title);
											content = mContext.getString(R.string.st_dialog_hope_gift_success_title);
									}
									confirmDialog.setTitle(title);
									confirmDialog.setContent(content);
									confirmDialog.show(fm, "confirm");
								} else if (resp.getCode() == NetStatusCode.ERR_UN_LOGIN
										|| resp.getCode() == NetStatusCode.ERR_BAD_SERVER) {
									// 登录状态失效
									ToastUtil.showShort(ConstString.TEXT_LOGIN_FIRST);
									AccountManager.getInstance().notifyUserAll(null);
								} else {
									if (AppDebugConfig.IS_DEBUG) {
										KLog.d(AppDebugConfig.TAG_MANAGER, (response.body() == null ? "解析失败" :
												response.body().error()));
									}
								}

								return;
							}
							return;
						}
						if (AppDebugConfig.IS_DEBUG) {
							KLog.d(AppDebugConfig.TAG_MANAGER, (response == null ? "返回失败" : response.message()));
						}
						ToastUtil.showShort(ConstString.TEXT_EXECUTE_ERROR);
					}

					@Override
					public void onFailure(Throwable t) {
						hideLoadingDialog();
						if (AppDebugConfig.IS_DEBUG) {
							KLog.d(AppDebugConfig.TAG_MANAGER, t);
						}
						ToastUtil.showShort(ConstString.TEXT_EXECUTE_ERROR);
					}
				});
	}


	/**
	 * 显示默认的加载中弹窗
	 */
	public void showLoadingDialog(FragmentManager fm) {
		showLoadingDialog(fm, mContext.getResources().getString(R.string.st_view_loading_more));
	}

	/**
	 * 显示加载弹窗，指定显示内容
	 */
	public void showLoadingDialog(final FragmentManager fm, final String loadText) {
		ThreadUtil.runInUIThread(new Runnable() {
			@Override
			public void run() {
				if (mLoadingDialog == null) {
					mLoadingDialog = LoadingDialog.newInstance();
				}
				mLoadingDialog.setCancelable(false);
				mLoadingDialog.setLoadText(loadText);
				mLoadingDialog.show(fm, LoadingDialog.class.getSimpleName());
			}
		});
	}

	/**
	 * 隐藏加载弹窗
	 */
	public void hideLoadingDialog() {
		if (mLoadingDialog != null) {
			mLoadingDialog.dismissAllowingStateLoss();
		}
	}

}
