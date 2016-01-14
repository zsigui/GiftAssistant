package net.ouwan.umipay.android.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Environment;
import android.view.View;


import net.ouwan.umipay.android.debug.Debug_Log;

import java.io.File;
import java.io.FileOutputStream;


/**
 * ScreenShot 屏幕截图类
 * Created by lujunming  on 14-7-24.
 */
public class Util_ScreenShot {
	static public String shot(String name, final Context context, View v) {
		if (null == name || null == context || null == v)
			return null;
		try {
			v.setDrawingCacheEnabled(true);
			v.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
					View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

			v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
			v.buildDrawingCache();

			Bitmap bitmap = v.getDrawingCache();

			// 保存截图到/Pictures/Screenshots/
			String dir = Util_FileHelper.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
					.getAbsolutePath();
			dir += "/Screenshots/";
			File screenshotDir = new File(dir);
			if (!screenshotDir.exists()) {
				screenshotDir.mkdirs();
			}
			final File file = new File(screenshotDir, name + ".jpg");
			FileOutputStream fos = new FileOutputStream(file);
			try {
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
				fos.flush();
			} catch (Throwable e) {
				Debug_Log.e(e);
			} finally {
				try {
					fos.close();
				} catch (Throwable ignore) {
				}
			}
			// 将图片插入到相片的媒体库中
//			String url = MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(),
// name, "");
			// 有些机器插入后不会立即在相册中出现，需要刷新相册环境，直接刷新插入相片所在的目录
			if (file.exists()) {
				try {
					new MyMediaScannerConnectionClient(context, file.getAbsolutePath()).getMsc().connect();
				} catch (Throwable e) {
					Debug_Log.e(e);
				}
			}

//			context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri
//					.parse(url)));
			return file.getAbsolutePath();// 返回截图路径对应url
		} catch (Exception e) {
			Debug_Log.e(e);
		}

		return null;
	}

	static class MyMediaScannerConnectionClient implements MediaScannerConnectionClient {
		private MediaScannerConnection msc;
		private String mFilePath;

		public MyMediaScannerConnectionClient(Context context, String filePath) {
			this.msc = new MediaScannerConnection(context, this);
			mFilePath = filePath;
		}

		public MediaScannerConnection getMsc() {
			return msc;
		}

		@Override
		public void onMediaScannerConnected() {
			msc.scanFile(mFilePath, "image/jpeg");
		}

		@Override
		public void onScanCompleted(String path, Uri uri) {
			msc.disconnect();
		}
	}
}