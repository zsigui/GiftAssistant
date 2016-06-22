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



    /*------------- 定义Toast显示内容 ----------------*/

    // 网络连接错误
    public static final String TOAST_NET_ERROR = "呜，网络正在发生流血事件...";
    // 应用程序执行出现异常情况
    public static final String TOAST_EXECUTE_ERROR = "哎呦，程序执行时脑袋抽风了";
    // 访问服务器出现异常情况
    public static final String TOAST_SERVER_ERROR = "呜，服务挂了！";
    // 服务器返回的数据格式出错，解析异常
    public static final String TOAST_SERVER_BAD_CALLBACK = "嚄，服务器大姨妈了？";
    // 页面数据过期 或者 非网络问题导致的获取失败
    public static final String TOAST_DATA_OVERTIME = "诶，这的东西我得去山沟沟找找看了";
    // 页面状态丢失导致的失效问题
    public static final String TOAST_MISS_STATE = "页面失效，请重新打开再进行尝试";
    // 参数传递出错导致的页面跳转失败
    public static final String TOAST_WRONG_PARAM = "呜呼，想要的页面去哪了呢";
    // 尚未登录
    public static final String TOAST_LOGIN_FIRST = "警告！非法禁止，请先登录";
    // 手机号码格式不符合要求
    public static final String TOAST_PHONE_ERROR = "你不是输错手机号码了吧？";
    // 偶玩账号或密码格式不符合要求
    public static final String TOAST_OUWAN_ERROR = "呜呜，账号密码格式错误了！";
    // 手机短信验证码发送提醒
    public static final String TOAST_PHONE_CODE_SEND = "短信已经发送，请注意接收";
    // 点击退出应用的提示
    public static final String TOAST_EXIT_APP = "再点我就离你而去了~~~~(>_<)~~~~";
    // 用户登录态失效
    public static final String TOAST_SESSION_UNAVAILABLE = "用户登录态失效，请重新登录";

    // 复制礼包码后提示
    public static final String TOAST_COPY_CODE = "已复制";
    // 已更新到最新版本提示
    public static final String TOAST_VERSION_NEWEST = "厉害，已经是更无可更了";
    // 当前版本不支持提示
    public static final String TOAST_VERSION_NOT_SUPPORT = "唷，当前版本不支持该功能显示喔，不如下载最新版本";
    // 未知异常状况
    public static final String TOAST_UNKNOWN_ERROR = "(⊙o⊙)? 哪里出错了，没有反应";
    // 请求获取数据为空
    public static final String TOAST_NO_RESPONSE_DATA = "呜呼唉哉，此页空空如也";
    // 连续过快请求
    public static final String TOAST_FREQUENT_REQUEST = "请求过于频繁，请勿连续点击";

    // 抢号支付方式选择有误
    public static final String TOAST_PAY_METHOD_ERROR = "选择支付类型有误，请重新选择";

    // 处于非WIFI下下载提示
    public static final String TOAST_DOWNLOADING_NOT_IN_WIFI = "当前正在移动网络下下载";
    // 添加了新的下载任务提示
    public static final String TOAST_ADD_NEW_DOWNLOAD_TASK = "已添加新的下载任务";
    // 安装文件被移除导致的安装失败提示
    public static final String TOAST_INSTALL_FAIL_FOR_NO_APK = "无法安装！安装文件已被移除";
    // 安装完删除安装文件提示
    public static final String TOAST_REMOVE_AFTER_SUCCESS_INSTALL = "已删除%s安装包，节省%s空间";

    // 分享成功
    public static final String TOAST_SHARE_SUCCESS = "分享成功";
    // 分享失败
    public static final String TOAST_SHARE_FAILED = "分享失败";
    // 取消分享
    public static final String TOAST_SHARE_QUICKED = "取消分享";
    // 没有提供分享按钮情况下异常触发的提示
    public static final String TOAST_SHARE_NO_OFFER = "该礼包无分享设置";
    // 活动获取发送Token失败
    public static final String TOAST_GET_TOKEN_FAILED = "获取Token失败";
    // 发表评论成功
    public static final String TOAST_COMMENT_SUCCESS = "发表评论成功";

    // 没有关注快讯
    public static final String TOAST_EMPTY_FOCUS_ACTIVITY = "没有相关关注快讯";
    // 获取关注快讯失败
    public static final String TOAST_GET_FOCUS_ACTIVITY_FAILED = "error~获取相关关注快讯失败";
    // 点击qq群直接调起qq失败
    public static final String TOAST_OPEN_QQ_GROUP_FAIL = "请先安装QQ";
    // 关注成功
    public static final String TOAST_FOCUS_SUCCESS = "关注成功";
    // 头像/昵称等资料修改成功
    public static final String TOAST_MODIFY_SUCCESS = "修改成功";
    // 上传头像等选择图片获取失败
    public static final String TOAST_GET_PIC_FAILED = "获取图片信息失败";
    // 清除缓存后提示
    public static final String TOAST_CLEAR_CACHE_SUCCESS = "清除缓存成功，总共释放 %s 空间";

    // 提交反馈成功
    public static final String TOAST_FEEDBACK_SUCCESS = "反馈成功，谢谢你";
    // 反馈填写信息不足
    public static final String TOAST_FEEDBACK_LACK = "请填写完整反馈内容和联系方式";
    // 反馈内容太少
    public static final String TOAST_FEEDBACK_CONTENT_NOT_ENOUGH = "反馈信息有点少，麻烦更详细地描述你的反馈(不少于10个字)";

    // 密码修改成功提示
    public static final String TOAST_MODIFY_PWD_SUCCESS = "密码修改成功，请使用新密码重新登录";

    /*------------- 定义Toast显示内容 ----------------*/
}
