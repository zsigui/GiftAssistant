package net.youmi.android.libs.common.v2.download;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import net.youmi.android.libs.common.basic.Basic_StringUtil;
import net.youmi.android.libs.common.debug.DLog;
import net.youmi.android.libs.common.v2.download.base.FinalDownloadStatus;
import net.youmi.android.libs.common.v2.download.core.DefaultSDKDownloader;
import net.youmi.android.libs.common.v2.download.executor.DefaultDownloadCacheExecutorService;
import net.youmi.android.libs.common.v2.download.listener.IMaxPriorityDownloadListener;
import net.youmi.android.libs.common.v2.download.listener.ImageDownloadListener;
import net.youmi.android.libs.common.v2.download.model.FileDownloadTask;
import net.youmi.android.libs.common.v2.download.notify.AbsDownloadNotifier;
import net.youmi.android.libs.common.v2.download.notify.DefaultDownloadNotifier;
import net.youmi.android.libs.common.v2.download.notify.ImageDownloadNotifier;
import net.youmi.android.libs.common.v2.pool.core.AbsCacheExecutorService;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 本类对于同一个资源，但是每次请求url都不同的情况(如：请求url需要加入时间戳的时候，那么每次请求的url都会不同)，支持不是友好
 * // TODO: 支持上面的情况
 *
 * @author zhitao
 * @since 2015-09-21 14:58
 */
public abstract class BaseBitmapCachedDownloadManager extends AbsCachedDownloadManager implements IMaxPriorityDownloadListener {

	/**
	 * 将bitmap保存在内存中
	 */
	private HashMap<String, SoftReference<Bitmap>> mCacheTableBitmaps;

	/**
	 * 图片的下载通知管理器
	 */
	private ImageDownloadNotifier mImageDownloadNotifier;

	protected BaseBitmapCachedDownloadManager(Context context) throws NullPointerException, IOException {
		super(context);
		mCacheTableBitmaps = new HashMap<String, SoftReference<Bitmap>>();
		mImageDownloadNotifier = new ImageDownloadNotifier();
	}

	/**
	 * 子类new一个下载任务的观察者监听管理器
	 *
	 * @return
	 */
	@Override
	public AbsDownloadNotifier newDownloadNotifier() {
		return new DefaultDownloadNotifier(this);
	}

	/**
	 * 子类new一个具体使用的Cache类型线程池
	 *
	 * @return
	 */
	@Override
	public AbsCacheExecutorService newAbsCacheExecutorService() {
		return new DefaultDownloadCacheExecutorService("ImageDownload-Cache");
	}

	/**
	 * 设置用的下载实体类
	 *
	 * @return
	 */
	@Override
	public Class getDownloaderClass() {
		return DefaultSDKDownloader.class;
	}

	public void registerImageDownloadListener(ImageDownloadListener imageDownloadListener) {
		if (mImageDownloadNotifier != null) {
			mImageDownloadNotifier.registerListener(imageDownloadListener);
		}
	}

	public void removeImageDownloadListerer(ImageDownloadListener imageDownloadListener) {
		if (mImageDownloadNotifier != null) {
			mImageDownloadNotifier.removeListener(imageDownloadListener);
		}
	}

	/**
	 * 需要在调用本方法之前先注册{@link net.youmi.android.libs.common.v2.download.listener.ImageDownloadListener}
	 * <p/>
	 * 获取图片，结果将回调到上述listener
	 * <p/>
	 * 1. 如果图片存在于内存缓存中，则直接返回Bitmap
	 * 2. 如果图片已经存在于本地文件中，则从文件中解析出Bitmap，可能会比较耗时
	 * 3. 从网络中异步获取该图片
	 *
	 * @param rawUrl 传入之前需要自己确保url是否为空，如果为空，那么是不会触发任何监听的
	 *
	 * @return 需要注册监听才知道结果
	 */
	public void loadBitmap(String rawUrl) {
		try {

			if (Basic_StringUtil.isNullOrEmpty(rawUrl)) {
				return;
			}

			// 从内存中获取图片
			Bitmap bm = getBitmapFromMemoryCache(rawUrl);
			if (bm != null && !bm.isRecycled()) {
				mImageDownloadNotifier.onNotifyImageDownloadSuccess(rawUrl, bm);
				return;
			}

			// 从文件中获取图片
			bm = getBitmapFromFile(rawUrl);
			if (bm != null && !bm.isRecycled()) {
				mImageDownloadNotifier.onNotifyImageDownloadSuccess(rawUrl, bm);
				return;
			}

			// 从网络中获取图片，如果本地文件存在的话就回直接返回，否则启动网络
			loadBitmapFromNetwork(rawUrl);

		} catch (Throwable e) {
			if (DLog.isDownloadLog) {
				DLog.te(DLog.mDownloadTag, this, e);
			}
		}
	}

	/**
	 * 从网络中预加载图片到本地，本过程会回调触发各个观察者，需要提前先注册好观察者
	 *
	 * @param rawUrl 下载url
	 */
	public void loadBitmapFromNetwork(String rawUrl) {
		try {
			FileDownloadTask task = new FileDownloadTask(rawUrl);
			download(task, true);
		} catch (Throwable e) {
			if (DLog.isDownloadLog) {
				DLog.te(DLog.mDownloadTag, this, e);
			}
		}
	}

	/**
	 * 从网络中预加载图片到本地，本过程并不会触发下载的回调，即观察者并不会触发
	 *
	 * @param rawUrl 下载url
	 */
	public void loadBitmapFromNetworkWithoutCallBack(String rawUrl) {
		try {
			FileDownloadTask task = new FileDownloadTask(rawUrl);
			download(task, false);
		} catch (Throwable e) {
			if (DLog.isDownloadLog) {
				DLog.te(DLog.mDownloadTag, this, e);
			}
		}
	}

	/**
	 * 尝试从本地文件中解析出图片，解析可能会耗时，需要注意
	 *
	 * @param url
	 *
	 * @return 如果存在该文件，就会返回decode出来的bitmap对象，并且在返回之前，会将这个图片写入内存中，方便下一次更快获取
	 */
	public Bitmap getBitmapFromFile(String url) {
		try {
			File storeFile = getDownloadDir().newDownloadStoreFile(url, null);
			if (storeFile.exists()) {
				if (DLog.isDownloadLog) {
					DLog.td(DLog.mDownloadTag, this, "[%s]图片存在于文件中，可用!", url);
				}

				Bitmap bm = BitmapFactory.decodeFile(storeFile.getAbsolutePath());
				if (bm != null && !bm.isRecycled()) {
					putBitmap2MemoryCache(url, bm); // 这里的结果并不十分关注
					return bm;
				}
			}
		} catch (Throwable e) {
			if (DLog.isDownloadLog) {
				DLog.te(DLog.mDownloadTag, this, e);
			}
		}
		return null;
	}

	/**
	 * 从内存缓存中获取Bitmap
	 * <p/>
	 * 如果之前从网络预下载过这个图片，那么就会缓存在内存中
	 *
	 * @param url 图片地址
	 *
	 * @return
	 */
	public Bitmap getBitmapFromMemoryCache(String url) {
		try {
			synchronized (mCacheTableBitmaps) {

				if (mCacheTableBitmaps.containsKey(url)) {
					SoftReference<Bitmap> srb = mCacheTableBitmaps.get(url);
					if (srb != null) {
						Bitmap bm = srb.get();
						if (bm != null && !bm.isRecycled()) {
							if (DLog.isDownloadLog) {
								DLog.td(DLog.mDownloadTag, this, "[%s]图片存在于内存缓存中，立即返回!", url);
							}
							return bm;
						}
						if (DLog.isDownloadLog) {
							DLog.td(DLog.mDownloadTag, this, "[%s]图片存在于内存缓存中，但已被系统recycled，删除引用!", url);
						}
					}

					// 到了这里说明缓存的图片有问题，直接删除之
					mCacheTableBitmaps.remove(url);
				}
			}
		} catch (Throwable e) {
			if (DLog.isDownloadLog) {
				DLog.te(DLog.mDownloadTag, this, e);
			}
		}
		return null;
	}

	/**
	 * 释放内存中的图片资源
	 *
	 * @return
	 */
	public boolean releaseMemoryCache() {
		synchronized (mCacheTableBitmaps) {
			try {
				// 先遍历所有bitmap 进行recycle
				Iterator<Map.Entry<String, SoftReference<Bitmap>>> temp = mCacheTableBitmaps.entrySet().iterator();
				while (temp.hasNext()) {
					Map.Entry<String, SoftReference<Bitmap>> entry = (Map.Entry<String, SoftReference<Bitmap>>) temp.next();
					SoftReference<Bitmap> srb = entry.getValue();
					if (srb != null) {
						Bitmap bm = srb.get();
						if (bm != null && !bm.isRecycled()) {
							bm.recycle();
						}
					}
				}

				// 然后删除hashmap所有项
				mCacheTableBitmaps.clear();

			} catch (Exception e) {
				if (DLog.isDownloadLog) {
					DLog.te(DLog.mDownloadTag, this, e);
				}
				return false;
			}
			return true;
		}
	}

	/**
	 * 将Bitmap放入缓存表中
	 *
	 * @param url
	 * @param bm
	 *
	 * @return
	 */
	protected boolean putBitmap2MemoryCache(String url, Bitmap bm) {
		try {

			// 将要设置的图片不可用的情况下，结果会以已经缓存的图片为准
			if (bm == null || bm.isRecycled()) {
				Bitmap bmFromMemory = getBitmapFromMemoryCache(url);// 从缓存表中获取可用的图片
				if (bmFromMemory == null || bmFromMemory.isRecycled()) {
					return false;
				}
				return true;
			}
			synchronized (mCacheTableBitmaps) {
				return mCacheTableBitmaps.put(url, new SoftReference<Bitmap>(bm)) != null;
			}

		} catch (Throwable e) {
			if (DLog.isDownloadLog) {
				DLog.te(DLog.mDownloadTag, this, e);
			}
		}
		return false;
	}

	/**
	 * 如果支持多进程下载，那么就会回调这里
	 * <p/>
	 * 如果刚刚调用停止下载的代码之后，立即点击再次重新下载的话，可能就会到这里）
	 * 添加下载开始之前，文件处于文件锁的回调
	 * <p/>
	 * 通知任务当前处于文件锁
	 * <p/>
	 * 一般需要重写的场合为SDK:
	 * <p/>
	 * 因为sdk一般是很多个app同时使用的，所以下载的文件基本是公用的，
	 * 这个时候，在下载之前就需要检查下下载文件是否已经被其他进程读取中，
	 * 如果是的话，这里要通知一下
	 *
	 * @param fileDownloadTask 下载任务模型
	 *
	 * @return false 不允许继续通知其他监听者
	 */
	@Override
	public boolean onDownloadBeforeStart_FileLock(FileDownloadTask fileDownloadTask) {
		return true;
	}

	/**
	 * 下载开始
	 *
	 * @param fileDownloadTask 下载任务模型
	 */
	@Override
	public boolean onDownloadStart(FileDownloadTask fileDownloadTask) {
		return true;
	}

	/**
	 * 下载进度回调：之类需要实现具体业务逻辑
	 *
	 * @param fileDownloadTask 下载任务模型
	 * @param totalLength      本次下载文件的总长度
	 * @param completeLength   已经完成的长度
	 * @param percent          当前完成百分比
	 * @param speedBytes       当前下载速度:每intervalTime_ms毫秒下载的长度(单位:bytes)
	 * @param intervalTime_ms  当前下载速度时间单位:每intervalTime_ms毫秒回回调一次本方法(单位:bytes)
	 *
	 * @return false 不允许继续通知其他监听者
	 */
	@Override
	public boolean onDownloadProgressUpdate(FileDownloadTask fileDownloadTask, long totalLength, long completeLength, int
			percent,
			long speedBytes, long intervalTime_ms) {
		return true;
	}

	/**
	 * 下载成功
	 *
	 * @param fileDownloadTask 下载任务模型
	 *
	 * @return false 不允许继续通知其他监听者
	 */
	@Override
	public boolean onDownloadSuccess(FileDownloadTask fileDownloadTask) {

		// 因为回调下载成功这个是下载的其中一个最终状态，所以即便这里从文件中解析bitmap十分耗时，也没有关系，因为后面没有其他状态了
		// 从文件中解析Bitmap
		if (mImageDownloadNotifier != null) {
			Bitmap bm = getBitmapFromFile(fileDownloadTask.getRawDownloadUrl());
			if (bm != null && !bm.isRecycled()) {
				mImageDownloadNotifier.onNotifyImageDownloadSuccess(fileDownloadTask.getRawDownloadUrl(), bm);
				return true;
			} else {
				mImageDownloadNotifier.onNotifyImageDownloadFailed(fileDownloadTask.getRawDownloadUrl());
				return false;
			}
		}
		return true;
	}

	/**
	 * 下载成功，文件本来就存在于本地
	 *
	 * @param fileDownloadTask 下载任务模型
	 *
	 * @return false 不允许继续通知其他监听者
	 */
	@Override
	public boolean onFileAlreadyExist(FileDownloadTask fileDownloadTask) {
		if (DLog.isDownloadLog) {
			DLog.td(DLog.mDownloadTag, this, "图片已存在于文件缓存中:%s", fileDownloadTask.getRawDownloadUrl());
		}
		return onDownloadSuccess(fileDownloadTask);
	}

	/**
	 * 下载失败
	 *
	 * @param fileDownloadTask    下载任务模型
	 * @param finalDownloadStatus 下载任务失败数据模型
	 *
	 * @return false 不允许继续通知其他监听者
	 */
	@Override
	public boolean onDownloadFailed(FileDownloadTask fileDownloadTask, FinalDownloadStatus finalDownloadStatus) {
		if (mImageDownloadNotifier != null) {
			mImageDownloadNotifier.onNotifyImageDownloadFailed(fileDownloadTask.getRawDownloadUrl());
		}
		return true;
	}

	/**
	 * 下载暂停
	 *
	 * @param fileDownloadTask 下载任务模型
	 * @param totalLength      本次下载的总长度
	 * @param completeLength   已下载的长度
	 * @param percent          下载停止时，已经完成的百分比
	 *
	 * @return false 不允许继续通知其他监听者
	 */
	@Override
	public boolean onDownloadStop(FileDownloadTask fileDownloadTask, long totalLength, long completeLength, int percent) {

		if (mImageDownloadNotifier != null) {
			mImageDownloadNotifier.onNotifyImageDownloadStop(fileDownloadTask.getRawDownloadUrl());
		}
		return true;
	}

}
