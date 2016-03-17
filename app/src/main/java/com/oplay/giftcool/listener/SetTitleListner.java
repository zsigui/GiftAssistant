package com.oplay.giftcool.listener;

import android.support.annotation.StringRes;

/**
 * 设置标题的接口
 *
 * Created by zsigui on 16-3-17.
 */
public interface SetTitleListner {

	void setBarTitle(@StringRes int res);

	void setBarTitle(String title);
}
