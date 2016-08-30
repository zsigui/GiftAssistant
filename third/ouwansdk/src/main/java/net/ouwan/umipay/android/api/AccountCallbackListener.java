package net.ouwan.umipay.android.api;

/**
 * 账户的登入登出接口
 *
 * @date 2014-08-18
 */
public interface AccountCallbackListener {
    /**
     * 账户登入回调方法
     *
     * @param code     状态码
     * @param userInfo
     */
    void onLogin(int code, GameUserInfo userInfo);

    /**
     * 账户登出回调方法
     *
     * @param code   状态码
     * @param params 自定义参数，透传
     */
    void onLogout(int code, Object params);
}
