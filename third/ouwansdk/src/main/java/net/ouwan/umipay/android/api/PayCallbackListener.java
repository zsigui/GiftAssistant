package net.ouwan.umipay.android.api;

/**
 * PayCallbackListener
 *
 * @author zacklpx
 *         date 15-1-29
 *         description
 */
public interface PayCallbackListener {
	/**
	 * 支付回调方法
	 *
	 * @param code 状态码
	 */
	public void onPay(int code);
}
