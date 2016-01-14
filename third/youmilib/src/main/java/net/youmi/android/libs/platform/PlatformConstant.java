package net.youmi.android.libs.platform;

/**
 * !!! 请不要随便更改包名和类名
 * <p/>
 * 平台基础类库通用字符串明文保存类
 * <p/>
 * 本类在构建广告sdk的时候会进行字符串加密替换处理，因此需要遵循下面的一些规则
 * <p/>
 * <ul>
 * <li> 只有十分明显的具有唯一标识特征的字符串才进行加密处理，其他的如“init”之类，可以写成 "in".trim()+ "it".trim()</li>
 * <li> 获取方法必须使用"get_"开头</li>
 * <li> 方法实体仅需要写return "xxx";就可以</li>
 * <p/>
 * <p/>
 *
 * @author zhitaocai
 * @since 2015-08-07 18:40
 */
public class PlatformConstant {

	public static String get_PlKey_YoumiSdk() {
		return "YoumiSdk";
	}

	public static String get_PlKey_YoumiChannel() {
		return "YOUMI_CHANNEL";
	}

	public static String get_PlKey_SheildChannel() {
		return "SHEILD_CHANNEL";
	}

	public static String get_PlKey_Cid_SecretKey() {
		return "yuJtmxbnRzbmWJnK";
	}

	public static String get_PlKey_CidFile_Name() {
		return "DD5E8CD46CF94B22BAAD68AB06710752";
	}

	public static String get_PlKey_CidFile_Key() {
		return "46C02DF8DF4C4C18A578C63449C7F64D";
	}

	//----------------------------------------------------------------------------------------------------------------
	// 开发者信息配置
	public static String get_PlKey_SpFileName() {
		return "CE94557724F842149D690D0E8CBB1CBD";
	}

	public static String get_PlKey_SpFile_AppidKey() {
		return "F1B19978F3D74302BA126760F96262CD";
	}

	public static String get_PlKey_SpFile_AppidSaltKey() {
		return "CBD2998A3D5A4744BF128B91E1410DEA";
	}

	public static String get_PlKey_SpFile_AppSecretKey() {
		return "A33E523A1CEF496dB37ABD886CBCB005";
	}

	public static String get_PlKey_SpFile_AppSecretSaltKey() {
		return "C97CE45F9A5A447c98BBB83D88790503";
	}

	public static String get_PlKey_SpFile_UserIdKey() {
		return "DD2E1AD5215B757A908C48D980702694";
	}

	public static String get_PlKey_SpFile_UserIdSaltKey() {
		return "B77BA25E94FF190AFD2ABAFACE2F7904";
	}

	public static String get_PlKey_SpFile_ChannelIdKey() {
		return "CM2DD1ADT311BYGN21033C8D98050252";
	}

	public static String get_PlKey_SpFile_ChannelIdSaltKey() {
		return "NDF74E2SDF5ASF21E4F5A6E3H584I324";
	}

	public static String get_PlKey_SpFile_SheildChannelIdKey() {
		return "XSDWC1ADT311BYGN21033C8D98050252";
	}

	public static String get_PlKey_SpFile_SheildChannelIdSaltKey() {
		return "NDF74E2SDF5ASF21E4F5A6E3H584I324";
	}

	public static String get_PlKey_SpFile_WeixinAppidIdKey() {
		return "WXC1X1C4D2A2X1863S21AX92LKLSDA";
	}

	public static String get_PlKey_SpFile_WeixinAppIdSaltKey() {
		return "WXKO0D2S0X9I29CKLX28XLKJSLKLX92D";
	}

	public static String get_PlKey_OldWeixinSdk_Appid() {
		return "2ec606046cd21efb";
	}

	public static String get_PlKey_OldWeixinSdk_AppSecret() {
		return "d9b603b129e2565e";
	}

	//----------------------------------------------------------------------------------------------------------------
	// 显示给开发者的提示语

	public static String get_Tips_ChannelIdError_tooSmall() {
		return "channel Id error: can't less than 0";
	}

	public static String get_Tips_ChannelIdError_tooBig() {
		return "channel Id error: can't less than 0";
	}

	//---------------------------------------------------------------------------------------------------------------
	// 过市场，请求报文首部字段
	public static String get_HEADER_CID() {
		return "X-YM-CID";
	}

	public static String get_HEADER_APP_ID() {
		return "X-YM-APP";
	}

	public static String get_HEADER_ENCODING() {
		return "X-YM-Encoding";
	}

	public static String get_HEADER_TID() {
		return "X-YM-TID";
	}

	public static String get_HEADER_SESSION() {
		return "X-YM-SESSION";
	}

}
