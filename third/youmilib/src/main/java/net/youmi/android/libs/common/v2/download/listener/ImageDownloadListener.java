package net.youmi.android.libs.common.v2.download.listener;

import android.graphics.Bitmap;

/**
 * 图片下载监听通知
 *
 * @author zhitao
 * @since 2015-10-21 08:38
 */
public interface ImageDownloadListener {

	void onImageDownloadSuccess(String url, Bitmap bm);

	void onImageDownloadFailed(String url);

	void onImageDownloadStop(String url);

}
