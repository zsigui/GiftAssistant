package net.youmi.android.libs.common.v2.download.listener;

import net.youmi.android.libs.common.v2.download.model.FileDownloadTask;

/**
 * 下载完成时，用于检查文件是否可用
 * <p/>
 * 主要用于文件下载完成时，在返回下载成功的回调之前，判断文件状态可用性的接口
 *
 * @author jen
 */
public interface IFileAvailableChecker {

	/**
	 * 下载完成时，检查文件是否可以用
	 * <p/>
	 * 使用场合：
	 * 如果你觉得你下载的文件被劫持为另一个url，那么就可以实现一下下面的接口
	 * <p/>
	 * 如：
	 * <ul>
	 * <li>重新计算文件的md5和task中的md5(服务器那边返回的比较一下)</li>
	 * <li>或者重新向服务器请求这个文件的md5然后做对比</li>
	 * </ul>
	 *
	 * @param fileDownloadTask 下载任务
	 * @return
	 */
	boolean isStoreFileAvailable(FileDownloadTask fileDownloadTask);

}
