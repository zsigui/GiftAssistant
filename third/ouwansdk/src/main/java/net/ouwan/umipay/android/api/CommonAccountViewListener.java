package net.ouwan.umipay.android.api;

import net.ouwan.umipay.android.entry.UmipayCommonAccount;

/**
 * Created by zsigui on 16-8-30.
 */
public interface CommonAccountViewListener {

    int CODE_CHANGE_ACCOUNT = 1;
    int CODE_SELECT_ACCOUNT = 2;

    /**
     * 处理通用账号界面选择后的回调操作方法
     *
     * @param code     类型码
     * @param account  选择操作账号，null代表取消操作
     * @param callback 回调处理
     */
    void onChooseAccount(int code, UmipayCommonAccount account, ResultActionCallback callback);

    interface ResultActionCallback {
        void onSuccess(Object obj);

        void onFailed(int code, String msg);

        void onCancel();
    }
}
