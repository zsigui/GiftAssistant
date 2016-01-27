package com.oplay.giftcool.util;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Base64;

import com.socks.library.KLog;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by zsigui on 16-1-17.
 */
public class BitmapUtil {
	private static final String TAG = BitmapUtil.class.getSimpleName();

	/**
	 * convert Bitmap to byte array
	 */
	public static byte[] bitmapToByte(Bitmap b) {
		ByteArrayOutputStream o = new ByteArrayOutputStream();
		b.compress(Bitmap.CompressFormat.PNG, 100, o);
		return o.toByteArray();
	}

	/**
	 * convert byte array to Bitmap
	 */
	public static Bitmap byteToBitmap(byte[] b) {
		return (b == null || b.length == 0) ? null : BitmapFactory.decodeByteArray(b, 0, b.length);
	}

	/**
	 * 把bitmap转换成Base64编码String
	 */
	public static String bitmapToString(Bitmap bitmap) {
		return Base64.encodeToString(bitmapToByte(bitmap), Base64.DEFAULT);
	}

	/**
	 * convert Drawable to Bitmap
	 */
	public static Bitmap drawableToBitmap(Drawable drawable) {
		return drawable == null ? null : ((BitmapDrawable) drawable).getBitmap();
	}

	/**
	 * convert Bitmap to Drawable
	 */
	public static Drawable bitmapToDrawable(Bitmap bitmap) {
		return bitmap == null ? null : new BitmapDrawable(bitmap);
	}

	/**
	 * scale image
	 */
	public static Bitmap scaleImageTo(Bitmap org, int newWidth, int newHeight) {
		return scaleImage(org, (float) newWidth / org.getWidth(), (float) newHeight / org.getHeight());
	}

	/**
	 * scale image
	 */
	public static Bitmap scaleImage(Bitmap org, float scaleWidth, float scaleHeight) {
		if (org == null) {
			return null;
		}
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		return Bitmap.createBitmap(org, 0, 0, org.getWidth(), org.getHeight(), matrix, true);
	}

	public static Bitmap toRoundCorner(Bitmap bitmap) {
		int height = bitmap.getHeight();
		int width = bitmap.getHeight();
		Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

		Canvas canvas = new Canvas(output);

		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, width, height);

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		//paint.setColor(0xff424242);
		paint.setColor(Color.TRANSPARENT);
		canvas.drawCircle(width / 2, height / 2, width / 2, paint);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		return output;
	}

	public static Bitmap createBitmapThumbnail(Bitmap bitMap, boolean needRecycle, int newHeight, int newWidth) {
		int width = bitMap.getWidth();
		int height = bitMap.getHeight();
		// 计算缩放比例
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// 取得想要缩放的matrix参数
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		// 得到新的图片
		Bitmap newBitMap = Bitmap.createBitmap(bitMap, 0, 0, width, height, matrix, true);
		if (needRecycle)
			bitMap.recycle();
		return newBitMap;
	}

	public static boolean saveBitmap(Bitmap bitmap, File file) {
		if (bitmap == null)
			return false;
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
			fos.flush();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	public static boolean saveBitmap(Bitmap bitmap, String absPath) {
		return saveBitmap(bitmap, new File(absPath));
	}

	public static Intent buildImageGetIntent(Uri saveTo, int outputX, int outputY, boolean returnData) {
		return buildImageGetIntent(saveTo, 1, 1, outputX, outputY, returnData);
	}

	public static Intent buildImageGetIntent(Uri saveTo, int aspectX, int aspectY,
	                                         int outputX, int outputY, boolean returnData) {
		Intent intent = new Intent();
		if (Build.VERSION.SDK_INT < 19) {
			intent.setAction(Intent.ACTION_GET_CONTENT);
		} else {
			intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
			intent.addCategory(Intent.CATEGORY_OPENABLE);
		}
		intent.setType("image/*");
		intent.putExtra("output", saveTo);
		intent.putExtra("aspectX", aspectX);
		intent.putExtra("aspectY", aspectY);
		intent.putExtra("outputX", outputX);
		intent.putExtra("outputY", outputY);
		intent.putExtra("scale", true);
		intent.putExtra("return-data", returnData);
		intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
		return intent;
	}

	public static Intent buildImageCropIntent(Uri uriFrom, Uri uriTo, int outputX, int outputY, boolean returnData) {
		return buildImageCropIntent(uriFrom, uriTo, 1, 1, outputX, outputY, returnData);
	}

	public static Intent buildImageCropIntent(Uri uriFrom, Uri uriTo, int aspectX, int aspectY,
	                                          int outputX, int outputY, boolean returnData) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uriFrom, "image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("output", uriTo);
		intent.putExtra("aspectX", aspectX);
		intent.putExtra("aspectY", aspectY);
		intent.putExtra("outputX", outputX);
		intent.putExtra("outputY", outputY);
		intent.putExtra("scale", true);
		intent.putExtra("return-data", returnData);
		intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
		return intent;
	}

	public static Intent buildImageCaptureIntent(Uri uri) {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		return intent;
	}

	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		int h = options.outHeight;
		int w = options.outWidth;
		int inSampleSize = 0;
		if (h > reqHeight || w > reqWidth) {
			float ratioW = (float) w / reqWidth;
			float ratioH = (float) h / reqHeight;
			inSampleSize = (int) Math.min(ratioH, ratioW);
		}
		inSampleSize = Math.max(1, inSampleSize);
		return inSampleSize;
	}

	public static Bitmap getSmallBitmap(String filePath, int reqWidth, int reqHeight) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(filePath, options);
	}

	public byte[] compressBitmapToBytes(String filePath, int reqWidth, int reqHeight, int quality) {
		Bitmap bitmap = getSmallBitmap(filePath, reqWidth, reqHeight);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
		byte[] bytes = baos.toByteArray();
		bitmap.recycle();
		return bytes;
	}

	public byte[] compressBitmapSmallTo(String filePath, int reqWidth, int reqHeight, int maxLenth) {
		int quality = 100;
		byte[] bytes = compressBitmapToBytes(filePath, reqWidth, reqHeight, quality);
		while (bytes.length > maxLenth && quality > 0) {
			quality = quality / 2;
			bytes = compressBitmapToBytes(filePath, reqWidth, reqHeight, quality);
		}
		return bytes;
	}

	public byte[] compressBitmapQuikly(String filePath) {
		return compressBitmapToBytes(filePath, 480, 800, 50);
	}

	public byte[] compressBitmapQuiklySmallTo(String filePath, int maxLenth) {
		return compressBitmapSmallTo(filePath, 480, 800, maxLenth);
	}

	/**
	 * 根据图片路径进行压缩图片
	 * @param srcPath
	 * @return
	 */
	public static Bitmap getBitmap(String srcPath, int size, int width, int height) {
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		//开始读入图片，此时把options.inJustDecodeBounds 设回true了,表示只返回宽高
		newOpts.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(srcPath,newOpts);//此时返回bm为空

		newOpts.inJustDecodeBounds = false;
		//当前图片宽高
		float w = newOpts.outWidth;
		float h = newOpts.outHeight;
		float hh = width;
		float ww = height;
		//缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
		int be = 1;//be=1表示不缩放
		if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
			KLog.d("fileupload","------原始缩放比例 --------" + (newOpts.outWidth / ww));
			be = (int)(newOpts.outWidth / ww);
			//有时会出现be=3.2或5.2现象，如果不做处理压缩还会失败
			if ((newOpts.outWidth / ww) > be) {

				be += 1;
			}
			//be = Math.round((float) newOpts.outWidth / (float) ww);
		} else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
			KLog.d("fileupload","------原始缩放比例 --------" + (newOpts.outHeight / hh));
			be = (int)(newOpts.outHeight / hh);
			if ((newOpts.outHeight / hh) > be) {

				be += 1;
			}
			//be = Math.round((float) newOpts.outHeight / (float) hh);
		}
		if (be <= 0){

			be = 1;
		}
		newOpts.inSampleSize = be;//设置缩放比例
		KLog.d("fileupload","------设置缩放比例 --------" + newOpts.inSampleSize);
		//重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
		bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
		return compressImage(bitmap,size);//压缩好比例大小后再进行质量压缩
	}

	/**
	 * 压缩图片
	 * @param image
	 * @param size
	 * @return
	 */
	private static Bitmap compressImage(Bitmap image,int size) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
		int options = 100;

		while ((baos.toByteArray().length / 1024) >= size) {  //循环判断如果压缩后图片是否大于等于size,大于等于继续压缩
			baos.reset();//重置baos即清空baos
			options -= 5;//每次都减少5
			image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
		}
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
		return bitmap;
	}

	/**
	 * Bitmap转byte数组
	 * @param bitmap
	 * @return
	 */
	public static byte[] compressBitmap(Bitmap bitmap) {
		if (bitmap == null ) {
			return null;//
		}
		/* 取得相片 */
		Bitmap tempBitmap = bitmap;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		tempBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);// 如果签名是png的话，则不管quality是多少，都不会进行质量的压缩
		byte[] byteData = baos.toByteArray();
		return byteData;
	}

	public static Bitmap compressResize(Bitmap bitmap, int reqWidth, int reqHeight) {
		final int width = bitmap.getWidth();
		final int height = bitmap.getHeight();
		float scaleWidth = ((float) reqWidth) / width;
		float scaleHeight = ((float) reqHeight) / height;
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		final Bitmap desBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
		return desBitmap;
	}
}
