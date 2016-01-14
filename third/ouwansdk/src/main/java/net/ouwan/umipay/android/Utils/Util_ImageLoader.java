package net.ouwan.umipay.android.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import net.ouwan.umipay.android.debug.Debug_Log;
import net.youmi.android.libs.common.basic.Basic_StringUtil;
import net.youmi.android.libs.common.coder.Coder_Md5;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Util_ImageLoader {

	public static final String TAG = "ImageLoader";
	public static final String savePath = ".umipay/ic/";
	// 使用线程池，来重复利用线程，优化内存
	private static final int DEFAULT_THREAD_POOL_SIZE = 1;
	private static ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors
			.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE);

	/*
	 * @Paramter imgUrl格式:
	 * http://localhost:8080/fileDownloadAction.action?id=1661
	 * 从SD卡上加载图片，如果存在则使用该图片； 如果不存在，则从网络下载保存到SD卡，然后使用该图片。
	 * 使用线程池对线程进行管理，优化内存使用和CPU效率
	 */
	public static Bitmap loadImage(final Context mContext, final String imgUrl,
	                               final ImageLoaderCallback imageDownloaderCallback) {

		// 主线程处理回调函数
		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				imageDownloaderCallback.imageLoaded((Bitmap) msg.obj);
			}
		};

		Bitmap bitmap = readBitmap(imgUrl);


		if (bitmap != null) {
			Message msg = Message.obtain();
			msg.obj = bitmap;

			handler.sendMessageDelayed(msg, 50);
			return bitmap;
		}

		executor.execute(new Runnable() {
			@Override
			public void run() {
				Bitmap bitmap = syncLoadBitmap(mContext, imgUrl);
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
	public static Bitmap syncLoadBitmap(Context context, String url) {
		try {
			if (Basic_StringUtil.isNullOrEmpty(url)) {
				return null;
			}
			//从本地读取
			Bitmap bitmap = readBitmap(url);
			if (bitmap != null) {
				Debug_Log.dd("ImageLoader 从本地缓存读取图片");
				return bitmap;
			}

			//从网络读取
			Debug_Log.dd("ImageLoader 从" + url + "读取图片");
			bitmap = loadBitmapFromNetWork(context, url);

			if (bitmap != null) {
				//缩放icon
				bitmap = Bitmap.createScaledBitmap(bitmap, 96, 96, true);
				//保存在本地
				saveBitmap(bitmap, url);
			}
			return bitmap;
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return null;
	}

	public static Bitmap loadBitmapFromNetWork(Context context, String url) {
		try {
			HttpGet request = new HttpGet(url);
			HttpResponse response = MyHttpClient.execute(context, request);

			// 取得请求内容
			InputStream inStream = response.getEntity().getContent();

			ByteArrayOutputStream outSteam = new ByteArrayOutputStream();

			byte[] buffer = new byte[4096];
			int len = 0;
			while ((len = inStream.read(buffer)) != -1) {
				outSteam.write(buffer, 0, len);
			}
			outSteam.close();
			inStream.close();

			byte[] data = outSteam.toByteArray();
			return BitmapFactory.decodeByteArray(data, 0, data.length);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return null;
	}

	public static void saveBitmap(Bitmap bitmap, String nameorpath) {
		try {
			File dir = new File(Environment.getExternalStorageDirectory()
					+ File.separator + savePath);
			// 若指定文件目录不存在，则创建
			if (!dir.exists()) {
				dir.mkdirs();
			}

			File bitmapFile = new File(dir, Coder_Md5.md5(nameorpath));
			if (bitmapFile.exists()) {
				bitmapFile.delete();
			}
			bitmapFile.createNewFile();
			FileOutputStream fOut = null;
			fOut = new FileOutputStream(bitmapFile);
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
			fOut.flush();
			try {
				fOut.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

	public static Bitmap readBitmap(String nameorpath) {
		try {
			File dir = new File(Environment.getExternalStorageDirectory()
					+ File.separator + savePath);
			// 若指定文件目录不存在，则创建
			if (!dir.exists()) {
				dir.mkdirs();
			}
			File bitmapFile = new File(dir, Coder_Md5.md5(nameorpath));
			Bitmap bitmap = null;
			if (bitmapFile.exists()) {
				bitmap = BitmapFactory.decodeFile(bitmapFile.getAbsolutePath());
			}
			return bitmap;
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return null;
	}

	// 清空图片本地缓存
	public static void clearImgCache() {
		File dir = new File(Environment.getExternalStorageDirectory()
				+ File.separator + savePath);
		if (dir.exists()) {
			// 获得该文件夹下的所有文件
			File[] fileList = dir.listFiles();
			for (File file : fileList) {
				Debug_Log.d("删除文件" + file.getName() + "删除" + file.delete());
			}
		}
	}

	public static void shutdownThreadPool() {
		executor.shutdown();
	}

	// 回调方法
	public interface ImageLoaderCallback {
		public void imageLoaded(Bitmap bitmap);
	}

}
