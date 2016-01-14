package net.ouwan.umipay.android.Utils;

import android.content.Context;
import android.os.Environment;

import org.apache.http.util.EncodingUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Util_FileHelper
 *
 * @author zacklpx
 *         date 15-3-5
 *         description
 */
public class Util_FileHelper {
	public static void deleteDir(File f) {
		if (f.exists() && f.isDirectory()) {
			for (File file : f.listFiles()) {
				if (file.isDirectory())
					deleteDir(file);
				file.delete();
			}
			f.delete();
		}
	}

	public static String getCanonical(File f) {
		if (f == null)
			return null;

		try {
			return f.getCanonicalPath();
		} catch (IOException e) {
			return f.getAbsolutePath();
		}
	}

	public static File[] listFilesAccordingPref(File f,
	                                            final boolean hiddenShown) {
		return f.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				if (filename == null)
					return false;
				File f = new File(dir, filename);
				if (!f.canRead() || !hiddenShown && f.isHidden())
					return false;
				return true;
			}
		});
	}

	public static void sortFilesBySize(File[] files) {
		Arrays.sort(files, new Comparator<File>() {
			@Override
			public int compare(File f1, File f2) {
				if (f1.isDirectory() && f2.isDirectory() || f1.isFile()
						&& f2.isFile())
					return Long.valueOf(f2.length()).compareTo(f1.length());
				else if (f1.isDirectory() && f2.isFile())
					return -1;
				else
					return 1;
			}
		});
	}

	public static void sortFilesByName(File[] files) {
		Arrays.sort(files, new Comparator<File>() {
			@Override
			public int compare(File f1, File f2) {
				if (f1.isDirectory() && f2.isDirectory() || f1.isFile()
						&& f2.isFile())
					return f1.getName().trim()
							.compareToIgnoreCase(f2.getName().trim());
				else if (f1.isDirectory() && f2.isFile())
					return -1;
				else
					return 1;
			}
		});
	}

	public static String getUrlFileName(String url) {
		int slashIndex = url.lastIndexOf('/');
		if (slashIndex > -1)
			return url.substring(slashIndex + 1);
		else
			return url;
	}

	public static String getUrlFileNameNoEx(String url) {
		int slashIndex = url.lastIndexOf('/');
		int dotIndex = url.lastIndexOf('.');
		String filenameWithoutExtension;
		if (dotIndex == -1) {
			filenameWithoutExtension = url.substring(slashIndex + 1);
		} else {
			filenameWithoutExtension = url.substring(slashIndex + 1, dotIndex);
		}
		return filenameWithoutExtension;
	}

	public static String getFileNameNoEx(String filename) {
		if ((filename != null) && (filename.length() > 0)) {
			int dot = filename.lastIndexOf('.');
			if ((dot > -1) && (dot < (filename.length()))) {
				return filename.substring(0, dot);
			}
		}
		return filename;
	}

	public static boolean sdAvailable() {
		return Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment
				.getExternalStorageState())
				|| Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState());
	}

	public static String getFileNameForTitle(String title) {
		int lastDot = title.lastIndexOf('.');
		return lastDot > 0 ? title.substring(0, lastDot) : title;
	}

	public static File getExternalStoragePublicDirectory(String type) {
		return new File(Environment.getExternalStorageDirectory(), type);
	}

	public static int getFileAvailable(String file) {
		return getFileAvailable(new File(file));
	}

	public static int getFileAvailable(File file) {
		int ds = 0;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			ds = fis.available();
		} catch (Exception e) {
		} finally {
			try {
				if (fis != null)
					fis.close();
			} catch (Exception e) {

			}
		}
		return ds;
	}

	public static void writeFileSdcard(String fileName, String message) {
		try {
			FileOutputStream fout = new FileOutputStream(fileName);
			BufferedOutputStream bos = new BufferedOutputStream(fout);
			byte[] bytes = message.getBytes();
			bos.write(bytes);
			bos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String readFileSdcard(String fileName) {
		String res = "";
		BufferedInputStream bis = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			FileInputStream fin = new FileInputStream(fileName);
			bis = new BufferedInputStream(fin);
			byte[] buffer = new byte[4 * 1024];
			int len = 0;
			while ((len = bis.read(buffer)) != -1) {
				baos.write(buffer, 0, len);
			}
			res = EncodingUtils.getString(baos.toByteArray(), "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			baos.close();
			if (bis != null) {
				bis.close();
			}
		} catch (Throwable e) {
		}
		return res;
	}

	public static String readFileAssets(Context context, String fileAssetsPath) {
		String res = "";
		BufferedInputStream bis = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			bis = new BufferedInputStream(context.getAssets().open(fileAssetsPath));
			byte[] buffer = new byte[4 * 1024];
			int len = 0;
			while ((len = bis.read(buffer)) != -1) {
				baos.write(buffer, 0, len);
			}
			res = EncodingUtils.getString(baos.toByteArray(), "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			baos.close();
			if (bis != null) {
				bis.close();
			}
		} catch (Throwable e) {
		}
		return res;
	}
}
