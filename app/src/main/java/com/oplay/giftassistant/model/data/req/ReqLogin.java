package com.oplay.giftassistant.model.data.req;

import android.text.TextUtils;

import com.oplay.giftassistant.config.AppDebugConfig;
import com.oplay.giftassistant.util.StringUtil;
import com.socks.library.KLog;

import net.youmi.android.libs.common.coder.Coder_Md5;

/**
 * Created by zsigui on 15-12-24.
 */
public class ReqLogin {

	/**
	 * 偶玩用户名
	 */
	private String username;
	/**
	 * 用户密码,只在用户使用偶玩账号登录的时候有效，其它时候设置为null<br />
	 * 默认加密规则: md5(password + md5( lowercase(username) )) <br />
	 */
	private String password;
	/**
	 * 手机账号
	 */
	private String phone;
	/**
	 * 手机登录验证码
	 */
	private String code;

	public String getPassword() {
		return password;
	}

	public String getUsername() {
		return username;
	}

	public String getPhone() {
		return phone;
	}


	public String getCode() {
		return code;
	}

	/**
	 * 偶玩用户登录
	 *
	 * @param username 用户名
	 * @param password 密码
	 * @return 设置成功返回true, 空值/格式不符合/加密失败返回false
	 */
	public boolean setOuwanUser(String username, String password) {
		this.username = username.toLowerCase();
		this.password = Coder_Md5.md5(Coder_Md5.md5(password) + this.username);
		if (this.password.length() == 0) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.d(AppDebugConfig.TAG_APP, "src : username = " + this.username + ", password = " + password);
			}
			return false;
		}
		return true;
	}

	/**
	 * 手机登录<br />
	 * 第一步获取验证码的时候调用
	 */
	public boolean setPhoneUser(String phone) {
		String phoneRegex = "^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$";
		if (!StringUtil.matches(phone, phoneRegex, false)) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.d(AppDebugConfig.TAG_APP, "src : phone = " + this.phone);
			}
			return false;
		}
		this.phone = phone;
		return true;
	}

	/**
	 * 手机登录<br />
	 * 第二步登录的是否调用
	 */
	public boolean setPhoneUser(String phone, String code) {
		String phoneRegex = "^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$";
		if (!StringUtil.matches(phone, phoneRegex, false)
				|| TextUtils.isEmpty(code)) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.d(AppDebugConfig.TAG_APP, "src : phone = " + this.phone);
			}
			return false;
		}
		this.phone = phone;
		this.code = code;
		return true;
	}
}