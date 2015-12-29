package net.youmi.android.libs.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import net.youmi.android.libs.common.debug.Debug_SDK;

/**
 * 执行外部命令
 * 
 * @author zhitao
 * @date 2015-5-5 下午3:39:44
 */
public class Util_System_Process {

	// 换行符
	private static final String BREAK_LINE = "\n";

	// 执行退出命令
	private static final byte[] COMMAND_EXIT = "\nexit\n".getBytes();

	// 错误缓冲
	private static byte[] BUFFER = new byte[32];

	private final static ProcessBuilder pb = new ProcessBuilder();

	/**
	 * ［sync］执行命令
	 * 
	 * @param params
	 *            eg: "/system/bin/ping", "-c", "4", "-s", "100","www.qiujuer.net"
	 * @return errstream重定向到inputstream的标准输入流的字符串
	 */
	public static String execute(String... params) {
		return execute(false, params);
	}

	private static String execute(boolean isSplitEveryLine, String... params) {
		Process process = null;
		StringBuilder sb = null;

		BufferedReader br = null;
		InputStreamReader isr = null;

		InputStream err = null;
		InputStream in = null;
		OutputStream out = null;

		try {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.td(Debug_SDK.mUtilTag, Util_System_Process.class, "--准备创建process");
			}

			// 几种创建process的实例方式
			process = pb.command(params).redirectErrorStream(true).start();
			// process = new ProcessBuilder().command(params).start();
			// process = Runtime.getRuntime().exec(params);

			if (Debug_SDK.isUtilLog) {
				Debug_SDK.td(Debug_SDK.mUtilTag, Util_System_Process.class, "--创建process成功");
			}

			err = process.getErrorStream();
			in = process.getInputStream();
			out = process.getOutputStream();

			// 除非是像su之后接一个命令，不然是比较少用到outputStream，因此这里直接将其close掉
			if (out != null) {
				out.close();
			}

			// 这里的作用在于执行完命令之后直接退出外部进程
			// 没有root的手机会在这里报错
			// out.write(COMMAND_EXIT);
			// out.flush();

			// 因为已经见errstream重定向到inputstream，所以这里就不用读取errstream了
			// if (Debug_SDK.isUtilLog) {
			// Debug_SDK.td(Debug_SDK.mUtilTag, Util_System_Process.class, "--开始读errstream");
			// }
			// 关于err和in 输入流的使用，其实更建议分开线程读取，或者直接价将errstream重定向到标准输入流解决
			// 一直写入errstream，不要让errstream的缓冲区满了，不然阻塞在waitFor那里
			// while ((err.read(BUFFER)) > 0) {
			// }

			isr = new InputStreamReader(in);
			br = new BufferedReader(isr);

			if (Debug_SDK.isUtilLog) {
				Debug_SDK.td(Debug_SDK.mUtilTag, Util_System_Process.class, "--开始读inputstream");
			}
			String s;
			if ((s = br.readLine()) != null) {
				sb = new StringBuilder();
				sb.append(s);
				if (isSplitEveryLine) {
					sb.append(BREAK_LINE);
				}
				while ((s = br.readLine()) != null) {
					sb.append(s);
					if (isSplitEveryLine) {
						sb.append(BREAK_LINE);
					}
				}
			}

			if (Debug_SDK.isUtilLog) {
				Debug_SDK.td(Debug_SDK.mUtilTag, Util_System_Process.class, "--waitfor之前");
			}

			process.waitFor();
			if (Debug_SDK.isUtilLog) {
				StringBuilder sb1 = new StringBuilder();
				for (String param : params) {
					sb1.append(param).append(" ");
				}
				Debug_SDK.td(Debug_SDK.mUtilTag, Util_System_Process.class, "--执行命令：%s 结束", sb1.toString());
			}
		} catch (Exception e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_Process.class, e);
			}
		} finally {
			closeAllStream(out, err, in, isr, br);

			if (process != null) {
				processDestroy(process);
				process = null;
			}
		}

		if (sb == null) {
			return null;

		} else {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.ti(Debug_SDK.mUtilTag, Util_System_Process.class, sb.toString());
			}
			return sb.toString();
		}
	}

	/**
	 * 关闭所有流
	 * 
	 * @param out
	 *            输出流
	 * @param err
	 *            错误流
	 * @param in
	 *            输入流
	 * @param isReader
	 *            输入流封装
	 * @param bReader
	 *            输入流封装
	 */
	private static void closeAllStream(OutputStream out, InputStream err, InputStream in, InputStreamReader isReader,
			BufferedReader bReader) {
		if (out != null)
			try {
				out.close();
			} catch (IOException e) {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_Process.class, e);
				}
			}
		if (err != null)
			try {
				err.close();
			} catch (IOException e) {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_Process.class, e);
				}
			}
		if (in != null)
			try {
				in.close();
			} catch (IOException e) {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_Process.class, e);
				}
			}
		if (isReader != null)
			try {
				isReader.close();
			} catch (IOException e) {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_Process.class, e);
				}
			}
		if (bReader != null)
			try {
				bReader.close();
			} catch (IOException e) {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_Process.class, e);
				}
			}
	}

	/**
	 * 通过Android底层实现进程关闭
	 * 
	 * @param process
	 *            进程
	 */
	private static void killProcess(Process process) {
		int pid = getProcessId(process);
		if (Debug_SDK.isUtilLog) {
			Debug_SDK.td(Debug_SDK.mUtilTag, Util_System_Process.class, "--pid ： %d", pid);
		}
		if (pid != 0) {
			try {
				// android kill process
				android.os.Process.killProcess(pid);
			} catch (Exception e) {
				try {
					process.destroy();
				} catch (Exception ex) {
				}
			}
		}
	}

	/**
	 * 获取进程的ID
	 * 
	 * @param process
	 *            进程
	 * @return
	 */
	private static int getProcessId(Process process) {
		try {
			String str = process.toString();
			int i = str.indexOf("=") + 1;
			int j = str.indexOf("]");
			str = str.substring(i, j);
			return Integer.parseInt(str);
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 * 销毁进程
	 * 
	 * @param process
	 *            进程
	 */
	private static void processDestroy(Process process) {
		if (process != null) {
			try {
				// 判断是否正常退出
				if (process.exitValue() != 0) {
					if (Debug_SDK.isUtilLog) {
						Debug_SDK.td(Debug_SDK.mUtilTag, Util_System_Process.class, "--非正常退出,准备kill");
					}
					killProcess(process);
				} else {
					if (Debug_SDK.isUtilLog) {
						Debug_SDK.td(Debug_SDK.mUtilTag, Util_System_Process.class, "--正常退出");
					}

				}
			} catch (IllegalThreadStateException e) {
				killProcess(process);
			}
		}
	}
}