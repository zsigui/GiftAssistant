package com.oplay.giftcool.ui.fragment.gift;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.GiftTypeUtil;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.config.StatusCode;
import com.oplay.giftcool.config.WebViewUrl;
import com.oplay.giftcool.download.ApkDownloadManager;
import com.oplay.giftcool.download.listener.OnDownloadStatusChangeListener;
import com.oplay.giftcool.download.listener.OnProgressUpdateListener;
import com.oplay.giftcool.listener.OnShareListener;
import com.oplay.giftcool.manager.ObserverManager;
import com.oplay.giftcool.manager.PayManager;
import com.oplay.giftcool.manager.ScoreManager;
import com.oplay.giftcool.model.data.req.ReqGiftDetail;
import com.oplay.giftcool.model.data.resp.GiftDetail;
import com.oplay.giftcool.model.data.resp.IndexGameNew;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.sharesdk.ShareSDKConfig;
import com.oplay.giftcool.sharesdk.ShareSDKManager;
import com.oplay.giftcool.ui.activity.GiftDetailActivity;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.oplay.giftcool.ui.widget.button.DownloadButtonView;
import com.oplay.giftcool.ui.widget.button.GiftButton;
import com.oplay.giftcool.util.BitmapUtil;
import com.oplay.giftcool.util.DateUtil;
import com.oplay.giftcool.util.NetworkUtil;
import com.oplay.giftcool.util.ToastUtil;
import com.oplay.giftcool.util.ViewUtil;
import com.socks.library.KLog;

import java.io.File;

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
	private TextView tvRemark;
	private GiftButton btnSend;
	private ProgressBar pbPercent;
	private TextView tvCode;
	private TextView btnCopy;
	private ImageView ivLimit;
	private DownloadButtonView btnDownload;
	private LinearLayout downloadLayout;

	private GiftDetail mData;
	private IndexGameNew mAppInfo;
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
		initViewManger(R.layout.fragment_gift_detail);
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
		tvRemark = getViewById(R.id.tv_remark);
		btnSend = getViewById(R.id.btn_send);
		pbPercent = getViewById(R.id.pb_percent);
		tvCode = getViewById(R.id.tv_gift_code);
		btnCopy = getViewById(R.id.btn_copy);
		ivLimit = getViewById(R.id.iv_limit);
		btnDownload = getViewById(R.id.btn_download);
		downloadLayout = getViewById(R.id.ll_download);
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
					// 设置分享成功后奖励类型
					String title;
					if (mData.giftData.isLimit) {
						ScoreManager.getInstance().setRewardType(ScoreManager.RewardType.SHARE_LIMIT);
						title = String.format(getResources().getString(R.string.st_share_limit_pattern),
								mData.giftData.name);
					} else {
						ScoreManager.getInstance().setRewardType(ScoreManager.RewardType.SHARE_NORMAL);
						title = mData.giftData.name;
					}

					String src = null;
					try {
						File file = ImageLoader.getInstance().getDiskCache().get(mData.gameData.img);
						src = (file != null ? file.getAbsolutePath() : null);
					} catch (Exception e) {
						// ImageLoader未初始化完成
					}
					ShareSDKManager.getInstance(mApp).share(getChildFragmentManager(),
							title,
							mData.giftData.content,
							WebViewUrl.GIFT_DETAIL + "?" + mData.giftData.id,
							mData.giftData.img, (src == null ? null : BitmapUtil.getSmallBitmap(src,
									ShareSDKConfig.THUMB_SIZE, ShareSDKConfig.THUMB_SIZE)));
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
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		ApkDownloadManager.getInstance(getActivity()).removeDownloadStatusListener(this);
		ApkDownloadManager.getInstance(getActivity()).removeProgressUpdateListener(this);
	}

	private void updateData(GiftDetail data) {
		if (data == null)
			return;
		mHasData = true;
		mViewManager.showContent();
		mData = data;
		IndexGiftNew giftData = data.giftData;
		if (giftData == null) {
			return;
		}
		tvName.setText(giftData.name);
		if (getActivity() instanceof GiftDetailActivity) {
			if (giftData.isLimit) {
				((GiftDetailActivity) getActivity()).showLimitTag(true);
			} else {
				((GiftDetailActivity) getActivity()).showLimitTag(false);
			}
		}
		if (giftData.isLimit) {
			ivLimit.setVisibility(View.VISIBLE);
		} else {
			ivLimit.setVisibility(View.GONE);
		}
		int state = GiftTypeUtil.getItemViewType(giftData);
		tvOr.setVisibility(View.GONE);
		tvRemain.setVisibility(View.GONE);
		pbPercent.setVisibility(View.GONE);
		tvBean.setVisibility(View.GONE);
		tvCode.setVisibility(View.GONE);
		btnCopy.setVisibility(View.GONE);
		tvScore.setVisibility(View.GONE);
		btnSend.setVisibility(View.VISIBLE);

		if (giftData.seizeStatus == GiftTypeUtil.SEIZE_TYPE_NEVER) {
			tvConsume.setVisibility(View.VISIBLE);
			if (giftData.priceType == GiftTypeUtil.PAY_TYPE_SCORE) {
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

		} else {
			tvConsume.setVisibility(View.GONE);
			tvCode.setVisibility(View.VISIBLE);
			btnCopy.setVisibility(View.VISIBLE);
			tvCode.setText(Html.fromHtml(String.format("礼包码: <font color='#ffaa17'>%s</font>", giftData.code)));
		}
		btnSend.setState(state);
		if (!mIsNotifyRefresh) {
			tvContent.setText(giftData.content);
			tvDeadline.setText(DateUtil.formatTime(giftData.useStartTime, "yyyy-MM-dd HH:mm") + " ~ "
					+ DateUtil.formatTime(giftData.useEndTime, "yyyy-MM-dd HH:mm"));
			tvUsage.setText(giftData.usage);
			tvRemark.setText(giftData.note);
			initDownload(mData.gameData);
		}
	}

	public void initDownload(IndexGameNew game) {
		if (game == null || btnDownload == null) {
			return;
		}
		mAppInfo = game;
		((BaseAppCompatActivity) getActivity()).setBarTitle(mAppInfo.name);
		ViewUtil.showImage(ivIcon, mAppInfo.img);
		mAppInfo.initAppInfoStatus(getActivity());
		int progress = ApkDownloadManager.getInstance(getActivity()).getProgressByUrl(mAppInfo
				.downloadUrl);
		btnDownload.setStatus(mAppInfo.appStatus, mAppInfo.size);
		btnDownload.setProgress(progress);
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
										updateData(response.body().getData());
										refreshSuccessEnd();
										return;
									}
									if (AppDebugConfig.IS_DEBUG) {
										KLog.d(AppDebugConfig.TAG_FRAG, "body = " + response.body());
									}
								}
								// 加载错误页面也行
								refreshSuccessEnd();
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
				if (mData != null && btnSend != null) {
					PayManager.getInstance().seizeGift(getActivity(), mData.giftData, btnSend);
				}
				break;
		}
	}

	@Override
	public void onDownloadStatusChanged(final IndexGameNew appInfo) {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {

				if (downloadLayout != null && downloadLayout.getVisibility() == View.VISIBLE) {
					mAppInfo.downloadStatus = appInfo.downloadStatus;
					mAppInfo.initAppInfoStatus(getActivity());
					btnDownload.setStatus(mAppInfo.appStatus, mAppInfo.size);
				}
			}
		});
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
	public String getPageName() {
		return PAGE_NAME;
	}
}
