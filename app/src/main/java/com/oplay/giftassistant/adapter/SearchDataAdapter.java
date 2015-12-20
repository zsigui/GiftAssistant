package com.oplay.giftassistant.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.oplay.giftassistant.model.json.base.JsonBaseImpl;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/20
 */
public class SearchDataAdapter<T> extends BaseAdapter{

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public T getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }

    public void doSuccess(T response) {

    }

    public void doFail(T response) {

    }

    public void doWithPrompt(JsonBaseImpl<T> body) {
    }
}
