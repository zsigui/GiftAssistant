package net.youmi.android.libs.common.util.zip.decompress;

import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.global.Global_Charsets;
import net.youmi.android.libs.common.util.Util_System_File;
import net.youmi.android.libs.common.util.Util_System_Runtime;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class UnZip implements Runnable {

	private final static int BUFF_SIZE = 2048;

	private File mZipFile;

	private String mDestDirPath;

	private IUnZipListener mListener;

	private boolean mIsCallBackInUiThread;

	/**
	 * @param zipFile              zip文件
	 * @param destDirPath          解压目录路径
	 * @param unZipListener        解压过程中的回调监听，可以为空
	 * @param isCallBackInUiThread 监听的各个回调是否执行在UI线程中
	 *                             <ul>
	 *                             <li>true ： 是</li>
	 *                             <li>false： 否</li>
	 *                             </ul>
	 */
	public UnZip(File zipFile, String destDirPath, IUnZipListener unZipListener, boolean isCallBackInUiThread) {
		super();
		this.mZipFile = zipFile;
		if (destDirPath != null) {
			this.mDestDirPath = destDirPath;
		} else {
			try {
				this.mDestDirPath = zipFile.getParent() + File.separator + zipFile.getName().split("\\.")[0];
			} catch (Throwable e) {
				this.mDestDirPath = zipFile.getParent() + File.separator + System.currentTimeMillis();
			}
		}
		this.mListener = unZipListener;
		this.mIsCallBackInUiThread = isCallBackInUiThread;
	}

	/**
	 * @param zipFile     zip文件
	 * @param destDirPath 解压目录路径
	 */
	public UnZip(File zipFile, String destDirPath) {
		this(zipFile, destDirPath, null, false);
	}

	@Override
	public void run() {
		unZip();
	}

	public boolean unZip() {
		// 初始化结果为成功
		boolean isUnZipSuccess = true;

		ZipFile zfile = null;
		int totalCount = 0; // 解压的总条目数量
		int failCount = 0; // 解压过程中解压失败的条目计数器
		try {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.td(Debug_SDK.mUtilTag, this, "待解压zip文件: %s  name:%s", mZipFile.getPath(), mZipFile.getName());
			}
			File destDir = new File(mDestDirPath);
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.td(Debug_SDK.mUtilTag, this, "解压缩目录: %s", destDir.getPath());
			}
			// 然后尝试清空解压缩目录，然后最后需要重新创建这个目录
			Util_System_File.delete(destDir);
			destDir.mkdirs();

			// 获取zip文件里面的所有条目，然后遍历判断是否
			zfile = new ZipFile(mZipFile);
			Enumeration<? extends ZipEntry> zList = zfile.entries();
			ZipEntry ze = null;
			int currentIndex = 0;

			if (zfile.size() > 0) {
				totalCount = zfile.size();
				publishUnZipStart();
			}
			while (zList.hasMoreElements()) {
				ze = (ZipEntry) zList.nextElement();
				publishUnZipProgressUpdate((++currentIndex) * 100 / zfile.size());

				// 这里先将条目转码
				String zeName = new String(ze.getName().getBytes(Global_Charsets.GBK));
				File file = new File(mDestDirPath, zeName);

				// if (Debug_SDK.isUtilLog) {
				// Debug_SDK.td(Debug_SDK.mUtilTag, this,
				// "条目：%s %s， 条目最后存放的位置：%s", ze.getName(),
				// ze.isDirectory() ? "是目录" : "不是目录", file.getPath());
				// }
				if (ze.isDirectory()) {
					// // 先尝试修正一下输入数据
					// if (mDestDirPath.endsWith(File.separator)) {
					// mDestDirPath = mDestDirPath.substring(0,
					// mDestDirPath.length() - 1);
					// }
					// String dirstr = mDestDirPath + ze.getName();
					// dirstr = new
					// String(dirstr.getBytes(Global_Charsets.ISO_8859_1),
					// Global_Charsets.GBK);
					// dirstr = new String(dirstr.getBytes("8859_1"), "GB2312");
					// File f = new File(dirstr);
					boolean isSuccess = file.mkdir();
					if (!isSuccess) {
						++failCount;
					}
					// if (Debug_SDK.isUtilLog) {
					// if (isSuccess) {
					// Debug_SDK.td(Debug_SDK.mUtilTag, this, "  == 创建目录成功");
					// } else {
					// Debug_SDK.te(Debug_SDK.mUtilTag, this, "  == 创建目录失败");
					// }
					// }
					continue;
				}
				InputStream is = null;
				OutputStream os = null;
				try {
					File temp = Util_System_File.getVaildFile(file);
					if (temp != null && temp.exists() && temp.isFile()) {
						// if (Debug_SDK.isUtilLog) {
						// Debug_SDK.td(Debug_SDK.mUtilTag, this,
						// "  == 创建文件成功");
						// }
						os = new BufferedOutputStream(new FileOutputStream(temp));
						is = new BufferedInputStream(zfile.getInputStream(ze)); // 获取条目的stream
						// 然后写入打上面的文件
						int readLen = 0;
						byte[] buf = new byte[BUFF_SIZE];
						while ((readLen = is.read(buf, 0, 1024)) != -1) {
							os.write(buf, 0, readLen);
						}
					} else {
						// if (Debug_SDK.isUtilLog) {
						// Debug_SDK.te(Debug_SDK.mUtilTag, this,
						// "  == 创建文件失败");
						// }
					}
				} catch (Exception e) {
					if (Debug_SDK.isUtilLog) {
						Debug_SDK.te(Debug_SDK.mUtilTag, this, e);
					}
					++failCount;
				} finally {
					try {
						if (is != null) {
							is.close();
						}
					} catch (Throwable e2) {
						if (Debug_SDK.isUtilLog) {
							Debug_SDK.te(Debug_SDK.mUtilTag, this, e2);
						}
					}
					try {
						if (os != null) {
							os.close();
						}
					} catch (Throwable e2) {
						if (Debug_SDK.isUtilLog) {
							Debug_SDK.te(Debug_SDK.mUtilTag, this, e2);
						}
					}
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, this, e);
			}
			isUnZipSuccess = false;
			publishUnZipError();
		} finally {
			try {
				if (zfile != null) {
					zfile.close();
				}
			} catch (Throwable e2) {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.te(Debug_SDK.mUtilTag, this, e2);
				}
			}
			if (isUnZipSuccess) {
				publishUnZipFinish(totalCount, failCount);
				if (failCount > 0) {
					isUnZipSuccess = false;
				}
			}
		}
		return isUnZipSuccess;

	}

	private void publishUnZipStart() {
		if (mListener == null) {
			return;
		}

		if (!mIsCallBackInUiThread) {
			mListener.onUnZipStart();
		} else {
			if (Util_System_Runtime.isInUIThread()) {
				mListener.onUnZipStart();
			} else {
				Util_System_Runtime.getInstance().runInUiThread(new Runnable() {

					@Override
					public void run() {
						mListener.onUnZipStart();
					}
				});
			}
		}
	}

	private void publishUnZipProgressUpdate(final int precent) {
		if (mListener == null) {
			return;
		}

		if (!mIsCallBackInUiThread) {
			mListener.onUnZipProgressUpdate(precent);
		} else {
			if (Util_System_Runtime.isInUIThread()) {
				mListener.onUnZipProgressUpdate(precent);
			} else {
				Util_System_Runtime.getInstance().runInUiThread(new Runnable() {

					@Override
					public void run() {
						mListener.onUnZipProgressUpdate(precent);
					}
				});
			}

		}
	}

	private void publishUnZipFinish(final int totalCount, final int failCount) {
		if (mListener == null) {
			return;
		}

		if (!mIsCallBackInUiThread) {
			mListener.onUnZipFinish(totalCount, failCount);
		} else {
			if (Util_System_Runtime.isInUIThread()) {
				mListener.onUnZipFinish(totalCount, failCount);
			} else {
				Util_System_Runtime.getInstance().runInUiThread(new Runnable() {

					@Override
					public void run() {
						mListener.onUnZipFinish(totalCount, failCount);
					}
				});
			}
		}
	}

	private void publishUnZipError() {
		if (mListener == null) {
			return;
		}

		if (!mIsCallBackInUiThread) {
			mListener.onUnZipError();
		} else {
			if (Util_System_Runtime.isInUIThread()) {
				mListener.onUnZipError();
			} else {
				Util_System_Runtime.getInstance().runInUiThread(new Runnable() {

					@Override
					public void run() {
						mListener.onUnZipError();
					}
				});
			}
		}
	}

}
