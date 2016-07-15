package com.oplay.giftcool.asynctask;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.config.AppConfig;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.SPConfig;
import com.oplay.giftcool.download.ApkDownloadManager;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.manager.OuwanSDKManager;
import com.oplay.giftcool.manager.PushMessageManager;
import com.oplay.giftcool.model.data.req.ReqInitApp;
import com.oplay.giftcool.model.data.resp.InitAppConfig;
import com.oplay.giftcool.model.data.resp.InitAppResult;
import com.oplay.giftcool.model.data.resp.UserModel;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.util.AppInfoUtil;
import com.oplay.giftcool.util.CommonUtil;
import com.oplay.giftcool.util.FileUtil;
import com.oplay.giftcool.util.SPUtil;
import com.oplay.giftcool.util.ThreadUtil;
import com.oplay.giftcool.util.log.GCLog;

import net.youmi.android.libs.common.global.Global_SharePreferences;

import java.io.File;

import retrofit2.Response;


/**
 * AsyncTask_InitApplication
 *
 * @author zacklpx
 *         date 16-1-14
 *         description
 */
public class AsyncTask_InitApplication extends AsyncTask<Object, Integer, Void> {
    private Context mContext;

    public AsyncTask_InitApplication(Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    protected Void doInBackground(Object... params) {
        try {
            //TODO异步初始化操作
            doInit();
            // 初始化下载列表
            ApkDownloadManager.getInstance(mContext).initDownloadList();
        } catch (Throwable e) {
            AppDebugConfig.w(AppDebugConfig.TAG_APP, e);
        }
        return null;
    }

    /**
     * 在此处进行对旧版的处理操作
     */
    public void doClearWorkForOldVer() {
        int oldVer = SPUtil.getInt(mContext, SPConfig.SP_APP_CONFIG_FILE, SPConfig.KEY_STORE_VER, 0);
        if (oldVer != AppConfig.SDK_VER) {
            if (oldVer < 3) {
                // 清除旧版的账号存储信息
                SPUtil.putString(mContext, SPConfig.SP_USER_INFO_FILE, SPConfig.KEY_LOGIN_PHONE, "");
                SPUtil.putString(mContext, SPConfig.SP_USER_INFO_FILE, SPConfig.KEY_LOGIN_OUWAN, "");
            }
            // 写入最新版本信息
            SPUtil.putInt(mContext, SPConfig.SP_APP_CONFIG_FILE, SPConfig.KEY_STORE_VER, AppConfig.SDK_VER);
            // 清空今日登录状态
            SPUtil.putLong(mContext, SPConfig.SP_USER_INFO_FILE, SPConfig.KEY_LOGIN_LATEST_OPEN_TIME, 0);
            AssistantApp.getInstance().setFirstOpenInThisVersion(true);
        }
    }



    /**
     * 需要在此完成一些APP全局常量初始化的获取工作
     */
    private void doInit() {
        final AssistantApp assistantApp = AssistantApp.getInstance();
//		if (assistantApp.isGlobalInit()) {
//			return;
//		}

        readAndSetDebugConfig();

        AppDebugConfig.v(AppDebugConfig.TAG_APP, "app has global initialed");

        // 存储打开APP时间
//        SPUtil.putLong(assistantApp, SPConfig.SP_APP_CONFIG_FILE,
//                SPConfig.KEY_LAST_OPEN_APP_TIME, System.currentTimeMillis());

        // 初始化照片墙控件
        assistantApp.initGalleryFinal();

//        testDownload();

        // 初始化设备配置
        assistantApp.initAppConfig();
        // 初始化设备状态
        CommonUtil.initMobileInfoModel(mContext);

        // 初始化网络下载模块
        assistantApp.initRetrofit();
        Global.resetNetEngine();

        // 初始化配置，获取更新信息
        if (!initAndCheckUpdate()) {
            AppDebugConfig.d("initAndCheckUpdate failed!");
        }

        doClearWorkForOldVer();
        Global.getInstalledAppNames();

        try {
            OuwanSDKManager.getInstance().init();
        } catch (Exception e) {
            AppDebugConfig.w(AppDebugConfig.TAG_APP, e);
        }


// 获取用户信息
        // 该信息使用salt加密存储再SharedPreference中
        UserModel user = null;
        try {
            String userJson = Global_SharePreferences.getStringFromSharedPreferences(mContext,
                    SPConfig.SP_USER_INFO_FILE, SPConfig.KEY_USER_INFO, SPConfig.SALT_USER_INFO, null);
            AppDebugConfig.d(AppDebugConfig.TAG_APP, "get from sp: user = " + userJson);
            user = AssistantApp.getInstance().getGson().fromJson(userJson, UserModel.class);
            if (user != null && user.userInfo != null) {
                // 将首次登录状态清掉，再次获取已经不属于首次登录
                user.userInfo.isFirstLogin = false;
            }
        } catch (Exception e) {
            AppDebugConfig.w(AppDebugConfig.TAG_APP, e);
        }
        final UserModel finalUser = user;
        ThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AccountManager.getInstance().notifyUserAll(finalUser);
            }
        });
        // 每次登录请求一次更新用户状态和数据
        AccountManager.getInstance().updateUserSession();

        // 初始化配置，获取更新信息
        if (!initAndCheckUpdate()) {
            AppDebugConfig.d("initAndCheckUpdate failed!");
        }
        // 初始化推送SDK
        PushMessageManager.getInstance().initPush(assistantApp);

        assistantApp.setGlobalInit(true);
    }

    /**
     * 读取 /sdcard/gift_cool/cache/dl 文件，值为boolean，为0/1
     * 文件格式需要为：
     * =========文件开始=========
     * LOG_DEBUG=1
     * FILE_DEBUG=0
     * =========文件结束=========
     */
    private void readAndSetDebugConfig() {
        File configFile = new File(FileUtil.getOwnCacheDirectory(mContext, Global.EXTERNAL_CACHE, true),
                Global.DEBUG_CONFIG);
        if (!configFile.exists() || !configFile.canRead()) {
            return;
        }
        try {
            String config = FileUtil.readString(configFile, FileUtil.DEFAULT_CHASET);
            AppDebugConfig.d(AppDebugConfig.TAG_DEBUG_INFO, "read debug config : \n" + config);
            String[] map = config.split("\n");
            for (String entry : map) {
                if (TextUtils.isEmpty(entry) || !entry.contains("=")) {
                    continue;
                }
                String[] keyValue = entry.split("=");
                if ("LOG_DEBUG".equals(keyValue[0])) {
                    AppDebugConfig.IS_DEBUG = !TextUtils.isEmpty(keyValue[1]) && "1".equals(keyValue[1]);
                    GCLog.init(AppDebugConfig.IS_DEBUG);
                } else if ("FILE_DEBUG".equals(keyValue[0])) {
                    AppDebugConfig.IS_FILE_DEBUG = !TextUtils.isEmpty(keyValue[1]) && "1".equals(keyValue[1]);
                }
            }

        } catch (Throwable t) {
            AppDebugConfig.w(AppDebugConfig.TAG_DEBUG_INFO, t);
        }
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
                            InitAppConfig config = initData.initAppConfig;
                            AssistantApp.getInstance().setAllowDownload(config.isShowDownload);
                            AssistantApp.getInstance().setQQInfo(config.qqInfo);
                            AssistantApp.getInstance().setStartImg(config.startImgUrl);
                            AssistantApp.getInstance().setBroadcastBanner(config.broadcastBanner);
                            AssistantApp.getInstance().setPhoneLoginType(config.phoneLoginType);
                            AssistantApp.getInstance().setPushSdk(config.pushSdk);
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

}
