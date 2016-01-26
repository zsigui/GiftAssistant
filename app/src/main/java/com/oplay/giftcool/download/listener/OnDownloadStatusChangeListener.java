package com.oplay.giftcool.download.listener;

import com.oplay.giftcool.model.data.resp.GameDownloadInfo;

/**
 * OnDownloadStatusChangeListener
 *
 * @author zacklpx
 *         date 16-1-5
 *         description
 */
public interface OnDownloadStatusChangeListener {
	public void onDownloadStatusChanged(GameDownloadInfo appInfo);
}
