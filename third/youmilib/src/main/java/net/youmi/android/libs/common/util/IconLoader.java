package net.youmi.android.libs.common.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import net.youmi.android.libs.common.CommonConstant;
import net.youmi.android.libs.common.basic.Basic_StringUtil;
import net.youmi.android.libs.common.coder.Coder_Md5;
import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.temp.BasicFileDownloader;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class IconLoader {

	public final static String savePath = CommonConstant.get_Key_IconDir();

	// 回调方法
	public interface IconLoaderCallback {

		public void iconLoaded(Bitmap bitmap);
	}

	// 使用线程池，来重复利用线程，优化内存
	private static final int DEFAULT_THREAD_POOL_SIZE = 1;

	private static ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool
			(DEFAULT_THREAD_POOL_SIZE);

	/*
	 * @Paramter imgUrl格式:
	 * http://localhost:8080/fileDownloadAction.action?id=1661
	 * 从SD卡上加载图片，如果存在则使用该图片； 如果不存在，则从网络下载保存到SD卡，然后使用该图片。
	 * 使用线程池对线程进行管理，优化内存使用和CPU效率
	 */
	public static Bitmap loadIcon(final Context mContext, final String url, final IconLoaderCallback
			iconDownloaderCallback) {

		// 主线程处理回调函数
		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				iconDownloaderCallback.iconLoaded((Bitmap) msg.obj);
			}
		};

		File file = new File(Environment.getExternalStorageDirectory() + File.separator + Coder_Md5.md5(url));
		Bitmap bitmap = null;
		if (file.exists()) {
			bitmap = readBitmap(file);
		}

		if (bitmap != null) {
			Message msg = Message.obtain();
			msg.obj = bitmap;

			handler.sendMessageDelayed(msg, 50);
			return bitmap;
		}
		if (executor == null) {
			executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE);
		}
		executor.execute(new Runnable() {
			@Override
			public void run() {
				Bitmap bitmap = syncLoadBitmap(mContext, url);
				Message msg = Message.obtain();
				msg.obj = bitmap;
				handler.sendMessage(msg);
			}
		});
		return null;
	}

	/**
	 * @param @param  context
	 * @param @param  url
	 * @param @return 传入参数名字
	 * @return Bitmap 返回类型
	 * @Title: syncLoadBitmap
	 * @Description:同步加载图片
	 * @date 2013-3-27 下午4:12:01
	 * @throw
	 */
	public static Bitmap syncLoadBitmap(Context context, String rawUrl) {
		try {
			if (Basic_StringUtil.isNullOrEmpty(rawUrl)) {
				return null;
			}
			String fileName = Coder_Md5.md5(rawUrl);
			File dir = new File(Environment.getExternalStorageDirectory() + savePath);
			if (!dir.exists()) {
				if (Debug_SDK.isDebug) {
					Debug_SDK.d("文件夹不存在，创建文件夹");
				}
				dir.mkdirs();
			}

			File destFile = new File(Environment.getExternalStorageDirectory() + savePath + fileName);
			// 从本地读取
			Bitmap bitmap = readBitmap(destFile);
			if (bitmap != null) {
				if (Debug_SDK.isDebug) {
					Debug_SDK.d("ImageLoader 从本地缓存读取图片");
				}
				return bitmap;
			}

			// 从网络读取
			bitmap = loadBitmapFromNetWork(context, rawUrl, destFile);

			return bitmap;
		} catch (Throwable e) {
			if (Debug_SDK.isDebug) {
				Debug_SDK.de(e);
			}
			e.printStackTrace();
		}
		return null;
	}

	public static Bitmap loadBitmapFromNetWork(Context context, String url, File destFile) {
		try {
			if (Debug_SDK.isDebug) {
				Debug_SDK.de("从网络读取图片 %s", url);
			}

			BasicFileDownloader downloadHandler = new BasicFileDownloader(context, url, destFile);
			int resultCode = downloadHandler.downloadToFile();

			if (resultCode == 0) {
				try {
					// long startTime = System.nanoTime();
					Bitmap bitmap = BitmapFactory.decodeFile(destFile.getAbsolutePath());
					// long endTime = System.nanoTime();
					// Debug_SDK.de( "Time use :%s", endTime-startTime);
					return bitmap;
				} catch (Throwable e) {
					if (Debug_SDK.isDebug) {
						Debug_SDK.de("");
					}
				}
			} else {
				if (Debug_SDK.isDebug) {
					Debug_SDK.de("下载图片失败：%s", url);
				}
				return null;
			}
		} catch (Throwable e) {
		}
		return null;
	}

	public static Bitmap readBitmap(File destFile) {
		try {
			if (!destFile.exists()) {
				return null;
			}
			Bitmap bitmap = BitmapFactory.decodeFile(destFile.getAbsolutePath());
			return bitmap;
		} catch (Throwable e) {
			if (Debug_SDK.isDebug) {
				Debug_SDK.e(e);
			}
		}
		return null;
	}

	public static void shutdownThreadPool() {
		executor.shutdown();
		executor = null;
	}

	public static boolean checkFileExist(Context context, String picString) {
		String fileName = Coder_Md5.md5(picString);
		File file = new File(Environment.getExternalStorageDirectory() + File.separator + fileName);
		if (Debug_SDK.isDebug) {
			Debug_SDK.d("checkFileExist:" + file.exists());
		}
		return file.exists();
	}
}
