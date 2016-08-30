package net.ouwan.umipay.android.interfaces;

import net.ouwan.umipay.android.entry.UmipayAccount;

import java.util.List;

/**
 * Interface_Account_Listener_Login
 *
 * @author zacklpx
 *         date 15-3-10
 *         description
 */
public interface Interface_Account_Listener_GetAccountList {

	void onGetAccountList(int code, String msg, List accountList);
}
