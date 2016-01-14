package net.youmi.android.libs.common.util.zip.decompress;

/**
 * 解压时的回调，均执行在UI线程中
 *
 * @author zhitaocai
 * @since 2014-10-30下午8:15:57
 */
public interface IUnZipListener {

	/**
	 * 解压开始
	 */
	void onUnZipStart();

	/**
	 * 解压进度更新
	 * <p/>
	 * <ul>
	 * <li>如果中途遇到某些文件解压失败，这里会跳过，同时更新进度</li>
	 * <li>如果压缩包里面压缩的条目十分多的话，可能会连续出现55 55 55 55 56 56 56这样的顺序 属于正常，因此请不要判断这里的100就当解压完成，请用
	 * {@linkplain net.youmi.android.libs.common.util.zip.decompress.IUnZipListener#onUnZipFinish} 来判断</li>
	 * </ul>
	 *
	 * @param percent 0~100 int
	 */
	void onUnZipProgressUpdate(int percent);

	/**
	 * 解压完成
	 * <p/>
	 * 这里还需要判断是否全部成功解压
	 *
	 * @param totalCount 解压的条目总数
	 * @param failCount  解压错误的条目数量
	 */
	void onUnZipFinish(int totalCount, int failCount);

	/**
	 * 解压错误
	 * <p/>
	 * 遇到不可跳过的解压错误时，会回调这里
	 */
	void onUnZipError();
}
