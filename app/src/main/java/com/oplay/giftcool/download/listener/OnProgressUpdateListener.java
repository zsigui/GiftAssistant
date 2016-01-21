package com.oplay.giftcool.download.listener;

/**
 * OnProgressUpdateListener
 *
 * @author zacklpx
 *         date 16-1-5
 *         description
 */
public interface OnProgressUpdateListener {
	public void onProgressUpdate(final String url, final int percent, final long speedBytesPers);
}
