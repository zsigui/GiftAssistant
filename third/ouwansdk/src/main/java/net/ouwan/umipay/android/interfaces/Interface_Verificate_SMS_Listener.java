package net.ouwan.umipay.android.interfaces;

import net.ouwan.umipay.android.entry.gson.Gson_Cmd_VerificateSMS;

/**
 * Interface_Verificate_SMS_Listener
 *
 * @author zacklpx
 *         date 15-3-9
 *         description
 */
public interface Interface_Verificate_SMS_Listener {
	public static final int TYPE_SEND_SMS = 1;
	public static final int TYPE_VERIFICATE_SMS = 2;

	void onVerificateSMS(Gson_Cmd_VerificateSMS gsonCmdVerificateSMS);
}
