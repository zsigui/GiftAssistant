package net.ouwan.umipay.android.interfaces;


import net.ouwan.umipay.android.entry.UmipayCommonAccount;

/**
 * Interface_Account_Listener_Login
 *
 * @author jimmy
 *         date 15-3-10
 *         description
 */
public interface Interface_Account_Listener_Validate_Session {
	void onVerified(int code, String msg, UmipayCommonAccount umipayCommonAccount);
}
