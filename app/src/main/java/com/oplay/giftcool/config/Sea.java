package com.oplay.giftcool.config;

import android.text.TextUtils;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oplay.giftcool.model.data.resp.AdInfo;

import java.io.File;

/**
 * Created by zsigui on 16-8-3.
 */
public class Sea {

    private static final class SingletonHolder {
        static final Sea instance = new Sea();
    }

    public static Sea getInstance() {
        return SingletonHolder.instance;
    }


    private AdInfo mAdInfo;

    public boolean isShowAd() {
        if (mAdInfo != null && mAdInfo.displayTime > 0 && !TextUtils.isEmpty(mAdInfo.img)) {
            File f = ImageLoader.getInstance().getDiskCache().get(mAdInfo.img);
            if (f != null && f.exists()) {
                return true;
            }
        }
        return false;
    }

    public AdInfo getAdInfo() {
        return mAdInfo;
    }

    public void setAdInfo(AdInfo adInfo) {
        mAdInfo = adInfo;
        if (mAdInfo != null && !TextUtils.isEmpty(mAdInfo.img)) {
            AppDebugConfig.d(AppDebugConfig.TAG_DEBUG_INFO, "start to load ad img : " + mAdInfo.img);
            ImageLoader.getInstance().loadImage(mAdInfo.img, null);
        }
    }
}
