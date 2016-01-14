package com.oplay.giftassistant.model;

/**
 * DownloadStatus
 *
 * @author zacklpx
 *         date 16-1-5
 *         description
 */
public enum DownloadStatus {

	PENDING,        //等待下载中
	DOWNLOADING,    //下载中
	PAUSED,         //暂停状态
	FAILED,         //下载失败状态
	FINISHED,       //下载完成状态
	DISABLE;        //无效状态

	public static DownloadStatus value2Name(int value) {
		for (DownloadStatus status : DownloadStatus.values()) {
			if (status.ordinal() == value) {
				return status;
			}
		}
		return null;
	}
}
