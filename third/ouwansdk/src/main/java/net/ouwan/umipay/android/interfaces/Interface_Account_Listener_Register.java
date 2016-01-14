package net.ouwan.umipay.android.interfaces;

import net.ouwan.umipay.android.entry.UmipayAccount;

public interface Interface_Account_Listener_Register {
	void onRegist(int type, int code, String msg, UmipayAccount account);
}
