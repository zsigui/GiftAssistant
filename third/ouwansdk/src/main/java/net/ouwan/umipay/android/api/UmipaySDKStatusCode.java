package net.ouwan.umipay.android.api;

import android.text.TextUtils;

import net.ouwan.umipay.android.config.ConstantString;
import net.ouwan.umipay.android.debug.Debug_Log;

public class UmipaySDKStatusCode {
    /**
     * @Fields SUCCESS : 通知游戏开发商登录成功
     */
    public static final int SUCCESS = 0;
    /**
     * @Fields LOGIN_CLOSE : 通知游戏开发商已经关闭登录界面
     */
    public static final int LOGIN_CLOSE = 1;
    /**
     * @Fields PAY_FINISH : 支付订单完成（不等于支付成功）
     */
    public static final int PAY_FINISH = 2;
    /**
     * @Fields CANCEL : 取消
     */
    public static final int CANCEL = 3;
    /**
     * @Fields EXIT_FINISH : 退屏成功退出
     */
    public static final int EXIT_FINISH = 4;
    /**
     * @Fields CHANGE_ACCOUNT : 切换账号
     */
    public static final int CHANGE_ACCOUNT = 5;

    /**
     * Err -998: 未定义
     */
    public static final int ERR_DEFAULT = -998;

    /**
     * Err -1:参数错误
     */
    public static final int PARAMETER_ERR = -1;
    /**
     * Err -2:初始化失败
     */
    public static final int INIT_FAIL = -2;
    /**
     * Err -3:获取用户列表失败
     */
    public static final int ERR_LOAD_USERNAME = -3;
    /**
     * Err -4:so库加载失败
     */
    public static final int ERR_NO_SOLIB = -4;
    /**
     * Err -5:so库错误
     */
    public static final int ERR_WRONG_SOLIB = -5;
    /**
     * Err -6:运行错误，一般是null
     */
    public static final int ERR_RUNTIME = -6;
    /**
     * Err -7: 账户未登录，遇到此错误时一般情况下需要进行登录操作
     */
    public final static int ERR_BEFORE_REQ_UNLOGIN = -7;

    /**
     * Err -8:
     * 连接失败，可能是网络原因，或服务器原因，遇到此接口一般是提示网络处理(逻辑调用方先检查一下网络情况如果有问题再提示用户进行网络设置)，
     * 然后稍等片刻再请求
     */
    public final static int ERR_REQ_CONNECTION_FAILED = -8;
    /**
     * Err -9:版本信息文件中版本号和sdk实际版本不符
     */
    public final static int ERR_WRONG_SDKVERSION = -9;
    /**
     * Err -10:
     * 获取渠道号失败
     */
    public final static int ERR_WRONG_CHANNELINFO = -10;
    /**
     * Err 10:参数错误,比如AppId或者AppSecret的长度不是16个字节
     */
    public static final int ERR_10_MP_ERR_PARAM = 10;
    /**
     * Err 11:打包错误，如参数为空
     */
    public static final int ERR_11_ERR_PACK = 11;
    /**
     * Err 12:加密错误，so库加密错误
     */
    public static final int ERR_12_ERR_ENCODE = 12;
    /**
     * Err 13:解密错误，so库解密错误
     */
    public static final int ERR_13_ERR_DECODE = 13;
    /**
     * Err 14:解包错误，解析服务器返回的包出错
     */
    public static final int ERR_14_ERR_UNPACK = 14;

    /**
     * Err 15:网络错误
     */
    public static final int ERR_15_MP_ERR_NETWORK = 15;
    /**
     * Err 107:解密错误，比如AppSecret错误导致解密失败
     */
    public static final int ERR_107_MP_ERR_DECODE = 107;

    /**
     * Err 110: 游戏appkey 不合法
     */
    public static final int ERR_110_MP_ERR_APPKEY = 110;
    /**
     * Err 112: 请求数据被非法修改
     */
    public static final int ERR_112_MP_ERR_HASH = 112;


    /**
     * Err 201: 查询积分余额失败
     */
    public static final int ERR_201_POINTS_QUERY_FAILED = 201;

    /**
     * Err 202: 积分余额不足
     */
    public static final int ERR_202_POINTS_INSUFFICIENT = 202;

    /**
     * Err 203: 奖励积分失败
     */
    public static final int ERR_203_POINTS_AWARD_FAILED = 203;

    /**
     * Err 204: 消费积分失败
     */
    public static final int ERR_204_POINTS_SPEND_FAILED = 204;

    /**
     * Err 205: 虚拟货币编号无效
     */
    public static final int ERR_205_POINTS_VID_INVALID = 205;

    /**
     * Err 250: 内部错误 连接用户模块失败
     */
    public static final int ERR_250_ACCOUNT_USER_CONNECT = 250;
    /**
     * Err 251: 账号名已经存在，不可重复注册
     */
    public static final int ERR_251_ACCOUNT_USER_REGISTERED = 251;

    /**
     * Err 252: 账户不存在
     */
    public static final int ERR_252_ACCOUNT_USER_NOT_EXIST = 252;

    /**
     * Err 253: 密码错误
     */
    public static final int ERR_253_ACCOUNT_USER_PASSWORD = 253;
    /**
     * Err 254: 内部错误 ，连接登录态失败
     */
    public static final int ERR_254_ACCOUNT_SESS_CONNECT = 254;

    /**
     * Err 255: 登录会话失效
     */
    public static final int ERR_255_ACCOUNT_SESSION_INVAILED = 255;
    /**
     * Err 256  内部错误 登录动作失败，设置登录态错误
     */
    public static final int ERR_256_MP_SESS_SET = 256;
    /**
     * Err 257  内部错误 登录态出错
     */
    public static final int ERR_257_MP_SESS_ERROR = 257;
    /**
     * ERR_MP_USER_VISITOR_BINDED : 游客账号已经绑定
     */
    public static final int ERR_259_MP_USER_VISITOR_BINDED = 259;

    /**
     * Err 260: 注册失败，密码过于简单
     */
    public static final int ERR_260_ACCOUNT_USER_PASSWORD_TO_SIMPLE = 260;

    /**
     * Err 270: 兑换失败，米宝余额不足
     */
    public static final int ERR_270_EXCHANGE_MIBAO_INSUFFICIENT = 270;

    /**
     * @Fields ERR_271_ACCOUNT_NICK_REGISTERED : 昵称已存在
     */
    public static final int ERR_271_ACCOUNT_NICK_REGISTERED = 271;

    /**
     * @Fields ERR_272_ACCOUNT_USER_INVALIDE : 密码不合法
     */
    public static final int ERR_272_ACCOUNT_PSW_ILLEAGE = 272;

    /**
     * Err 310: 账号名不合法
     */
    public static final int ERR_301_ACCOUNT_USER_NAME_ILLEAGE = 301;
    /**
     * Err 310: 要兑换的appkey不存在
     */
    public static final int ERR_310_MP_APP_NOT_EXIST = 310;
    /**
     * Err 311: 没开通偶玩业务
     */
    public static final int ERR_311_MP_APP_NO_MIPAY = 311;

    /**
     * Err 401: 初始化失败
     */
    public static final int ERR_401_MP_INIT = 401;


    public static String handlerMessage(int code, String msg) {
        if (!TextUtils.isEmpty(msg)) {
            return msg;
        }
        switch (code) {
            case SUCCESS:
                msg = "操作成功";
                break;
//		case ERR_BEFORE_REQ_UNLOGIN:
//			msg = "账户未登录";
//			break;
//		case ERR_BEFORE_REQ_PARAMS_ERROR:
//			msg = "参数错误";
//			break;
//		case NdkStatus.ERR_REQ_CONNECTION_FAILED:
//			msg = "网络连接错误";
//			break;
//		case NdkStatus.ERR_REQ_EXCEPTION:
//			msg = "无法解决的异常";
//			break;
            case ERR_NO_SOLIB:
                msg = "加载so库失败";
                break;
            case ERR_WRONG_SOLIB:
                msg = "so库错误";
                break;
            case ERR_RUNTIME:
                msg = "运行错误";
                break;
            case ERR_BEFORE_REQ_UNLOGIN:
                msg = "账户未登录";
                break;
            case ERR_REQ_CONNECTION_FAILED:
                msg = "网络连接错误";
                break;
            case PARAMETER_ERR:
                msg = ConstantString.getParameterErrMsg();
                break;
            case INIT_FAIL:
                msg = "未知初始化错误";
                break;
            case ERR_10_MP_ERR_PARAM:
                msg = "参数错误";
                break;
            case ERR_15_MP_ERR_NETWORK:
                msg = "网络错误";
                break;
            case ERR_107_MP_ERR_DECODE:
                msg = "解密错误";
                break;
            case ERR_110_MP_ERR_APPKEY:
                msg = "AppKey不合法";
                break;
            case ERR_112_MP_ERR_HASH:
                msg = "请求数据被非法修改";
                break;
            case ERR_250_ACCOUNT_USER_CONNECT:
                msg = "内部错误";
                break;
            case ERR_251_ACCOUNT_USER_REGISTERED:
                msg = "账号名已存在";
                break;
            case ERR_252_ACCOUNT_USER_NOT_EXIST:
                msg = "账号名不存在";
                break;
            case ERR_253_ACCOUNT_USER_PASSWORD:
                msg = "密码错误";
                break;
            case ERR_254_ACCOUNT_SESS_CONNECT:
                msg = "内部错误";
                break;
            case ERR_255_ACCOUNT_SESSION_INVAILED:
                msg = "登录态失效，请重新登录";
                break;
            case ERR_256_MP_SESS_SET:
            case ERR_257_MP_SESS_ERROR:
                msg = "内部错误";
                break;
            case ERR_259_MP_USER_VISITOR_BINDED:
                msg = "游客账号已经绑定,请重新使用绑定账号登录";
                break;
            case ERR_260_ACCOUNT_USER_PASSWORD_TO_SIMPLE:
                msg = "密码过于简单";
                break;
            case ERR_301_ACCOUNT_USER_NAME_ILLEAGE:
                msg = "账号名不合法";
                break;
            case ERR_310_MP_APP_NOT_EXIST:
                msg = "游戏app key不存在";
                break;
            case ERR_311_MP_APP_NO_MIPAY:
                msg = "尚未开通偶玩业务";
                break;
            default:
                msg = "错误码:" + code;
        }
        Debug_Log.e("错误码：%d", code);
        return msg;
    }
}
