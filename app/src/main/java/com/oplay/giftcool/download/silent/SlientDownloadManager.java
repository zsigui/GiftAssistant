package com.oplay.giftcool.download.silent;

import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.download.silent.bean.DownloadInfo;
import com.oplay.giftcool.util.FileUtil;
import com.socks.library.KLog;

import net.youmi.android.libs.common.coder.Coder_Md5;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * Created by zsigui on 16-4-25.
 */
public class SlientDownloadManager {

	public HashMap<String, DownloadInfo> mDownloads;

	private final String DEFAULT_ENCODE = "UTF-8";
	private final String CONFIG_FILE = "silent.download.config";
	private final String T_START = "t_start";
	private final String T_FILENAME = "t_filename";
	private final String T_DOWNLOAD_URL = "t_download_url";
	private final String T_END = "t_end";

	private String mDirPath;

	/**
	 * 判断下载任务是否存在<br />
	 * 判断逻辑：<br />
	 *
	 * @return
	 */
	public boolean existsDownloadTask(String url) {
		if (mDownloads == null) {
			mDownloads = new HashMap<>();
		}
		return mDownloads.containsKey(Coder_Md5.md5(url));
	}

	public void initDownloadTasks(String dirPath) {
		File dirFile = new File(dirPath);
		if (!FileUtil.mkdirs(dirFile)) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.d(AppDebugConfig.TAG_WARN, "failed to create dirPath : " + dirPath);
			}
			return;
		}
		mDirPath = dirPath;
		File configFile = new File(dirPath, CONFIG_FILE);
		mDownloads = readConfigFile(configFile);
	}

	private HashMap<String,DownloadInfo> readConfigFile(File configFile) {
		HashMap<String, DownloadInfo> result = new HashMap<>();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(configFile), DEFAULT_ENCODE));
			String line;
			while ((line = br.readLine()) != null) {
				if (T_START.startsWith(line)) {

				} else if (T_END.startsWith(line)) {

				} else if (T_DOWNLOAD_URL.startsWith(line)) {

				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
}
