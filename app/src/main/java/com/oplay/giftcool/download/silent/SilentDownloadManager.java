package com.oplay.giftcool.download.silent;

import android.content.Context;
import android.text.TextUtils;

import com.nostra13.universalimageloader.utils.StorageUtils;
import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.download.silent.bean.DownloadInfo;
import com.oplay.giftcool.util.FileUtil;
import com.socks.library.KLog;

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
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by zsigui on 16-4-25.
 */
public class SilentDownloadManager {

	private ConcurrentHashMap<String, DownloadInfo> mTotalDownloadMap;
	private ConcurrentLinkedQueue<DownloadInfo> mWaitDownloadQueue;

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
	/**
	 * 是否处于下载中
	 */
	private boolean mIsRunning;

	private static SilentDownloadManager mInstance;

	public static SilentDownloadManager getInstance() {
		if (mInstance == null) {
			mInstance = new SilentDownloadManager(AssistantApp.getInstance(), Global.EXTERNAL_DOWNLOAD);
		}
		return mInstance;
	}


	private SilentDownloadManager(Context context, String dirPath) {
		mTotalDownloadMap = new ConcurrentHashMap<>();
		mWaitDownloadQueue = new ConcurrentLinkedQueue<>();
		initDownloadTasks(context, dirPath);
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

	/**
	 * 初始化下载配置
	 */
	private void initDownloadTasks(Context context, String dirPath) {
		File dirFile = StorageUtils.getOwnCacheDirectory(context, dirPath);
		if (!FileUtil.mkdirs(dirFile)) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.d(AppDebugConfig.TAG_WARN, "failed to create dirPath : " + dirPath);
			}
			return;
		}
		mDirPath = dirPath;
		File configFile = new File(mDirPath, CONFIG_FILE);
		mTotalDownloadMap.putAll(readConfigFile(configFile));
		mWaitDownloadQueue.addAll(mTotalDownloadMap.values());
	}

	/**
	 * 开启下载线程
	 */
	private synchronized void startDownload() {
		for (DownloadInfo info : mTotalDownloadMap.values()) {
			info.setIsDownload(true);
		}
		mIsRunning = true;

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
				if (T_START.startsWith(line)) {
					info = new DownloadInfo();
				} else if (info != null){
					if (T_DOWNLOAD_URL.startsWith(line)) {
						info.setDownloadUrl(line.substring(T_DOWNLOAD_URL.length()));
					} else if (T_DEST_URL.startsWith(line)) {
						info.setDestUrl(line.substring(T_DEST_URL.length()));
					} else if (T_TOTAL_SIZE.startsWith(line)) {
						info.setTotalSize(Long.parseLong(line.substring(T_TOTAL_SIZE.length())));
					} else if (T_MD5_SUM.startsWith(line)) {
						info.setMd5Sum(line.substring(T_MD5_SUM.length()));
					} else if (T_END.startsWith(line)) {
						final File storeFile = new File(mDirPath, info.getStoreFileName());
						if (storeFile.exists() && storeFile.isFile()) {
							// 文件已经下载完成，不做处理
							if (AppDebugConfig.IS_DEBUG) {
								KLog.d(AppDebugConfig.TAG_WARN, "apk has been downloaded");
							}
						} else {
							final File tempFile = new File(mDirPath, info.getTempFileName());
							if (tempFile.exists() || tempFile.isFile()) {
								info.setDownloadSize(tempFile.length());
							}
							result.put(info.getDownloadUrl(), info);
						}
						info = null;
					}
				}
			}
			br.close();
		} catch (IOException e) {
			AppDebugConfig.warn(e);
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
				bw.write(T_START + '\n');
				bw.write(T_DOWNLOAD_URL + ':' + info.getDownloadUrl());
				bw.write(T_DEST_URL + ':' + info.getDestUrl());
				bw.write(T_TOTAL_SIZE + ':' + info.getTotalSize());
				bw.write(T_MD5_SUM + ':' + info.getMd5Sum());
				bw.write(T_END + '\n');
				bw.flush();
			}
			bw.close();
		} catch (IOException e) {
			AppDebugConfig.warn(e);
		}

	}

	public void startDownload(DownloadInfo downloadInfo){

	}

	/**
	 * 获取待执行的下载任务
	 */
	synchronized DownloadInfo obtainDownload() {
		if (mWaitDownloadQueue.size() > 0) {
			return mWaitDownloadQueue.remove();
		}
		return null;
	}

	/**
	 * 移除已经完成或者取消的下载任务
	 */
	public synchronized void removeDownload(DownloadInfo info, boolean removeTemp) {
		if (info != null) {
			if (removeTemp) {
				final File tempFile = new File(info.getTempFileName());
				if (tempFile.exists() && tempFile.isFile()) {
					Util_System_File.delete(tempFile);
				}
			}
			mTotalDownloadMap.remove(info.getDownloadUrl());
			mWaitDownloadQueue.remove(info);
		}
	}

	/**
	 * 添加新的下载任务
	 */
	public synchronized void addDownload(DownloadInfo info) {
		if (info != null && !mTotalDownloadMap.containsKey(info.getDownloadUrl())
				&& isValid(info)) {
			final File storeFile = new File(info.getStoreFileName());
			if (!storeFile.exists()) {
				if (!mTotalDownloadMap.containsKey(info.getDownloadUrl())) {
					mTotalDownloadMap.put(info.getDownloadUrl(), info);
				}
				if (!mTotalDownloadMap.containsKey(info)) {
					mWaitDownloadQueue.add(info);
				}
			}
		}
	}

	/**
	 * 移除所有的下载任务，即是清除下载，会把对应的下载缓存文件一起删除 <br />
	 * 已经下载完成的由于已经移除队列，不会被处理
	 */
	public synchronized void removeAllDownload() {
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
	}

	/**
	 * 停止所有的下载任务，即是取消所有下载的网络请求 <br />
	 * 此时会记录
	 */
	public synchronized void stopAllDownload() {
		for (DownloadInfo info : mTotalDownloadMap.values()) {
			info.setIsDownload(false);
		}
		mWaitDownloadQueue.clear();
		writeConfigFile(new File(mDirPath, CONFIG_FILE), mTotalDownloadMap);
	}



	/**
	 * 判断传入的下载对象是否有效 <br />
	 * 至少需要具备下载地址和目标地址
	 */
	private boolean isValid(DownloadInfo info) {
		return !TextUtils.isEmpty(info.getDownloadUrl()) && !TextUtils.isEmpty(info.getDestUrl());
	}

	public void onProgressUpdate() {

	}
}
