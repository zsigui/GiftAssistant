package net.ouwan.umipay.android.asynctask;

/**
 * CommandResponse 命令字任务返回结果
 * <p>
 * {@link net.ouwan.umipay.android.asynctask.UmipayCommandTask} 返回的结果类
 * </p>
 * Created by liangpeixing on 8/15/14.
 */
public class CommandResponse {
	private int mCode;
	private Object mResult;
	private String mMsg;
	private int mCmd;

	public CommandResponse() {
	}

	/**
	 * 命令字任务返回结果
	 *
	 * @param code   错误码
	 * @param result json结果
	 * @param msg    附带信息
	 * @param cmd    命令字
	 */
	public CommandResponse(int code, Object result, String msg, int cmd) {
		mCode = code;
		mResult = result;
		mMsg = msg;
		mCmd = cmd;
	}

	/**
	 * @return 错误码
	 */
	public int getCode() {
		return mCode;
	}

	/**
	 * @param code 错误码
	 */
	public void setCode(int code) {
		mCode = code;
	}

	/**
	 * @return 请求结果
	 */
	public Object getResult() {
		return mResult;
	}

	/**
	 * @param result 请求结果
	 */
	public void setResult(Object result) {
		mResult = result;
	}

	/**
	 * @return 错误信息
	 */
	public String getMsg() {
		return mMsg;
	}

	/**
	 * @param msg 错误信息
	 */
	public void setMsg(String msg) {
		mMsg = msg;
	}

	/**
	 * @return 命令字
	 */
	public int getCmd() {
		return mCmd;
	}

	/**
	 * @param cmd 命令字
	 */
	public void setCmd(int cmd) {
		mCmd = cmd;
	}
}
