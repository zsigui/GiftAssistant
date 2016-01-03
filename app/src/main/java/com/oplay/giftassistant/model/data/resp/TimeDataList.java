package com.oplay.giftassistant.model.data.resp;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author JackieZhuang
 * @email zsigui@foxmail.com
 * @date 2016/1/3
 */
public class TimeDataList<T> implements Serializable{

    public ArrayList<T> data;
    public String date;
}
