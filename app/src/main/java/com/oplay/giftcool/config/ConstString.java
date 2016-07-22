package com.oplay.giftcool.config;

/**
 * 定义重复出现的格式化字符串(特别是带html代码不能被格式化的)
 * <p/>
 * Created by zsigui on 16-3-3.
 */
public class ConstString {

    public static final String TEXT_HEADER = "pn=%s&vc=%d&vn=%s&chn=%d&ov=%s";



    public static final String TEXT_SEIZE = "开抢时间：<font color='#ffaa17'>%s</font>";
    public static final String TEXT_SEARCH = "开淘时间：<font color='#ffaa17'>%s</font>";
    public static final String TEXT_SEARCHED = "已淘号：<font color='#ffaa17'>%d</font>次";
    public static final String TEXT_GIFT_CODE = "礼包码: <font color='#ffaa17'>%s</font>";
    public static final String TEXT_GIFT_TOTAL = "共 <font color='#ffaa17'>%s</font> 款礼包";
    public static final String TEXT_GIFT_FREE_SEIZE = "%s免费抢";

    public static final String TEXT_POST_BTN = "查看帖子";

    /*------------- 定义Toast显示内容 ----------------*/

    // 网络连接错误
    public static final String TOAST_NET_ERROR = "呜，网络有点挤，请稍后重试！";
    // 应用程序执行出现异常情况
    public static final String TOAST_EXECUTE_ERROR = "呜，网络有点挤，请稍后重试！";
    // 访问服务器出现异常情况
    public static final String TOAST_SERVER_ERROR = "呜，网络有点挤，请稍后重试！";
    // 服务器返回的数据格式出错，解析异常
    public static final String TOAST_SERVER_BAD_CALLBACK = "呜，网络有点挤，请稍后重试！";
    // 页面数据过期 或者 非网络问题导致的获取失败
    public static final String TOAST_DATA_OVERTIME = "呜，网络有点挤，请稍后重试！";
    // 页面状态丢失导致的失效问题
    public static final String TOAST_MISS_STATE = "页面失效，请重新打开再进行尝试";
    // 参数传递出错导致的页面跳转失败
    public static final String TOAST_WRONG_PARAM = "呜，网络有点挤，请稍后重试！";
    // 尚未登录
    public static final String TOAST_LOGIN_FIRST = "为了您的账户安全，请先登录哦～";
    // 手机号码格式不符合要求
    public static final String TOAST_PHONE_ERROR = "您好像输错手机号码了？请重新输入哦～";
    // 偶玩账号或密码格式不符合要求
    public static final String TOAST_OUWAN_ERROR = "呜呜，账号密码格式化错误了！请重新输入哦～";
    // 手机短信验证码发送提醒
    public static final String TOAST_PHONE_CODE_SEND = "短信已经发送，请注意接收";
    // 点击退出应用的提示
    public static final String TOAST_EXIT_APP = "再点我就离你而去了~~~~(>_<)~~~~";
    // 用户登录态失效
    public static final String TOAST_SESSION_UNAVAILABLE = "太久没有登录啦，为了您的账户安全，请重新登录哦～";

    // 复制礼包码后提示
    public static final String TOAST_COPY_CODE = "已复制，请去游戏中粘贴兑换";
    // 已更新到最新版本提示
    public static final String TOAST_VERSION_NEWEST = "666！你已经更新到最新版本咯！";
    // 当前版本不支持提示
    public static final String TOAST_VERSION_NOT_SUPPORT = "该版本不支持该功能喔，帅的人都更新了";
    // 未知异常状况
    public static final String TOAST_UNKNOWN_ERROR = "(⊙o⊙)?请尝试重新加载页面或重启应用";
    // 请求获取数据为空
    public static final String TOAST_NO_RESPONSE_DATA = "咦～页面跑丢了，请尝试重新打开页面";
    // 连续过快请求
    public static final String TOAST_FREQUENT_REQUEST = "手速太快啦，请耐心等待哦～";

    // 抢号支付方式选择有误
    public static final String TOAST_PAY_METHOD_ERROR = "选择支付类型有误，请重新选择";

    // 处于非WIFI下下载提示
    public static final String TOAST_DOWNLOADING_NOT_IN_WIFI = "正在使用移动网络，下载会耗费流量哦~";
    // 添加了新的下载任务提示
    public static final String TOAST_ADD_NEW_DOWNLOAD_TASK = "已添加新的下载任务";
    // 安装文件被移除导致的安装失败提示
    public static final String TOAST_INSTALL_FAIL_FOR_NO_APK = "安装失败！安装包已被删除，请重新下载";
    // 安装完删除安装文件提示
    public static final String TOAST_REMOVE_AFTER_SUCCESS_INSTALL = "已删除%s安装包，为您节省了%s空间";

    // 获取不到分享内容
    public static final String SHARE_ERROR = "分享内容出错";
    // 分享成功
    public static final String TOAST_SHARE_SUCCESS = "分享成功";
    // 分享失败
    public static final String TOAST_SHARE_FAILED = "分享失败，请检查网络后重新尝试哦！";
    // 取消分享
    public static final String TOAST_SHARE_QUICKED = "分享已取消";
    // 没有提供分享按钮情况下异常触发的提示
    public static final String TOAST_SHARE_NO_OFFER = "该礼包好像不能分享，去分享其他礼包吧！";
    // 活动获取发送Token失败
    public static final String TOAST_GET_TOKEN_FAILED = "呜，网络有点挤，请稍后重试！";
    // 发表评论成功
    public static final String TOAST_COMMENT_SUCCESS = "发表评论成功";

    // 没有关注快讯
    public static final String TOAST_EMPTY_FOCUS_ACTIVITY = "您还没有关注任何游戏哦～赶紧去关注吧～";
    // 获取关注快讯失败
    public static final String TOAST_GET_FOCUS_ACTIVITY_FAILED = "呜，网络有点挤，请稍后重试！";
    // 点击qq群直接调起qq失败
    public static final String TOAST_OPEN_QQ_GROUP_FAIL = "未安装QQ，安装QQ后才可以加群哦~";
    // 取消关注成功
    public static final String TOAST_QUICK_FOCUS_SUCCESS = "取消关注成功";
    // 头像/昵称等资料修改成功
    public static final String TOAST_MODIFY_SUCCESS = "修改成功";
    // 上传头像等选择图片获取失败
    public static final String TOAST_GET_PIC_FAILED = "获取图片信息失败，请重新上传哦~";
    // 清除缓存后提示
    public static final String TOAST_CLEAR_CACHE_SUCCESS = "清除缓存成功，总共释放 %s 空间";

    // 提交反馈成功
    public static final String TOAST_FEEDBACK_SUCCESS = "反馈成功，感谢您的宝贵建议！";
    // 反馈填写信息不足
    public static final String TOAST_FEEDBACK_LACK = "请填写完整反馈内容和联系方式";
    // 反馈内容太少
    public static final String TOAST_FEEDBACK_CONTENT_NOT_ENOUGH
            = "反馈信息不能少于10个字，麻烦说的更详细点哦~我们才能更好的帮您解决问题";

    // 密码修改成功提示
    public static final String TOAST_MODIFY_PWD_SUCCESS = "密码修改成功，请使用新密码重新登录~";


    /*------------- 定义Toast显示内容 ----------------*/
}
