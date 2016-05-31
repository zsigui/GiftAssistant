package com.oplay.giftcool.ui.fragment.gift;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.GiftDetailPicsAdapter;
import com.oplay.giftcool.adapter.itemdecoration.DividerItemDecoration;
import com.oplay.giftcool.assist.CountTimer;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.config.NetStatusCode;
import com.oplay.giftcool.config.util.GameTypeUtil;
import com.oplay.giftcool.config.util.GiftTypeUtil;
import com.oplay.giftcool.download.ApkDownloadManager;
import com.oplay.giftcool.download.listener.OnDownloadStatusChangeListener;
import com.oplay.giftcool.download.listener.OnProgressUpdateListener;
import com.oplay.giftcool.listener.OnShareListener;
import com.oplay.giftcool.manager.ObserverManager;
import com.oplay.giftcool.manager.PayManager;
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
import com.oplay.giftcool.ui.widget.DeletedTextView;
import com.oplay.giftcool.ui.widget.button.DownloadButtonView;
import com.oplay.giftcool.ui.widget.button.GiftButton;
import com.oplay.giftcool.util.DateUtil;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.MixUtil;
import com.oplay.giftcool.util.NetworkUtil;
import com.oplay.giftcool.util.ThreadUtil;
import com.oplay.giftcool.util.ToastUtil;
import com.oplay.giftcool.util.ViewUtil;
import com.socks.library.KLog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
    private TextView tvSeizeHint;
    private TextView tvContent;
    private TextView tvDeadline;
    private TextView tvUsage;
    private TextView tvUsageTitle;
    private RecyclerView rvUsage;
    private GiftDetailPicsAdapter mAdapter;
    private GiftButton btnSend;
    private ProgressBar pbPercent;
    private TextView tvCode;
    private TextView btnCopy;
    private TextView tvQQ;
    private DownloadButtonView btnDownload;
    private LinearLayout downloadLayout;
    private DeletedTextView tvOriginPrice;

    private GiftDetail mData;
    private IndexGameNew mAppInfo;
    private CountTimer mTimer;
    private int mId;
    private String QQ_TEXT;


    public static GiftDetailFragment newInstance(int id) {
        GiftDetailFragment fragment = new GiftDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(KeyConfig.KEY_DATA, id);
        fragment.setArguments(bundle);
        fragment.mDataRunnable = new DataRunnable(fragment);
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
        tvRemain = getViewById(R.id.tv_new_add);
        tvSeizeHint = getViewById(R.id.tv_seize_hint);
        tvOriginPrice = getViewById(R.id.tv_price);
        pbPercent = getViewById(R.id.pb_percent);
        btnCopy = getViewById(R.id.btn_copy);
        tvCode = getViewById(R.id.tv_gift_code);
        btnSend = getViewById(R.id.btn_send);
        btnDownload = getViewById(R.id.btn_download);
        downloadLayout = getViewById(R.id.ll_download);

    }

    @Override
    protected void setListener() {
        btnCopy.setOnClickListener(this);
        btnSend.setOnClickListener(this);
        btnDownload.setOnClickListener(this);
        ivIcon.setOnClickListener(this);
        ObserverManager.getInstance().addGiftUpdateListener(this);
        ApkDownloadManager.getInstance(getActivity()).addDownloadStatusListener(this);
        ApkDownloadManager.getInstance(getActivity()).addProgressUpdateListener(this);
        if (getActivity() instanceof GiftDetailActivity) {
            ((GiftDetailActivity) getActivity()).setOnShareListener(new OnShareListener() {
                @Override
                public void share() {
                    if (mData == null || mData.giftData == null || mData.gameData == null) {
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
            ToastUtil.showShort(ConstString.TEXT_ENTER_ERROR);
            return;
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
        mViewManager.showLoading();
        QQ_TEXT = getResources().getString(R.string.st_gift_qq);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ObserverManager.getInstance().removeGiftUpdateListener(this);
        ApkDownloadManager.getInstance(getActivity()).removeDownloadStatusListener(this);
        ApkDownloadManager.getInstance(getActivity()).removeProgressUpdateListener(this);
    }

    public void updateData(int id) {
        if (id <= 0) {
            return;
        }
        mId = id;
        lazyLoad();
    }

    private void updateData(GiftDetail data) {
        if (data == null || data.giftData == null) {
            ToastUtil.showShort("该请求数据可能已经过期");
            mViewManager.showEmpty();
            return;
        }
        try {
            mHasData = true;
            mViewManager.showContent();
            mData = data;
            final IndexGiftNew giftData = data.giftData;
            setTag(giftData);

            int type = GiftTypeUtil.getItemViewType(giftData);
            inflateVsView(giftData);

            btnSend.setState(type);
            tvOr.setVisibility(View.GONE);
            pbPercent.setVisibility(View.GONE);
            tvBean.setVisibility(View.GONE);
            tvCode.setVisibility(View.GONE);
            btnCopy.setVisibility(View.GONE);
            tvScore.setVisibility(View.GONE);
            btnSend.setVisibility(View.VISIBLE);
            tvSeizeHint.setVisibility(View.GONE);

            tvQQ.setText(String.format(QQ_TEXT, MixUtil.getQQInfo()[0]));
            if (giftData.seizeStatus == GiftTypeUtil.SEIZE_TYPE_NEVER
                    || (giftData.giftType == GiftTypeUtil.GIFT_TYPE_LIMIT_FREE
                    && giftData.totalType == GiftTypeUtil.TOTAL_TYPE_FIRST_CHARGE
                    && (giftData.status == GiftTypeUtil.STATUS_DISABLED_RESERVE
                    || giftData.status == GiftTypeUtil.STATUS_RESERVE
                    || giftData.status == GiftTypeUtil.STATUS_RESERVE_FINISHED))) {
                tvConsume.setVisibility(View.VISIBLE);
                tvRemain.setVisibility(View.VISIBLE);
                setMoneyConsume(giftData);
                if (giftData.giftType == GiftTypeUtil.GIFT_TYPE_LIMIT_FREE) {
                    ViewUtil.siteValueUI(tvOriginPrice, giftData.originPrice, true);
                    tvOriginPrice.setVisibility(View.VISIBLE);
                    setRemainProgress(giftData);
                    if (giftData.freeStartTime > System.currentTimeMillis()) {
                        tvSeizeHint.setVisibility(View.VISIBLE);
                        tvSeizeHint.setText(String.format(Locale.CHINA,
                                "%s免费抢", DateUtil.formatUserReadDate(giftData.freeStartTime)));
                    }
                } else {
                    tvUsage.setText(giftData.usage);
                    switch (type) {
                        case GiftTypeUtil.TYPE_NORMAL_SEARCH:
                            tvRemain.setText(Html.fromHtml(String.format(Locale.CHINA, ConstString.TEXT_SEARCHED,
                                    giftData.searchCount)));
                            break;
                        case GiftTypeUtil.TYPE_LIMIT_SEIZE:
                            if (giftData.freeStartTime > System.currentTimeMillis()) {
                                tvSeizeHint.setVisibility(View.VISIBLE);
                                tvSeizeHint.setText(String.format(Locale.CHINA,
                                        "%s免费抢", DateUtil.formatUserReadDate(giftData.freeStartTime)));
                            }
                            break;
                        case GiftTypeUtil.TYPE_NORMAL_SEIZE:
                            setRemainProgress(giftData);
                            break;
                        case GiftTypeUtil.TYPE_LIMIT_WAIT_SEIZE:
                        case GiftTypeUtil.TYPE_NORMAL_WAIT_SEIZE:
                            tvRemain.setText(Html.fromHtml(String.format(ConstString.TEXT_SEIZE,
                                    giftData.seizeTime)));
                            break;
                        case GiftTypeUtil.TYPE_NORMAL_WAIT_SEARCH:
                            tvRemain.setText(Html.fromHtml(String.format(ConstString.TEXT_SEARCH,
                                    giftData.searchTime)));
                            break;
                    }
                }

            } else {
                tvConsume.setVisibility(View.GONE);
                tvRemain.setVisibility(View.GONE);
                tvCode.setVisibility(View.VISIBLE);
                btnCopy.setVisibility(View.VISIBLE);
                tvCode.setText(Html.fromHtml(String.format(ConstString.TEXT_GIFT_CODE, giftData.code)));
            }
            setDeadCount();

            if (!mIsNotifyRefresh) {
                if (giftData.giftType == GiftTypeUtil.GIFT_TYPE_LIMIT_FREE
                        && giftData.totalType == GiftTypeUtil.TOTAL_TYPE_FIRST_CHARGE) {
                    tvName.setText(String.format("%s(%s)", giftData.name, giftData.platform));
                    mAdapter.updateData(giftData.usagePics);
                } else {
                    tvName.setText(giftData.name);
                    tvUsage.setText(giftData.usage);
                }
                tvContent.setText(giftData.content);
                tvDeadline.setText(String.format("%s ~ %s",
                        DateUtil.formatTime(giftData.useStartTime, "yyyy-MM-dd HH:mm"),
                        DateUtil.formatTime(giftData.useEndTime, "yyyy-MM-dd HH:mm")));
                initDownload(mData.gameData);
            }

            if (giftData.totalType == GiftTypeUtil.TOTAL_TYPE_FIRST_CHARGE
                    && giftData.giftType == GiftTypeUtil.GIFT_TYPE_LIMIT_FREE) {
                showGuidePage();
            }
        } catch (Throwable t) {
            if (AppDebugConfig.IS_DEBUG) {
                AppDebugConfig.warn(AppDebugConfig.TAG_FRAG, t);
            }
        }
    }

    /**
     * 设置消费金币提示
     */
    private void setMoneyConsume(IndexGiftNew giftData) {
        if (giftData.priceType == GiftTypeUtil.PAY_TYPE_SCORE
                && giftData.giftType != GiftTypeUtil.GIFT_TYPE_LIMIT_FREE) {
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
    }

    /**
     * 填充详情的ViewStub内容
     */
    private void inflateVsView(IndexGiftNew o) {
        if (tvContent == null) {
            if (o.giftType == GiftTypeUtil.GIFT_TYPE_LIMIT_FREE
                    && o.totalType == GiftTypeUtil.TOTAL_TYPE_FIRST_CHARGE) {
                // 首充券
                View vsCharge = ((ViewStub) getViewById(R.id.vs_first_charge)).inflate();
                tvContent = getViewById(vsCharge, R.id.tv_content);
                tvDeadline = getViewById(vsCharge, R.id.tv_deadline);
                tvUsageTitle = getViewById(vsCharge, R.id.tv_usage_title);
                LinearLayoutManager llm = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
                DividerItemDecoration decoration = new DividerItemDecoration(getContext(),
                        llm.getOrientation(),
                        ViewUtil.getColor(getContext(), R.color.co_transparent),
                        getContext().getResources().getDimensionPixelSize(R.dimen.di_list_item_gap_small));
                rvUsage = getViewById(vsCharge, R.id.rv_usage);
                rvUsage.setLayoutManager(llm);
                rvUsage.addItemDecoration(decoration);
                mAdapter = new GiftDetailPicsAdapter(getContext());
                rvUsage.setAdapter(mAdapter);
                tvQQ = getViewById(R.id.tv_qq);
            } else {
                // 礼包
                View vsGift = ((ViewStub) getViewById(R.id.vs_gift)).inflate();
                tvContent = getViewById(vsGift, R.id.tv_content);
                tvDeadline = getViewById(vsGift, R.id.tv_deadline);
                tvUsage = getViewById(vsGift, R.id.tv_usage);
                tvQQ = getViewById(R.id.tv_qq);
            }
            tvQQ.setOnClickListener(this);
        }
    }

    /**
     * 设置进度条信息
     */
    private void setRemainProgress(IndexGiftNew giftData) {
        int progress = giftData.remainCount * 100 / giftData.totalCount;
        tvRemain.setText(Html.fromHtml(String.format(Locale.CHINA, "剩余%d%%", progress)));
        tvRemain.setVisibility(View.VISIBLE);
        pbPercent.setVisibility(View.VISIBLE);
        pbPercent.setMax(100);
        pbPercent.setProgress(progress);
    }

    /**
     * 设置0元抢的倒计时
     */
    private void setDeadCount() {
        if (mData.giftData.status != GiftTypeUtil.STATUS_WAIT_SEIZE
                || mData.giftData.seizeStatus == GiftTypeUtil.SEIZE_TYPE_SEIZED) {
            return;
        }
        if (mTimer != null) {
            mTimer.cancel();
        }
        btnSend.setState(GiftTypeUtil.STATUS_WAIT_SEIZE);
        final long remainTime = DateUtil.getTime(mData.giftData.seizeTime)
                - System.currentTimeMillis() + Global.sServerTimeDiffLocal;
        mTimer = new CountTimer(remainTime, Global.COUNTDOWN_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                btnSend.setText(DateUtil.formatTime(millisUntilFinished, "HH:mm:ss"));
            }

            @Override
            public void onFinish() {
                mData.giftData.status = GiftTypeUtil.STATUS_SEIZE;
                if (getActivity() != null) {
                    ((BaseAppCompatActivity) getActivity()).getHandler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            ToastUtil.showShort("自动刷新抢ing");
                            if (!mIsNotifyRefresh) {
                                mIsNotifyRefresh = true;
                                lazyLoad();
                            }
                        }
                    }, 2500);
                }
            }
        };
        mTimer.start();
    }


    /**
     * 设置礼包标识
     */
    private void setTag(IndexGiftNew giftData) {
        if (giftData.exclusive == 1) {
            tvName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_exclusive, 0, 0, 0);
        } else {
            tvName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }

        if (getActivity() instanceof GiftDetailActivity) {
            if (giftData.giftType == GiftTypeUtil.GIFT_TYPE_LIMIT) {
                ((GiftDetailActivity) getActivity()).showLimitTag(true, R.string.st_gift_tag_limit);
            } else if (giftData.giftType == GiftTypeUtil.GIFT_TYPE_LIMIT_FREE) {
                ((GiftDetailActivity) getActivity()).showLimitTag(true, R.string.st_gift_tag_free);
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
    }

    public void initDownload(IndexGameNew game) {
        if (getActivity() == null || game == null || btnDownload == null) {
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

    /**
     * 异步请求获取网络数据线程执行体
     */
    private DataRunnable mDataRunnable;

    @Override
    protected void lazyLoad() {
        refreshInitConfig();
        if (mDataRunnable != null) {
            Global.THREAD_POOL.execute(mDataRunnable);
        }

    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.btn_download:
                if (mAppInfo != null) {
                    mAppInfo.handleOnClick(getChildFragmentManager());
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
                // 取消强制要求下载的模块
//				if (AssistantApp.getInstance().isAllowDownload()
//						&& mData.giftData.giftType == GiftTypeUtil.GIFT_TYPE_LIMIT_FREE) {
//					return;
//				}
                PayManager.getInstance().seizeGift(getContext(), mData.giftData, btnSend);
                break;
            case R.id.tv_qq:
                IntentUtil.joinQQGroup(getContext(), MixUtil.getQQInfo()[1]);
                break;
            case R.id.iv_icon:
                if (mData != null && mData.gameData != null) {
                    IntentUtil.jumpGameDetail(getContext(), mData.gameData.id, GameTypeUtil.JUMP_STATUS_GIFT);
                }
                break;
        }
    }

    /**
     * 判断是否安装了游戏，如果没有，弹窗提示下载安装
     */
//	private boolean isInstalledGame() {
//		HashSet<String> appNames = Global.getInstalledAppNames();
//		for (String name : appNames) {
//			if (mData.gameData.name.startsWith(name)) {
//				// 前缀匹配成功，表明有安装该游戏，返回成功
//				return true;
//			}
//		}
//		if (mAppInfo == null) {
//			ToastUtil.showShort("页面信息错误，请重新进入");
//			return false;
//		}
//		final ConfirmDialog dialog = ConfirmDialog.newInstance();
//		dialog.setTitle("小贴士");
//		dialog.setContent(Html.fromHtml(String.format("下载「<font color='#ffaa17'>%s</font>」安装，马上参与0元抢购！",
//				mData.gameData.name)));
//		if (mAppInfo.downloadStatus != null && mAppInfo.getAppStatus(mAppInfo.downloadStatus) == AppStatus
//				.INSTALLABLE) {
//			dialog.setPositiveBtnText(mApp.getResources().getString(R.string.st_dialog_btn_install));
//			dialog.setPositiveBtnBackground(R.drawable.selector_btn_download_blue);
//		} else {
//			dialog.setPositiveBtnText(mApp.getResources().getString(R.string.st_dialog_btn_download));
//		}
//		dialog.setListener(new BaseFragment_Dialog.OnDialogClickListener() {
//			@Override
//			public void onCancel() {
//				dialog.dismissAllowingStateLoss();
//			}
//
//			@Override
//			public void onConfirm() {
//				if (mAppInfo != null) {
//					if (mAppInfo.downloadStatus != null) {
//						if (mAppInfo.downloadStatus == DownloadStatus.DOWNLOADING) {
//							ToastUtil.showShort("已经在下载中，请等待下载完成");
//							dialog.dismissAllowingStateLoss();
//							return;
//						}
//						mAppInfo.appStatus = mAppInfo.getAppStatus(mAppInfo.downloadStatus);
//					}
//					mAppInfo.handleOnClick(getChildFragmentManager());
//				}
//				dialog.dismissAllowingStateLoss();
//			}
//		});
//		dialog.show(getChildFragmentManager(), ConfirmDialog.class.getSimpleName());
//		return false;
//	}

    private Runnable mProgressRunnable;

    @Override
    public void onDownloadStatusChanged(final GameDownloadInfo appInfo) {
        if (mProgressRunnable != null && getActivity() != null) {
            ((BaseAppCompatActivity) getActivity()).getHandler().removeCallbacks(mProgressRunnable);
        }
        mProgressRunnable = new Runnable() {
            @Override
            public void run() {

                if (downloadLayout != null && downloadLayout.getVisibility() == View.VISIBLE) {
                    mAppInfo.downloadStatus = appInfo.downloadStatus;
                    mAppInfo.initAppInfoStatus(getContext());
                    btnDownload.setStatus(mAppInfo.appStatus, "");
                }
                mProgressRunnable = null;
            }
        };
        if (getActivity() != null) {
            ((BaseAppCompatActivity) getActivity()).getHandler().post(mProgressRunnable);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void onProgressUpdate(String url, final int percent, long speedBytesPers) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (downloadLayout != null && downloadLayout.getVisibility() == View.VISIBLE) {
                        btnDownload.setProgress(percent);
                    }
                }
            });
        }
    }

    @Override
    public void release() {
        super.release();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
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
        btnDownload = null;
        downloadLayout = null;
        tvOriginPrice = null;
        rvUsage = null;
        mData = null;
        mAppInfo = null;
        if (mDataRunnable != null && mDataRunnable.mCall != null) {
            mDataRunnable.mCall.cancel();
            mDataRunnable.mWeakFragment.clear();
        }
    }

    @Override
    public String getPageName() {
        return PAGE_NAME;
    }

    /**
     * 执行网络请求的异步请求任务
     */
    private static class DataRunnable implements Runnable {

        private WeakReference<GiftDetailFragment> mWeakFragment;

        public DataRunnable(GiftDetailFragment fragment) {
            mWeakFragment = new WeakReference<GiftDetailFragment>(fragment);
        }

        private Call<JsonRespBase<GiftDetail>> mCall;

        @Override
        public void run() {
            if (mWeakFragment == null || mWeakFragment.get() == null) {
                return;
            }
            final GiftDetailFragment f = mWeakFragment.get();
            if (!NetworkUtil.isConnected(f.getContext())) {
                f.refreshFailEnd();
                return;
            }
            if (mCall != null) {
                mCall.cancel();
            }
            ReqGiftDetail data = new ReqGiftDetail();
            data.id = f.mId;
            mCall = Global.getNetEngine().obtainGiftDetail(new JsonReqBase<ReqGiftDetail>(data));
            mCall.enqueue(new Callback<JsonRespBase<GiftDetail>>() {
                @Override
                public void onResponse(Call<JsonRespBase<GiftDetail>> call, Response<JsonRespBase<GiftDetail>>
                        response) {
                    if (call.isCanceled() || !f.mCanShowUI) {
                        return;
                    }
                    if (response != null && response.code() == 200) {
                        Global.sServerTimeDiffLocal =
                                System.currentTimeMillis() - response.headers().getDate("Date").getTime();
                        if (response.body() != null && response.body().getCode() == NetStatusCode.SUCCESS) {
                            f.refreshSuccessEnd();
//                            f.updateData(response.body().getData());
                            f.updateData(f.initTestData());
                            return;
                        }
                        if (AppDebugConfig.IS_DEBUG) {
                            KLog.d(AppDebugConfig.TAG_FRAG, "body = " + response.body());
                        }
                    }
                    // 加载错误页面也行
                    f.refreshFailEnd();
                }

                @Override
                public void onFailure(Call<JsonRespBase<GiftDetail>> call, Throwable t) {
                    if (call.isCanceled() || !f.mCanShowUI) {
                        return;
                    }
                    if (AppDebugConfig.IS_DEBUG) {
                        KLog.e(t);
                    }
                    f.refreshFailEnd();
                }
            });
        }
    }

    private boolean mIsShowGuide = true;

    /**
     * 显示首充券使用的指引页面
     */
    public void showGuidePage() {
        if (mIsShowGuide) {
            ThreadUtil.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final Dialog dialog = new Dialog(getContext(), R.style.DefaultCustomDialog_NoDim);
                    View v = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                            .inflate(R.layout.overlay_hint_focus, null);
                    ImageView ivConfirm = ViewUtil.getViewById(v, R.id.iv_confirm);
                    if (ivConfirm != null) {
                        ivConfirm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.cancel();
                            }
                        });
                    }
                    dialog.setCancelable(true);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.setContentView(v);
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    dialog.show();
                }
            }, 500);
        }
    }

    private GiftDetail initTestData() {
        GiftDetail result = new GiftDetail();
        IndexGiftNew gift = new IndexGiftNew();
        result.giftData = gift;
        gift.status = (int) (Math.random() * 3) + 7;
        gift.totalType = GiftTypeUtil.TOTAL_TYPE_FIRST_CHARGE;
        gift.giftType = GiftTypeUtil.GIFT_TYPE_LIMIT_FREE;
        int[] seize = new int[]{GiftTypeUtil.SEIZE_TYPE_RESERVED, GiftTypeUtil.SEIZE_TYPE_SEIZED, GiftTypeUtil
                .SEIZE_TYPE_NEVER,
                GiftTypeUtil.SEIZE_TYPE_UN_RESERVE};
        gift.seizeStatus = seize[(int) (Math.random() * 4)];
        gift.remainCount = 10;
        gift.totalCount = 10;
        gift.bean = (int) (Math.random() * 100);
        gift.score = 500;
        gift.content = "充值减少6元人民币";
        gift.platform = "偶玩版";
        gift.name = "6元首充券";
        gift.useStartTime = "2016-06-01 12:00:00";
        gift.useEndTime = "2016-00-05 12:00:00";
        gift.originPrice = 32;
        gift.code = "sdkkk3431qercx341iZX";
        gift.freeStartTime = System.currentTimeMillis() + 3600 * 24 * 1000;
        gift.usagePics = new ArrayList<>();
        gift.usagePics.add("http://owan-img.ymapp.com/giftcool/slider/2016/05/24/b6ed8a61b59586478705589a2ed5f5da" +
                ".jpg_512_512_95.jpg");
        gift.usagePics.add("http://owan-img.ymapp.com/giftcool/slider/2016/04/27/96a7d5d0beebdcd2d7a33bd036bf8efa" +
                ".jpg_512_512_95.jpg");
        gift.usagePics.add("http://owan-img.ymapp.com/giftcool/slider/2016/05/13/33fa2d7bebb68d2abc8c1d56e8dd4119" +
                ".jpg_512_512_95.jpg");
        gift.usagePics.add("http://owan-img.ymapp.com/giftcool/slider/2016/05/18/a6227d33c7d59f024d384964a99bd6d1" +
                ".jpg_512_512_95.jpg");
        gift.id = 136;
        IndexGameNew game = new IndexGameNew();
        game.id = 133;
        game.name = "宠物小精灵";
        game.img = "http://owan-img.ymapp.com/app/11004/icon/icon_1460627334.png_128_128_70.png";
        result.gameData = game;
        return result;
    }
}
