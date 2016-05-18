package net.youmi.android.libs.platform.global;

import android.content.Context;
import android.os.Build;

import net.youmi.android.libs.common.basic.Basic_Properties;
import net.youmi.android.libs.common.basic.Basic_Random;
import net.youmi.android.libs.common.coder.Coder_CECoder;
import net.youmi.android.libs.common.coder.Coder_Md5;
import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.global.Global_Runtime_SystemInfo;
import net.youmi.android.libs.common.global.Global_Runtime_SystemInfo_Simulator;
import net.youmi.android.libs.platform.PlatformConstant;

import java.util.Properties;

/**
 * 服务器会对cid进行校验，如果imei或imsi存在，则cid={imei+imsi} 如果imei和imsi都不存在，则cid={android_id} 如果android_id不存在，则cid={mac}
 * <p/>
 * sdk需要将四个参数都传给服务器。
 *
 * @author youmi
 */
public class Global_Runtime_ClientId {

	private String imei = "";

	private String imsi = "";

	private String cid;

	/**
	 * 组成cid的bd (android id或mac)
	 */
	private String bd = "";

	private boolean _isEmulator = false;

	/**
	 * 不知道什么密码
	 */
	// private static String keyWord = "yuJtmxbnRzbmWJnK";
	private static String keyWord = PlatformConstant.get_PlKey_Cid_SecretKey();

	public Global_Runtime_ClientId(Context context) {

		// 获取imei和imsi

		imei = Global_Runtime_SystemInfo.getImei(context);
		imsi = Global_Runtime_SystemInfo.getImsi(context);

		boolean t_imei = false;
		boolean t_imsi = false;
		boolean t_androidId = false;
		boolean t_mac = false;

		if (imei == null) {
			t_imei = true;
			imei = "";

		} else {
			if (imei.length() == 0) {
				t_imei = true;
			}
		}

		if (imsi == null) {
			t_imsi = true;
			imsi = "";
		} else {
			if (imsi.length() == 0) {
				t_imsi = true;
			}
		}

		if (t_imei && t_imsi) {
			// 如果imei和imsi都获取不到
			// 则获取clientId
			// 获取android_id
			bd = Global_Runtime_SystemInfo.getAndroidId(context);
			if (bd != null && bd.length() == 0) {
				// android_id获取失败
				t_androidId = true;
			}
		}

		// 这里只需要检查t_androidId即可，因为如果t_android为true，则说明imei和imsi都不通过，而且androidId也不通过，否则说明至少androidId是通过的。
		if (t_androidId) {
			// 如果clientId也为空，则获取mac地址
			bd = Global_Runtime_SystemInfo.getMac(context);

			if (bd != null && bd.length() == 0) {
				// mac获取失败
				t_mac = true;
			}
		}

		if (t_mac) {
			// 四个值都为空，
			// 获取不精确的clientId
			String cidStr = getIndeterminacyClientId(context);
			String cidSrcString = Coder_CECoder.converHexTo64_GwRule(cidStr.substring(7, 25));
			cid = toValidationCid(cidSrcString);// 加上校验码

		} else {
			// if (Debug_SDK.isGlobalLog) {
			// Debug_SDK.ti(Debug_SDK.mGlobalTag, this, "cid成功过程(原始字符串):[%s%s%s%s]", imei, imsi,
			// bd, keyWord);
			// }

			// 四个值中至少有一个不为空(优先级是imei|imsi >android_id>mac)
			// 注意有两个参数是使用temp的，它们与收集到的参数有可能不一样。
			String cidStr = Coder_Md5.md5(imei + imsi + bd + keyWord);

			// if (Debug_SDK.isGlobalLog) {
			// Debug_SDK.td(Debug_SDK.mGlobalTag, this, "cid成功过程(md5):[%s]", cidStr);
			// }

			String cidSrcString = Coder_CECoder.converHexTo64_GwRule(cidStr.substring(7, 25));

			// if (Debug_SDK.isGlobalLog) {
			// Debug_SDK.td(Debug_SDK.mGlobalTag, this, "cid成功过程(提取字符串):[%s]",
			// cidStr.substring(7, 25));
			// }

			// 加上校验码
			cid = toValidationCid(cidSrcString);
		}

		_isEmulator = Global_Runtime_SystemInfo_Simulator.isEmulator(context);

	}

	/**
	 * 获取不精确的client Id原始串的md5值 如果android_id不为空，直接返回md5(android_id) 否则返回md5(厂商+型号+随机数+系统时间+平台)
	 */
	private String getIndeterminacyClientId(Context context) {
		try {
			String cidStr = null;

			String fileName = PlatformConstant.get_PlKey_CidFile_Name();

			Properties properties = Basic_Properties.getPropertiesFromFile(context, fileName);

			String clientKey = PlatformConstant.get_PlKey_CidFile_Key();

			if (properties.containsKey(clientKey)) {
				cidStr = (String) properties.get(clientKey);
			}

			if (cidStr == null) {

				cidStr = Coder_Md5
						.md5(Global_Runtime_SystemInfo.getManufacturerInfo() + Global_Runtime_SystemInfo
								.getDeviceModel() +
								Basic_Random.nextInt(Integer.MAX_VALUE) + System.currentTimeMillis() +
								Global_Runtime_SystemInfo.getDeviceOsRelease() + keyWord);
				if (cidStr != null) {

					try {
						properties.put(clientKey, cidStr);
						Basic_Properties.savePropertiesToFile(context, properties, fileName);
					} catch (Throwable e) {
					}
				}
			}

			if (cidStr == null) {
				return Coder_Md5.md5(Build.MODEL);
			}

			return cidStr;

		} catch (Throwable e) {
		}
		return "";
	}

	/**
	 * 校验码
	 *
	 * @param srcCid
	 * @return
	 */
	private static String toValidationCid(String srcCid) {
		try {

			int sum = 0;

			for (int i = 0; i < srcCid.length(); i++) {

				char c = srcCid.charAt(i);

				if (i % 2 == 0) {
					// 偶数
					// 得到十进制数
					int a = Coder_CECoder.conver64ToDecimal(c);
					// 乘以2
					a *= 2;
					sum += (a & 63);
					sum += (a >> 6);

				} else {
					// 基数
					// 得到十进制数
					int a = Coder_CECoder.conver64ToDecimal(c);
					// 平方
					int b = a * a;
					sum += b;
				}

			}

			int unit = sum & 63;

			if (unit == 0) {
				// 个位为0，则校验位为0
				return srcCid + "0";
			}

			// 个位不为0，则为64(10进制)减去个位数($num & 63)
			int index = 64 - unit;

			return srcCid + Character.toString(Coder_CECoder.converDecimalTo64(index));

		} catch (Throwable e) {
			if (Debug_SDK.isGlobalLog) {
				Debug_SDK.te(Debug_SDK.mGlobalTag, Global_Runtime_ClientId.class, e);
			}
		}

		return srcCid;
	}

	public String getCid() {
		if (Debug_SDK.isGlobalLog) {
			Debug_SDK.td(Debug_SDK.mGlobalTag, this, "cid:%s", cid);
		}
		return cid;
	}

	public String getBd() {
		return bd;
	}

	public String getImsi() {
		return imsi;
	}

	public String getImei() {
		return imei;
	}

	public boolean isEmulator() {
		return _isEmulator;
	}
}
