package com.oplay.giftassistant.model.json.base;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/19
 */
public interface JsonBase<T> {
    public T getData();

    public void setData(T data);

    public int getCode();

    public void setCode(int code);

    public String getMsg();

    public void setMsg(String msg);
}
