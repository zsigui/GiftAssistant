package net.ouwan.umipay.android.global;

import net.ouwan.umipay.android.debug.Debug_Log;
import net.youmi.android.libs.common.basic.Basic_Random;
import net.youmi.android.libs.common.coder.Coder_CECoder;
import net.youmi.android.libs.common.coder.Coder_Md5;

/**
 * Global_Data_Offers
 *
 * @author zacklpx
 *         date 15-3-18
 *         description
 */
public class Global_Data_Offers {

	private static String umipayOffersActivitySession;

	public static String getUmipayOffersActivitySession() {
		try {
			if (umipayOffersActivitySession == null) {
				String md5 = Coder_Md5.md5(System.currentTimeMillis() + "_" + Basic_Random.createRandom_Number_String
						(10));
				umipayOffersActivitySession = Coder_CECoder.converHexTo64_GwRule(md5.substring(5, 23));
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return umipayOffersActivitySession;
	}
}
