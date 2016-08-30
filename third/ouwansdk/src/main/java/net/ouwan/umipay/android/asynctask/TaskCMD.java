package net.ouwan.umipay.android.asynctask;

/**
 * TaskCMD
 *
 * @author zacklpx
 *         date 15-2-28
 *         description
 */
public class TaskCMD {
	//获取账户列表
	public static final int MP_CMD_GETACCOUNTLIST = 0x304;
	//短信验证码接口
	public static final int MP_CMD_SMSOP = 0x308;
	//登录
	public static final int MP_CMD_OPENLOGIN = 0x301;
	//注册
	public static final int MP_CMD_OPENREGISTER = 0x300;
	//快速注册
	public static final int MP_CMD_QUICKREGISTER = 0x311;
	//第三方登录
	public static final int MP_CMD_OPENTHIRDLOGIN = 0x303;
	//删除账号
	public static final int MP_CMD_OPENUSERDELETE = 0x312;
	//上报角色信息nti
	public static final int MP_CMD_PUSHROLEINFO = 0x309;
	//获取推送信息
	public static final int MP_CMD_OPENGETPUSHLIST = 0x305;
	//获取小红点消息
	public static final int MP_CMD_REDPOINT = 0x315;
	//新初始化接口
	public static final int MP_CMD_INIT = 0x316;
	//获取可注册账号和密码
	public static final int MP_CMD_GETREGISTRABLEACCOUNT = 0x317;
	//获取手机登录验证码
	public static final int MP_CMD_GETMOBILELOGINSMS = 0x318;
	//手机登录验证码验证
	public static final int MP_CMD_MOBILELOGIN_GETACCOUNTLIST = 0x319;
	//手机登录接口
	public static final int MP_CMD_MOBILELOGIN_LOGIN = 0x320;
	//已有登录态登陆
	public static final int MP_CMD_AUTOLOGIN = 0x321;
	//验证多个通用登录态是否有效
	public static final int MP_CMD_VALIDATE_SESSIONS = 0x322;
	//绑定偶玩账号接口
	public static final int MP_CMD_BINDOAUTH = 0x323;
	//登录态转换
	public static final int MP_CMD_SESSION_CONVERT = 0x324;

	/**********************************************************************/
	//登出
	public static final int MP_CMD_LOGOUT = 0x401;
}
