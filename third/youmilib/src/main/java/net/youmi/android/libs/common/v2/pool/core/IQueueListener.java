package net.youmi.android.libs.common.v2.pool.core;

/**
 * 队列任务加入到队列和从队列中移出时的监听
 *
 * @author zhitao
 * @since 2015-09-03 10:50
 */
public interface IQueueListener<T> {

	/**
	 * 当达到同时下载任务上限数时，任务加入到等待队列时的回调
	 *
	 * @param currentWaitQueueLength 添加到队列之后,队列的当前长度
	 */
	void onOffer(T t, int currentWaitQueueLength);

	/**
	 * 当达到同时下载任务上限数时，如果有正在执行的任务完成了时，从等待队列中拿新的任务加入到执行线程工作队列中时的回调
	 *
	 * @param currentWaitQueueLength 从队列中提取出来之后,当前队列的长度
	 */
	void onTake(T t, int currentWaitQueueLength);

}
