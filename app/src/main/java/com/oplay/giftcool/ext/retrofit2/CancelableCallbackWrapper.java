package com.oplay.giftcool.ext.retrofit2;

import java.io.IOException;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * 封装Callback用于执行取消时的操作
 *
 * @author JackieZhuang
 * @email zsigui@foxmail.com
 * @date 2016/1/1
 */
public class CancelableCallbackWrapper<T> implements Call<T> {

    private boolean mCanceled = false;
    private Call<T> mRealCall;

    public CancelableCallbackWrapper(Call<T> realCall) {
        mRealCall = realCall;
    }

    public boolean isCanceled() {
        return mCanceled;
    }

    @Override
    public Response<T> execute() throws IOException {
        mCanceled = false;
        return mRealCall == null ? null : mRealCall.execute();
    }

    @Override
    public void enqueue(final Callback<T> callback) {
        if (mCanceled) {
            return;
        }
        if (mRealCall != null) {
            mRealCall.enqueue(new Callback<T>() {
                @Override
                public void onResponse(Response<T> response, Retrofit retrofit) {
                    if (mCanceled) {
                        return;
                    }
                    callback.onResponse(response, retrofit);
                }

                @Override
                public void onFailure(Throwable t) {
                    if (mCanceled) {
                        return;
                    }
                    callback.onFailure(t);
                }
            });
        }
    }

    @Override
    public void cancel() {
        mCanceled = true;
        if (mRealCall != null) {
            mRealCall.cancel();
        }
    }

    @Override
    public Call<T> clone() {
        return new CancelableCallbackWrapper<>(mRealCall == null ? null : mRealCall.clone());
    }
}
