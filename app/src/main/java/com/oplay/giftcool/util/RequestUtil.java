package com.oplay.giftcool.util;

import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.model.json.base.JsonRespBase;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 网络请求
 * Created by zsigui on 16-11-9.
 */
public class RequestUtil {


    /**
     * 对评论点赞/取消点赞
     */
    public static void putAdmireState(int postId, int commentId, boolean addLike) {
        Global.getNoEncryptEngine().putAdmireState(postId, commentId, addLike ? 0 : 1)
                .enqueue(new Callback<JsonRespBase<Void>>() {
                    @Override
                    public void onResponse(Call<JsonRespBase<Void>> call, Response<JsonRespBase<Void>>
                            response) {
                        AppDebugConfig.warnResp(AppDebugConfig.TAG_ENCRYPT, response);
                    }

                    @Override
                    public void onFailure(Call<JsonRespBase<Void>> call, Throwable t) {
                        AppDebugConfig.d(AppDebugConfig.TAG_ENCRYPT, t);
                    }
                });
    }
}
