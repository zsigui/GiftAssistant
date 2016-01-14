package net.ouwan.umipay.android.weibo;

import android.os.Bundle;


/**
 * Created by mink on 15-12-11.
 */
public interface WeiboAuthListener {
	public void onComplete(Bundle result);

	void onWeiboException(WeiboException var1);

	void onError(WeiboDialogError var1);

	void onCancel();
}
