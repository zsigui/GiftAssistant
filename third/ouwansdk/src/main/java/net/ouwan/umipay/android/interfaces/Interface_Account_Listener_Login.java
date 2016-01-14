package net.ouwan.umipay.android.interfaces;

import net.ouwan.umipay.android.entry.UmipayAccount;

/**
 * Interface_Account_Listener_Login
 *
 * @author zacklpx
 *         date 15-3-10
 *         description
 */
public interface Interface_Account_Listener_Login {

	void onLogin(int code, String msg, UmipayAccount account);
}
