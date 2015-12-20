package com.oplay.giftassistant.model.json.base;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/19
 */
public class JsonBaseImpl<T> implements JsonBase<T>, Serializable {

    @SerializedName("c")
    private int mCode = -1;

    @SerializedName("d")
    private T mData;

    @SerializedName("msg")
    private String mMsg;

    @Override
    public T getData() {
        return mData;
    }

    @Override
    public void setData(T data) {
        mData = data;
    }

    @Override
    public int getCode() {
        return mCode;
    }

    @Override
    public void setCode(int code) {
        mCode = code;
    }

    @Override
    public String getMsg() {
        return mMsg;
    }

    @Override
    public void setMsg(String msg) {
        mMsg = msg;
    }
}
