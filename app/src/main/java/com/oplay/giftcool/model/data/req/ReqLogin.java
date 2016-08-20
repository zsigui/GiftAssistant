package com.oplay.giftcool.model.data.req;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.util.StringUtil;

import net.youmi.android.libs.common.coder.Coder_Md5;

/**
 * Created by zsigui on 15-12-24.
 */
public class ReqLogin {

    /**
     * 偶玩用户名
     */
    @SerializedName("username")
    private String username;
    /**
     * 用户密码,只在用户使用偶玩账号登录的时候有效，其它时候设置为null<br />
     * 默认加密规则: md5(password + md5( lowercase(username) )) <br />
     */
    @SerializedName("password")
    private String password;
    /**
     * 手机账号
     */
    @SerializedName("phone")
    private String phone;
    /**
     * 手机登录验证码
     */
    @SerializedName("code")
    private String code;

    @SerializedName("send_type")
    public String sendType;

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
    public boolean setOuwanUser(String username, String password, boolean needEncrypt) {
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            return false;
        }
        if (!needEncrypt) {
            // 使用记住密码的方式，这时不需要再次加密
            this.username = username;
            this.password = password;
        } else {
            this.username = username.toLowerCase();
            this.password = Coder_Md5.md5(Coder_Md5.md5(password) + this.username);

            this.username = username.toLowerCase();
            byte[] psw_md5_bytes = Coder_Md5.md5_16(password);
            byte[] user_bytes = this.username.getBytes();
            byte[] bytedata = Coder_Md5.concatByteArrays(psw_md5_bytes, user_bytes);
            this.password = Coder_Md5.md5(bytedata);
            if (this.password.length() == 0) {
                AppDebugConfig.d(AppDebugConfig.TAG_DEBUG_INFO, "src : username = " + this.username + ", password = "
                        + password);
                return false;
            }
        }
        return true;
    }

    /**
     * 手机登录<br />
     * 第一步获取验证码的时候调用
     */
    public boolean setPhoneUser(String phone) {
        String phoneRegex = "^1\\d{10}$";
        if (!StringUtil.matches(phone, phoneRegex, false)) {
            AppDebugConfig.d(AppDebugConfig.TAG_DEBUG_INFO, "src : phone = " + this.phone);
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
        String phoneRegex = "^1\\d{10}$";
        if (!StringUtil.matches(phone, phoneRegex, false)
                || TextUtils.isEmpty(code)) {
            AppDebugConfig.d(AppDebugConfig.TAG_DEBUG_INFO, "src : phone = " + this.phone);
            return false;
        }
        this.phone = phone;
        this.code = code;
        return true;
    }
}
