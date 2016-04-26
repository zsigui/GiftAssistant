package com.oplay.giftcool.download.silent;

import android.text.TextUtils;

import com.oplay.giftcool.config.AppConfig;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.download.silent.bean.DownloadInfo;

import net.youmi.android.libs.common.coder.Coder_Md5;
import net.youmi.android.libs.common.util.Util_System_File;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by zsigui on 16-4-26.
 */
public class DownloadThread extends Thread {

	private boolean mIsStop = false;

	public void setIsStop(boolean isStop) {
		mIsStop = isStop;
	}

	private boolean initRange(DownloadInfo info) throws IOException {
		if (info.getDownloadSize() == 0) {
			URL url = new URL(info.getDownloadUrl());
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(5000);
			connection.setRequestMethod("GET");
			// 默认HttpURLConnection会进行Gzip压缩，这时无法通过getContentLength获取长度，所以要禁掉这个
			connection.setRequestProperty("Accept-Encoding", "identity");
			connection.connect();
			if (connection.getResponseCode() == 200) {
				info.setDownloadSize(connection.getContentLength());
			}
			connection.disconnect();

			if (info.getDownloadSize() != 0) {
				RandomAccessFile accessFile = new RandomAccessFile(info.getTempFileName(), "rwd");
				accessFile.setLength(info.getDownloadSize());
				accessFile.close();
				return true;
			} else {
				return false;
			}
		}
		return true;
	}

	/**
	 * 根据Url打开Http连接
	 */
	private HttpURLConnection openConnection(URL url) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		connection.setConnectTimeout(AppConfig.NET_CONNECT_TIMEOUT);
		connection.setReadTimeout(AppConfig.NET_READ_TIMEOUT);
		connection.setUseCaches(false);
		connection.setInstanceFollowRedirects(true);
		connection.setRequestMethod("GET");

		return connection;
	}

	@Override
	public void run() {
		mIsStop = false;
		while (!mIsStop) {
			try {
				final DownloadInfo info = SilentDownloadManager.getInstance().obtainDownload();
				if (info == null) {
					// 列表已经无任务了，退出
					return;
				}
				if (initRange(info)) {
					URL url = new URL(info.getDownloadUrl());
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					connection.setFixedLengthStreamingMode(info.getTotalSize() - info.getDownloadSize());

					// 默认HttpURLConnection会进行Gzip压缩，这时无法通过getContentLength获取长度，所以要禁掉这个
					connection.setRequestProperty("Accept-Encoding", "identity");
					connection.setRequestMethod("GET");
					connection.setConnectTimeout(5000);
					connection.setReadTimeout(30000);
					if (info.getTotalSize() > 0) {
						connection.setRequestProperty("Range", "bytes=" + info.getDownloadSize() + "-"
								+ (info.getTotalSize() - 1));
					} else {
						connection.setRequestProperty("Range", "bytes=" + info.getDownloadSize() + "-");
					}
					if (connection.getResponseCode() == 200) {
						InputStream in = connection.getInputStream();
						BufferedInputStream bin = new BufferedInputStream(in);
						RandomAccessFile out = new RandomAccessFile(info.getTempFileName(), "rwd");
						out.seek(info.getDownloadSize());
						int length;
						byte[] bs = new byte[1024];
						while ((length = bin.read(bs)) != -1) {
							out.write(bs, 0, length);
							info.setDownloadSize(info.getDownloadSize() + length);
							SilentDownloadManager.getInstance().onProgressUpdate();
							if (!info.isDownload()) {
								// 暂停下载
								break;
							}
						}
						if (info.isDownload() && length == -1) {
							// 下载完成
							final File tempFile = new File(info.getTempFileName());
							if (!TextUtils.isEmpty(info.getMd5Sum())) {
								if (Coder_Md5.checkMd5Sum(tempFile, info.getMd5Sum())) {
									// 验证不通过，下载的包有问题，需要重新下载
									SilentDownloadManager.getInstance().removeDownload(info, true);
									SilentDownloadManager.getInstance().addDownload(info);
									break;
								}
							}
							Util_System_File.cp(tempFile, new File(info.getStoreFileName()));
							SilentDownloadManager.getInstance().removeDownload(info, true);
						} else {

						}

					} else {
						// 连接错误，下载失败，将任务重新移动队列末尾
						SilentDownloadManager.getInstance().removeDownload(info, false);
						SilentDownloadManager.getInstance().addDownload(info);
					}

				}

			} catch (IOException e) {
				if (AppDebugConfig.IS_DEBUG) {
					e.printStackTrace();
				}
			}
		}
	}
}
