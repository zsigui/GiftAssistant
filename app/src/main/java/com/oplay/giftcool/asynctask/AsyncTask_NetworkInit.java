package com.oplay.giftcool.asynctask;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.SPConfig;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.manager.HotFixManager;
import com.oplay.giftcool.model.data.req.AppBaseInfo;
import com.oplay.giftcool.model.data.req.ReqReportedInfo;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.util.DateUtil;
import com.oplay.giftcool.util.SPUtil;
import com.oplay.giftcool.util.SystemUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 进行网络请求的初始化
 * <p/>
 * Created by zsigui on 16-7-6.
 */
public class AsyncTask_NetworkInit extends AsyncTask<Object, Integer, Void> {

    private Context mContext;
    private HashSet<String> mOldSrcPackage;
    private HashSet<String> mOldSrcName;
    private HashSet<String> mDiffPackage;
    private HashSet<String> mDiffName;

    public AsyncTask_NetworkInit(Context context) {
        mContext = context;
    }

    @Override
    protected Void doInBackground(Object... params) {
        try {

            // 每次登录请求一次更新用户状态和数据
            AccountManager.getInstance().updateUserSession(false);

            HotFixManager.getInstance().requestPatchFromServer();
            Global.setInstalledAppNames(SystemUtil.getInstalledAppName(mContext));
            ArrayList<AppBaseInfo> infos = getAppInfos(mContext);
            // 判断是否今天首次打开APP
            if (judgeFirstOpenToday()) {
                // 进行应用信息上报
                reportedAppInfo(infos);
            }

        } catch (Throwable e) {
            e.printStackTrace();
            AppDebugConfig.d(AppDebugConfig.TAG_APP, e);
        }
        return null;
    }

    /**
     * 判断是否今天首次登录
     */
    public boolean judgeFirstOpenToday() {
        long lastOpenTime = SPUtil.getLong(mContext, SPConfig.SP_USER_INFO_FILE, SPConfig.KEY_LOGIN_LATEST_OPEN_TIME,
                0);
        // 首次打开APP 或者 今天首次登录
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
        String nameSrc = SPUtil.getString(context, SPConfig.SP_APP_INFO_FILE, SPConfig.KEY_INSTALL_SRC_APP_NAMES, "");
        if (TextUtils.isEmpty(nameSrc)) {
            mOldSrcName = new HashSet<>();
        } else {
            mOldSrcName = AssistantApp.getInstance().getGson()
                    .fromJson(nameSrc, new TypeToken<HashSet<String>>() {
                    }.getType());
        }
        String packageSrc = SPUtil.getString(context, SPConfig.SP_APP_INFO_FILE, SPConfig
                .KEY_INSTALL_SRC_PACKAGE_NAMES, "");
        if (TextUtils.isEmpty(packageSrc)) {
            mOldSrcPackage = new HashSet<>();
        } else {
            mOldSrcPackage = AssistantApp.getInstance().getGson()
                    .fromJson(packageSrc, new TypeToken<HashSet<String>>() {
                    }.getType());
        }

        mDiffName = Global.getInstalledAppNames();
        mDiffPackage = Global.getInstalledPackageNames();

        HashSet<String> newSrcName = new HashSet<>();
        HashSet<String> newSrcPackage = new HashSet<>();

        final ArrayList<AppBaseInfo> result = new ArrayList<>();
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(0);
        for (PackageInfo packageInfo : packages) {
            try {
                final AppBaseInfo info = new AppBaseInfo();
                info.name = packageInfo.applicationInfo.loadLabel(context.getPackageManager()).toString();
                info.pkg = packageInfo.packageName;
                info.vc = String.valueOf(packageInfo.versionCode);
                info.vn = packageInfo.versionName;
                result.add(info);

                newSrcName.add(info.name);
                newSrcPackage.add(info.pkg);

            } catch (Throwable e) {
                AppDebugConfig.w(AppDebugConfig.TAG_APP, e);
            }
        }

        HashSet<String> nameSetCopy = new HashSet<>(newSrcName);
        HashSet<String> packageSetCopy = new HashSet<>(newSrcPackage);

        // 将原始集合写入文件
        Gson gson = AssistantApp.getInstance().getGson();
        SPUtil.putString(context, SPConfig.SP_APP_INFO_FILE, SPConfig.KEY_INSTALL_SRC_APP_NAMES,
                gson.toJson(newSrcName));
        SPUtil.putString(context, SPConfig.SP_APP_INFO_FILE, SPConfig.KEY_INSTALL_SRC_PACKAGE_NAMES,
                gson.toJson(newSrcPackage));

        // new剩余新增的文件集合
        newSrcName.removeAll(mOldSrcName);
        newSrcPackage.removeAll(mOldSrcPackage);

        // old剩余被删除的文件集合
        mOldSrcPackage.removeAll(packageSetCopy);
        // 已差分的减去被删除的
        mDiffPackage.removeAll(mOldSrcPackage);
        // 已差分的添加新增加的
        mDiffPackage.addAll(newSrcPackage);

        mOldSrcName.removeAll(nameSetCopy);
        mDiffName.removeAll(mOldSrcName);
        mDiffName.addAll(newSrcName);

        Global.setInstalledAppNames(mDiffName);
        Global.setInstalledPackageNames(mDiffPackage);

        return result;
    }
}
