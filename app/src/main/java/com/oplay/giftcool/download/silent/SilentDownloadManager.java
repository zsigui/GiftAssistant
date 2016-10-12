package com.oplay.giftcool.download.silent;

import android.content.Context;
import android.text.TextUtils;

import com.nostra13.universalimageloader.utils.StorageUtils;
import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.download.ApkDownloadManager;
import com.oplay.giftcool.download.silent.bean.DownloadInfo;
import com.oplay.giftcool.manager.ObserverManager;
import com.oplay.giftcool.util.FileUtil;
import com.oplay.giftcool.util.NetworkUtil;

import net.youmi.android.libs.common.coder.Coder_Md5;
import net.youmi.android.libs.common.util.Util_System_File;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by zsigui on 16-4-25.
 */
public class SilentDownloadManager implements ObserverManager.UserActionListener {

    private ConcurrentHashMap<String, DownloadInfo> mTotalDownloadMap;
    private LinkedBlockingQueue<DownloadInfo> mWaitDownloadQueue;

    private final String DEFAULT_ENCODE = "UTF-8";
    private final String CONFIG_FILE = "silent.download.config";
    private final String T_START = "t_start";
    private final String T_FILENAME = "t_filename";
    private final String T_DEST_URL = "t_dest_url";
    private final String T_DOWNLOAD_URL = "t_download_url";
    private final String T_TOTAL_SIZE = "t_total_size";
    private final String T_DOWNLOAD_SIZE = "t_download_size";
    private final String T_MD5_SUM = "t_md5_sum";
    private final String T_END = "t_end";

    private String mDirPath;
    private Context mContext;
    /**
     * 是否处于下载中
     */

    private static SilentDownloadManager mInstance;

    // 用于判断是否不管网络是否处于wifi下都进行下载
    private boolean mForceDownload = false;
    private DownloadThread[] mThreads;
    private boolean mIsRunning = false;

    public static SilentDownloadManager getInstance() {
        if (mInstance == null) {
            mInstance = new SilentDownloadManager(AssistantApp.getInstance().getApplicationContext(),
                    Global.EXTERNAL_DOWNLOAD);
        }
        return mInstance;
    }


    private SilentDownloadManager(Context context, String dirPath) {
        mTotalDownloadMap = new ConcurrentHashMap<>();
        mWaitDownloadQueue = new LinkedBlockingQueue<>();
        initDownloadTasks(context, dirPath);
        mContext = context;
        ObserverManager.getInstance().addUserActionListener(this);
    }

    /**
     * 判断下载任务是否存在<br />
     * 判断逻辑：<br />
     *
     * @return
     */
    public boolean existsDownloadTask(String url) {
        if (mTotalDownloadMap == null) {
            mTotalDownloadMap = new ConcurrentHashMap<>();
        }
        return mTotalDownloadMap.containsKey(Coder_Md5.md5(url));
    }

    public File concatDownloadFilePath(String filename) {
        if (TextUtils.isEmpty(mDirPath))
            return null;
        return new File(mDirPath, filename);
    }

    /**
     * 初始化下载配置
     */
    private void initDownloadTasks(Context context, String dirPath) {
        AppDebugConfig.d(AppDebugConfig.TAG_DOWNLOAD, "初始化下载任务列表");
        File dirFile = StorageUtils.getOwnCacheDirectory(context, dirPath);
        if (!FileUtil.mkdirs(dirFile)) {
            AppDebugConfig.d(AppDebugConfig.TAG_DOWNLOAD, "failed to create dirPath : " + dirPath);
            return;
        }
        mIsRunning = false;
        mDirPath = dirFile.getAbsolutePath();
        File configFile = new File(mDirPath, CONFIG_FILE);
        mTotalDownloadMap.putAll(readConfigFile(configFile));
        mWaitDownloadQueue.addAll(mTotalDownloadMap.values());
    }

    public synchronized void startDownload(DownloadInfo info) {
        AppDebugConfig.d(AppDebugConfig.TAG_DOWNLOAD, "开始下载：" + info.getDownloadUrl());
        addDownload(info);
        startDownload();
    }


    public void resetDownloadDir() {
        mDirPath = StorageUtils.getOwnCacheDirectory(mContext, Global.EXTERNAL_DOWNLOAD).getAbsolutePath();
    }

    /**
     * 开启下载线程
     */
    public synchronized void startDownload() {
        if (mIsRunning || mTotalDownloadMap.isEmpty()) {
            AppDebugConfig.d(AppDebugConfig.TAG_DOWNLOAD, "下载器状态: " + mIsRunning + ", 当前下载列表数量: " +
                    mTotalDownloadMap.size());
            return;
        }
        AppDebugConfig.d(AppDebugConfig.TAG_DOWNLOAD, "开始执行下载线程！");
        for (Iterator<Map.Entry<String, DownloadInfo>> it = mTotalDownloadMap.entrySet().iterator(); it.hasNext(); ) {
            final Map.Entry<String, DownloadInfo> entry = it.next();
            final DownloadInfo info = entry.getValue();
            if (ApkDownloadManager.getInstance(mContext).contains(entry.getKey())) {
                it.remove();
                info.setIsDownload(false);
                mWaitDownloadQueue.remove(info);
            } else {
                if (!mWaitDownloadQueue.contains(info)) {
                    info.setIsDownload(true);
                    mWaitDownloadQueue.add(info);
                }
            }
        }
        if (!mForceDownload && !NetworkUtil.isWifiConnected(mContext)) {
            AppDebugConfig.d(AppDebugConfig.TAG_DOWNLOAD, "正处于非WIFI网络下，不进行强制下载……");
            return;
        }
        if (mThreads == null) {
            mThreads = new DownloadThread[1];
        }
        for (int i = 0; i < mThreads.length; i++) {
            // 执行线程下载
            if (mThreads[i] == null || mThreads[i].isStop()) {
                mThreads[i] = new DownloadThread(mDirPath, mWaitDownloadQueue);
                mThreads[i].setTag("T" + i);
                mThreads[i].start();
            }
        }
        mIsRunning = true;
    }

    public void setForceDownload(boolean forceDownload) {
        mForceDownload = forceDownload;
    }

    /**
     * 读取下载配置文件的信息，然后存储到Map中
     *
     * @param configFile
     * @return
     */
    private ConcurrentHashMap<String, DownloadInfo> readConfigFile(File configFile) {
        ConcurrentHashMap<String, DownloadInfo> result = new ConcurrentHashMap<>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(configFile),
                    DEFAULT_ENCODE));
            String line;
            DownloadInfo info = null;
            while ((line = br.readLine()) != null) {
                // 读取文件结构：
                // t_start
                // t_download_url=http://xxxx
                // t_dest_url=http://xxxx
                // t_end
                if (line.startsWith(T_START)) {
                    info = new DownloadInfo();
                } else if (info != null) {
                    if (line.startsWith(T_DOWNLOAD_URL)) {
                        info.setDownloadUrl(line.substring(T_DOWNLOAD_URL.length() + 1));
                    } else if (line.startsWith(T_DEST_URL)) {
                        info.setDestUrl(line.substring(T_DEST_URL.length() + 1));
                    } else if (line.startsWith(T_TOTAL_SIZE)) {
                        info.setTotalSize(Long.parseLong(line.substring(T_TOTAL_SIZE.length() + 1)));
                    } else if (line.startsWith(T_MD5_SUM)) {
                        info.setMd5Sum(line.substring(T_MD5_SUM.length() + 1));
                    } else if (line.startsWith(T_DOWNLOAD_SIZE)) {
                        info.setDownloadSize(Long.parseLong(line.substring(T_DOWNLOAD_SIZE.length() + 1)));
                    } else if (line.startsWith(T_END)) {
                        if (isValid(info)) {
                            result.put(info.getDownloadUrl(), info);
                        }
                        info = null;
                    }
                }
            }
            br.close();
            AppDebugConfig.d(AppDebugConfig.TAG_DOWNLOAD, "成功从config文件读取文件");
        } catch (IOException e) {
            AppDebugConfig.w(AppDebugConfig.TAG_DOWNLOAD, e);
        }
        return result;
    }

    /**
     * 写入下载信息到配置文件中，以待下次开启时读取
     */
    private void writeConfigFile(File configFile, Map<String, DownloadInfo> mDownloadMap) {
        try {
            if (configFile.exists() && configFile.isFile()) {
                Util_System_File.delete(configFile);
            }
            BufferedWriter bw = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(configFile), DEFAULT_ENCODE));
            for (DownloadInfo info : mDownloadMap.values()) {
                bw.write(String.format("%s\n", T_START));
                bw.write(String.format("%s:%s\n", T_DOWNLOAD_URL, info.getDownloadUrl()));
                bw.write(String.format("%s:%s\n", T_DEST_URL, info.getDestUrl()));
                bw.write(String.format("%s:%s\n", T_TOTAL_SIZE, info.getTotalSize()));
                bw.write(String.format("%s:%s\n", T_DOWNLOAD_SIZE, info.getDownloadSize()));
                bw.write(String.format("%s:%s\n", T_MD5_SUM, info.getMd5Sum()));
                bw.write(String.format("%s\n", T_END));
                bw.flush();
            }
            bw.close();
            AppDebugConfig.d(AppDebugConfig.TAG_DOWNLOAD, "成功写入数据到config文件");
        } catch (IOException e) {
            AppDebugConfig.w(AppDebugConfig.TAG_DOWNLOAD, e);
        }

    }

    /**
     * 获取待执行的下载任务
     */
//     DownloadInfo obtainDownload() {
//      AppDebugConfig.d(AppDebugConfig.TAG_DOWNLOAD, "获取下载: " + mWaitDownloadQueue.size());
//        if (mWaitDownloadQueue.size() > 0) {
//            try {
//                return mWaitDownloadQueue.take();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//        return null;
//    }

    /**
     * 移除已经完成或者取消的下载任务
     */
    public synchronized void removeDownload(DownloadInfo info, boolean removeTemp) {
        if (info != null) {
            if (removeTemp) {
                final File tempFile = new File(mDirPath, info.getTempFileName());
                if (tempFile.exists() && tempFile.isFile()) {
                    Util_System_File.delete(tempFile);
                }
            }
            quickDownload(info.getDownloadUrl());
            mWaitDownloadQueue.remove(info);
        }
    }

    /**
     * 添加新的下载任务
     */
    public synchronized void addDownload(DownloadInfo info) {
        if (info != null) {
            if (ApkDownloadManager.getInstance(mContext).contains(info.getDownloadUrl())) {
                // 存在于正常下载列表中
                AppDebugConfig.d(AppDebugConfig.TAG_DOWNLOAD, "添加的任务已经存在下载");
                return;
            }
            if (isValid(info)) {
                if (!mTotalDownloadMap.containsKey(info.getDownloadUrl())) {
                    AppDebugConfig.d(AppDebugConfig.TAG_DOWNLOAD, "添加新的下载任务：" + info.getDownloadUrl());
                    final File storeFile = new File(mDirPath, info.getStoreFileName());
                    if (!storeFile.exists()) {
                        info.setIsDownload(true);
                        if (!mTotalDownloadMap.containsKey(info.getDownloadUrl())) {
                            mTotalDownloadMap.put(info.getDownloadUrl(), info);
                        }
                        if (!mWaitDownloadQueue.contains(info)) {
                            mWaitDownloadQueue.add(info);
                        }
                    }
                } else if (info.getListener() != null){
                    mTotalDownloadMap.get(info.getDownloadUrl()).setListener(info.getListener());
                }
            }
        }
    }

    /**
     * 判断该下载任务是否已经存在
     */
    public boolean contains(String downloadUrl) {
        return mTotalDownloadMap.containsKey(downloadUrl);
    }

    /**
     * 取消可能存在的下载任务的下载行为
     */
    public void quickDownload(String downloadUrl) {
        if (contains(downloadUrl)) {
            DownloadInfo info = mTotalDownloadMap.get(downloadUrl);
            info.setIsDownload(false);
            mTotalDownloadMap.remove(downloadUrl);
            writeConfigFile(new File(mDirPath, CONFIG_FILE), mTotalDownloadMap);
        }
    }

    /**
     * 移除所有的下载任务，即是清除下载，会把对应的下载缓存文件一起删除 <br />
     * 已经下载完成的由于已经移除队列，不会被处理
     */
    public synchronized void removeAllDownload() {
        stopThreadRunning();
        Iterator<Map.Entry<String, DownloadInfo>> it = mTotalDownloadMap.entrySet().iterator();
        while (it.hasNext()) {
            final DownloadInfo info = it.next().getValue();
            final File tempFile = new File(info.getTempFileName());
            if (tempFile.exists() && tempFile.isFile()) {
                Util_System_File.delete(tempFile);
            }
            info.setIsDownload(false);
            it.remove();
        }
        mWaitDownloadQueue.clear();
        writeConfigFile(new File(mDirPath, CONFIG_FILE), mTotalDownloadMap);
    }

    private boolean mIsBeingStop = false;

    /**
     * 停止所有的下载任务，即是取消所有下载的网络请求 <br />
     * 此时会记录
     */
    public synchronized void stopAllDownload() {
        if (mIsBeingStop || !mIsRunning) {
            AppDebugConfig.d(AppDebugConfig.TAG_DOWNLOAD, "正处于正在暂停或已停止运行状态，不操作");
            return;
        }
        AppDebugConfig.d(AppDebugConfig.TAG_DOWNLOAD, "停止所有下载任务");
        mIsBeingStop = true;
        for (DownloadInfo info : mTotalDownloadMap.values()) {
            AppDebugConfig.d(AppDebugConfig.TAG_DOWNLOAD, info.getTempFileName() + " 设置停止状态");
            info.setIsDownload(false);
        }
        stopThreadRunning();
        mWaitDownloadQueue.clear();
        writeConfigFile(new File(mDirPath, CONFIG_FILE), mTotalDownloadMap);
        mIsBeingStop = false;
        AppDebugConfig.d(AppDebugConfig.TAG_DOWNLOAD, "当前下载器状态:  " + mIsRunning);
    }

    private void stopThreadRunning() {
        if (mThreads != null) {
            for (int i = 0; i < mThreads.length; i++) {
                if (mThreads[i] != null) {
                    mThreads[i].setIsStop(true);
                    mThreads[i] = null;
//                    t.interrupt();
                }
            }
        }
        mIsRunning = false;
    }

    public void judgeIsRunning() {
        if (mThreads != null) {
            for (DownloadThread t : mThreads) {
                if (t != null && !t.isStop()) {
                    mIsRunning = true;
                    break;
                }
            }
        }
        mIsRunning = false;
    }

    /**
     * 判断传入的下载对象是否有效 <br />
     * 至少需要具备下载地址和目标地址
     */
    private boolean isValid(DownloadInfo info) {
        boolean isValid = !TextUtils.isEmpty(info.getDownloadUrl()) && !TextUtils.isEmpty(info.getDestUrl());
        if (isValid) {
            final File storeFile = new File(mDirPath, info.getStoreFileName());
            if (storeFile.exists() && storeFile.isFile()) {
                // 文件已经下载完成，不做处理
                AppDebugConfig.d(AppDebugConfig.TAG_DOWNLOAD, "apk has been downloaded");
                isValid = false;
            } else {
                AppDebugConfig.d(AppDebugConfig.TAG_DOWNLOAD, "apk has not been downloaded");
                final File tempFile = new File(mDirPath, info.getTempFileName());
                if (tempFile.exists() && tempFile.isFile()) {
                    AppDebugConfig.d(AppDebugConfig.TAG_DOWNLOAD, "tempFile has exist : " + tempFile.length());
                    info.setDownloadSize(tempFile.length());
                }
            }
        }
        return isValid;
    }

    public void onProgressUpdate(DownloadInfo info, int elapse_time) {
        AppDebugConfig.d(AppDebugConfig.TAG_DOWNLOAD, "-------------onProgressUpdate----------------");
        AppDebugConfig.d(AppDebugConfig.TAG_DOWNLOAD, "下载地址：" + info.getDownloadUrl());
        AppDebugConfig.d(AppDebugConfig.TAG_DOWNLOAD, "当前下载： 已完成-" + (info.getDownloadSize() / 1024)
                + "KB，总大小-" + (info.getTotalSize() / 1024) + "KB");
        AppDebugConfig.d(AppDebugConfig.TAG_DOWNLOAD, "下载进度: " + (info.getDownloadSize() * 100 / info.getTotalSize())
                + "%");
        AppDebugConfig.d(AppDebugConfig.TAG_DOWNLOAD, "---------------------------------------------");
        if (!NetworkUtil.isWifiAvailable(AssistantApp.getInstance().getApplicationContext())) {
            // 如果非Wifi条件下，停止下载
            AppDebugConfig.d(AppDebugConfig.TAG_DOWNLOAD, "当前不处于Wifi下，停止后台下载");
            stopAllDownload();
        }
    }

    @Override
    public void onUserActionFinish(int action, int code) {
        if (action == ObserverManager.UserActionListener.ACTION_CHANGE_NET_STATE) {
            if (NetworkUtil.isWifiConnected(mContext)) {
                AppDebugConfig.d(AppDebugConfig.TAG_DOWNLOAD, "已切换WIFI环境，重新开启下载~");
                startDownload();
            } else {
                AppDebugConfig.d(AppDebugConfig.TAG_DOWNLOAD, "已切换非WIFI环境，当前强制下载状态：" + mForceDownload);
                if (!mForceDownload) {
                    stopAllDownload();
                }
            }
        }
    }

}
