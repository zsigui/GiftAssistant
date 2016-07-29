package com.oplay.giftcool.sharesdk;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

/**
 * Created by yxf on 14-12-16.
 * 加载默认图片的URL
 */
public class DefaultShareIconUrlLoader {
    private static DefaultShareIconUrlLoader instance;
    private LoaderListener mListener;

    public static synchronized DefaultShareIconUrlLoader getInstance() {
        if (instance == null) {
            instance = new DefaultShareIconUrlLoader();
        }
        return instance;
    }

    public void getDefaultShareIcon(Context context, final String title, final String description, final String url,
                                    final String iconUrl, final int shareType, final int type) {
        ImageLoader.getInstance().loadImage(url, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {

            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                if (mListener != null) {
                    String localUrl = ImageLoader.getInstance().getDiskCache().get(s).getAbsolutePath();
                    mListener.onFetch(title, description, localUrl, iconUrl, shareType, type);
                }
            }

            @Override
            public void onLoadingCancelled(String s, View view) {

            }
        });
    }

    public void setListener(LoaderListener listener) {
        mListener = listener;
    }

    public interface LoaderListener {
        void onFetch(String title, String description, String url, String iconUrl, int shareType, int contentType);
    }
}

