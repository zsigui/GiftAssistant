package com.oplay.giftcool.listener;

/**
 * 实现该接口以实现下拉时通知显示或隐藏列表脚视图
 *
 * Created by zsigui on 16-1-26.
 */
public interface FooterListener {

	/**
	 * 显示列表的脚视图
	 *
	 * @param isShow
	 */
	void showFooter(boolean isShow);
}
