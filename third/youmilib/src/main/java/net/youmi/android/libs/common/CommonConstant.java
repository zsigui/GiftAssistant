package net.youmi.android.libs.common;

/**
 * !!! 请不要随便更改包名和类名
 *
 * 通用基础类库通用字符串明文保存类
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
 * @since 2015-08-07 20:41
 */
public class CommonConstant {

	public static String get_Key_IconDir() {
		return "/Android/data/.youmicache/.ECDF5D7F3EB26A54256D1994F72EEB4D/";
	}

	public static String get_CacheFileName_Mac() {
		return "m929bb76e8110d1a70260af57b446ebc";
	}


	public static String get_CacheFileName_Imei() {
		return "i42d45df023jnkdd93la483f9xGFKXI";
	}

	public static String get_CacheFileName_Imsi() {
		return "s92TjjdfoP2n3o9dfji2l9s1olkjf0p";
	}

	public static String get_CacheFileName_PSW() {
		return "pY32nlkjdf0xik2ljso0d9fi21LOI23";
	}


	public static String get_Url_ErrorReport() {
		return "exrep.youmi.net";
	}

	public static String get_DatabaseName_AccelerationSensor() {
		return "sab1dx2x9";
	}

	public static String get_DatabaseName_GyroscopeSensor() {
		return "sgax920kd";
	}

	public static String get_DatabaseName_LightSensor() {
		return "slx93hldx";
	}

	public static String get_DatabaseName_DefaultTableName() {
		return "slx93hldx";
	}


}