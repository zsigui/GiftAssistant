package com.oplay.giftcool.ui.fragment.gift;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.assist.CountTimer;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.config.NetStatusCode;
import com.oplay.giftcool.config.TypeStatusCode;
import com.oplay.giftcool.config.WebViewUrl;
import com.oplay.giftcool.config.util.GameTypeUtil;
import com.oplay.giftcool.config.util.GiftTypeUtil;
import com.oplay.giftcool.download.ApkDownloadManager;
import com.oplay.giftcool.download.listener.OnDownloadStatusChangeListener;
import com.oplay.giftcool.download.listener.OnProgressUpdateListener;
import com.oplay.giftcool.listener.OnHandleListener;
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
import com.oplay.giftcool.util.ToastUtil;
import com.oplay.giftcool.util.ViewUtil;

import java.lang.ref.WeakReference;
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

    // 用于防止重复打开指引
//    static boolean mIsFirstOpenPage = true;

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
    private GiftButton btnSend;
    private ProgressBar pbPercent;
    private TextView tvCode;
    private TextView btnCopy;
    private TextView tvRemark;
    private TextView tvQQ;
    private DownloadButtonView btnDownload;
    private LinearLayout downloadLayout;
    private DeletedTextView tvOriginPrice;
    private TextView tvBroadcast;


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
            ((GiftDetailActivity) getActivity()).setOnHandleListener(new OnHandleListener() {
                @Override
                public void deal() {
                    if (mData == null || mData.giftData == null || mData.gameData == null) {
                        AppDebugConfig.d(AppDebugConfig.TAG_SHARE, "share for null failed");
                        ToastUtil.showShort(ConstString.TOAST_WRONG_PARAM);
                        return;
                    }
                    try {
                        mData.giftData.gameName = mData.gameData.name;
                        mData.giftData.img = mData.gameData.img;
                        ShareSDKManager.getInstance(mApp).shareGift(mApp, getChildFragmentManager(), mData.giftData);
                    } catch (Throwable e) {
                        AppDebugConfig.w(AppDebugConfig.TAG_SHARE, e);
                        ToastUtil.showShort(ConstString.TOAST_UNKNOWN_ERROR);
                    }

                }
            });
        }
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        if (getArguments() == null) {
            ToastUtil.showShort(ConstString.TOAST_WRONG_PARAM);
            return;
        }
        mId = getArguments().getInt(KeyConfig.KEY_DATA);
        AppDebugConfig.d(AppDebugConfig.TAG_FRAG, "transfer id = " + mId);
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
            ToastUtil.showShort(ConstString.TOAST_DATA_OVERTIME);
            mViewManager.showEmpty();
            return;
        }
        try {
            mHasData = true;
            mViewManager.showContent();
            mData = data;
            final IndexGiftNew giftData = data.giftData;
            giftData.buttonState = (giftData.buttonState == 0 ?
                    GiftTypeUtil.getButtonState(giftData) : giftData.buttonState);
            setTag(giftData);

            inflateVsView(giftData);

            btnSend.setState(giftData.buttonState);
            tvOr.setVisibility(View.GONE);
            pbPercent.setVisibility(View.GONE);
            tvBean.setVisibility(View.GONE);
            tvCode.setVisibility(View.GONE);
            btnCopy.setVisibility(View.GONE);
            tvScore.setVisibility(View.GONE);
            btnSend.setVisibility(View.VISIBLE);
            tvSeizeHint.setVisibility(View.GONE);
            tvOriginPrice.setVisibility(View.GONE);

            tvQQ.setText(String.format(QQ_TEXT, MixUtil.getQQInfo()[0]));

            if (giftData.freeStartTime * 1000 > System.currentTimeMillis()) {
                tvSeizeHint.setVisibility(View.VISIBLE);
                tvSeizeHint.setText(String.format(Locale.CHINA,
                        ConstString.TEXT_GIFT_FREE_SEIZE,
                        DateUtil.formatUserReadDateForDetail(giftData.freeStartTime)));
            }

            if (giftData.seizeStatus == GiftTypeUtil.SEIZE_TYPE_SEARCHED
                    || giftData.seizeStatus == GiftTypeUtil.SEIZE_TYPE_SEIZED) {
                showCode(giftData);
            } else {
                tvConsume.setVisibility(View.VISIBLE);
                tvRemain.setVisibility(View.VISIBLE);
                setMoneyConsume(giftData);
                switch (giftData.buttonState) {
                    case GiftTypeUtil.BUTTON_TYPE_WAIT_SEIZE:
                        tvRemain.setText(Html.fromHtml(String.format(ConstString.TEXT_SEIZE,
                                giftData.seizeTime)));
                        break;
                    case GiftTypeUtil.BUTTON_TYPE_ACTIVITY_WAIT:
                        tvRemain.setText(Html.fromHtml(getString(R.string.st_gift_detail_activity_wait,
                                giftData.seizeTime)));
                        break;
                    case GiftTypeUtil.BUTTON_TYPE_WAIT_SEARCH:
                        tvRemain.setText(Html.fromHtml(String.format(ConstString.TEXT_SEARCH,
                                giftData.searchTime)));
                        break;
                    case GiftTypeUtil.BUTTON_TYPE_SEARCH:
                        tvRemain.setText(Html.fromHtml(String.format(Locale.CHINA, ConstString.TEXT_SEARCHED,
                                giftData.searchCount)));
                        break;
                    default:
                        if (giftData.giftType == GiftTypeUtil.GIFT_TYPE_LIMIT_FREE
                                || giftData.giftType == GiftTypeUtil.GIFT_TYPE_LIMIT) {
                            // 限量及限量免费
                            ViewUtil.siteValueUI(tvOriginPrice, giftData.originPrice, true);
                        }
                        setRemainProgress(giftData);
                }
            }

            if (!mIsNotifyRefresh) {
                if (giftData.totalType == GiftTypeUtil.TOTAL_TYPE_COUPON) {
                    tvName.setText(String.format("%s(%s)", giftData.name, giftData.platform));
//                    mAdapter.updateData(giftData.usagePicsThumb, giftData.usagePicsBig);
                } else {
                    tvName.setText(giftData.name);
                }
                ViewUtil.handleLink(tvUsage, giftData.usage, WebViewUrl.PROTOCOL);
                tvContent.setText(giftData.content);
                tvDeadline.setText(String.format("%s ~ %s",
                        DateUtil.formatTime(giftData.useStartTime, "yyyy-MM-dd HH:mm"),
                        DateUtil.formatTime(giftData.useEndTime, "yyyy-MM-dd HH:mm")));
                initDownload(mData.gameData);
            }

        } catch (Throwable t) {
            AppDebugConfig.w(AppDebugConfig.TAG_FRAG, t);
        }
    }

    private void showCode(IndexGiftNew giftData) {
        tvConsume.setVisibility(View.GONE);
        tvRemain.setVisibility(View.GONE);
        tvCode.setVisibility(View.VISIBLE);
        tvSeizeHint.setVisibility(View.GONE);
        if (giftData.totalType == GiftTypeUtil.TOTAL_TYPE_COUPON) {
            btnCopy.setVisibility(View.INVISIBLE);
            tvCode.setText(Html.fromHtml(getString(R.string.st_gift_detail_coupon_seized)));
        } else {
            btnCopy.setVisibility(View.VISIBLE);
            tvCode.setText(Html.fromHtml(String.format(ConstString.TEXT_GIFT_CODE, giftData.code)));
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
            if (o.totalType == GiftTypeUtil.TOTAL_TYPE_COUPON) {
                // 首充券
                View vsCharge = ((ViewStub) getViewById(R.id.vs_first_charge)).inflate();
                tvContent = getViewById(vsCharge, R.id.tv_content);
                tvDeadline = getViewById(vsCharge, R.id.tv_deadline);
                tvUsage = getViewById(vsCharge, R.id.tv_usage);
                tvRemark = getViewById(vsCharge, R.id.tv_remark);
                tvRemark.setText(TextUtils.isEmpty(o.remark) ?
                        getContext().getResources().getString(R.string.st_coupon_hint_content) : o.remark);
                tvQQ = getViewById(vsCharge, R.id.tv_qq);
            } else {
                // 礼包
                View vsGift = ((ViewStub) getViewById(R.id.vs_gift)).inflate();
                tvContent = getViewById(vsGift, R.id.tv_content);
                tvDeadline = getViewById(vsGift, R.id.tv_deadline);
                tvUsage = getViewById(vsGift, R.id.tv_usage);
                tvQQ = getViewById(vsGift, R.id.tv_qq);
                tvRemark = getViewById(vsGift, R.id.tv_remark);
                tvRemark.setText(TextUtils.isEmpty(o.remark) ?
                        getContext().getResources().getString(R.string.st_gift_hint_content) : o.remark);
                if (o.nature == GiftTypeUtil.NATURE_ACTIVITY && tvBroadcast == null) {
                    tvBroadcast = getViewById(((ViewStub) getViewById(R.id.vs_broadcast)).inflate(),
                            R.id.tv_activity_hint);
                    tvBroadcast.setOnClickListener(this);
                    String text = o.activityTitle;
                    switch (o.activityStatus) {
                        case TypeStatusCode.POST_WAIT:
                            text += " <font color='#ffaa17'>[等开始]</font>";
                            break;
                        case TypeStatusCode.POST_BEING:
                            text += " <font color='#f85454'>[进行中]</font>";
                            break;
                        case TypeStatusCode.POST_FINISHED:
                            text += " <font color='#888888'>[已结束]</font>";
                            break;
                    }
                    tvBroadcast.setText(Html.fromHtml(text));
                }
            }
            tvQQ.setOnClickListener(this);
        }
    }

    /**
     * 设置进度条信息
     */
    private void setRemainProgress(IndexGiftNew o) {
        int progress = MixUtil.calculatePercent(o.remainCount, o.totalCount);
        tvRemain.setText(Html.fromHtml(String.format(Locale.CHINA, "剩余%d%%", progress)));
        tvRemain.setVisibility(View.VISIBLE);
        pbPercent.setVisibility(View.VISIBLE);
        pbPercent.setMax(100);
        pbPercent.setProgress(progress);
    }

    /**
     * 设置礼包标识
     */
    private void setTag(IndexGiftNew giftData) {
        if (giftData.exclusive == 1) {
            tvName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_tag_exclusive, 0, 0, 0);
        } else {
            tvName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }

        if (getActivity() instanceof GiftDetailActivity) {
            GiftDetailActivity pActivity = (GiftDetailActivity) getActivity();
            if (giftData.nature == GiftTypeUtil.NATURE_ACTIVITY) {
                pActivity.showLimitTag(true, R.string.st_gift_tag_activity);
            } else {
                switch (giftData.giftType) {
                    case GiftTypeUtil.GIFT_TYPE_LIMIT:
                        pActivity.showLimitTag(true, R.string.st_gift_tag_limit);
                        break;
                    case GiftTypeUtil.GIFT_TYPE_LIMIT_FREE:
                        pActivity.showLimitTag(true, R.string.st_gift_tag_free);
                        break;
                }
            }
            switch (mData.giftData.buttonState) {
                case GiftTypeUtil.BUTTON_TYPE_FINISH:
                case GiftTypeUtil.BUTTON_TYPE_EMPTY:
                case GiftTypeUtil.BUTTON_TYPE_ACTIVITY_FINISHED:
                case GiftTypeUtil.BUTTON_TYPE_TAKE_OFF:
                    pActivity.showShareButton(false);
                    break;
                default:
                    pActivity.showShareButton(true);
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
            if (mAppInfo.downloadState == 1
                    && !TextUtils.isEmpty(mAppInfo.downloadUrl)) {
                btnDownload.setEnabled(true);
                int progress = ApkDownloadManager.getInstance(getActivity()).getProgressByUrl(mAppInfo
                        .downloadUrl);
                btnDownload.setStatus(mAppInfo.appStatus, "");
                btnDownload.setProgress(progress);
                mAppInfo.initAppInfoStatus(getActivity());
            } else {
                btnDownload.setEnabled(false);
            }
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
                ToastUtil.showShort(ConstString.TOAST_COPY_CODE);
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
                PayManager.getInstance().seizeGift(getActivity(), mData.giftData, btnSend, this);
                break;
            case R.id.tv_activity_hint:
                if (mData == null) {
                    return;
                }
                IntentUtil.jumpPostDetail(getContext(), mData.giftData.activityId);
                break;
            case R.id.tv_qq:
                IntentUtil.joinQQGroup(getContext(), MixUtil.getQQInfo()[1]);
                break;
            case R.id.iv_icon:
                if (mData != null && mData.gameData != null) {
                    IntentUtil.jumpGameDetail(getContext(), mData.gameData.id, GameTypeUtil.JUMP_STATUS_GIFT);
                }
                break;
//            case R.id.iv_confirm:
//                mDialog.cancel();
//                break;
//            case R.id.ll_content:
//                if (mAdapter != null) {
//                    GalleryFinal.openMultiPhoto(0, mAdapter.getPics());
//                }
//                mDialog.cancel();
//                break;
        }
    }

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
                            f.updateData(response.body().getData());
                            return;
                        }
                    }
                    AppDebugConfig.warnResp(AppDebugConfig.TAG_FRAG, response);
                    // 加载错误页面也行
                    f.refreshFailEnd();
                }

                @Override
                public void onFailure(Call<JsonRespBase<GiftDetail>> call, Throwable t) {
                    if (call.isCanceled() || !f.mCanShowUI) {
                        return;
                    }
                    AppDebugConfig.w(AppDebugConfig.TAG_FRAG, t);
                    f.refreshFailEnd();
                }
            });
        }
    }

//    private Dialog mDialog;
//
//    /**
//     * 显示首充券使用的指引页面
//     */
//    public void showGuidePage() {
//        if (mDialog == null) {
//            mDialog = new Dialog(getContext(), R.style.DefaultCustomDialog_NoDim);
//            View v = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
//                    .inflate(R.layout.overlay_hint_gift_detail_usage, (ViewGroup) mContentView.getParent(), false);
//            View llTop = getViewById(v, R.id.ll_top);
//            View llContent = getViewById(v, R.id.ll_content);
//            final int top = getResources().getDimensionPixelSize(R.dimen.di_tool_bar_bg_height) + rlHeader.getHeight()
//                    - svContent.getScrollY();
//            final int padding = getResources().getDimensionPixelSize(R.dimen.di_list_item_gap_small);
//
//            if (llTop != null) {
//                ViewGroup.LayoutParams lp = llTop.getLayoutParams();
//                lp.height = top + llUsageTitle.getTop() + padding;
//                llTop.setLayoutParams(lp);
//            }
//            if (llContent != null) {
//                ViewGroup.LayoutParams lp = llContent.getLayoutParams();
//                lp.height = rvUsage.getBottom() - llUsageTitle.getTop() + padding;
//                llContent.setLayoutParams(lp);
//                llContent.setOnClickListener(this);
//            }
//            ImageView ivConfirm = ViewUtil.getViewById(v, R.id.iv_confirm);
//            if (ivConfirm != null) {
//                ivConfirm.setOnClickListener(this);
//            }
//            mDialog.setCancelable(true);
//            mDialog.setCanceledOnTouchOutside(false);
//            mDialog.setContentView(v);
//            mDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams
//                    .MATCH_PARENT);
//        }
//        mDialog.show();
//        mIsFirstOpenPage = false;
//    }
}
