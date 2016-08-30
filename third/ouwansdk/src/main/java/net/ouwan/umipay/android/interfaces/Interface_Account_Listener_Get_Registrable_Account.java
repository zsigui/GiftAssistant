package net.ouwan.umipay.android.interfaces;

import net.ouwan.umipay.android.entry.UmipayAccount;

public interface Interface_Account_Listener_Get_Registrable_Account {
	void onGetRegistrableAccount(int type, int code, String msg, UmipayAccount account);
}
