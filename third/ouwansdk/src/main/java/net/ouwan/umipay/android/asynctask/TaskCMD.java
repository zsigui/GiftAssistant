package net.ouwan.umipay.android.asynctask;

/**
 * TaskCMD
 *
 * @author zacklpx
 *         date 15-2-28
 *         description
 */
public class TaskCMD {
	//新初始化接口
	public static final int MP_CMD_INIT = 0x316;
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

	/**********************************************************************/
	//登出
	public static final int MP_CMD_LOGOUT = 0x401;
}
