package net.ouwan.umipay.android.asynctask;

import android.os.Bundle;

/**
 * CommandResponseListener
 *
 * @author zacklpx
 *         date 15-1-30
 *         description
 */
public interface CommandResponseListener {
	void onResponse(CommandResponse response, Bundle... extResponse);
}
