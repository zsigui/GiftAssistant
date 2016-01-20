package com.oplay.giftassistant.ui.fragment.gift;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oplay.giftassistant.R;
import com.oplay.giftassistant.config.AppDebugConfig;
import com.oplay.giftassistant.config.GiftTypeUtil;
import com.oplay.giftassistant.config.Global;
import com.oplay.giftassistant.config.KeyConfig;
import com.oplay.giftassistant.config.StatusCode;
import com.oplay.giftassistant.download.ApkDownloadManager;
import com.oplay.giftassistant.download.listener.OnDownloadStatusChangeListener;
import com.oplay.giftassistant.download.listener.OnProgressUpdateListener;
import com.oplay.giftassistant.listener.OnShareListener;
import com.oplay.giftassistant.manager.ObserverManager;
import com.oplay.giftassistant.manager.PayManager;
import com.oplay.giftassistant.model.data.req.ReqGiftDetail;
import com.oplay.giftassistant.model.data.resp.GiftDetail;
import com.oplay.giftassistant.model.data.resp.IndexGameNew;
import com.oplay.giftassistant.model.data.resp.IndexGiftNew;
import com.oplay.giftassistant.model.json.base.JsonReqBase;
import com.oplay.giftassistant.model.json.base.JsonRespBase;
import com.oplay.giftassistant.ui.activity.GiftDetailActivity;
import com.oplay.giftassistant.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment;
import com.oplay.giftassistant.ui.widget.button.DownloadButtonView;
import com.oplay.giftassistant.ui.widget.button.GiftButton;
import com.oplay.giftassistant.util.DateUtil;
import com.oplay.giftassistant.util.NetworkUtil;
import com.oplay.giftassistant.util.ToastUtil;
import com.socks.library.KLog;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by zsigui on 15-12-29.
 */
public class GiftDetailFragment extends BaseFragment implements OnDownloadStatusChangeListener,
		OnProgressUpdateListener {

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
		ObserverManager.getInstance().addGiftUpdateListener(this);
		ApkDownloadManager.getInstance(getActivity()).addDownloadStatusListener(this);
		ApkDownloadManager.getInstance(getActivity()).addProgressUpdateListener(this);
		if (getActivity() instanceof GiftDetailActivity) {
			((GiftDetailActivity) getActivity()).setOnShareListener(new OnShareListener() {
				@Override
				public void share() {
					Intent sendIntent = new Intent();
					sendIntent.setAction(Intent.ACTION_SEND);
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
		if (getActivity() instanceof  GiftDetailActivity) {
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
					tvRemain.setText(Html.fromHtml(String.format("已淘号 <font color='#ffaa17'>%d</font>", giftData.searchCount)));
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
					tvRemain.setText(Html.fromHtml(String.format("开抢时间：<font color='#ffaa17'>%s</font>", giftData.seizeTime)));
					break;
				case GiftTypeUtil.TYPE_NORMAL_WAIT_SEARCH:
					tvRemain.setText(Html.fromHtml(String.format("开淘时间：<font color='#ffaa17'>%s</font>", giftData.searchTime)));
					break;
			}

		} else {
			tvConsume.setVisibility(View.GONE);
			tvCode.setVisibility(View.VISIBLE);
			btnCopy.setVisibility(View.VISIBLE);
			tvCode.setText(Html.fromHtml(String.format("礼包码: <font color='#ffaa17'>%s</font>", giftData.code)));
		}
		btnSend.setState(state);
		tvContent.setText(giftData.content);
		tvDeadline.setText(DateUtil.formatTime(giftData.useStartTime, "yyyy-MM-dd HH:mm") + " ~ "
				+ DateUtil.formatTime(giftData.useEndTime, "yyyy-MM-dd HH:mm"));
		tvUsage.setText(giftData.usage);
		tvRemark.setText(giftData.note);
		initDownload(mData.gameData);
	}

	public void initDownload(IndexGameNew game) {
		if (game == null || btnDownload == null) {
			return;
		}
		mAppInfo = game;
		((BaseAppCompatActivity)getActivity()).setBarTitle(mAppInfo.name);
		ImageLoader.getInstance().displayImage(mAppInfo.img, ivIcon, Global.IMAGE_OPTIONS);
		mAppInfo.initAppInfoStatus(getActivity());
		int progress = ApkDownloadManager.getInstance(getActivity()).getProgressByUrl(mAppInfo.downloadUrl);
		btnDownload.setStatus(mAppInfo.appStatus);
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
										refreshSuccessEnd();
										updateData(response.body().getData());
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
	public void onDownloadStatusChanged(IndexGameNew appInfo) {
		if (downloadLayout.isShown()) {
			btnDownload.setStatus(appInfo.appStatus);
		}
	}

	@Override
	public void onProgressUpdate(String url, int percent, long speedBytesPers) {
		if (downloadLayout.isShown()) {
			btnDownload.setProgress(ApkDownloadManager.getInstance(getActivity()).getProgressByUrl(url));
		}
	}
}
