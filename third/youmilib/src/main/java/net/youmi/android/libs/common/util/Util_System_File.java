package net.youmi.android.libs.common.util;

import android.content.Context;

import net.youmi.android.libs.common.debug.Debug_SDK;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 文件操作类
 *
 * @author jen
 */
public class Util_System_File {

	/**
	 * 改变原始文件的权限
	 *
	 * @param file
	 * @param destFilePermission
	 *
	 * @return
	 */
	public static boolean chmod(File file, String destFilePermission) {
		try {

			if (file == null) {
				return false;
			}

			if (!file.exists()) {
				return false;
			}

			if (destFilePermission != null) {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.td(Debug_SDK.mUtilTag, Util_System_File.class, "chmod file: %s permission is %s",
							file.getAbsolutePath(), destFilePermission);
				}

				StringBuilder sb = new StringBuilder(100);
				sb.append("chmod ").append(destFilePermission).append(" ").append(file.getAbsolutePath());
				String cmd = sb.toString();
				Runtime.getRuntime().exec(cmd);
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.td(Debug_SDK.mUtilTag, Util_System_File.class, "chmod cmd is:[%s]", destFilePermission);
				}
				return true;
			}
		} catch (Throwable e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_File.class, e);
			}
		}
		return false;

	}

	/**
	 * 移动文件，成功之后会删除原始文件
	 *
	 * @param srcFile
	 * @param destFile
	 *
	 * @return
	 */
	public static boolean mv(File srcFile, File destFile) {
		try {

			if (srcFile == null || destFile == null) {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.td(Debug_SDK.mUtilTag, Util_System_File.class, "move file failed: src file or dest file is null");
				}
				return false;
			}

			if (!srcFile.exists()) {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.td(Debug_SDK.mUtilTag, Util_System_File.class, "move file failed: src file is exists == false");
				}
				return false;
			}

			if (srcFile.renameTo(destFile)) {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.td(Debug_SDK.mUtilTag, Util_System_File.class, "move file success: srcFile.renameTo destFile");
				}
				return true;
			}

			if (cp(srcFile, destFile)) {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.td(Debug_SDK.mUtilTag, Util_System_File.class, "move file: copy file success");
				}

				try {
					// 删除原始文件
					if (srcFile.delete()) {
						if (Debug_SDK.isUtilLog) {
							Debug_SDK.td(Debug_SDK.mUtilTag, Util_System_File.class, "move file: delete src file success");
						}
					} else {
						if (Debug_SDK.isUtilLog) {
							Debug_SDK.td(Debug_SDK.mUtilTag, Util_System_File.class, "move file: delete src file failed");
						}
					}
				} catch (Throwable e) {
					if (Debug_SDK.isUtilLog) {
						Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_File.class, e);
					}
				}
				return true;
			}
		} catch (Throwable e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_File.class, e);
			}
		}

		return false;
	}

	/**
	 * 复制文件
	 *
	 * @param srcFile
	 * @param destFile
	 *
	 * @return
	 */
	public static boolean cp(File srcFile, File destFile) {

		FileOutputStream fos = null;
		FileInputStream fis = null;

		long startTime = System.currentTimeMillis();
		long fileLen = 0;
		String fileNameSrc = null;
		String fileNameDest = null;
		try {
			if (srcFile == null) {
				return false;
			}

			if (!srcFile.exists()) {
				return false;
			}

			if (destFile == null) {
				return false;
			}
			try {

				fileLen = srcFile.length();
				fileNameSrc = srcFile.getAbsolutePath();
				fileNameDest = destFile.getAbsolutePath();

			} catch (Throwable e) {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_File.class, e);
				}
			}

			fis = new FileInputStream(srcFile);
			fos = new FileOutputStream(destFile);

			byte[] buff = new byte[1024];
			int len = 0;

			while ((len = fis.read(buff)) > 0) {
				fos.write(buff, 0, len);
			}

			fos.flush();
			fos.close();
			fos = null;
			return true;

		} catch (Throwable e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_File.class, e);
			}
		} finally {
			try {
				if (fos != null) {
					fos.close();
				}
			} catch (Throwable e) {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_File.class, e);
				}
			}

			try {
				if (fis != null) {
					fis.close();
				}
			} catch (Throwable e) {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_File.class, e);
				}
			}

			if (Debug_SDK.isUtilLog) {
				long nt = System.currentTimeMillis();
				long span = nt - startTime;
				Debug_SDK.td(Debug_SDK.mUtilTag, Util_System_File.class,
						"copy file from [%s] to [%s] , length is [%d] B , cost [%d] ms", fileNameSrc, fileNameDest, fileLen,
						span);
			}
		}
		return false;
	}

	/**
	 * （请使用线程执行）从Assets中复制文件
	 *
	 * @param context
	 * @param srcFileName
	 * @param destFile
	 *
	 * @return
	 */
	public static boolean cpFromAssets(Context context, String srcFileName, File destFile) {
		InputStream inputStream = null;
		FileOutputStream outputStream = null;
		try {
			if (context == null) {
				return false;
			}
			if (srcFileName == null) {
				return false;
			}

			if (destFile == null) {
				return false;
			}

			inputStream = context.getAssets().open(srcFileName);
			outputStream = new FileOutputStream(destFile);
			int len = 0;
			byte[] buff = new byte[1024];

			while ((len = inputStream.read(buff)) > 0) {

				outputStream.write(buff, 0, len);
			}

			outputStream.flush();

			outputStream.close();
			outputStream = null;

			return true;

		} catch (Throwable e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_File.class, e);
			}
		} finally {
			try {
				if (outputStream != null) {
					outputStream.close();
				}
			} catch (Throwable e) {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_File.class, e);
				}
			}
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (Throwable e) {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_File.class, e);
				}
			}
		}
		return false;
	}

	/**
	 * （同步）支持删除文件或者文件夹，使用时请注意启用线程
	 * <p/>
	 * 使用时<strong>请注意启用线程</strong>
	 *
	 * @param file 要删除的文件或者目录
	 *
	 * @return true or false
	 */
	public final static boolean delete(final File file) {
		try {
			if (file == null) {
				return false;
			}
			if (file.exists()) {
				if (file.isFile()) {
					boolean isSuccess = file.delete();
					if (Debug_SDK.isUtilLog) {
						if (isSuccess) {
							Debug_SDK.ti(Debug_SDK.mUtilTag, Util_System_File.class, "删除成功： %s", file.getAbsolutePath());
						} else {
							Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_File.class, "删除失败： %s", file.getAbsolutePath());
						}
					}
					return isSuccess;
				} else if (file.isDirectory()) {
					for (File f : file.listFiles()) {
						if (!delete(f)) {
							return false;
						}
					}
					boolean isSuccess = file.delete();
					if (Debug_SDK.isUtilLog) {
						if (isSuccess) {
							Debug_SDK.ti(Debug_SDK.mUtilTag, Util_System_File.class, "删除成功： %s", file.getAbsolutePath());
						} else {
							Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_File.class, "删除失败： %s", file.getAbsolutePath());
						}
					}
					return isSuccess;
				}
			} else {
				// 因为最终目的是令该文件不存在，所以如果文件一开始就不存在，那么也就意味着删除成功
				return true;
			}
		} catch (Throwable e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_Zip.class, e);
			}
		}
		return false;
	}

	/**
	 * 获取指定的路径的文件，如果没有会创建(支持自动补全所有父目录)
	 *
	 * @param file 文件
	 */
	public final static File getVaildFile(File file) {
		try {
			if (file == null) {
				return null;
			}
			// 先检查是不是目录，如果是的话就没办法啦，返回null
			if (file.exists()) {
				if (file.isDirectory()) {
					return null;
				}
				// 如果不是目录也不能立即返回，需要检查父目录
			}

			// 如果不存在父目录的话, 自动补全所有的根目录,然后创建
			if (!file.getParentFile().exists()) {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.tw(Debug_SDK.mUtilTag, Util_System_File.class, "当前文件：%s不存在父目录，将补全", file.getAbsoluteFile());
				}
				String[] filePathParts = file.getPath().split(File.separator);
				StringBuilder parentDirPath = new StringBuilder();
				for (int i = 0; i < filePathParts.length - 1; ++i) {
					parentDirPath.append(File.separator).append(filePathParts[i]);
					// File parentDirFile = new File(parentDirPath.toString());
					// if (!parentDirFile.exists()) {
					// if (Debug_SDK.isUtilLog) {
					// Debug_SDK.te(Debug_SDK.mUtilTag, Util_Zip.class, "路径:%s 不存在任何内容",
					// parentDirFile.getAbsoluteFile());
					// }
					// // boolean chmodResult = chmod(parentDirFile.getParentFile(), "777");
					// // if (Debug_SDK.isUtilLog) {
					// // Debug_SDK.te(Debug_SDK.mUtilTag, Util_Zip.class, "修改 %s 权限 %s", parentDirFile.getParent(),
					// // chmodResult ? "成功" : "失败");
					// // }
					// boolean result = parentDirFile.mkdir();
					// if (Debug_SDK.isUtilLog) {
					// Debug_SDK.te(Debug_SDK.mUtilTag, Util_Zip.class, "目录%s 创建 %s",
					// parentDirFile.getAbsoluteFile(), result ? "成功" : "失败");
					// }
					// } else {
					// // boolean chmodResult = chmod(parentDirFile, "777");
					// // if (Debug_SDK.isUtilLog) {
					// // Debug_SDK.te(Debug_SDK.mUtilTag, Util_Zip.class, "修改 %s 权限 %s", parentDirFile.getParent(),
					// // chmodResult ? "成功" : "失败");
					// // }
					// if (Debug_SDK.isUtilLog) {
					// Debug_SDK.te(Debug_SDK.mUtilTag, Util_Zip.class, "路径:%s 存在 是%s",
					// parentDirFile.getAbsoluteFile(),
					// parentDirFile.isFile() ? "文件" : parentDirFile.isDirectory() ? "目录" : "其他");
					// }
					// }
				}
				File parentDirFile = new File(parentDirPath.toString());
				if (!parentDirFile.exists()) {
					if (Debug_SDK.isUtilLog) {
						Debug_SDK
								.tw(Debug_SDK.mUtilTag, Util_System_File.class, "路径:%s,不存在任何内容", parentDirFile.getAbsoluteFile
										());
					}
				}
				boolean result = parentDirFile.mkdirs();
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.ti(Debug_SDK.mUtilTag, Util_System_File.class, "补全父目录%s %s", parentDirFile.getAbsoluteFile(),
							result ? "成功" : "失败");
				}
			}
			file.createNewFile();
			return file;
		} catch (Throwable e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_File.class, e);
			}
		}
		return null;
	}

	/**
	 * 获取指定的路径的文件，如果没有会创建(支持自动补全所有父目录)
	 *
	 * @param path 文件路径
	 */
	public final static File getVaildFile(String path) {
		return getVaildFile(new File(path));
	}

	@Deprecated
	public static boolean copyStream(InputStream inputStream, OutputStream outputStream) {
		try {

			if (inputStream == null) {
				return false;
			}

			if (outputStream == null) {
				return false;
			}

			int len = 0;

		} catch (Throwable e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_File.class, e);
			}
		}
		return false;
	}

}
