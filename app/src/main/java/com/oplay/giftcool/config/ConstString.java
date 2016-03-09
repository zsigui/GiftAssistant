package com.oplay.giftcool.config;

/**
 * 定义重复出现的格式化字符串(特别是带html代码不能被格式化的)
 *
 * Created by zsigui on 16-3-3.
 */
public class ConstString {

	public static final String TEXT_HEADER = "pn=%s&vc=%d&vn=%s&chn=%d";

	public static final String TEXT_NET_ERROR = "网络连接错误";
	public static final String TEXT_EXECUTE_ERROR = "执行出错，请求失败";

	public static final String TEXT_LOGIN_FIRST = "请求失败,请先登录";
	public static final String TEXT_HOPE_GIFT_SUCCESS = "求礼包成功";

	public static final String TEXT_SEIZE = "开抢时间：<font color='#ffaa17'>%s</font>";
	public static final String TEXT_SEARCH = "开淘时间：<font color='#ffaa17'>%s</font>";
	public static final String TEXT_SEARCHED = "已淘号：<font color='#ffaa17'>%d</font>次";
	public static final String TEXT_GIFT_CODE = "礼包码: <font color='#ffaa17'>%s</font>";
	public static final String TEXT_GIFT_TOTAL = "共 <font color='#ffaa17'>%s</font> 款礼包";
}
