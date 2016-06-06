package com.oplay.giftcool.download.silent;

import android.os.Build;
import android.os.Process;
import android.text.TextUtils;

import com.oplay.giftcool.config.AppConfig;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.download.silent.bean.DownloadInfo;
import com.socks.library.KLog;

import net.youmi.android.libs.common.coder.Coder_Md5;
import net.youmi.android.libs.common.util.Util_System_File;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by zsigui on 16-4-26.
 */
public class DownloadThread extends Thread {

	private boolean mIsStop = false;
	private String mTag;
	private String mDirPath;
	private LinkedBlockingQueue<DownloadInfo> mWaitQueue;

	public DownloadThread(String dirPath, LinkedBlockingQueue<DownloadInfo> waitQueue) {
		this.mWaitQueue = waitQueue;
		mDirPath = dirPath;
	}

	public void setIsStop(boolean isStop) {
		mIsStop = isStop;
	}

	public boolean isStop() {
		return mIsStop;
	}

	public void setTag(String tag) {
		mTag = tag;
	}

	private boolean initRange(DownloadInfo info) throws IOException {
		boolean result;
		URL url = new URL(info.getDownloadUrl());
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setConnectTimeout(5000);
		connection.setRequestMethod("GET");
		// 默认HttpURLConnection会进行Gzip压缩，这时无法通过getContentLength获取长度，所以要禁掉这个
		connection.setRequestProperty("Accept-Encoding", "identity");
		connection.connect();
		final int code = connection.getResponseCode();
		if (code >= 200 && code < 300) {
			info.setTotalSize(connection.getContentLength());
		}
		connection.disconnect();
		//				RandomAccessFile accessFile = new RandomAccessFile(info.getTempFileName(), "rwd");
//				accessFile.setLength(info.getTotalSize());
//				accessFile.close();

		result = info.getTotalSize() > 0;
		if (result && info.getTotalSize() < info.getDownloadSize()) {
			info.setDownloadSize(0);
		}
		return result;
	}

	@Override
	public void run() {
		mIsStop = false;
		if (AppDebugConfig.IS_DEBUG) {
			KLog.d(AppDebugConfig.TAG_UTIL, String.format("线程%s开始执行。。。。", mTag));
		}
		android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
		DownloadInfo info = null;
		while (!mIsStop) {
			try {
				info = mWaitQueue.take();
				if (info == null) {
					// 列表已经无任务了，退出
					if (AppDebugConfig.IS_DEBUG) {
						KLog.d(AppDebugConfig.TAG_UTIL, String.format("线程%s无任务，退出执行", mTag));
					}
					mIsStop = true;
					return;
				}
				if (AppDebugConfig.IS_DEBUG) {
					KLog.d(AppDebugConfig.TAG_UTIL, String.format("线程%s执行下载任务：%s", mTag, info.getDownloadUrl()));
				}
				if (initRange(info)) {
					if (info.isDownload()) {
						if (AppDebugConfig.IS_DEBUG) {
							KLog.d(AppDebugConfig.TAG_UTIL, String.format("下载任务：%s， 初始化完毕！", info.getDownloadUrl()));
						}
						URL url = new URL(info.getDownloadUrl());
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						if (info.getTotalSize() > info.getDownloadSize()) {
							if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
								connection.setFixedLengthStreamingMode(info.getTotalSize() - info.getDownloadSize());
							} else {
								connection.setFixedLengthStreamingMode((int) (info.getTotalSize() - info
										.getDownloadSize
										()));

							}
						} else {
							connection.setChunkedStreamingMode((int) info.getTotalSize());
						}

						// 默认HttpURLConnection会进行Gzip压缩，这时无法通过getContentLength获取长度，所以要禁掉这个
						connection.setRequestProperty("Accept-Encoding", "identity");
						connection.setRequestMethod("GET");
						connection.setConnectTimeout(AppConfig.NET_CONNECT_TIMEOUT);
						connection.setReadTimeout(AppConfig.NET_READ_TIMEOUT);
						if (info.getTotalSize() > 0) {
							connection.setRequestProperty("Range", "bytes=" + info.getDownloadSize() + "-"
									+ (info.getTotalSize() - 1));
						} else {
							connection.setRequestProperty("Range", "bytes=" + info.getDownloadSize() + "-");
						}
						if (AppDebugConfig.IS_DEBUG) {
							KLog.d(AppDebugConfig.TAG_UTIL, "下载开始：code = " + connection.getResponseCode() + ":" +
									connection.getResponseMessage());

						}
						int code = connection.getResponseCode();
						if (code >= 200 && code < 300) {
							InputStream in = connection.getInputStream();
							BufferedInputStream bin = new BufferedInputStream(in);
							RandomAccessFile out = new RandomAccessFile(new File(mDirPath, info.getTempFileName()),
									"rwd");
							out.seek(info.getDownloadSize());
							int length;
							byte[] bs = new byte[4096];
							long startTime = System.currentTimeMillis();
							long stopTime;
							while ((length = bin.read(bs)) != -1) {
								out.write(bs, 0, length);
								info.setDownloadSize(info.getDownloadSize() + length);
								stopTime = System.currentTimeMillis();
								if (stopTime - startTime > 5000) {
									SilentDownloadManager.getInstance().onProgressUpdate(info);
									startTime = stopTime;
								}
								if (!info.isDownload()) {
									// 暂停下载
									break;
								}
							}
							out.close();
							if (info.isDownload() && length == -1) {
								// 下载完成
								final File tempFile = new File(mDirPath, info.getTempFileName());
								if (!TextUtils.isEmpty(info.getMd5Sum())) {
									if (Coder_Md5.checkMd5Sum(tempFile, info.getMd5Sum())) {
										// 验证不通过，下载的包有问题，需要重新下载
										failDownload(info, true);
										if (AppDebugConfig.IS_DEBUG) {
											KLog.d(AppDebugConfig.TAG_UTIL, String.format("url : %s 执行验证，验证MD5不通过！",
													info.getDownloadUrl()));
										}
									} else {
										finishDownload(info, tempFile);
										if (AppDebugConfig.IS_DEBUG) {
											KLog.d(AppDebugConfig.TAG_UTIL,
													String.format("url : %s 执行验证，验证MD5通过，下载完成！",
															info.getDownloadUrl()));
										}
									}
								} else {
									finishDownload(info, tempFile);
									if (AppDebugConfig.IS_DEBUG) {
										KLog.d(AppDebugConfig.TAG_UTIL, String.format("url : %s 不执行验证，下载完成！",
												info.getDownloadUrl()));
									}
								}
								continue;
							}
						}
						// 取消下载
						SilentDownloadManager.getInstance().removeDownload(info, false);
						if (AppDebugConfig.IS_DEBUG) {
							KLog.d(AppDebugConfig.TAG_UTIL, String.format("url : %s 取消下载！", info.getDownloadUrl
									()));
						}

					} else {
						// 连接错误，下载失败，将任务重新移动队列末尾
						failDownload(info, false);
						if (AppDebugConfig.IS_DEBUG) {
							KLog.d(AppDebugConfig.TAG_UTIL, String.format("url : %s 下载失败！", info.getDownloadUrl()));
						}
					}
				} else {
					if (AppDebugConfig.IS_DEBUG) {
						KLog.d(AppDebugConfig.TAG_UTIL, "InitRange Failed");
					}
				}

			} catch (IOException | InterruptedException e) {
				AppDebugConfig.warn(e);
				if (info != null) {
					failDownload(info, false);
				}
			}
		}
	}

	private void failDownload(DownloadInfo info, boolean removeTemp) {
		if (info.getRetryTime() > 4) {
			return;
		}
		info.setRetryTime(info.getRetryTime() + 1);
		SilentDownloadManager.getInstance().removeDownload(info, removeTemp);
		SilentDownloadManager.getInstance().addDownload(info);
	}

	private void finishDownload(DownloadInfo info, File tempFile) {
		Util_System_File.cp(tempFile, new File(mDirPath, info.getStoreFileName()));
		SilentDownloadManager.getInstance().removeDownload(info, true);
	}
}
