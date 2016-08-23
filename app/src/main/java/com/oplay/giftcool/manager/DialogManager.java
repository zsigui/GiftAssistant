package com.oplay.giftcool.manager;

import android.app.Dialog;
import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.NetStatusCode;
import com.oplay.giftcool.download.silent.SilentDownloadManager;
import com.oplay.giftcool.download.silent.bean.DownloadInfo;
import com.oplay.giftcool.model.data.req.ReqHopeGift;
import com.oplay.giftcool.model.data.resp.IndexGameNew;
import com.oplay.giftcool.model.data.resp.UpdateInfo;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Dialog;
import com.oplay.giftcool.ui.fragment.dialog.ConfirmDialog;
import com.oplay.giftcool.ui.fragment.dialog.HintDialog;
import com.oplay.giftcool.ui.fragment.dialog.HopeGiftDialog;
import com.oplay.giftcool.ui.fragment.dialog.LoadingDialog;
import com.oplay.giftcool.ui.fragment.dialog.PicDialog;
import com.oplay.giftcool.ui.fragment.dialog.WelcomeDialog;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.NetworkUtil;
import com.oplay.giftcool.util.ThreadUtil;
import com.oplay.giftcool.util.ToastUtil;
import com.oplay.giftcool.util.ViewUtil;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

    long mLastClickTime = 0;

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
                StatisticsManager.getInstance().trace(mContext,
                        StatisticsManager.ID.USER_HOPE_GIFT_QUICK,
                        StatisticsManager.ID.STR_USER_HOPE_GIFT,
                        StatisticsManager.ID.STR_USER_HOPE_GIFT_QUICK);
            }

            @Override
            public void onConfirm() {
                long curTime = System.currentTimeMillis();
                if (curTime - mLastClickTime < Global.CLICK_TIME_INTERVAL) {
                    mLastClickTime = curTime;
                    return;
                }
                handleHopeGiftRequest(fm, dialog, dialog.getGameId(), dialog.getName(), dialog.getNote());
                StatisticsManager.getInstance().trace(mContext,
                        StatisticsManager.ID.USER_HOPE_GIFT_SUCCESS,
                        StatisticsManager.ID.STR_USER_HOPE_GIFT,
                        StatisticsManager.ID.STR_USER_HOPE_GIFT_SUCCESS);
            }
        };
        dialog.setListener(dialogClickListener);
        dialog.show(fm, "hope_gift");
    }

    /**
     * 求礼包的网络请求声明
     */
    private Call<JsonRespBase<Void>> mCallHopeGift;

    /**
     * 执行求礼包的请求
     */
    private void handleHopeGiftRequest(final FragmentManager fm, final HopeGiftDialog dialog,
                                       int id, String name, String note) {
        if (!NetworkUtil.isConnected(mContext)) {
            ToastUtil.showShort(ConstString.TOAST_NET_ERROR);
            return;
        }
        if (mCallHopeGift != null) {
            mCallHopeGift.cancel();
        }
        ReqHopeGift reqHopeGift = new ReqHopeGift();
        reqHopeGift.gameId = id;
        reqHopeGift.gameName = name;
        reqHopeGift.note = note;
        mCallHopeGift = Global.getNetEngine().commitHopeGift(new JsonReqBase<ReqHopeGift>(reqHopeGift));
        showLoadingDialog(fm);
        mCallHopeGift.enqueue(new Callback<JsonRespBase<Void>>() {
            @Override
            public void onResponse(Call<JsonRespBase<Void>> call, Response<JsonRespBase<Void>> response) {
                hideLoadingDialog();
                if (call.isCanceled()) {
                    return;
                }
                if (response != null && response.isSuccessful()) {
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
                                    ScoreManager.getInstance().setTaskFinished(true);
                                    title = mContext.getString(R.string.st_dialog_hope_gift_success_title);
                                    content = mContext.getString(R.string.st_dialog_hope_gift_success_content);
                            }
                            confirmDialog.setTitle(title);
                            confirmDialog.setContent(content);
                            confirmDialog.show(fm, "confirm");
                            return;
                        } else if (resp.getCode() == NetStatusCode.ERR_UN_LOGIN
                                || resp.getCode() == NetStatusCode.ERR_BAD_USER_SERVER) {
                            // 登录状态失效
                            ToastUtil.showShort(ConstString.TOAST_LOGIN_FIRST);
                            AccountManager.getInstance().notifyUserAll(null);
                            return;
                        }
                    }
                }
                ToastUtil.blurErrorResp(response);
            }

            @Override
            public void onFailure(Call<JsonRespBase<Void>> call, Throwable t) {
                hideLoadingDialog();
                if (call.isCanceled()) {
                    return;
                }
                ToastUtil.blurThrow(t);
            }
        });
    }

    /**
     * @Hint 显示重点内容
     * <p/>
     * 显示重点提示窗
     */
    public void showHintDialog(FragmentManager fm, String title, String content, String hint, String tag) {
        final HintDialog dialog = HintDialog.newInstance();
        dialog.setHint(hint);
        dialog.setContent(content);
        dialog.setTitle(title);
        dialog.show(fm, tag);
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
    public synchronized void showLoadingDialog(final FragmentManager fm, final String loadText) {
        if (fm == null) {
            return;
        }
        ThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final String tag = LoadingDialog.class.getSimpleName();
                fm.executePendingTransactions();
                mLoadingDialog = (LoadingDialog) fm.findFragmentByTag(tag);
                if (mLoadingDialog != null && mLoadingDialog.isAdded()) {
                    return;
                }
                if (mLoadingDialog == null) {
                    mLoadingDialog = LoadingDialog.newInstance();
                }
                mLoadingDialog.setCancelable(false);
                mLoadingDialog.setLoadText(loadText);
                mLoadingDialog.show(fm, tag);
            }
        });
    }

    /**
     * 隐藏加载弹窗，避免出错
     */
    public synchronized void hideLoadingDialog() {
        if (mLoadingDialog != null) {
            mLoadingDialog.dismissAllowingStateLoss();
            mLoadingDialog = null;
        }
    }

    /**
     * 显示更新弹窗，有更新弹窗并返回true，没则直接返回false
     */
    public boolean showUpdateDialog(final Context context, final FragmentManager fm, boolean forceShow) {
        return showUpdateDialog(context, fm, forceShow, AssistantApp.getInstance().getUpdateInfo());
    }

    /**
     * 显示更新弹窗，有更新弹窗并返回true，没则直接返回false
     */
    public boolean showUpdateDialog(final Context context, final FragmentManager fm, boolean forceShow
            , final UpdateInfo updateInfo) {
        if (updateInfo != null && updateInfo.checkoutUpdateInfo(context)) {
            final IndexGameNew appInfo = new IndexGameNew();
            appInfo.id = Global.GIFTCOOL_GAME_ID;
            appInfo.name = context.getString(R.string.app_name);
            appInfo.apkFileSize = updateInfo.apkFileSize;
            //没icon地址，随便填个
            appInfo.img = updateInfo.downloadUrl;
            appInfo.downloadUrl = updateInfo.downloadUrl;
            appInfo.destUrl = updateInfo.downloadUrl;
            appInfo.packageName = updateInfo.packageName;
            appInfo.versionName = updateInfo.versionName;
            appInfo.size = appInfo.getApkFileSizeStr();
            appInfo.initFile();
            if (forceShow || appInfo.isFileExists()) {
                appInfo.initAppInfoStatus(context);
                BaseFragment_Dialog confirmDialog = getUpdateDialog(context, appInfo, updateInfo.content,
                        updateInfo.updatePercent);
                confirmDialog.show(fm, "update");
                return true;
            } else {
                if (!SilentDownloadManager.getInstance().contains(appInfo.downloadUrl)) {
                    AppDebugConfig.d(AppDebugConfig.TAG_DEBUG_INFO, "start to download app silent!");
                    DownloadInfo info = new DownloadInfo();
                    info.setDestUrl(appInfo.destUrl);
                    info.setDownloadUrl(appInfo.downloadUrl);
                    info.setTotalSize(appInfo.apkFileSize);
                    info.setMd5Sum(appInfo.apkMd5);
                    info.setIsDownload(true);
                    SilentDownloadManager.getInstance().startDownload(info);
                }
            }
        }
        return false;
    }

    /**
     * 根据传入内容获取更新弹窗
     */
    private WelcomeDialog getUpdateDialog(final Context context, final IndexGameNew appInfo,
                                          final String content, int updatePercent) {
        final WelcomeDialog confirmDialog = WelcomeDialog.newInstance(R.layout.dialog_welcome_update);
        confirmDialog.setTitle(content);
        confirmDialog.setPositiveBtnText(context.getResources().getString(R.string.st_welcome_update_confirm));
        confirmDialog.setNegativeBtnText(context.getResources().getString(R.string.st_welcome_update_cancel));
        confirmDialog.setPercent(updatePercent);
        confirmDialog.setListener(new BaseFragment_Dialog.OnDialogClickListener() {
            @Override
            public void onCancel() {
                if (!SilentDownloadManager.getInstance().contains(appInfo.downloadUrl)) {
                    // 允许默认先下载
                    DownloadInfo info = new DownloadInfo();
                    info.setDestUrl(appInfo.destUrl);
                    info.setDownloadUrl(appInfo.downloadUrl);
                    info.setTotalSize(appInfo.apkFileSize);
                    info.setMd5Sum(appInfo.apkMd5);
                    info.setIsDownload(true);
                    SilentDownloadManager.getInstance().startDownload(info);
                }
                confirmDialog.dismiss();
            }

            @Override
            public void onConfirm() {
                StatisticsManager.getInstance().trace(context,
                        StatisticsManager.ID.UPGRADE,
                        StatisticsManager.ID.STR_UPGRADE);
                appInfo.startDownload();
                confirmDialog.dismiss();
            }
        });
        return confirmDialog;
    }

    /**
     * 显示签到弹窗
     */
    public void showSignInDialog(final boolean canShow, final Context context, final FragmentManager fm) {
        if (canShow) {
            final PicDialog dialog = PicDialog.newInstance("drawable://" + R.drawable.pic_lottery_everyday);
            dialog.setDialogClickListener(new BaseFragment_Dialog.OnDialogClickListener() {
                @Override
                public void onCancel() {
                    dialog.dismissAllowingStateLoss();
                }

                @Override
                public void onConfirm() {
                    IntentUtil.jumpSignIn(context);
                    dialog.dismissAllowingStateLoss();
                }
            });
            dialog.show(fm, "signin");
        }
    }


    /**
     * 显示首充券使用的指引页面
     */
    public void showGuidePage(Context context) {
        final Dialog dialog = new Dialog(context, R.style.DefaultCustomDialog_NoDim);
        View v = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.overlay_hint_focus_game, null);
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
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams
                .MATCH_PARENT);
        dialog.show();
    }
}
