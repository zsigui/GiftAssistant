package com.oplay.giftassistant.download.listener;

import com.oplay.giftassistant.model.data.resp.IndexGameNew;

/**
 * OnDownloadStatusChangeListener
 *
 * @author zacklpx
 *         date 16-1-5
 *         description
 */
public interface OnDownloadStatusChangeListener {
	public void onDownloadStatusChanged(IndexGameNew appInfo);
}
