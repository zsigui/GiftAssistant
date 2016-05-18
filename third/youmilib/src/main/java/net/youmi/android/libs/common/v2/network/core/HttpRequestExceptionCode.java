package net.youmi.android.libs.common.v2.network.core;

/**
 * 客户端错误码
 *
 * @author jen created on 2013-5-19
 * @author zhitaocai edit 2014-5-13
 */
public class HttpRequestExceptionCode {

	/**
	 * 正常请求
	 */
	public final static int NoException = 0;

	/**
	 * 还没有定义的或者是未知的异常
	 */
	public final static int UnknownException = -99;

	/**
	 * 网络与服务器建立连接超时
	 */
	public final static int ConnectTimeoutException = -100;

	/**
	 * 从ConnectionManager管理的连接池中取出连接超时
	 */
	public final static int ConnectionPoolTimeoutException = -101;

	/**
	 * 请求超时
	 */
	public final static int SocketTimeoutException = -102;

	/**
	 * 一般是网络没有打开或者没有没配置好网络权限的时候会抛出这个异常
	 */
	public final static int UnknownHostException = -103;

	/**
	 * 一般是网络切换的时候引发的比如：wifi关闭切换到3g 或者从网络切换到没有网络的情况下
	 */
	public final static int HttpHostConnectException = -104;

	/**
	 * 请求被主动关闭
	 */
	public final static int SocketException = -105;
}
