package net.youmi.android.libs.common.v2.download.model;

import android.util.SparseArray;

import net.youmi.android.libs.common.basic.Basic_StringUtil;
import net.youmi.android.libs.common.coder.Coder_Md5;
import net.youmi.android.libs.common.debug.Debug_SDK;

import java.io.File;

/**
 * 下载任务模型
 * <p/>
 * v1版本:
 * 本类会以最终下载url的md5后32位字符串的hashcode作为本类示例的hashcode
 * <p/>
 * v2版本: 不建议继承本类
 * 默认情况下：本类会以原始下载url的md5后32位字符串的hashcode作为本类示例的hashcode
 * <p/>
 * <b>But</b>
 * 有时候，同一个资源下载，可能不同时刻创建的下载url会不同，如url中加入聊请求时间戳，那么每次的下载url都会不同
 * 但是其实还是同一个下载任务，那么这个时候需要调用 {@link #setIdentify(String)} 进行自定义本类的hashcode，标记上面话说的这种url其实都是同一个对象
 * <p/>
 * <p/>
 *
 * @version 2
 */
final public class FileDownloadTask {

	/**
	 * 原始文件下载链接(可能为302/301的url) 也有可能直接等于 {@link #mDestDownloadUrl}
	 */
	private String mRawDownloadUrl;

	/**
	 * 文件最终下载地址(最后的下载地址,如:***.apk)
	 * <p/>
	 * 默认是等于原始地址
	 * <p/>
	 * 如果下载的URL是重定向类型，那么在重定向到最终地址时，这个值需要通过{@link #setDestDownloadUrl(String)} 进行修改
	 */
	private String mDestDownloadUrl;

	/**
	 * 缓存文件位置，进入下载之前创建，不在创建对象的时候传入
	 * <p/>
	 * 下载时会先下载在缓存文件的位置上,下载成功之后会重命名缓存文件为最终文件的名字（缓存文件会被删除）
	 */
	private File mTempFile;

	/**
	 * 文件的最终保存位置，创建对象的时候传入
	 */
	private File mStoreFile;

	/**
	 * 本次下载文件在服务器中的md5校验码
	 */
	private String mDownloadFileMd5sum;

	/**
	 * 缓存文件的下载总长度
	 */
	private long mTotalLength = -1;

	/**
	 * 定义本次任务的下载进度回调时间间隔为每1秒回调一次
	 */
	private int mIntervalTime_ms = 1000;

	/**
	 * 额外业务对象的存储模型
	 */
	private SparseArray<IFileDownloadTaskExtendObject> mIFileDownloadTaskExtendObjectSparseArray;

	private String mIdentify = null;

	/**
	 * @param rawDownloadUrl     原始下载url
	 * @param downloadFileMd5Sum 服务器上本次下载文件的md5值，用于下载完毕后的校验，可以不传入来
	 * @param totalLength        下载文件的总长度，用于下载完毕后的校验，可以不传入来(即传入-1)
	 * @param intervalTime_ms    本次任务的下载进度回调时间间隔(默认为1000ms)，可以不传入来(即传入-1)
	 */
	public FileDownloadTask(String rawDownloadUrl, String downloadFileMd5Sum, long totalLength, int intervalTime_ms) {
		mRawDownloadUrl = rawDownloadUrl;
		mDestDownloadUrl = mRawDownloadUrl;
		mDownloadFileMd5sum = downloadFileMd5Sum;
		if (totalLength > 0) {
			mTotalLength = totalLength;
		}
		if (intervalTime_ms > 0) {
			mIntervalTime_ms = intervalTime_ms;
		}
	}

	public FileDownloadTask(String rawDownloadUrl, String downloadFileMd5sum) {
		this(rawDownloadUrl, downloadFileMd5sum, -1, -1);
	}

	public FileDownloadTask(String rawDownloadUrl) {
		this(rawDownloadUrl, null);
	}

	/**
	 * 获取传入来的标识，不等于本类的hashcode
	 *
	 * @return
	 */
	public String getIdentify() {
		return mIdentify;
	}

	/**
	 * 设置本类的唯一标示，不掉用的话，本类会默认用原始下载地址的url的hashcode作为本类的hashcode
	 * <p/>
	 * 本方法的使用场合：如果同一个下载资源，可能会有多个原始下载地址的url（如url中加入了请求时间戳），那么建议用本方法设置一下新的标识(hashcode)
	 *
	 * @param identyfy
	 */
	public void setIdentify(String identyfy) {
		mIdentify = identyfy;
	}

	/**
	 * 检查当前下载任务是否可用
	 *
	 * @return
	 */
	public boolean isValid() {
		if (Basic_StringUtil.isNullOrEmpty(mRawDownloadUrl)) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, "下载任务无效：原始下载地址无效");
			}
			return false;
		}
		// 因为一开始是不能确定最终下载地址的，所以这里代码注释
		//		if (Basic_StringUtil.isNullOrEmpty(mDestDownloadUrl)) {
		//			if (Debug_SDK.isDownloadLog) {
		//				Debug_SDK.te(Debug_SDK.mDownloadTag,this, "最终下载地址无效");
		//			}
		//			return false;
		//		}
		if (hashCode() == 0) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, "下载任务无效：hashcode = 0");
			}
			return false;
		}
		if (mTempFile == null) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, "下载任务无效：缓存文件对象为空，需要先调用 setTempFile");
			}
			return false;
		}

		if (mStoreFile == null) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, "下载任务无效：最终存储文件对象为空，需要先调用 setStoreFile");
			}
			return false;
		}

		if (mIFileDownloadTaskExtendObjectSparseArray != null) {
			for (int i = 0; i < mIFileDownloadTaskExtendObjectSparseArray.size(); ++i) {
				if (!mIFileDownloadTaskExtendObjectSparseArray.valueAt(i).isExtendObjectValid()) {
					if (Debug_SDK.isDownloadLog) {
						Debug_SDK.te(Debug_SDK.mDownloadTag, this, "下载任务无效：第%d个扩展对象value无效", i);
					}
					return false;
				}
			}
		}
		return true;

	}

	/**
	 * 获取额外的信息
	 *
	 * @return
	 */
	public IFileDownloadTaskExtendObject getIFileDownloadTaskExtendObject(int key) {
		if (mIFileDownloadTaskExtendObjectSparseArray == null) {
			return null;
		}
		return mIFileDownloadTaskExtendObjectSparseArray.get(key, null);
	}

	/**
	 * 添加额外的对象，比如可以在下载开始的时候传入额外的信息，下载成功的时候在取出这个额外的信息
	 * <p/>
	 * 支持添加多个额外对象，不通过对象需要通过key进行识别
	 *
	 * @param iFileDownloadTaskExtendObject
	 */
	public void addIFileDownloadTaskExtendObject(int key, IFileDownloadTaskExtendObject
			iFileDownloadTaskExtendObject) {
		if (iFileDownloadTaskExtendObject == null) {
			return;
		}
		if (mIFileDownloadTaskExtendObjectSparseArray == null) {
			mIFileDownloadTaskExtendObjectSparseArray = new SparseArray<IFileDownloadTaskExtendObject>();
		}
		mIFileDownloadTaskExtendObjectSparseArray.put(key, iFileDownloadTaskExtendObject);
	}

	public SparseArray<IFileDownloadTaskExtendObject> getIFlieDownloadTaskExtendObjectArray() {
		return mIFileDownloadTaskExtendObjectSparseArray;
	}

	/**
	 * 　设置文件的最终下载地址
	 *
	 * @param destDownloadUrl
	 */
	public void setDestDownloadUrl(String destDownloadUrl) {
		mDestDownloadUrl = destDownloadUrl;
		if (Debug_SDK.isDownloadLog) {
			Debug_SDK.ti(Debug_SDK.mDownloadTag, this, "修改了最终下载地址后参数模型状态：\n%s", this.toString());
		}
	}

	/**
	 * 获取文件下载地址(最终下载地址)
	 *
	 * @return
	 */
	public String getDestDownloadUrl() {
		return mDestDownloadUrl;
	}

	/**
	 * 获取原始的url
	 *
	 * @return
	 */
	public String getRawDownloadUrl() {
		return mRawDownloadUrl;
	}

	//	/**
	//	 * 设置长度，contentlength如果要传入来的话的，最好在构造函数的时候传入
	//	 *
	//	 * @param contentLength
	//	 */
	//	@Deprecated
	//	public void setContentLength(long contentLength) {
	//		this.mTotalLength = contentLength;
	//	}

	/**
	 * 获取长度
	 *
	 * @return
	 */
	public long getTotalLength() {
		return mTotalLength;
	}

	/**
	 * 获取下载文件的md5校验码(等于最终下载url的md5)
	 *
	 * @return
	 */
	public String getDownloadFileMd5sum() {
		return mDownloadFileMd5sum;
	}

	/**
	 * 获取最终存储的文件地址
	 *
	 * @return
	 */
	public File getStoreFile() {
		return mStoreFile;
	}

	/**
	 * 设置最终存储的文件地址
	 *
	 * @param storeFile
	 */
	public void setStoreFile(File storeFile) {
		mStoreFile = storeFile;
	}

	/**
	 * 获取缓存文件的地址，可能为空，只有在开始下载和下载过程中，才会可能获取到真实的缓存地址
	 *
	 * @return
	 */
	public File getTempFile() {
		return mTempFile;
	}

	/**
	 * 设置缓存文件的地址，在下载开始的时候才设置
	 *
	 * @param mTempFile
	 */
	public void setTempFile(File mTempFile) {
		this.mTempFile = mTempFile;
	}

	/**
	 * 本次下载任务的回调进度时间间隔
	 *
	 * @return
	 */
	public int getIntervalTime_ms() {
		return mIntervalTime_ms;
	}

	/**
	 * 如果有调用 {@link #setIdentify(String)} 方法设置本类的唯一标示
	 * 那么hashcode优先使用传入来的标识的md5字符串的hashcode作为本类的hashcode
	 * 否则就用原始url的md5字符串的hashcode作为本类的hashcode
	 *
	 * @return
	 */
	@Override
	public int hashCode() {

		if (!Basic_StringUtil.isNullOrEmpty(mIdentify)) {
			String identifyMd5 = Coder_Md5.md5(mIdentify);
			if (!Basic_StringUtil.isNullOrEmpty(identifyMd5)) {
				return identifyMd5.hashCode();
			}
		}
		String temp = Coder_Md5.md5(mRawDownloadUrl);
		if (Basic_StringUtil.isNullOrEmpty(temp)) {
			return 0;
		} else {
			return temp.hashCode();
		}
	}

	@Override
	public boolean equals(Object o) {
		return o != null && o.hashCode() == this.hashCode();
	}

	@Override
	public String toString() {
		if (Debug_SDK.isDownloadLog) {
			try {
				final StringBuilder sb = new StringBuilder("FileDownloadTask {\n");
				sb.append("  mRawDownloadUrl=\"").append(mRawDownloadUrl).append('\"').append("\n");
				sb.append("  mDestDownloadUrl=\"").append(mDestDownloadUrl).append('\"').append("\n");
				sb.append("  mTempFile=").append(mTempFile).append("\n");
				sb.append("  mStoreFile=").append(mStoreFile).append("\n");
				sb.append("  mDownloadFileMd5sum=\"").append(mDownloadFileMd5sum).append('\"').append("\n");
				sb.append("  mTotalLength=").append(mTotalLength).append("\n");
				sb.append("  mIntervalTime_ms=").append(mIntervalTime_ms).append("\n");
				sb.append("  mIFileDownloadTaskExtendObjectSparseArray=").append
						(mIFileDownloadTaskExtendObjectSparseArray)
						.append("\n");
				sb.append('}');
				return sb.toString();
			} catch (Throwable e) {
				if (Debug_SDK.isDownloadLog) {
					Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
				}
			}
		}
		return super.toString();
	}

}
