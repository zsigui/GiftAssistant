package com.oplay.giftcool.asynctask;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.SPConfig;
import com.oplay.giftcool.manager.HotFixManager;
import com.oplay.giftcool.model.data.req.AppBaseInfo;
import com.oplay.giftcool.model.data.req.ReqInitApp;
import com.oplay.giftcool.model.data.req.ReqReportedInfo;
import com.oplay.giftcool.model.data.resp.InitAppResult;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.util.AppInfoUtil;
import com.oplay.giftcool.util.DateUtil;
import com.oplay.giftcool.util.SPUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 进行网络请求的初始化
 *
 * Created by zsigui on 16-7-6.
 */
public class AsyncTask_NetworkInit extends AsyncTask<Object, Integer, Void> {

    private Context mContext;

    public AsyncTask_NetworkInit(Context context) {
        mContext = context;
    }

    @Override
    protected Void doInBackground(Object... params) {
        try {
            HotFixManager.getInstance().requestPatchFromServer();
            // 判断是否今日首次打开APP
            ArrayList<AppBaseInfo> infos = getAppInfos(mContext);
            if (judgeFirstOpenToday()) {
                // 进行应用信息上报
                reportedAppInfo(infos);
            }

            // 初始化配置，获取更新信息
            if (!initAndCheckUpdate()) {
                AppDebugConfig.d("initAndCheckUpdate failed!");
            }

        } catch (Throwable e) {
            e.printStackTrace();
            AppDebugConfig.d(AppDebugConfig.TAG_APP, e);
        }
        return null;
    }

    private boolean initAndCheckUpdate() {
        ReqInitApp data = new ReqInitApp();
        data.curVersionCode = AppInfoUtil.getAppVerCode(mContext);
        JsonReqBase<ReqInitApp> reqData = new JsonReqBase<>(data);
        try {
            Response<JsonRespBase<InitAppResult>> response = Global.getNetEngine().initAPP(reqData).execute();
            if (response != null && response.isSuccessful()) {
                if (response.body() != null && response.body().isSuccess()) {
                    InitAppResult initData = response.body().getData();
                    if (initData != null) {
                        if (initData.initAppConfig != null) {
                            AssistantApp.getInstance().setAllowDownload(initData.initAppConfig
                                    .isShowDownload);
                            AssistantApp.getInstance().setQQInfo(initData.initAppConfig.qqInfo);
                            AssistantApp.getInstance().setStartImg(initData.initAppConfig
                                    .startImgUrl);
                            AssistantApp.getInstance().setBroadcastBanner(initData.initAppConfig
                                    .broadcastBanner);
                            AssistantApp.getInstance().setPhoneLoginType(initData.initAppConfig.phoneLoginType);
                        }
                        if (initData.updateInfo != null) {
                            AssistantApp.getInstance().setUpdateInfo(initData.updateInfo);
                        }
                        return true;
                    }
                }
            }
            AppDebugConfig.warnResp(AppDebugConfig.TAG_APP, response);
        } catch (Throwable e) {
            AppDebugConfig.w(AppDebugConfig.TAG_APP, e);
        }
        return false;
    }

    /**
     * 判断是否今日首次登录
     */
    public boolean judgeFirstOpenToday() {
        long lastOpenTime = SPUtil.getLong(mContext, SPConfig.SP_USER_INFO_FILE, SPConfig.KEY_LOGIN_LATEST_OPEN_TIME,
                0);
        // 首次打开APP 或者 今日首次登录
        SPUtil.putLong(mContext, SPConfig.SP_USER_INFO_FILE, SPConfig.KEY_LOGIN_LATEST_OPEN_TIME, System
                .currentTimeMillis());
        return (lastOpenTime == 0 || !DateUtil.isToday(lastOpenTime));
    }

    /**
     * 上报应用信息
     */
    private void reportedAppInfo(ArrayList<AppBaseInfo> appBaseInfos) {
        ReqReportedInfo info = new ReqReportedInfo();
        info.brand = Build.MODEL;
        info.osVersion = Build.VERSION.RELEASE;
        info.sdkVersion = String.valueOf(Build.VERSION.SDK_INT);
        info.appInfos = appBaseInfos;
        final JsonReqBase<ReqReportedInfo> reqData = new JsonReqBase<ReqReportedInfo>(info);
        Global.getNetEngine().reportedAppInfo(reqData)
                .enqueue(new Callback<JsonRespBase<Void>>() {
                    @Override
                    public void onResponse(Call<JsonRespBase<Void>> call, Response<JsonRespBase<Void>>
                            response) {
                        if (response != null && response.isSuccessful()
                                && response.body() != null && response.body().isSuccess()) {
                            // 执行上报成功
                            AppDebugConfig.d(AppDebugConfig.TAG_APP, "信息上报成功");
                            return;
                        }
                        AppDebugConfig.warnResp(AppDebugConfig.TAG_APP, response);
//								ThreadUtil.runOnUiThread(new Runnable() {
//									@Override
//									public void run() {
//										reportedAppInfo();
//									}
//								}, 30 * 1000);
                    }

                    @Override
                    public void onFailure(Call<JsonRespBase<Void>> call, Throwable t) {
                        AppDebugConfig.w(AppDebugConfig.TAG_APP, t);
                        // 上报失败，等待30秒后继续执行
//								ThreadUtil.runOnUiThread(new Runnable() {
//									@Override
//									public void run() {
//										reportedAppInfo();
//									}
//								}, 30 * 1000);
                    }
                });
    }

    /**
     * 获取已安装应用信息
     */
    private ArrayList<AppBaseInfo> getAppInfos(Context context) {
        HashSet<String> appNames = new HashSet<>();

        final ArrayList<AppBaseInfo> result = new ArrayList<>();
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(0);
        for (PackageInfo packageInfo : packages) {
            try {
                final AppBaseInfo info = new AppBaseInfo();
                info.name = pm.getApplicationLabel(pm.getApplicationInfo(packageInfo.packageName,
                        PackageManager.GET_META_DATA)).toString();
                appNames.add(info.name);
                info.pkg = packageInfo.packageName;
                info.vc = String.valueOf(packageInfo.versionCode);
                info.vn = packageInfo.versionName;
                result.add(info);
            } catch (Throwable e) {
                AppDebugConfig.w(AppDebugConfig.TAG_APP, e);
            }
        }

        Global.setInstalledAppNames(appNames);
        return result;
    }
}
