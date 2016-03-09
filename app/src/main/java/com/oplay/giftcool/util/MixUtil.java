package com.oplay.giftcool.util;

import android.content.Context;
import android.text.TextUtils;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.model.data.resp.InitQQ;
import com.socks.library.KLog;

import java.util.ArrayList;

/**
 * 混杂工具类，用于放置一些重复的方法
 * <p/>
 * Created by zsigui on 16-3-1.
 */
public class MixUtil {

	/**
	 * 根据初始化结果配置官方QQ群信息
	 */
	public static String[] getQQInfo() {
		String[] result = new String[2];
		ArrayList<InitQQ> qqInfo = AssistantApp.getInstance().getQQInfo();
		result[0] = "515318514";
		result[1] = "8MdlDK-VEslpLGRDOIlcqZUbSYuv0pNb";
		String qqStrServer = "";
		if (qqInfo != null && qqInfo.size() > 0) {
			for (InitQQ item : qqInfo) {
				qqStrServer = item.qq + ',';
			}
			if (qqStrServer.length() > 0) {
				qqStrServer = qqStrServer.substring(0, qqStrServer.length() - 1);
			}
		}
		if (!TextUtils.isEmpty(qqStrServer)) {
			result[0] = qqStrServer;
			// 选择第一个作为默认跳转加入
			assert qqInfo != null;
			result[1] = qqInfo.get(0).key;
		}
		return result;
	}

	/**
	 * 判断传入的Url是否指明需要先进行登录
	 */
	public static boolean isUrlNeedLoginFirst(Context context, String url) {
		if (context == null || url == null) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.d(AppDebugConfig.TAG_UTIL, "context和url不允许定义为空");
			}
			return false;
		}
		int index = url.indexOf("need_validate");
		if ((index != -1 && "1".equals(url.substring(index + 14, index + 15)))
				&& !AccountManager.getInstance().isLogin()) {
			IntentUtil.jumpLogin(context);
//			if (context instanceof OnBackPressListener) {
//				((OnBackPressListener) context).onBack();
//			}
			return true;
		}
		return false;
	}
}