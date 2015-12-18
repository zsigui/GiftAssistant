package net.youmi.android.libs.common.v2.download.notify;

import android.graphics.Bitmap;

import net.youmi.android.libs.common.debug.DLog;
import net.youmi.android.libs.common.v2.download.listener.ImageDownloadListener;
import net.youmi.android.libs.common.v2.template.TListenersManager;

import java.util.List;

/**
 * 下载观察者回调通知管理器
 *
 * @author zhitao
 * @since 2015-09-10 09:34
 */
public class ImageDownloadNotifier extends TListenersManager<ImageDownloadListener> {

	public void onNotifyImageDownloadSuccess(String url, Bitmap bm) {
		try {
			final List<ImageDownloadListener> list = getListeners();
			if (list != null && !list.isEmpty()) {
				if (DLog.isDownloadLog) {
					DLog.td(DLog.mDownloadTag, this, "当前共有[%d]个监听者要处理", list.size());
				}
				for (int i = 0; i < list.size(); ++i) {
					try {
						if (DLog.isDownloadLog) {
							DLog.ti(DLog.mDownloadTag, this, "处理第[%d]个监听者[%s]", i + 1, list.get(i).getClass().getName());
						}
						list.get(i).onImageDownloadSuccess(url, bm);
					} catch (Throwable e) {
						DLog.te(DLog.mDownloadTag, this, e);
					}
				}
			}
		} catch (Throwable e) {
			if (DLog.isDownloadLog) {
				DLog.te(DLog.mDownloadTag, this, e);
			}
		}

	}

	public void onNotifyImageDownloadFailed(String url) {
		try {
			final List<ImageDownloadListener> list = getListeners();
			if (list != null && !list.isEmpty()) {
				if (DLog.isDownloadLog) {
					DLog.td(DLog.mDownloadTag, this, "当前共有[%d]个监听者要处理", list.size());
				}
				for (int i = 0; i < list.size(); ++i) {
					try {
						if (DLog.isDownloadLog) {
							DLog.ti(DLog.mDownloadTag, this, "处理第[%d]个监听者[%s]", i + 1, list.get(i).getClass().getName());
						}
						list.get(i).onImageDownloadFailed(url);
					} catch (Throwable e) {
						DLog.te(DLog.mDownloadTag, this, e);
					}
				}
			}
		} catch (Throwable e) {
			if (DLog.isDownloadLog) {
				DLog.te(DLog.mDownloadTag, this, e);
			}
		}
	}

	public void onNotifyImageDownloadStop(String url) {
		try {
			final List<ImageDownloadListener> list = getListeners();
			if (list != null && !list.isEmpty()) {
				if (DLog.isDownloadLog) {
					DLog.td(DLog.mDownloadTag, this, "当前共有[%d]个监听者要处理", list.size());
				}
				for (int i = 0; i < list.size(); ++i) {
					try {
						if (DLog.isDownloadLog) {
							DLog.ti(DLog.mDownloadTag, this, "处理第[%d]个监听者[%s]", i + 1, list.get(i).getClass().getName());
						}
						list.get(i).onImageDownloadFailed(url);
					} catch (Throwable e) {
						DLog.te(DLog.mDownloadTag, this, e);
					}
				}
			}
		} catch (Throwable e) {
			if (DLog.isDownloadLog) {
				DLog.te(DLog.mDownloadTag, this, e);
			}
		}
	}

}
