package com.oplay.giftcool.ui.fragment.setting;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppConfig;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.NetStatusCode;
import com.oplay.giftcool.model.data.req.ReqInitApp;
import com.oplay.giftcool.model.data.resp.IndexGameNew;
import com.oplay.giftcool.model.data.resp.UpdateInfo;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Dialog;
import com.oplay.giftcool.ui.fragment.dialog.ConfirmDialog;
import com.oplay.giftcool.util.AppInfoUtil;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.MixUtil;
import com.oplay.giftcool.util.NetworkUtil;
import com.oplay.giftcool.util.ThreadUtil;
import com.socks.library.KLog;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by zsigui on 16-1-6.
 */
public class AboutFragment extends BaseFragment {

    private final static String PAGE_NAME = "关于";
    private RelativeLayout rlUpdate;
    private RelativeLayout rlQQ;
    private TextView tvUpdate;
    private TextView tvQQ;
    private TextView tvVersion;
    private Context mContext;

    private UpdateInfo mUpdateInfo;

    public static AboutFragment newInstance() {
        return new AboutFragment();
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.fragment_about);
        rlUpdate = getViewById(R.id.rl_update);
        rlQQ = getViewById(R.id.rl_qq);
        tvUpdate = getViewById(R.id.tv_update);
        tvQQ = getViewById(R.id.tv_qq);
        tvVersion = getViewById(R.id.tv_version);
    }

    @Override
    protected void setListener() {
        rlUpdate.setOnClickListener(this);
        rlQQ.setOnClickListener(this);
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        mContext = getContext();
        tvUpdate.setText(mContext.getResources().getString(R.string.st_about_checking_update));
        tvVersion.setText("礼包酷 " + AppConfig.SDK_VER_NAME);
        tvQQ.setText(MixUtil.getQQInfo()[0]);
    }

    /**
     * 获取最新更新信息的网络请求声明
     */
    private Call<JsonRespBase<UpdateInfo>> mCall;

    @Override
    protected void lazyLoad() {
        if (!NetworkUtil.isConnected(getContext())) {
            setUpdate(mContext.getResources().getString(R.string.st_about_check_update_failed));
            return;
        }
        if (mCall != null) {
            mCall.cancel();
        }
        ReqInitApp data = new ReqInitApp();
        data.curVersionCode = AppInfoUtil.getAppVerCode(getContext());
        JsonReqBase<ReqInitApp> reqData = new JsonReqBase<>(data);
        mCall = Global.getNetEngine().checkUpdate(reqData);
        mCall.enqueue(new Callback<JsonRespBase<UpdateInfo>>() {
            @Override
            public void onResponse(Call<JsonRespBase<UpdateInfo>> call, Response<JsonRespBase<UpdateInfo>> response) {
                if (!mCanShowUI || call.isCanceled()) {
                    return;
                }
                if (response != null && response.isSuccessful() && response.body() != null &&
                        response.body().getCode() == NetStatusCode.SUCCESS) {
                    mUpdateInfo = response.body().getData();
                    if (mUpdateInfo != null && mUpdateInfo.checkoutUpdateInfo(getContext())) {
                        setUpdate(String.format(mContext.getResources().getString(R.string
                                .st_about_wait_update_text), mUpdateInfo.versionName));
                        return;
                    }
                }
                setUpdate(mContext.getResources().getString(R.string.st_about_update_text));
            }

            @Override
            public void onFailure(Call<JsonRespBase<UpdateInfo>> call, Throwable t) {
                if (!mCanShowUI || call.isCanceled()) {
                    return;
                }
                if (AppDebugConfig.IS_DEBUG) {
                    KLog.e(AppDebugConfig.TAG_FRAG, t);
                }
                setUpdate(mContext.getResources().getString(R.string
                        .st_about_check_update_failed));
            }
        });

    }

    private void setUpdate(final String str) {
        ThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (tvUpdate != null) {
                    tvUpdate.setText(str);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.rl_update:
                handleUpdate();
                break;
            case R.id.rl_qq:
                IntentUtil.joinQQGroup(getContext(), MixUtil.getQQInfo()[1]);
                break;
        }
    }

    private void handleUpdate() {
        if (mUpdateInfo == null || !mUpdateInfo.checkoutUpdateInfo(getContext())) {
            return;
        }
        final IndexGameNew appInfo = new IndexGameNew();
        appInfo.id = Global.GIFTCOOL_GAME_ID;
        appInfo.name = getString(R.string.app_name);
        appInfo.apkFileSize = mUpdateInfo.apkFileSize;
        //没icon地址，随便填个
        appInfo.img = mUpdateInfo.downloadUrl;
        appInfo.downloadUrl = mUpdateInfo.downloadUrl;
        appInfo.destUrl = mUpdateInfo.downloadUrl;
        appInfo.packageName = mUpdateInfo.packageName;
        appInfo.versionName = mUpdateInfo.versionName;
        appInfo.size = appInfo.getApkFileSizeStr();
        appInfo.initAppInfoStatus(getContext());
        ConfirmDialog confirmDialog = getUpdateDialog(appInfo, mUpdateInfo.content);
        confirmDialog.show(getFragmentManager(), "update");
    }

    private ConfirmDialog getUpdateDialog(final IndexGameNew appInfo, final String content) {
        final ConfirmDialog confirmDialog = ConfirmDialog.newInstance();
        confirmDialog.setTitle("更新提示");
        confirmDialog.setContent(content);
        confirmDialog.setPositiveBtnText("马上更新");
        confirmDialog.setNegativeBtnText("暂不更新");
        confirmDialog.setListener(new BaseFragment_Dialog.OnDialogClickListener() {
            @Override
            public void onCancel() {
                confirmDialog.dismiss();
            }

            @Override
            public void onConfirm() {
                appInfo.startDownload();
                confirmDialog.dismiss();
            }
        });
        return confirmDialog;
    }

    @Override
    public String getPageName() {
        return PAGE_NAME;
    }
}
