package com.oplay.giftassistant.config;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.cache.ExternalCacheDiskCacheFactory;
import com.bumptech.glide.module.GlideModule;
import com.bumptech.glide.request.target.ViewTarget;
import com.oplay.giftassistant.R;
import com.socks.library.KLog;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/19
 */
public class GlideConfig implements GlideModule {

    public static String EXTERNAL_CACHE_DIR = "glide_imgs";

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        KLog.e();
        builder.setDecodeFormat(DecodeFormat.PREFER_ARGB_8888);
        builder.setDiskCache(new ExternalCacheDiskCacheFactory(context, EXTERNAL_CACHE_DIR, 20 * 1024 * 1024));
        ViewTarget.setTagId(R.id.glide_tag_id);
    }

    @Override
    public void registerComponents(Context context, Glide glide) {

    }
}
