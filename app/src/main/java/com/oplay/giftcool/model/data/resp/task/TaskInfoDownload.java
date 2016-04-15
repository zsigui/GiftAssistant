package com.oplay.giftcool.model.data.resp.task;

import com.oplay.giftcool.util.DateUtil;

/**
 * 下载任务信息，用于积分任务记录状态
 *
 * Created by zsigui on 16-4-15.
 */
public class TaskInfoDownload extends TaskInfoThree {

	/**
	 * 任务代号
	 */
	public String code;

	/**
	 * 任务可执行时间，为此时间点的时间内，单位 ms
	 */
	public long taskTime;


	/**
	 * 已经积累的游戏时间 单位 s
	 */
	public int hasPlayTime;

	/**
	 * 任务是否已经完成
	 */
	public boolean isFinished() {
		return hasPlayTime >= time;
	}

	/**
	 * 判断任务的时间是否今日，非今日的任务予以清除
	 */
	public boolean isToday() {
		return DateUtil.isToday(taskTime);
	}

	public TaskInfoDownload() {
		this.taskTime = System.currentTimeMillis();
	}

	public TaskInfoDownload(String code, TaskInfoThree info) {
		this();
		this.code = code;
		this.packName = info.packName;
		this.time = info.time;
		this.appId = info.appId;
	}
}
