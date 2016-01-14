package net.ouwan.umipay.android.api;

/**
 * GameUserInfo
 *
 * @author zacklpx
 *         date 15-1-28
 *         description
 */
public class GameUserInfo {
	/**
	 * @Fields mOpenId : 用户id
	 */
	private String mOpenId;
	/**
	 * @Fields mTimestamp : utc秒数,1970至今秒数。用以游戏服验证登陆时效性。
	 */
	private int mTimestamp_s;
	/**
	 * @Fields mSign : 签名
	 * 所有参数+session_secret的MD5码后转换成小写签名拼串:
	 * userInfo.uid值 + & + userInfo.timestamp值 + & + session_secret值（计算时无+号）
	 * 注：session_secret平台合作方分配的，双方加密使用。如果发生泄密等影响安全的情况，双方协商后可以刷新。
	 */
	private String mSign;

	public String getOpenId() {
		return mOpenId;
	}

	public void setOpenId(String openId) {
		mOpenId = openId;
	}

	public int getTimestamp_s() {
		return mTimestamp_s;
	}

	public void setTimestamp_s(int timestamp_s) {
		mTimestamp_s = timestamp_s;
	}

	public String getSign() {
		return mSign;
	}

	public void setSign(String sign) {
		mSign = sign;
	}
}
