package com.oplay.giftcool.manager;

import com.alipay.euler.andfix.patch.PatchManager;
import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.config.AppConfig;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.download.silent.SilentDownloadManager;
import com.oplay.giftcool.download.silent.bean.DownloadInfo;
import com.oplay.giftcool.model.data.req.ReqPatchInfo;
import com.oplay.giftcool.model.data.resp.GameDownloadInfo;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.util.FileUtil;

import net.youmi.android.libs.common.coder.Coder_Md5;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author JackieZhuang
 * @email zsigui@foxmail.com
 * @date 2016/7/2
 */
public class HotFixManager {

    private static class SingletonHolder {
        static final HotFixManager sInstance = new HotFixManager();
    }

    private HotFixManager() {
        mPatchManager = new PatchManager(AssistantApp.getInstance());
        mPatchManager.init(String.valueOf(AppConfig.SDK_VER));
        AppDebugConfig.d(AppDebugConfig.TAG_MANAGER, "初始化HotFixManager实例");
    }

    public static HotFixManager getInstance() {
        return SingletonHolder.sInstance;
    }

    private PatchManager mPatchManager = null;

    public void requestPatchFromServer() {
        AppDebugConfig.d(AppDebugConfig.TAG_MANAGER, "请求补丁文件，当前版本号：" + AppConfig.SDK_VER);
        ReqPatchInfo info = new ReqPatchInfo();
        info.verCode = AppConfig.SDK_VER;
        Global.getNetEngine().requestPatch(new JsonReqBase<ReqPatchInfo>(info))
                .enqueue(new Callback<JsonRespBase<GameDownloadInfo>>() {
                    @Override
                    public void onResponse(final Call<JsonRespBase<GameDownloadInfo>> call, Response<JsonRespBase<GameDownloadInfo>> response) {
                        if (response != null && response.isSuccessful()
                                && response.body() != null && response.body().isSuccess()) {
                            GameDownloadInfo data = response.body().getData();
                            if (data != null) {
                                DownloadInfo info = new HotFixDownloadInfo();
                                info.setDestUrl(data.destUrl);
                                info.setDownloadUrl(data.downloadUrl);
                                info.setMd5Sum(data.apkMd5);
                                info.setTotalSize(data.completeSize);
                                info.setListener(new HotFixDownloadListener());
                                // 下载补丁文件
                                SilentDownloadManager.getInstance().startDownload(info);
                            }
                            return;
                        }
                        AppDebugConfig.warnResp(AppDebugConfig.TAG_MANAGER, response);
                    }

                    @Override
                    public void onFailure(Call<JsonRespBase<GameDownloadInfo>> call, Throwable t) {
                        AppDebugConfig.w(AppDebugConfig.TAG_MANAGER, t);
                    }
                });
        test();
    }

    public void test() {
        try {
            AppDebugConfig.d(AppDebugConfig.TAG_WARN, "当前版本：" + AppConfig.SDK_VER);
            mPatchManager.addPatch(FileUtil.getOwnCacheDirectory(AssistantApp.getInstance(),
                    Global.EXTERNAL_DOWNLOAD, true).getAbsolutePath());
            AppDebugConfig.d(AppDebugConfig.TAG_WARN, "升级后版本：" + AppConfig.SDK_VER);
        } catch (IOException e) {
            AppDebugConfig.w(AppDebugConfig.TAG_WARN, "测试");
        }
    }

    static class HotFixDownloadInfo extends DownloadInfo{
        @Override
        public String getStoreFileName() {
            return Coder_Md5.md5(getDownloadUrl()) + ".apatch";
        }
    }

    /**
     * 补丁文件下载监听函数
     */
    static class HotFixDownloadListener implements DownloadInfo.DownloadListener {
        @Override
        public void onProgressUpdate(DownloadInfo info, int elapsedTime) {

        }

        @Override
        public void onFinishDownload(DownloadInfo info) {
            // 下载完成，将Patch转移，添加执行Patch
            try {
                AppDebugConfig.d(AppDebugConfig.TAG_MANAGER, "补丁包下载完成，开始执行补丁加载逻辑");
                // PatchManager 本身会在 init() 判断并删除旧版本的补丁包
//                HotFixManager.getInstance().mPatchManager.removeAllPatch();
                HotFixManager.getInstance().mPatchManager.addPatch(
                        SilentDownloadManager.getInstance()
                                .concatDownloadFilePath(info.getStoreFileName())
                                .getAbsolutePath()
                );
                AppDebugConfig.d(AppDebugConfig.TAG_MANAGER, "补丁包加载完成");
            } catch (Throwable t) {
                AppDebugConfig.w(AppDebugConfig.TAG_MANAGER, t);
            }
        }

        @Override
        public void onFailDownload(DownloadInfo info, String err) {

        }
    }
}
