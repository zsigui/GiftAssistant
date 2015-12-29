package net.youmi.android.libs.common.v2.download.base;

import android.content.Context;

import net.youmi.android.libs.common.basic.Basic_StringUtil;
import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.util.Util_System_File;
import net.youmi.android.libs.common.v2.download.model.FileDownloadTask;
import net.youmi.android.libs.common.v2.network.NetworkUtil;
import net.youmi.android.libs.common.v2.network.core.HttpRequesterFactory;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionPoolTimeoutException;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;

/**
 * 单纯地对执行下载文件任务
 * 不做任何其他额外的逻辑(如：下载前的文件目录整理，下载成功后的文件检验处理等，都不会在这里实现)
 * 只返回本次下载的结果
 * <p/>
 * <b>本类的同一个实例支持多次调用 {@link #download()} 方法，并且下载失败时，且属于连接异常类型失败（hosts文件被篡改会引起这个异常），
 * 在第二次调用 {@link #download()}时，支持下载失败类HTTPDNS的效果</b>
 * <p/>
 * {@link net.youmi.android.libs.common.v2.download.base.DefaultAutoRetryFileDownloader}　这个类对本类进行了封装，支持下载失败后的重试
 *
 * @author zhitao
 * @since 2015-09-19 08:27
 * <p/>
 * TODO: 测试重定向下载的时候，是否能获取到真正的地址以及是否能真的下载
 */
public class BaseHttpURLConnectionFileDownloader implements IDownloader {

	/**
	 * 每次读入网络流写入到文件的长度(字节)
	 */
	private final static int BUFFER_SIZE = 2048;

	/**
	 * 文件的最终长度
	 */
	private long mDownloadFileFinalLength = 0;

	/**
	 * 下载任务是否正在进行中
	 */
	private boolean mIsRunning;

	/**
	 * 是否被主动停止了
	 */
	private boolean mIsStop;

	private Context mContext;

	private FileDownloadTask mFileDownloadTask;

	/**
	 * 同一个实例下，第二次调用 {@link #download()}方法的时候，会判断是否需要采用httpdns进行域名解析操作
	 */
	private boolean isNeedToAnalysRealIpFromHTTPDNS = false;

	public BaseHttpURLConnectionFileDownloader(Context context, FileDownloadTask fileDownloadTask) {
		mContext = context;
		mFileDownloadTask = fileDownloadTask;
		mIsRunning = false;
		mIsStop = false;
	}

	/**
	 * 检查参数是否有效
	 *
	 * @return
	 */
	private boolean isParamsVaild() {
		try {
			mContext = mContext.getApplicationContext();
		} catch (Throwable e) {
			return false;
		}
		if (!mFileDownloadTask.isValid()) {
			return false;
		}
		return true;
	}

	/**
	 * 开始下载
	 *
	 * @return 下载的最终状态
	 */
	@Override
	public FinalDownloadStatus download() {
		// 开始之前先检查任务状态
		if (!isParamsVaild()) {
			return new FinalDownloadStatus(FinalDownloadStatus.Code.FAILED_ERROR_PARAMS);
		}

		RandomAccessFile accessFile = null;
		InputStream inputStream = null;
		HttpURLConnection httpURLConnection = null;

		// 标记为开始
		mIsRunning = true;
		mIsStop = false;

		try {

			// 触发这里的前提条件是：
			// 1. 调用过一次download，但是下载失败并且是连接异常的失败，然后检查过后发现是hosts文件被篡改过，那么就会标记这个值为true，
			// 2. 在同一个实例下，第二次调用download方法就会触发这个判断
			if (isNeedToAnalysRealIpFromHTTPDNS) {
				String host = new URI(mFileDownloadTask.getRawDownloadUrl()).getHost();
				httpURLConnection = HttpRequesterFactory.newHttpURLConnection(mContext, mFileDownloadTask.getDestDownloadUrl());
				httpURLConnection.addRequestProperty("HOST", host);
			}
			// 创建默认的HttpURLConnection
			else {
				httpURLConnection = HttpRequesterFactory.newHttpURLConnection(mContext, mFileDownloadTask.getRawDownloadUrl());
			}

			if (httpURLConnection == null) {
				throw new NullPointerException("connection is null");
			}

			// 设置请求方式 GET
			httpURLConnection.setRequestMethod("GET");

			// 是否使用断点续传
			// 如果起始点大于0，即指定了下载起始点，需要配置块下载——起始至文件结束
			// 如果起始点为0,而结束点没有指定，则说明是从文件头部开始下载整个文件，因此不指定RANGE
			long start = mFileDownloadTask.getTempFile().length();
			if (start > 0) {
				httpURLConnection.setRequestProperty("RANGE", String.format("bytes=%d-", start));
			}

			if (httpURLConnection.getResponseCode() >= 200 && httpURLConnection.getResponseCode() < 300) {
				inputStream = httpURLConnection.getInputStream();
			} else {
				//				inputStream = httpURLConnection.getErrorStream();
				return new FinalDownloadStatus(FinalDownloadStatus.Code.FAILED_ERROR_HTTP_CODE);
			}

			if (inputStream == null) {
				throw new NullPointerException("InputStream is null");
			}
			mFileDownloadTask.setDestDownloadUrl(httpURLConnection.getURL().toURI().toString());
			// 这里特别解释一下：
			// httpURLConnection.getContentLength()拿到的是本次http请求的文件部分长度。
			// 而可能本次http请求之前就已经下载了其他一部分数据(mFileDownloadTask.getTempFile.length())
			// 因此mDownloadFileFinalLength是两者相加的。
			long contentLength = httpURLConnection.getContentLength();
			if (contentLength == -1) {
				return new FinalDownloadStatus(FinalDownloadStatus.Code.FAILED_ERROR_HTTP_RESPONSE);
			}

			// 文件最终的长度= 当前文件长度 + 本次下载长度
			mDownloadFileFinalLength = start + contentLength;

			if (Debug_SDK.isDownloadLog) {
				StringBuilder sb = new StringBuilder(256);
				sb.append("本次下载信息:");
				sb.append("\n * 原始下载url:").append(mFileDownloadTask.getRawDownloadUrl());
				sb.append("\n * 最终下载url:").append(mFileDownloadTask.getDestDownloadUrl());
				sb.append("\n * http返回状态码:").append(httpURLConnection.getResponseCode());
				sb.append("\n * 本次下载的长度(ContentLength):").append(contentLength);
				sb.append("\n * 目标下载文件路径:").append(mFileDownloadTask.getTempFile().getAbsolutePath());
				sb.append("\n * 起始下载位置:").append(start);
				Debug_SDK.td(Debug_SDK.mDownloadTag, this, "\n%s", sb.toString());
			}

			// 设置文件的写入起点
			File tempFile = Util_System_File.getVaildFile(mFileDownloadTask.getTempFile());
			if (tempFile == null) {
				return new FinalDownloadStatus(FinalDownloadStatus.Code.FAILED_ERROR_UNKOWN);
			}
			accessFile = new RandomAccessFile(tempFile, "rw");
			accessFile.seek(start);

			byte[] buff = new byte[BUFFER_SIZE];
			int length;
			while (mIsRunning && ((length = inputStream.read(buff)) > 0)) {
				accessFile.write(buff, 0, length);
			}

			// 下载成功之后，这里做一个简单的判断，判断文件当前长度是否等于已经完成的长度
			if (mDownloadFileFinalLength == mFileDownloadTask.getTempFile().length()) {

				// 这里需要将缓存文件重命名为最终文件
				if (mFileDownloadTask.getTempFile().renameTo(mFileDownloadTask.getStoreFile())) {
					return new FinalDownloadStatus(FinalDownloadStatus.Code.SUCCESS);
				} else {
					return new FinalDownloadStatus(FinalDownloadStatus.Code.FAILED_EXCEPTION_RENAME_TEMPFILE_TO_STOREFILE);
				}
			}

			// 到这里就表示mContentLength != mFileDownloadTask.getTempFile().length()
			// 那么出现上面的结论是有两种情况的
			// 1. mIsRunning依旧为true 即用户并没有终止下载，那么mContentLength 还是不等于下载缓存文件长度的话，
			//    那么可能就是被注入了额外的内容，这个时候需要表示为下载失败，而不是下载停止
			// 2. mIsRunning为fasle 即用户主动终止了下载，那么肯定是不相等的
			if (mIsRunning) {
				return new FinalDownloadStatus(FinalDownloadStatus.Code.FAILED_EXCEPTION_DOWNLOADFLIE_CHANGE);
			} else {
				if (Debug_SDK.isDownloadLog) {
					StringBuilder sb = new StringBuilder(256);
					sb.append("下载被停止:");
					sb.append("\n * 原始下载url:").append(mFileDownloadTask.getRawDownloadUrl());
					sb.append("\n * 最终下载url:").append(mFileDownloadTask.getDestDownloadUrl());
					sb.append("\n * 目标文件路径:").append(mFileDownloadTask.getTempFile().getAbsolutePath());
					sb.append("\n * 文件当前长度:").append(mFileDownloadTask.getTempFile().length());
					sb.append("\n * 文件下载完的预计长度:").append(mDownloadFileFinalLength);
					sb.append("\n * 停止时，已完成的进度百分比:").append(getDownloadPercent());
					Debug_SDK.td(Debug_SDK.mDownloadTag, this, sb.toString());
				}
				// 这里后续可以考虑在更早的地方判断mIsRunning来判断是否被用户停止了下载任务
				return new FinalDownloadStatus(FinalDownloadStatus.Code.STOP);
			}
		} catch (ConnectException e) {

			// 如果是连接异常，比如hosts文件为127.0.0.1开头的域名ip指定，那么会触发这里
			// 然后这里就保存一下hostString, 下一次再次调用download方法的时候就会进行dns域名解析重新进行下载

			String hostIpArrayString = NetworkUtil.request4HostIp(mFileDownloadTask.getRawDownloadUrl());
			if (!Basic_StringUtil.isNullOrEmpty(hostIpArrayString)) {
				if (Debug_SDK.isOfferLog) {
					Debug_SDK.te(Debug_SDK.mOfferTag, this, "连接异常：\n原始url: %s\n对应ip: %s", mFileDownloadTask.getRawDownloadUrl(),
							hostIpArrayString);
				}

				String[] hostIpArray = hostIpArrayString.split(";");
				for (String hostIp : hostIpArray) {
					if (hostIp == null) {
						continue;
					}
					hostIp = hostIp.trim();
					if (hostIp.startsWith("127.")) {
						// 如果是127.开头的ip，就采用dns解析获取真实的ip
						String newUrl = NetworkUtil.getReallyIpAddress(mFileDownloadTask.getRawDownloadUrl());
						if (Debug_SDK.isOfferLog) {
							Debug_SDK.te(Debug_SDK.mOfferTag, this, "进入DNS解析后\n原始url: %s\n新的url: %s",
									mFileDownloadTask.getRawDownloadUrl(), newUrl);
						}
						if (Basic_StringUtil.isNullOrEmpty(newUrl)) {
							continue;
						}

						mFileDownloadTask.setDestDownloadUrl(newUrl);
						isNeedToAnalysRealIpFromHTTPDNS = true;
						if (Debug_SDK.isOfferLog) {
							Debug_SDK.te(Debug_SDK.mOfferTag, this, "下一次重新下载将采用替换后的ip地址进行请求");
						}
						break;
					}
				}
			}

			return new FinalDownloadStatus(FinalDownloadStatus.Code.FAILED_EXCEPTION_UNKNOWN, e);
		} catch (ConnectionPoolTimeoutException e) {
			// 从ConnectionManager管理的连接池中取出连接超时
			return new FinalDownloadStatus(FinalDownloadStatus.Code.FAILED_EXCEPTION_UNKNOWN, e);

		} catch (ConnectTimeoutException e) {
			// 网络与服务器建立连接超时
			return new FinalDownloadStatus(FinalDownloadStatus.Code.FAILED_EXCEPTION_UNKNOWN, e);

		} catch (SocketTimeoutException e) {
			// 请求超时
			return new FinalDownloadStatus(FinalDownloadStatus.Code.FAILED_EXCEPTION_UNKNOWN, e);

		} catch (UnknownHostException e) {
			// 网络没有打开或者没有配置网络权限的时候会抛出这个异常
			return new FinalDownloadStatus(FinalDownloadStatus.Code.FAILED_EXCEPTION_UNKNOWN, e);

			//		} catch (HttpHostConnectException e) {
			//			// 网络没有打开或者没有配置网络权限的时候会抛出这个异常
			//			return new FinalDownloadStatus(FinalDownloadStatus.Code.FAILED_EXCEPTION_UNKNOWN, e);

		} catch (SocketException e) {
			// 请求被关闭
			return new FinalDownloadStatus(FinalDownloadStatus.Code.FAILED_EXCEPTION_UNKNOWN, e);

		} catch (NullPointerException e) {
			// 空指针异常
			return new FinalDownloadStatus(FinalDownloadStatus.Code.FAILED_ERROR_UNKOWN, e);

		} catch (Exception e) {
			// 返回未知异常(慎重)，因为返回这个的话，那么重发机制也不会触发
			// return DownloadStatusCode.FAILED_UNKOWN_EXCEPTION;

			// 未知异常
			return new FinalDownloadStatus(FinalDownloadStatus.Code.FAILED_EXCEPTION_UNKNOWN, e);
		} finally {
			// 标记下载停止
			mIsRunning = false;

			try {
				if (httpURLConnection != null) {
					httpURLConnection.disconnect();
				}
			} catch (Throwable e) {
				if (Debug_SDK.isDownloadLog) {
					Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
				}
			}

			// inputStream的释放需要在HttpGet调用了abort之后在释放，
			// 不然如果停止的时候还有100M没有下载完成，那么连接还是在的，并且只有完成读完这100M才会close掉
			// 最终导致这里的finally会执行好长好长时间
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (Throwable e) {
				if (Debug_SDK.isDownloadLog) {
					Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
				}
			}

			try {
				if (accessFile != null) {
					accessFile.close();
				}
			} catch (Throwable e) {
				if (Debug_SDK.isDownloadLog) {
					Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
				}
			}
			
		}
	}

	/**
	 * 获取本次下载的任务模型
	 *
	 * @return
	 */
	@Override
	public FileDownloadTask getFileDownloadTask() {
		return mFileDownloadTask;
	}

	/**
	 * 获取已经完成的长度
	 *
	 * @return
	 */
	@Override
	public long getCompleteLength() {
		return mFileDownloadTask.getTempFile().length();
	}

	/**
	 * 获取下载进度的百分比
	 *
	 * @return
	 */
	@Override
	public int getDownloadPercent() {
		long contentLength = mDownloadFileFinalLength;
		if (contentLength > 0) {
			long completeLength = mFileDownloadTask.getTempFile().length();
			return (int) ((completeLength * 100) / contentLength);
		}
		return 0;
	}

	/**
	 * 获取本次下载文件的总长度
	 *
	 * @return
	 */
	@Override
	public long getDownloadFileFinalLength() {
		return mDownloadFileFinalLength;
	}

	/**
	 * 停止下载过程
	 * <p/>
	 * 调用下载停止之后，并不会立即停止下载的，而是程序跑到判断是否需要停止下载的地方才会停止的，
	 */
	@Override
	public void stop() {
		mIsRunning = false;
		mIsStop = true;
	}

	/**
	 * 是否被停止，只有调用了{@link #stop()}才会变为true，而调用了{@link #download()}之后就会变为false
	 *
	 * @return
	 */
	@Override
	public boolean isStop() {
		return mIsStop;
	}

	/**
	 * 是否正在下载中
	 *
	 * @return
	 */
	@Override
	public boolean isRunning() {
		return mIsRunning;
	}

}
