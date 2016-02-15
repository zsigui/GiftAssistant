package com.oplay.giftcool.ui.fragment.gift;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.GiftTypeUtil;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.config.StatusCode;
import com.oplay.giftcool.download.ApkDownloadManager;
import com.oplay.giftcool.download.listener.OnDownloadStatusChangeListener;
import com.oplay.giftcool.download.listener.OnProgressUpdateListener;
import com.oplay.giftcool.listener.OnShareListener;
import com.oplay.giftcool.manager.ObserverManager;
import com.oplay.giftcool.manager.PayManager;
import com.oplay.giftcool.model.AppStatus;
import com.oplay.giftcool.model.DownloadStatus;
import com.oplay.giftcool.model.data.req.ReqGiftDetail;
import com.oplay.giftcool.model.data.resp.GameDownloadInfo;
import com.oplay.giftcool.model.data.resp.GiftDetail;
import com.oplay.giftcool.model.data.resp.IndexGameNew;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.sharesdk.ShareSDKManager;
import com.oplay.giftcool.ui.activity.GiftDetailActivity;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Dialog;
import com.oplay.giftcool.ui.fragment.dialog.ConfirmDialog;
import com.oplay.giftcool.ui.widget.DeletedTextView;
import com.oplay.giftcool.ui.widget.button.DownloadButtonView;
import com.oplay.giftcool.ui.widget.button.GiftButton;
import com.oplay.giftcool.util.DateUtil;
import com.oplay.giftcool.util.NetworkUtil;
import com.oplay.giftcool.util.ToastUtil;
import com.oplay.giftcool.util.ViewUtil;
import com.socks.library.KLog;

import java.util.HashSet;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by zsigui on 15-12-29.
 */
public class GiftDetailFragment extends BaseFragment implements OnDownloadStatusChangeListener,
		OnProgressUpdateListener {

	private final static String PAGE_NAME = "礼包详情";
	private ImageView ivIcon;
	private TextView tvName;
	private TextView tvConsume;
	private TextView tvScore;
	private TextView tvOr;
	private TextView tvBean;
	private TextView tvRemain;
	private TextView tvContent;
	private TextView tvDeadline;
	private TextView tvUsage;
	private GiftButton btnSend;
	private ProgressBar pbPercent;
	private TextView tvCode;
	private TextView btnCopy;
	private ImageView ivLimit;
	private DownloadButtonView btnDownload;
	private LinearLayout downloadLayout;
	private LinearLayout llZero;
	private DeletedTextView tvOriginPrice;
	private TextView tvZeroRemain;

	private GiftDetail mData;
	private IndexGameNew mAppInfo;
	private CountDownTimer mTimer;
	private int mId;


	public static GiftDetailFragment newInstance(int id) {
		GiftDetailFragment fragment = new GiftDetailFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(KeyConfig.KEY_DATA, id);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		setContentView(R.layout.fragment_gift_detail);
		ivIcon = getViewById(R.id.iv_icon);
		tvName = getViewById(R.id.tv_name);
		tvConsume = getViewById(R.id.tv_consume);
		tvScore = getViewById(R.id.tv_score);
		tvOr = getViewById(R.id.tv_or);
		tvBean = getViewById(R.id.tv_bean);
		tvRemain = getViewById(R.id.tv_remain);
		tvContent = getViewById(R.id.et_content);
		tvDeadline = getViewById(R.id.tv_deadline);
		tvUsage = getViewById(R.id.tv_usage);
		btnSend = getViewById(R.id.btn_send);
		pbPercent = getViewById(R.id.pb_percent);
		tvCode = getViewById(R.id.tv_gift_code);
		btnCopy = getViewById(R.id.btn_copy);
		ivLimit = getViewById(R.id.iv_limit);
		btnDownload = getViewById(R.id.btn_download);
		downloadLayout = getViewById(R.id.ll_download);
		llZero = getViewById(R.id.ll_zero);
		tvOriginPrice = getViewById(R.id.tv_src);
		tvZeroRemain = getViewById(R.id.tv_zero_remain);
	}

	@Override
	protected void setListener() {
		btnCopy.setOnClickListener(this);
		btnSend.setOnClickListener(this);
		btnDownload.setOnClickListener(this);
		ObserverManager.getInstance().addGiftUpdateListener(this);
		ApkDownloadManager.getInstance(getActivity()).addDownloadStatusListener(this);
		ApkDownloadManager.getInstance(getActivity()).addProgressUpdateListener(this);
		if (getActivity() instanceof GiftDetailActivity) {
			((GiftDetailActivity) getActivity()).setOnShareListener(new OnShareListener() {
				@Override
				public void share() {
					if (mData == null || mData.giftData == null || mData.gameData == null) {
						if (AppDebugConfig.IS_DEBUG) {
							KLog.d("mData = " + mData);
						}
						ToastUtil.showShort("页面出错，请重新进入");
						return;
					}
					mData.giftData.gameName = mData.gameData.name;
					mData.giftData.img = mData.gameData.img;
					ShareSDKManager.getInstance(mApp).shareGift(mApp, getChildFragmentManager(), mData.giftData);

				}
			});
		}
	}

	@Override
	protected void processLogic(Bundle savedInstanceState) {
		if (getArguments() == null) {
			throw new IllegalStateException("need to set data here");
		}
		mId = getArguments().getInt(KeyConfig.KEY_DATA);
		if (AppDebugConfig.IS_FRAG_DEBUG) {
			KLog.d(AppDebugConfig.TAG_FRAG, "transfer id = " + mId);
		}
		if (!AssistantApp.getInstance().isAllowDownload()) {
			downloadLayout.setVisibility(View.GONE);
		} else {
			downloadLayout.setVisibility(View.VISIBLE);
		}
//		mViewManager.showLoading();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		ApkDownloadManager.getInstance(getActivity()).removeDownloadStatusListener(this);
		ApkDownloadManager.getInstance(getActivity()).removeProgressUpdateListener(this);
	}

	private void updateData(GiftDetail data) {
		if (data == null || data.giftData == null) {
//			mViewManager.showEmpty();
			ToastUtil.showShort("传递参数获取数据为空");
			getActivity().finish();
			return;
		}
		mHasData = true;
//		mViewManager.showContent();
		mData = data;
		final IndexGiftNew giftData = data.giftData;
		tvName.setText(String.format("[%s]%s", (mData.gameData == null ? "" : mData.gameData.name), giftData.name));
		if (getActivity() instanceof GiftDetailActivity) {
			if (giftData.giftType == GiftTypeUtil.GIFT_TYPE_LIMIT) {
				((GiftDetailActivity) getActivity()).showLimitTag(true, R.drawable.ic_tool_limit);
			} else if (giftData.giftType == GiftTypeUtil.GIFT_TYPE_ZERO_SEIZE) {
				((GiftDetailActivity) getActivity()).showLimitTag(true, R.drawable.ic_tool_0_seize);
			} else {
				((GiftDetailActivity) getActivity()).showLimitTag(false, 0);
			}
			int type = GiftTypeUtil.getItemViewType(mData.giftData);
			if (type == GiftTypeUtil.TYPE_LIMIT_FINISHED
					|| type == GiftTypeUtil.TYPE_LIMIT_EMPTY
					|| type == GiftTypeUtil.TYPE_NORMAL_FINISHED) {
				((GiftDetailActivity) getActivity()).showShareButton(false);
			} else {
				((GiftDetailActivity) getActivity()).showShareButton(true);
			}
		}
		setLimitTag(giftData);
		int state = GiftTypeUtil.getItemViewType(giftData);
		btnSend.setState(state);
		tvOr.setVisibility(View.GONE);
		tvRemain.setVisibility(View.GONE);
		pbPercent.setVisibility(View.GONE);
		tvBean.setVisibility(View.GONE);
		tvCode.setVisibility(View.GONE);
		btnCopy.setVisibility(View.GONE);
		tvScore.setVisibility(View.GONE);
		btnSend.setVisibility(View.VISIBLE);
		llZero.setVisibility(View.GONE);
		if (giftData.seizeStatus == GiftTypeUtil.SEIZE_TYPE_NEVER) {
			tvConsume.setVisibility(View.VISIBLE);
			if (giftData.priceType == GiftTypeUtil.PAY_TYPE_SCORE
					&& giftData.giftType != GiftTypeUtil.GIFT_TYPE_ZERO_SEIZE) {
				tvScore.setVisibility(View.VISIBLE);
				tvScore.setText(String.valueOf(giftData.score));
			} else if (giftData.priceType == GiftTypeUtil.PAY_TYPE_BEAN) {
				tvBean.setVisibility(View.VISIBLE);
				tvBean.setText(String.valueOf(giftData.bean));
			} else {
				tvScore.setVisibility(View.VISIBLE);
				tvBean.setVisibility(View.VISIBLE);
				tvOr.setVisibility(View.VISIBLE);
				tvScore.setText(String.valueOf(giftData.score));
				tvBean.setText(String.valueOf(giftData.bean));
			}
			if (giftData.giftType == GiftTypeUtil.GIFT_TYPE_ZERO_SEIZE) {
				tvScore.setTextColor(mApp.getResources().getColor(R.color.co_common_app_main_bg));
				tvBean.setTextColor(mApp.getResources().getColor(R.color.co_common_app_main_bg));
				llZero.setVisibility(View.VISIBLE);
				tvOriginPrice.setText(
						Html.fromHtml(String.format("<line-through>原价 <font color='#f85454'>¥%d</font>",
								giftData.originPrice)));
				tvOriginPrice.setPaint(mApp.getResources().getColor(R.color.co_common_app_main_bg), 3);
				tvZeroRemain.setText(
						Html.fromHtml(String.format("剩余 <font color='#ffaa17'>%d</font>个", giftData.remainCount)));

			} else {
				tvRemain.setVisibility(View.VISIBLE);
				switch (state) {
					case GiftTypeUtil.TYPE_LIMIT_SEIZE:
						tvRemain.setText(Html.fromHtml(String.format("剩余 <font color='#ffaa17'>%d个</font>",
								giftData.remainCount)));
						break;
					case GiftTypeUtil.TYPE_NORMAL_SEARCH:
						tvRemain.setText(Html.fromHtml(String.format("已淘号 <font color='#ffaa17'>%d</font>",
								giftData.searchCount)));
						break;
					case GiftTypeUtil.TYPE_NORMAL_SEIZE:
						int progress = giftData.remainCount * 100 / giftData.totalCount;
						tvRemain.setText(Html.fromHtml(String.format("剩余%d%%", progress)));
						pbPercent.setVisibility(View.VISIBLE);
						pbPercent.setProgress(progress);
						break;
					case GiftTypeUtil.TYPE_LIMIT_WAIT_SEIZE:
					case GiftTypeUtil.TYPE_NORMAL_WAIT_SEIZE:
						tvRemain.setVisibility(View.VISIBLE);
						tvRemain.setText(Html.fromHtml(String.format("开抢时间：<font color='#ffaa17'>%s</font>",
								giftData.seizeTime)));
						break;
					case GiftTypeUtil.TYPE_NORMAL_WAIT_SEARCH:
						tvRemain.setText(Html.fromHtml(String.format("开淘时间：<font color='#ffaa17'>%s</font>",
								giftData.searchTime)));
						break;
				}
			}

		} else {
			tvConsume.setVisibility(View.GONE);
			tvCode.setVisibility(View.VISIBLE);
			btnCopy.setVisibility(View.VISIBLE);
			tvCode.setText(Html.fromHtml(String.format("礼包码: <font color='#ffaa17'>%s</font>", giftData.code)));
		}
		setDeadCount();

		if (!mIsNotifyRefresh) {
			tvContent.setText(giftData.content);
			tvDeadline.setText(DateUtil.formatTime(giftData.useStartTime, "yyyy-MM-dd HH:mm") + " ~ "
					+ DateUtil.formatTime(giftData.useEndTime, "yyyy-MM-dd HH:mm"));
			tvUsage.setText(giftData.usage);
			initDownload(mData.gameData);
		}


	}

	private void setDeadCount() {
		if (mData.giftData.status != GiftTypeUtil.STATUS_WAIT_SEIZE
				|| mData.giftData.seizeStatus == GiftTypeUtil.SEIZE_TYPE_SEIZED) {
			return;
		}
		if (mTimer != null) {
			mTimer.cancel();
		}
		long seizeTime = DateUtil.getTime(mData.giftData.seizeTime);
		btnSend.setState(GiftTypeUtil.STATUS_WAIT_SEIZE);
		mTimer = new CountDownTimer(seizeTime - System.currentTimeMillis(), 1000) {
			@Override
			public void onTick(long millisUntilFinished) {
				btnSend.setText(DateUtil.formatRemain(millisUntilFinished, "HH:mm:ss"));
			}

			@Override
			public void onFinish() {
				mData.giftData.status = GiftTypeUtil.STATUS_SEIZE;
				btnSend.setState(GiftTypeUtil.getItemViewType(mData.giftData));
			}
		};
		mTimer.start();
	}


	private void setLimitTag(IndexGiftNew giftData) {
		if (giftData.giftType == GiftTypeUtil.GIFT_TYPE_LIMIT) {
			ivLimit.setVisibility(View.VISIBLE);
			ivLimit.setImageResource(R.drawable.ic_tag_limit);
		} else if (giftData.giftType == GiftTypeUtil.GIFT_TYPE_ZERO_SEIZE) {
			ivLimit.setVisibility(View.VISIBLE);
			ivLimit.setImageResource(R.drawable.ic_tag_0_seize);
		} else {
			ivLimit.setVisibility(View.GONE);
		}
	}

	public void initDownload(IndexGameNew game) {
		if (game == null || btnDownload == null) {
			return;
		}
		mAppInfo = game;
		((BaseAppCompatActivity) getActivity()).setBarTitle(mAppInfo.name);
		ViewUtil.showImage(ivIcon, mAppInfo.img);
		if (AssistantApp.getInstance().isAllowDownload()) {
			mAppInfo.initAppInfoStatus(getActivity());
			int progress = ApkDownloadManager.getInstance(getActivity()).getProgressByUrl(mAppInfo
					.downloadUrl);
			btnDownload.setStatus(mAppInfo.appStatus, "");
			btnDownload.setProgress(progress);
		}
	}

	@Override
	protected void lazyLoad() {
		refreshInitConfig();
		Global.THREAD_POOL.execute(new Runnable() {
			@Override
			public void run() {
				if (!NetworkUtil.isConnected(getContext())) {
					refreshFailEnd();
					return;
				}
				ReqGiftDetail data = new ReqGiftDetail();
				data.id = mId;
				Global.getNetEngine().obtainGiftDetail(new JsonReqBase<ReqGiftDetail>(data))
						.enqueue(new Callback<JsonRespBase<GiftDetail>>() {
							@Override
							public void onResponse(Response<JsonRespBase<GiftDetail>> response, Retrofit retrofit) {
								if (!mCanShowUI) {
									return;
								}
								if (response != null && response.code() == 200) {
									if (response.body() != null && response.body().getCode() == StatusCode.SUCCESS) {
										refreshSuccessEnd();
										updateData(response.body().getData());
										return;
									}
									if (AppDebugConfig.IS_DEBUG) {
										KLog.d(AppDebugConfig.TAG_FRAG, "body = " + response.body());
									}
								}
								// 加载错误页面也行
								refreshFailEnd();
							}

							@Override
							public void onFailure(Throwable t) {
								if (!mCanShowUI) {
									return;
								}
								if (AppDebugConfig.IS_DEBUG) {
									KLog.e(t);
								}
								refreshFailEnd();
							}
						});
			}
		});

	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
			case R.id.btn_download:
				if (mAppInfo != null) {
					mAppInfo.handleOnClick(getFragmentManager());
				}
				break;
			case R.id.btn_copy:
				ClipboardManager cmb = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
				cmb.setPrimaryClip(ClipData.newPlainText("礼包码", mData.giftData.code));
				ToastUtil.showShort("已复制");
				break;
			case R.id.btn_send:
				if (mData == null) {
					return;
				}
				if (mData.giftData.giftType == GiftTypeUtil.GIFT_TYPE_ZERO_SEIZE
						&& !isInstalledGame()) {
					return;
				}
				PayManager.getInstance().seizeGift(getActivity(), mData.giftData, btnSend);
				break;
		}
	}

	private boolean isInstalledGame() {
		HashSet<String> appNames = Global.getInstalledAppNames();
		for (String name : appNames) {
			if (mData.gameData.name.startsWith(name)) {
				// 前缀匹配成功，表明有安装该游戏，返回成功
				return true;
			}
		}
		if (mAppInfo == null) {
			ToastUtil.showShort("页面信息错误，请重新进入");
			return false;
		}
		final ConfirmDialog dialog = ConfirmDialog.newInstance();
		dialog.setTitle("小贴士");
		dialog.setContent(Html.fromHtml(String.format("下载「<font color='#ffaa17'>%s</font>」安装，马上参与0元抢购！",
				mData.gameData.name)));
		if (mAppInfo.downloadStatus != null && mAppInfo.getAppStatus(mAppInfo.downloadStatus) == AppStatus
				.INSTALLABLE) {
			dialog.setPositiveBtnText(mApp.getResources().getString(R.string.st_dialog_btn_install));
			dialog.setPositiveBtnBackground(R.drawable.selector_btn_download_blue);
		} else {
			dialog.setPositiveBtnText(mApp.getResources().getString(R.string.st_dialog_btn_download));
		}
		dialog.setListener(new BaseFragment_Dialog.OnDialogClickListener() {
			@Override
			public void onCancel() {
				dialog.dismissAllowingStateLoss();
			}

			@Override
			public void onConfirm() {
				if (mAppInfo != null) {
					if (mAppInfo.downloadStatus != null) {
						if (mAppInfo.downloadStatus == DownloadStatus.DOWNLOADING) {
							ToastUtil.showShort("已经在下载中，请等待下载完成");
							dialog.dismissAllowingStateLoss();
							return;
						}
						mAppInfo.appStatus = mAppInfo.getAppStatus(mAppInfo.downloadStatus);
					}
					mAppInfo.handleOnClick(getFragmentManager());
				}
				dialog.dismissAllowingStateLoss();
			}
		});
		dialog.show(getChildFragmentManager(), ConfirmDialog.class.getSimpleName());
		return false;
	}

	@Override
	public void onDownloadStatusChanged(final GameDownloadInfo appInfo) {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {

				if (downloadLayout != null && downloadLayout.getVisibility() == View.VISIBLE) {
					mAppInfo.downloadStatus = appInfo.downloadStatus;
					mAppInfo.initAppInfoStatus(getActivity());
					btnDownload.setStatus(mAppInfo.appStatus, "");
				}
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

	}

	@Override
	public void onProgressUpdate(String url, final int percent, long speedBytesPers) {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (downloadLayout != null && downloadLayout.getVisibility() == View.VISIBLE) {
					btnDownload.setProgress(percent);
				}
			}
		});
	}

	@Override
	public void release() {
		super.release();
		mTimer.cancel();
		mTimer = null;
		ivIcon = null;
		tvName = null;
		tvConsume = null;
		tvScore = null;
		tvOr = null;
		tvBean = null;
		tvRemain = null;
		tvContent = null;
		tvDeadline = null;
		tvUsage = null;
		btnSend = null;
		pbPercent = null;
		tvCode = null;
		btnCopy = null;
		ivLimit = null;
		btnDownload = null;
		downloadLayout = null;
		llZero = null;
		tvOriginPrice = null;
		tvZeroRemain = null;
		mData = null;
		mAppInfo = null;
	}

	@Override
	public String getPageName() {
		return PAGE_NAME;
	}
}
