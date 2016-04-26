package com.oplay.giftcool.download.silent;

import android.os.Build;
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

/**
 * Created by zsigui on 16-4-26.
 */
public class DownloadThread extends Thread {

	private boolean mIsStop = false;
    private String mTag;

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
		if (info.getDownloadSize() == 0) {
            KLog.d(AppDebugConfig.TAG_WARN, "开始计算DownloadSize");
			URL url = new URL(info.getDownloadUrl());
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(5000);
			connection.setRequestMethod("GET");
			// 默认HttpURLConnection会进行Gzip压缩，这时无法通过getContentLength获取长度，所以要禁掉这个
			connection.setRequestProperty("Accept-Encoding", "identity");
			connection.connect();
            KLog.d(AppDebugConfig.TAG_WARN, "返回错误码：" + connection.getResponseCode() + "," + connection.getResponseMessage());
			if (connection.getResponseCode() == 200) {
                KLog.d(AppDebugConfig.TAG_WARN, "返回长度：" + connection.getContentLength());
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

	@Override
	public void run() {
		mIsStop = false;
        if (AppDebugConfig.IS_DEBUG) {
            KLog.d(AppDebugConfig.TAG_WARN, String.format("线程%s开始执行。。。。", mTag));
        }
		while (!mIsStop) {
			try {
				final DownloadInfo info = SilentDownloadManager.getInstance().obtainDownload();
				if (info == null) {
					// 列表已经无任务了，退出
                    if (AppDebugConfig.IS_DEBUG) {
                        KLog.d(AppDebugConfig.TAG_WARN, String.format("线程%s无任务，退出执行", mTag));
                    }
                    mIsStop = true;
					return;
				}
                if (AppDebugConfig.IS_DEBUG) {
                    KLog.d(AppDebugConfig.TAG_WARN, String.format("线程%s执行下载任务：%s", mTag, info.getDownloadUrl()));
                }
				if (initRange(info)) {
                    if (AppDebugConfig.IS_DEBUG) {
                        KLog.d(AppDebugConfig.TAG_WARN, String.format("下载任务：%s， 初始化完毕！", info.getDownloadUrl()));
                    }
					URL url = new URL(info.getDownloadUrl());
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        connection.setFixedLengthStreamingMode(info.getTotalSize() - info.getDownloadSize());
                    } else {
                        connection.setFixedLengthStreamingMode((int)(info.getTotalSize() - info.getDownloadSize()));
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
							SilentDownloadManager.getInstance().onProgressUpdate(info);
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
						} else {
                            // 取消下载
                            SilentDownloadManager.getInstance().removeDownload(info, false);
                            if (AppDebugConfig.IS_DEBUG) {
                                KLog.d(AppDebugConfig.TAG_UTIL, String.format("url : %s 取消下载！", info.getDownloadUrl()));
                            }
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
                        KLog.d(AppDebugConfig.TAG_WARN, "InitRange Failed");
                    }
                }

			} catch (IOException e) {
				if (AppDebugConfig.IS_DEBUG) {
					e.printStackTrace();
				}
			}
		}
	}

    private void failDownload(DownloadInfo info, boolean removeTemp) {
        SilentDownloadManager.getInstance().removeDownload(info, removeTemp);
        SilentDownloadManager.getInstance().addDownload(info);
    }

    private void finishDownload(DownloadInfo info, File tempFile) {
        Util_System_File.cp(tempFile, new File(info.getStoreFileName()));
        SilentDownloadManager.getInstance().removeDownload(info, true);
    }
}
