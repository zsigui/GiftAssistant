package com.oplay.giftcool.model.json.base;

import com.google.gson.annotations.SerializedName;
import com.oplay.giftcool.config.NetStatusCode;

import java.io.Serializable;
import java.util.Locale;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/19
 */
public class JsonRespBase<T> implements Serializable {

	/**
	 * 错误码, 0为成功
	 */
    @SerializedName("c")
    private int mCode = -1;

	/**
	 * 数据存放
	 */
    @SerializedName("d")
    private T mData;

	/**
	 * 错误消息说明
	 */
    @SerializedName("m")
    private String mMsg;

    public T getData() {
        return mData;
    }

    public void setData(T data) {
        mData = data;
    }

    public int getCode() {
        return mCode;
    }

    public void setCode(int code) {
        mCode = code;
    }

    public String getMsg() {
        return mMsg;
    }

    public void setMsg(String msg) {
        mMsg = msg;
    }

	@Override
	public String toString() {
		return "c : " + mCode + ", m : " + mMsg + ", data : " + mData;
	}

	public String error() {
		return String.format(Locale.CHINA, "(%d)%s", mCode, mMsg);
	}

	public boolean isSuccess() {
		return getCode() == NetStatusCode.SUCCESS;
	}
}
