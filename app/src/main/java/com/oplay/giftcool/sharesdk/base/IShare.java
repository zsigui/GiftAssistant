package com.oplay.giftcool.sharesdk.base;

import android.content.Context;
import android.graphics.Bitmap;

/**
 * 分享SDK的基类
 * Created by yxf on 14-11-7.
 */
public abstract class IShare {
    protected Context mContext;
    private int mIconId;
    private String mDescription;

    public IShare(Context context, int iconId, String description) {
        mContext = context;
        mIconId = iconId;
        mDescription = description;
    }

    /**
     * @param title
     * @param description 朋友圈是使用title作为右边展示部分
     * @param url
     * @param iconUrl
     * @param Iconbitmap
     */
    public abstract void share(
            final String title,
            final String description,
            final String url,
            final String iconUrl,
            final Bitmap Iconbitmap,
            final int type
    );

    public interface TYPE {
        int WEB = 0;
        String STR_WEB = "webpage";
        int TEXT = 1;
        String STR_TEXT = "text";
        int IMG = 2;
        String STR_IMG = "img";
        int MUSIC = 3;
        String STR_MUSIC = "music";
        int VIDEO = 4;
        String STR_VIDEO = "video";
    }

    public abstract boolean isSupport();

    public int getIconId() {
        return mIconId;
    }

    public String getDescription() {
        return mDescription;
    }

    protected String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }
}
