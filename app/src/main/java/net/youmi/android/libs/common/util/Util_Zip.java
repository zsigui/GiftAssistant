package net.youmi.android.libs.common.util;

import net.youmi.android.libs.common.global.Global_Executor;
import net.youmi.android.libs.common.util.zip.decompress.IUnZipListener;
import net.youmi.android.libs.common.util.zip.decompress.UnZip;

import java.io.File;

/**
 * 考虑到sdk大小，这里没有是用zip4j，采用的是jdk里面的zip
 * <p/>
 * 因此不支持中文，也不支持密码
 *
 * @author zhitaocai
 */
public class Util_Zip {

	/**
	 * （同步）在当前线程中进行解压缩，压缩成功之后，解压缩存放的目录为{$destDir/zipFile.getName()}
	 * <p/>
	 * 使用时请注意启用线程
	 *
	 * @param zipFile     zip文件
	 * @param destDirPath 解压后存放的路径
	 *
	 * @return 解压是否成功
	 */
	public static boolean sync_unZip(File zipFile, String destDirPath) {
		return new UnZip(zipFile, destDirPath).unZip();
	}

	/**
	 * （异步）线程池中启动一个线程进行解压缩，压缩成功之后，解压缩存放的目录为{$destDir/zipFile.getName()}
	 *
	 * @param zipFile              zip文件
	 * @param destDirPath          解压后存放的路径
	 * @param unZiplistener        解压缩监听器，支持开始，进行，完成，错误的回调，可以不传
	 * @param isCallBackInUiThread 监听的各个回调是否执行在UI线程中，如果没有传监听器对象，则这个参数填什么都可以
	 *                             <ul>
	 *                             <li>true ： 是</li>
	 *                             <li>false： 否</li>
	 *                             </ul>
	 *
	 * @return
	 */
	public static void async_unZIp(File zipFile, String destDirPath, IUnZipListener unZiplistener, boolean
			isCallBackInUiThread) {
		Global_Executor.getCachedThreadPool().execute(new UnZip(zipFile, destDirPath, unZiplistener, isCallBackInUiThread));
	}

}
