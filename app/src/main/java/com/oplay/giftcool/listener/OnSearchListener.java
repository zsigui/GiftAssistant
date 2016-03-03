package com.oplay.giftcool.listener;

/**
 * @author JackieZhuang
 * @email zsigui@foxmail.com
 * @date 2016/3/3
 */
public interface OnSearchListener {

    void sendSearchRequest(String keyword, int id);

    void clearHistory();
}
