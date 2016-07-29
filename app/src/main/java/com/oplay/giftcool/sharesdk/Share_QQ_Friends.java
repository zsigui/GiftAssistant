package com.oplay.giftcool.sharesdk;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import com.oplay.giftcool.R;
import com.oplay.giftcool.sharesdk.activity.QQEntryActivity;
import com.oplay.giftcool.sharesdk.base.IShare;


/**
 * 分享给QQ好友
 * Created by yxf on 14-12-15.
 */
public class Share_QQ_Friends extends IShare {

    public Share_QQ_Friends(Context context) {
        super(context, R.drawable.ic_share_qq, context.getString(R.string.st_share_qq_friends));
    }

    @Override
    public void share(String title, String description, String url, String iconUrl, Bitmap bitmap, final int type) {
        Intent intent = new Intent(mContext, QQEntryActivity.class);
        intent.putExtra(ShareSDKConfig.ARGS_TITLE, title);
        intent.putExtra(ShareSDKConfig.ARGS_DESCRIPTION, description);
        intent.putExtra(ShareSDKConfig.ARGS_URL, url);
        intent.putExtra(ShareSDKConfig.ARGS_ICON_URL, iconUrl);
        intent.putExtra(ShareSDKConfig.ARGS_SHARE_TYPE, ShareSDKConfig.QQFRIENDS);
        intent.putExtra(ShareSDKConfig.ARGS_CONTENT_TYPE, type);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mContext.startActivity(intent);
    }

    @Override
    public boolean isSupport() {
        return false;
    }

}
